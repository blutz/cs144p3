package edu.ucla.cs.cs144;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hit;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;

import java.util.Date;
import java.util.Iterator;
import java.text.SimpleDateFormat;
import java.util.Arrays;

import edu.ucla.cs.cs144.DbManager;
import edu.ucla.cs.cs144.SearchConstraint;
import edu.ucla.cs.cs144.SearchResult;

public class AuctionSearch implements IAuctionSearch {

	/* 
         * You will probably have to use JDBC to access MySQL data
         * Lucene IndexSearcher class to lookup Lucene index.
         * Read the corresponding tutorial to learn about how to use these.
         *
         * Your code will need to reference the directory which contains your
	 * Lucene index files.  Make sure to read the environment variable 
         * $LUCENE_INDEX with System.getenv() to build the appropriate path.
	 *
	 * You may create helper functions or classes to simplify writing these
	 * methods. Make sure that your helper functions are not public,
         * so that they are not exposed to outside of this class.
         *
         * Any new classes that you create should be part of
         * edu.ucla.cs.cs144 package and their source files should be
         * placed at src/edu/ucla/cs/cs144.
         *
         */

	private IndexSearcher searcher = null;
	private QueryParser parser = null;


	/* Call this function before using searcher or parser */
	private void createSearchEngine() throws IOException
	{
		if (searcher == null)
		    searcher = new IndexSearcher(System.getenv("LUCENE_INDEX") + "/index1");
		if (parser == null)
		    parser = new QueryParser("content", new StandardAnalyzer());
	}
	
	public SearchResult[] basicSearch(String query, int numResultsToSkip, 
			int numResultsToReturn) {
	try
	{
		createSearchEngine();
	    Query parsedQuery = parser.parse(query);
	    Hits hits = searcher.search(parsedQuery);
	    SearchResult[] results = new SearchResult[hits.length()];

	    for(int i = 0; i < hits.length(); i++) {
		   Document doc = hits.doc(i);
		   results[i] = new SearchResult(doc.get("id"), doc.get("name"));
		}
		int to = numResultsToSkip + numResultsToReturn;
		if (numResultsToReturn == 0)
			to = results.length;
		return Arrays.copyOfRange(results, numResultsToSkip, to);
	} catch (ParseException e)
	{
		return new SearchResult[0];
	}
	catch (IOException e)
	{
		System.err.println("IOException occured");
		return new SearchResult[0];
	}
	}

	public SearchResult[] advancedSearch(SearchConstraint[] constraints, 
			int numResultsToSkip, int numResultsToReturn) {
		boolean querySql = false;
		boolean queryLucene = false;
		String queryLuceneText = "";
		String querySqlText = "";
		// Get the Lucene and MySQL queries
		for(int i = 0; i < constraints.length; i++)
		{
			String name = constraints[i].getFieldName();
			if(name == FieldName.ItemName || name == FieldName.Category 
				|| name == FieldName.Description)
			{
				// This requires a Lucene query
				if(queryLucene)
				{
					queryLuceneText += " AND ";
				}
				else
				{
					queryLuceneText = "";
					queryLucene = true;
				}
				if (name == FieldName.ItemName)
					queryLuceneText += "name:(";
				else if (name == FieldName.Category)
					queryLuceneText += "category:(";
				else if (name == FieldName.Description)
					queryLuceneText += "description:(";

				queryLuceneText += constraints[i].getValue() + ")";
			}
			else if(name == FieldName.SellerId || name == FieldName.BuyPrice 
				|| name == FieldName.BidderId || name == FieldName.EndTime)
			{
				// This requres a MySQL query
				if(querySql)
				{
					querySqlText += " AND ";
				}
				else
				{
					querySqlText = "SELECT Item.item_id, Item.name FROM Item " +
						"LEFT JOIN Bid ON (Item.item_id = Bid.item_id) WHERE";
					querySql = true;
				}
				if(name == FieldName.SellerId)
				{
					querySqlText += " Item.seller_id = ?";
				}
				else if (name == FieldName.BuyPrice)
				{
					querySqlText += " Item.buy_now_price = ?";
				}
				else if (name == FieldName.BidderId)
				{
					querySqlText += " Bid.user_id = ?";
				}
				else if (name == FieldName.EndTime)
				{
					querySqlText += " Item.ends = ?";
				}
			}
		}
		if(querySql)
			querySqlText += ";";
		System.err.println("Lucene Query: " + queryLuceneText);
		System.err.println("SQL Query: " + querySqlText);
		// TODO: Make this bigger or fix it
	 //    SearchResult[] results = new SearchResult[0];
		// int to = numResultsToSkip + numResultsToReturn;
		// if (numResultsToReturn == 0)
		// 	to = results.length;
		// return Arrays.copyOfRange(results, numResultsToSkip, to);
		return new SearchResult[0];
	}

	public String getXMLDataForItemId(String itemId) {
		// TODO: Your code here!
		return null;
	}
	
	public String echo(String message) {
		return message;
	}

}
