create database sample;

CREATE TABLE table_hash
(
    id BIGINT,
    name VARCHAR(2048),
    age SMALLINT DEFAULT 10,
    created datetime default CURRENT_TIMESTAMP
)
    UNIQUE KEY(id)
DISTRIBUTED BY HASH (id) BUCKETS 32
    PROPERTIES (
    "replication_num" = "1"
);

create view v_test as
select * from table_hash;

select * from v_test;

insert into table_hash(id,name,age) values(1,'tom',10);

create materialized view mv_test
refresh complete on schedule every 10 hour
distributed by hash (id) buckets 32
properties (
    "replication_num" = "1"
)
as
select * from table_hash;

drop table if exists user_info2;
CREATE TABLE IF NOT EXISTS user_info2
(
    user_id         VARCHAR(10)        NOT NULL,
    user_name       VARCHAR(50)     NOT NULL,
    city            VARCHAR(20),
    age             int,
    sex             int,
    INDEX idx_name1 (`user_name`) USING INVERTED PROPERTIES("parser" = "english"),
    INDEX idx_name2 (`city`) USING INVERTED PROPERTIES("parser" = "english")
)
UNIQUE KEY(user_id,user_name,city)
DISTRIBUTED BY HASH(user_id) BUCKETS 10
PROPERTIES (
    "enable_unique_key_merge_on_write" = "true",
    "replication_num" = "1",
    "bloom_filter_columns"="age,sex"
);


INSERT INTO user_info2 (user_id, user_name, city, age, sex) VALUES
(1, 'Alice', 'New York', 30, 1),
(2, 'Bob', 'Los Angeles', 25, 0),
(3, 'Charlie', 'Chicago', 35, 1),
(4, 'David', 'Houston', 40, 1),
(5, 'Eva', 'Phoenix', 28, 0),
(6, 'Frank', 'Philadelphia', 32, 1),
(7, 'Grace', 'San Antonio', 29, 0),
(8, 'Hank', 'San Diego', 31, 1),
(9, 'Ivy', 'Dallas', 27, 0),
(10, 'Jack', 'San Jose', 33, 1);

select * from user_info2;

create view v_test_01 as select * from user_info2;

-- 创建物化视图
-- 创建原始表 orders
CREATE TABLE orders (
    order_id INT,
    user_id INT,
    item_id INT,
    order_date DATE,
    quantity INT SUM DEFAULT '0',
    amount DECIMAL(10, 2) SUM DEFAULT '0'
) AGGREGATE KEY(order_id, user_id, item_id,order_date)
DISTRIBUTED BY HASH(order_id) BUCKETS 10
PROPERTIES (
    "replication_num" = "1"
);

-- 插入示例数据
INSERT INTO orders (order_id, user_id, item_id, quantity, amount, order_date) VALUES
(1, 1, 101, 2, 100.00, '2023-01-01'),
(2, 1, 102, 1, 50.00, '2023-01-02'),
(3, 2, 101, 3, 150.00, '2023-01-03'),
(4, 2, 103, 2, 80.00, '2023-01-04');

CREATE MATERIALIZED VIEW complete_mv (
order_id COMMENT '订单日期',
user_id COMMENT '订单键', t
item_id COMMENT '部件键',
amount comment '金额'
)
BUILD IMMEDIATE
REFRESH AUTO
ON SCHEDULE EVERY 1 DAY STARTS '2025-06-05 15:00:00'
DISTRIBUTED BY HASH (order_id) BUCKETS 2
PROPERTIES
("replication_num" = "1")
AS
SELECT
order_id,
user_id,
item_id,
amount
FROM
orders;

-- 外部表
CREATE CATALOG mysql PROPERTIES (
    "type"="jdbc",
    "user"="root",
    "password"="123456",
    "jdbc_url" = "jdbc:mysql://localhost:3306",
    "driver_url" = "mysql-connector-j-8.3.0.jar",
    "driver_class" = "com.mysql.cj.jdbc.Driver"
);