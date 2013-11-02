package edu.ucla.cs.cs144;

import java.io.IOException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.Document;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;

public class Indexer {

    /** Creates a new instance of Indexer */
    public Indexer() {
    }

    private IndexWriter indexWriter = null;

    public IndexWriter getIndexWriter(boolean create) throws IOException {
        String indexDir = System.getenv("LUCENE_INDEX") + "/index1";
        if (indexWriter == null) {
            indexWriter = new IndexWriter(indexDir,
                                          new StandardAnalyzer(),
                                          create);
        }
        return indexWriter;
    }

    public void closeIndexWriter() throws IOException {
        if (indexWriter != null) {
            indexWriter.close();
        }
    }

    public void rebuildIndexes() throws IOException {

        Connection conn = null;

        // create a connection to the database to retrieve Items from MySQL
	try {
	    conn = DbManager.getConnection(true);
	} catch (SQLException ex) {
	    System.out.println(ex);
	}

        // Erase existing index
        IndexWriter writer = getIndexWriter(true);

        // Index everthing we need to (as individual items)
    try
    {
        Statement stmt = conn.createStatement();

        ResultSet items = stmt.executeQuery(
            "SELECT a.item_id, a.name, a.description, GROUP_CONCAT(b.category SEPARATOR ' ') as category FROM Item as a LEFT OUTER JOIN ItemCategory as b ON a.item_id = b.item_id GROUP BY a.item_id;"
        );
        while (items.next()) {
            Document doc = new Document();
            doc.add(new Field("id", items.getString("item_id"), Field.Store.YES, Field.Index.NO));
            doc.add(new Field("name", items.getString("name"), Field.Store.YES, Field.Index.TOKENIZED));
            doc.add(new Field("description", items.getString("description"), Field.Store.NO, Field.Index.TOKENIZED));
            doc.add(new Field("category", items.getString("category"), Field.Store.YES, Field.Index.TOKENIZED));
            String content = items.getString("name") + " " + items.getString("description") + " " + items.getString("category");
            doc.add(new Field("content", content, Field.Store.NO, Field.Index.TOKENIZED));
            writer.addDocument(doc);
        }

    } catch (SQLException ex) {
        System.err.println("SQLException: " + ex.getMessage());
        closeIndexWriter();
        System.exit(1);
    }

        // Close the index writer
        closeIndexWriter();

        // close the database connection
	try {
	    conn.close();
	} catch (SQLException ex) {
	    System.out.println(ex);
	}
    }

    public static void main(String args[]) throws IOException {
        Indexer idx = new Indexer();
        idx.rebuildIndexes();
    }
}

