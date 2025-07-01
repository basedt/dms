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

-- 增量物化视图
CREATE TABLE base_table
(
    user_id UInt64,
    username String,
    registration_time DateTime
) ENGINE = MergeTree()
ORDER BY user_id;

INSERT INTO base_table (user_id, username, registration_time) VALUES
(1, 'Alice', now()),
(2, 'Bob', now() - INTERVAL 1 DAY),
(3, 'Charlie', now() - INTERVAL 2 DAY),
(4, 'David', now() - INTERVAL 3 DAY),
(5, 'Eve', now() - INTERVAL 4 DAY),
(6, 'Frank', now() - INTERVAL 5 DAY),
(7, 'Grace', now() - INTERVAL 6 DAY),
(8, 'Hank', now() - INTERVAL 7 DAY),
(9, 'Ivy', now() - INTERVAL 8 DAY),
(10, 'Jack', now() - INTERVAL 9 DAY);

CREATE TABLE summing_table
(
  day Date,
  cnt UInt32
)
ENGINE = SummingMergeTree
ORDER BY day;

create materialized view mv_incremental_test to summing_table
as
select
    toDate(toStartOfDay(registration_time)) as day,
    count(1) as cnt
from base_table
group by toDate(toStartOfDay(registration_time))
;

-- 全量物化视图

CREATE TABLE base_table2
(
    user_id UInt64,
    username String,
    registration_time DateTime
) ENGINE = MergeTree()
ORDER BY user_id;

INSERT INTO base_table2 (user_id, username, registration_time) VALUES
(1, 'Alice', now()),
(2, 'Bob', now() - INTERVAL 1 DAY),
(3, 'Charlie', now() - INTERVAL 2 DAY),
(4, 'David', now() - INTERVAL 3 DAY),
(5, 'Eve', now() - INTERVAL 4 DAY),
(6, 'Frank', now() - INTERVAL 5 DAY),
(7, 'Grace', now() - INTERVAL 6 DAY),
(8, 'Hank', now() - INTERVAL 7 DAY),
(9, 'Ivy', now() - INTERVAL 8 DAY),
(10, 'Jack', now() - INTERVAL 9 DAY);

CREATE TABLE summing_snapshot
(
  day Date,
  cnt UInt32
)
ENGINE = SummingMergeTree
ORDER BY day;

create materialized view mv_refreshable_test
REFRESH EVERY 60 MINUTE TO summing_snapshot as
select
    toDate(toStartOfDay(registration_time)) as day,
    count(1) as cnt
from base_table2
group by toDate(toStartOfDay(registration_time))
;

SELECT * FROM mv_refreshable_test;

-- create file in user_files
CREATE TABLE external_csv_table
(
    `name` String,
    `age` Int32,
    `city` String
)
ENGINE = File(CSV, 'test.csv');