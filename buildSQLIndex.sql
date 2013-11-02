-- Create indexes, not including those already specified as primary keys in
-- project part 1's create.sql
CREATE INDEX IdxOnSellerId ON Item(seller_id);
CREATE INDEX IdxOnBuyNowPrice ON Item(buy_now_price);
CREATE INDEX IdxOnEnds ON Item(ends);
CREATE INDEX IdxOnItemId ON Bid(item_id);
CREATE INDEX IdxOnBidderId ON Bid(user_id);

