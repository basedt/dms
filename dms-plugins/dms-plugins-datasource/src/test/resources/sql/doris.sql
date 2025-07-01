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
