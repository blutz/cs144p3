CS 144 - Fall 2013 - Project Part 3

PARTNERS:
---------
David Voong
ID: 303886364

Byron Lutz
ID: 403945853

PART A.2: DECIDE INDEXES TO CREATE
==================================

Lucene
------
Create indexes on:
    Item.name
    Item.description
    ItemCategory.category
And additionally store the fields:
    Item.item_id
    ItemCategory.item_id

This is because the only fields that need inverted index lookups are name, description, and category. However, to be able to talk to MySQL again and know what item to reference, we need to store the item_id in Lucene.

MySQL
-----
Create indexes on:
    Item.seller
    Item.buy_now_price
    Item.ends
    Bid.item_id
    Bid.user_id

These MySQL indexes were chosen because users need to be able to search on item
name, category, seller, buy price, bidder, ending time, and description.
Because item name, category, and description only need to be searched using
inverted indexes, it is a waste to create indexes on them in MySQL.

Indexes on Bid.item_id and Bid.user_id are created because the primary key on
Bid is (item_id, user_id, time), rather than a single field. Though this extra
index can be considered a waste, it is a tradeoff for not creating a unique ID
for each of the bids.

