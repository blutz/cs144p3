-- Drop indexes, not including those already specified as primary keys in
-- project part 1's create.sql
DROP INDEX IdxOnSellerId ON Item;
DROP INDEX IdxOnBuyNowPrice ON Item;
DROP INDEX IdxOnEnds ON Item;
DROP INDEX IdxOnItemId ON Bid;
DROP INDEX IdxOnBidderId ON Bid;

