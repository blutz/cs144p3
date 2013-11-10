package edu.ucla.cs.cs144;

import java.util.Calendar;
import java.util.Date;

import edu.ucla.cs.cs144.AuctionSearch;
import edu.ucla.cs.cs144.SearchResult;
import edu.ucla.cs.cs144.SearchConstraint;
import edu.ucla.cs.cs144.FieldName;

public class AuctionSearchTest {
	public static void main(String[] args1)
	{
		AuctionSearch as = new AuctionSearch();

		String message = "Test message";
		String reply = as.echo(message);
		System.out.println("Reply: " + reply);

		System.out.println("============");
		System.out.println("Basic Search");
		System.out.println("============");

        String query = "superman";
		System.out.println("");
		System.out.println("superman (72)");
		SearchResult[] basicResults = as.basicSearch(query, 0, 0);
		System.out.println("Received " + basicResults.length + " results");
		// for(SearchResult result : basicResults) {
		// 	System.out.println(result.getItemId() + ": " + result.getName());
		// }

        query = "kitchenware";
		System.out.println("");
		System.out.println("kitchenware (1462)");
		basicResults = as.basicSearch(query, 0, 0);
		System.out.println("Received " + basicResults.length + " results");
		// for(SearchResult result : basicResults) {
		// 	System.out.println(result.getItemId() + ": " + result.getName());
		// }

        query = "star trek";
		System.out.println("");
		System.out.println("star trek (767)");
		basicResults = as.basicSearch(query, 0, 0);
		System.out.println("Received " + basicResults.length + " results");
		// for(SearchResult result : basicResults) {
		// 	System.out.println(result.getItemId() + ": " + result.getName());
		// }

        System.out.println("");
		System.out.println("===============");
		System.out.println("Advanced Search");
		System.out.println("===============");

        System.out.println("");
        System.out.println("BuyPrice:5.99 (1)");
		SearchConstraint constraint = new SearchConstraint(FieldName.BuyPrice, "5.99");
		SearchConstraint[] constraints = {constraint};

		SearchResult[] advancedResults = as.advancedSearch(constraints, 0, 0);
		System.out.println("Received " + advancedResults.length + " results");
		for(SearchResult result : advancedResults) {
			System.out.println(result.getItemId() + ": " + result.getName());
		}

        System.out.println("");
        System.out.println("ItemName:pan, Category:kitchenware (16)");

        constraint = new SearchConstraint(FieldName.ItemName, "pan");
        SearchConstraint constraint2 = new SearchConstraint(FieldName.Category, "kitchenware");
        constraints = new SearchConstraint[]{constraint, constraint2};
		advancedResults = as.advancedSearch(constraints, 0, 0);
		System.out.println("Received " + advancedResults.length + " results");
		for(SearchResult result : advancedResults) {
			System.out.println(result.getItemId() + ": " + result.getName());
		}

        System.out.println("");
        System.out.println("ItemName:Precious Moments, SellerId:waltera317a (2)");
        constraint = new SearchConstraint(FieldName.ItemName, "Precious Moments");
        constraint2 = new SearchConstraint(FieldName.SellerId, "waltera317a");
        constraints = new SearchConstraint[]{constraint, constraint2};

		advancedResults = as.advancedSearch(constraints, 0, 0);
		System.out.println("Received " + advancedResults.length + " results");
		for(SearchResult result : advancedResults) {
			System.out.println(result.getItemId() + ": " + result.getName());
		}

        System.out.println("");
        System.out.println("EndTime:Dec-14-01 21:00:05 (1)");
        constraint = new SearchConstraint(FieldName.EndTime, "Dec-14-01 21:00:05");
        constraints = new SearchConstraint[]{constraint};

		advancedResults = as.advancedSearch(constraints, 0, 0);
		System.out.println("Received " + advancedResults.length + " results");
		for(SearchResult result : advancedResults) {
			System.out.println(result.getItemId() + ": " + result.getName());
		}

		String itemId = "1497595357";
		String item = as.getXMLDataForItemId(itemId);
		System.out.println("XML data for ItemId: " + itemId);
		System.out.println(item);

		itemId = "1497226981";
		item = as.getXMLDataForItemId(itemId);
		System.out.println("XML data for ItemId: " + itemId);
		System.out.println(item);
	}
}
