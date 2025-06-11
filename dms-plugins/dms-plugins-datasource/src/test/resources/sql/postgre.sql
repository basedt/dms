-- create table

CREATE TABLE orders (
    order_id SERIAL,
    product_name VARCHAR(255),
    order_date DATE NOT NULL,
    PRIMARY KEY (order_id, order_date)
) PARTITION BY RANGE (order_date);

CREATE TABLE orders_q1_2023 PARTITION OF orders
FOR VALUES FROM ('2023-01-01') TO ('2023-04-01');

CREATE TABLE orders_q2_2023 PARTITION OF orders
FOR VALUES FROM ('2023-04-01') TO ('2023-07-01');

CREATE TABLE orders_q3_2023 PARTITION OF orders
FOR VALUES FROM ('2023-07-01') TO ('2023-10-01');

INSERT INTO orders (product_name, order_date) VALUES
('Product A', '2023-01-15'),
('Product B', '2023-02-20'),
('Product C', '2023-03-25');

INSERT INTO orders (product_name, order_date) VALUES
('Product D', '2023-04-15'),
('Product E', '2023-05-20'),
('Product F', '2023-06-25');

INSERT INTO orders (product_name, order_date) VALUES
('Product G', '2023-07-15'),
('Product H', '2023-08-20'),
('Product I', '2023-09-25');

SELECT * FROM orders;

CREATE TABLE test_table (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    amount decimal(10, 2) NOT NULL
);

INSERT INTO test_table (name, amount) VALUES
('Item 1', 123.45),
('Item 2', 678.90),
('Item 3', 99999.99);

select * from test_table;

-- create materialized view
CREATE TABLE customers (
    customer_id SERIAL PRIMARY KEY,
    name VARCHAR(255),
    email VARCHAR(255)
);

INSERT INTO customers (name, email) VALUES ('张三', 'zhangsan@example.com');
INSERT INTO customers (name, email) VALUES ('李四', 'lisi@example.com');

CREATE MATERIALIZED VIEW mv_customer AS
SELECT
    c.customer_id,
    c.name
FROM
    customers c;

-- foreign table
CREATE EXTENSION file_fdw;

CREATE SERVER csv_server
  FOREIGN DATA WRAPPER file_fdw;

CREATE FOREIGN TABLE csv_table (
  id INT,
  name TEXT,
  age INT
)
SERVER csv_server
OPTIONS (filename '/opt/csv/file.csv', format 'csv');