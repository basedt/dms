CREATE TABLE purchases
(
    `user_id` UInt32,
    `product_id` UInt32,
    `quantity` Int32,
    `price` Float64,
    `purchase_time` DateTime
) ENGINE = MergeTree()
ORDER BY purchase_time;

CREATE MATERIALIZED VIEW mv_user_total_spending
ENGINE = SummingMergeTree()
ORDER BY user_id
POPULATE AS
SELECT
    user_id,
    sum(price * quantity) as total_spent
FROM
    purchases
GROUP BY
    user_id;

-- create file in user_files
CREATE TABLE external_csv_table
(
    `name` String,
    `age` Int32,
    `city` String
)
ENGINE = File(CSV, 'test.csv');