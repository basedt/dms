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