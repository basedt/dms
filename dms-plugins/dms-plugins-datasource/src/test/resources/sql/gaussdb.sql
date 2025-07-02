create database sample;
-- 全量物化视图
-- 修改表的默认类型
set enable_default_ustore_table=off;

 CREATE TABLE t1(c1 int, c2 int);
 INSERT INTO t1 VALUES(1, 1);
 INSERT INTO t1 VALUES(2, 2);

--创建全量物化视图。
 CREATE MATERIALIZED VIEW mv AS select count(*) from t1;
 INSERT INTO t1 VALUES(3, 3);

 REFRESH MATERIALIZED VIEW mv;
 SELECT * FROM mv;
--删除物化视图，删除表。
 DROP MATERIALIZED VIEW mv;

-- 增量物化视图

--创建增量物化视图。
 CREATE INCREMENTAL MATERIALIZED VIEW mv_i AS SELECT * FROM t1;

 INSERT INTO t1 VALUES(4, 4);
--增量刷新物化视图。
 REFRESH INCREMENTAL MATERIALIZED VIEW mv_i;
--插入数据。
 INSERT INTO t1 VALUES(5, 5);
--全量刷新物化视图。
 REFRESH MATERIALIZED VIEW mv_i;
--查询物化视图结果。
 select * from mv_i;

--删除物化视图，删除表。
 DROP MATERIALIZED VIEW mv_i;