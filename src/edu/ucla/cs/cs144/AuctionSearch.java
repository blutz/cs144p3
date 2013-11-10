package edu.ucla.cs.cs144;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;

import java.util.ArrayList;
import java.util.List;

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
		if (numResultsToReturn == 0 || to > results.length)
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
		List<SqlQueryParameter> querySqlParameters = new ArrayList<SqlQueryParameter>();
		// Get the Lucene and MySQL queries
		for(int i = 0; i < constraints.length; i++)
		{
			String name = constraints[i].getFieldName();
			if(name.equals(FieldName.ItemName) 
				|| name.equals(FieldName.Category) 
				|| name.equals(FieldName.Description))
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
				if (name.equals(FieldName.ItemName))
					queryLuceneText += "name:(";
				else if (name.equals(FieldName.Category))
					queryLuceneText += "category:(";
				else if (name.equals(FieldName.Description))
					queryLuceneText += "description:(";

				queryLuceneText += constraints[i].getValue() + ")";
			}
			else if(name.equals(FieldName.SellerId) || name.equals(FieldName.BuyPrice) 
				|| name.equals(FieldName.BidderId) || name.equals(FieldName.EndTime))
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
				if(name.equals(FieldName.SellerId))
				{
					querySqlText += " Item.seller_id = ?";
					querySqlParameters.add(new SqlQueryParameter(constraints[i].getValue(), "string"));
				}
				else if (name.equals(FieldName.BuyPrice))
				{
					querySqlText += " Item.buy_now_price = ?";
					querySqlParameters.add(new SqlQueryParameter(constraints[i].getValue(), "float"));
				}
				else if (name.equals(FieldName.BidderId))
				{
					querySqlText += " Bid.user_id = ?";
					querySqlParameters.add(new SqlQueryParameter(constraints[i].getValue(), "string"));
				}
				else if (name.equals(FieldName.EndTime))
				{
					querySqlText += " Item.ends = ?";
					querySqlParameters.add(new SqlQueryParameter(constraints[i].getValue(), "timestamp"));
				}
			}
		}
		// System.err.println("Lucene Query: " + queryLuceneText);
		// System.err.println("SQL Query: " + querySqlText + ";");

		// Now actually execute these queries. By the end of this, all results
		// should be in 'result'
		SearchResult[] results = new SearchResult[0];
		SearchResult[] luceneResults = new SearchResult[0];
		SearchResult[] sqlResults = new SearchResult[0];
		// If we need to talk to Lucene
		if(queryLucene)
		{
		try
		{
			createSearchEngine();
		    Query parsedQuery = parser.parse(queryLuceneText);
		    Hits hits = searcher.search(parsedQuery);
			luceneResults = new SearchResult[hits.length()];

		    for(int i = 0; i < hits.length(); i++) {
			   Document doc = hits.doc(i);
			   luceneResults[i] = new SearchResult(doc.get("id"), doc.get("name"));
			}
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
		// Get the DB connection if we need it
	try
	{
		Connection con = DbManager.getConnection(false);
		PreparedStatement stmt;
		// If we ONLY need MySQL (and not lucene)
		if(querySql && !queryLucene)
		{
			stmt = con.prepareStatement(querySqlText + ";");
		}
		else
		{
			querySqlText += " AND (";
			for(int i = 0; i < luceneResults.length; i++)
			{
				if(i != 0)
					querySqlText += " OR";
				querySqlText += " Item.item_id = " + luceneResults[i].getItemId();
			}
			stmt = con.prepareStatement(querySqlText + ");");
		}

		for(int i = 0; i < querySqlParameters.size(); i++)
		{
				querySqlParameters.get(i).setParameter(i+1, stmt);
		}
		ResultSet rs = stmt.executeQuery();
		int rsSize = 0;
		if(rs.last())
		{
			rs.last();
			rsSize = rs.getRow();
			rs.beforeFirst();
		}
		sqlResults = new SearchResult[rsSize];

		for(int i = 0; i < rsSize && rs.next(); i++)
		{
			sqlResults[i] = new SearchResult(rs.getString("Item.item_id"), 
				rs.getString("Item.name"));
		}
	} catch (SQLException e)
	{
		System.err.println("SQL Exception");
	}


		// Now check what we want to return
		if(queryLucene && !querySql)
		{
			int to = numResultsToSkip + numResultsToReturn;
			if (numResultsToReturn == 0 || to > luceneResults.length)
				to = luceneResults.length;
			return Arrays.copyOfRange(luceneResults, numResultsToSkip, to);
		}
		else if(querySql)
		{
			int to = numResultsToSkip + numResultsToReturn;
			if (numResultsToReturn == 0 || to > sqlResults.length)
				to = sqlResults.length;
			return Arrays.copyOfRange(sqlResults, numResultsToSkip, to);
		}
		else
		{
			return new SearchResult[0];
		}
	}

	public String getXMLDataForItemId(String itemId) {
        String item_id = "",
                name = "",
                buy_now_price = "",
                minimum_bid = "",
                started = "",
                ends = "",
                seller_id = "",
                description = "",
                xmlBidData = "",
                highestBid = "",
                location = "",
                country = "",
                num_bids = "",
                sellerRating = "";

        List<String> categories = new ArrayList<String>();

    try {
        Connection con = DbManager.getConnection(false);
        PreparedStatement query = con.prepareStatement(
            "SELECT * FROM Item WHERE item_id = ?");
        query.setInt(1, Integer.parseInt(itemId));
        ResultSet rs = query.executeQuery();
        while (rs.next()) {
            item_id = rs.getString("item_id");
            name = rs.getString("name");
            buy_now_price = "$" + rs.getString("buy_now_price");
            minimum_bid = "$" + rs.getString("minimum_bid");
            started = rs.getString("started");
            ends = rs.getString("ends");
            seller_id = rs.getString("seller_id");
            description = rs.getString("description");
        }

        query = con.prepareStatement(
            "SELECT * FROM ItemCategory WHERE item_id = ?");
        query.setInt(1, Integer.parseInt(itemId));
        rs = query.executeQuery();
        while (rs.next()) {
            categories.add(rs.getString("category"));
        }

        query = con.prepareStatement(
            "SELECT * FROM Bid WHERE item_id = ?");
        query.setInt(1, Integer.parseInt(itemId));
        rs = query.executeQuery();
        while (rs.next()) {
            String bidder_id = rs.getString("user_id");
            String time = rs.getString("time");
            String amount = rs.getString("amount");

            xmlBidData += "\t\t<Bid>\n";
            xmlBidData += "\t\t\t<Bidder>\n";
            // TODO: These are optional
            // xmlBidData += "\t\t\t\t<Location>" + + "</Location>\n";
            // xmlBidData += "\t\t\t\t<Country>" + + "</Country>\n";
            // TODO: Parse time format
            xmlBidData += "\t\t\t\t<Time>" + time + "</Time>\n";
            xmlBidData += "\t\t\t\t<Amount>" + amount + "</Amount>\n";
            xmlBidData += "\t\t\t</Bidder>\n";
            xmlBidData += "\t\t</Bid>\n";
        }

        query = con.prepareStatement(
            "SELECT COUNT(*) as num_bids FROM Bid WHERE item_id = ?");
        query.setInt(1, Integer.parseInt(itemId));
        rs = query.executeQuery();
        while (rs.next()) {
            num_bids = rs.getString("num_bids");
        }

        query = con.prepareStatement(
            "SELECT amount FROM Bid WHERE item_id = ? ORDER BY amount DESC LIMIT 1");
        query.setInt(1, Integer.parseInt(itemId));
        rs = query.executeQuery();
        while (rs.next()) {
            highestBid = "$" + rs.getString("amount");
        }

        query = con.prepareStatement(
            "SELECT * FROM User WHERE user_id = ?");
        query.setString(1, seller_id);
        rs = query.executeQuery();
        while (rs.next()) {
            sellerRating = rs.getString("rating");
            location = rs.getString("location");
            country = rs.getString("country");
        }
    }
    catch (SQLException e) {
        System.err.println("SQL exception occurred");
    }
   		String xmlData = "";
        xmlData += "<Item>\n";
        xmlData += "\t<Name>" + name + "</Name>\n";
        for(int i = 0; i < categories.size(); i++) {
            xmlData += "\t<Category>" + categories.get(i) + "</Category>\n";
        }
        xmlData += "\t<Currently>"+ highestBid + "</Currently>\n";
        if (buy_now_price != null && !buy_now_price.isEmpty()) {
            xmlData += "\t<Buy_Price>" + buy_now_price + "</Buy_Price>\n";
        }
        xmlData += "\t<First_Bid>" + minimum_bid + "</First_Bid>\n";
        xmlData += "\t<Number_of_Bids>" + num_bids + "</Number_of_Bids>\n";
        xmlData += "\t<Bids>\n";
        xmlData += xmlBidData;
        xmlData += "\t</Bids>\n";
        xmlData += "\t<Location>" + location +  "</Location>\n";
        xmlData += "\t<Country>" + country + "</Country>\n";
        xmlData += "\t<Started>" + started + "</Started>\n";
        xmlData += "\t<Ends>" + ends +  "</Ends>\n";
        xmlData += "\t<Seller UserId=\"" + seller_id + "\" Rating=\"" + sellerRating + "\"/>\n";
        xmlData += "\t<Description>" + description + "</Description>\n";
        xmlData += "</Item>";
		return xmlData;
	}

	public String echo(String message) {
		return message;
	}

}
