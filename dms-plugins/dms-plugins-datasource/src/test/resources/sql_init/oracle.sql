create table t_test_table as
    select * from all_objects;
-- create index
alter table t_test_table add constraint T_TEST_TABLE_PK primary key (OBJECT_ID);
create index idx_t_test_table_name on T_TEST_TABLE(OBJECT_NAME);

create view t_test_view as
    select * from t_test_table;