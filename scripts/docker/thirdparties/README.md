### Doris

启动docker容器

```shell
cd scripts/docker/thirdparties/doris
chmod 775 start-doris.sh
./start-doris.sh
```

数据库默认root和admin用户均没有密码，登录设置密码

```shell
mysql -uroot -P9030 -h127.0.0.1
```

```sql
set password for 'root' = password('123456');
set password for 'admin' = password('123456');
create database sample;
```

### Oracle

启动docker容器

```shell
docker compose -p oracle up -d
```

修改密码

```shell
cd /opt/oracle
./setPassword.sh 123456
```

登录并配置数据库

```sql
sqlplus sys/123456@orclcdb as sysdba
-- 查看是cdb还是pdb
select name,cdb,open_mode,con_id from v$database;
-- 查看pdb列表
show pdbs;
-- 打开pdb
alter pluggable database orclpdb1 open;
-- 切换到pdb
alter session set container = orclpdb1;
-- 关闭pdb
shutdown immediate;
-- 启动pdb
startup;
-- orclpdb1 数据库默认用户：pdbadmin / 123456
```
### SQL Server
启动docker容器,MAC电脑需要先安装并启用rosetta
```shell
docker compose -p mssql up -d
```

登录并配置数据库
```sql
-- /opt/mssql-tools18/bin/sqlcmd -C -S localhost -U sa -P #password123
create
login [test_user]
with password = '#password123';
alter server role [sysadmin] add member [test_user];

create database sample;


CREATE TABLE users
(
    Id        INT IDENTITY(1,1) PRIMARY KEY,
    Name      NVARCHAR(50) NOT NULL,
    Gender    varchar(1)   NULL,
    BirthDate DATE         NULL,
    CreatedAt DATETIME2    NOT NULL DEFAULT SYSDATETIME(),
    UpdatedAt DATETIME2    NOT NULL DEFAULT SYSDATETIME()
);

INSERT INTO Users (Name, Gender, BirthDate)
VALUES ('John Smith', 'M', '1998-04-25'),
       ('Emily Johnson', 'F', '2002-09-14'),
       ('Michael Brown', 'M', '1985-12-01'),
       ('Sarah Davis', 'F', '1995-07-18'),
       ('David Wilson', NULL, '1978-03-12'),
       ('Jessica Miller', 'F', NULL),
       ('Robert Taylor', 'M', '2010-11-30'),
       ('Olivia Anderson', 'F', '2005-02-28'),
       ('William Thomas', 'M', '1992-08-09'),
       ('Sophia Martinez', NULL, NULL);

-- jdbc连接配置注意增加参数encrypt=true;trustServerCertificate=true

```
### Greenplum 
启动docker容器

```shell
cd scripts/docker/thirdparties/greenplum
docker compose up -d
```

创建用户和数据库

```shell
cd /usr/local/gpdb/bin
./psql -h localhost -p 5432 -U gpadmin -d postgres
```
```sql
create user gptest with password 'Passwd@123';
create database sample;
grant all privileges on database sample to gptest;
```
### Gaussdb
启动docker容器
```shell
cd scripts/docker/thirdparties/gaussdb
docker compose up -d
```
登录并创建数据库
```sql
-- 默认账号密码 gaussdb/Passwd@123
create database sample;
```

### Hive
启动docker容器,连接beeline
```shell
cd scripts/docker/thirdparties/hive
export MYSQL_JDBC_LOCAL_PATH=your_local_path_to_mysql_driver
docker compose up -d
docker exec -it hiveserver2 beeline -u 'jdbc:hive2://hiveserver2:10000'
```
```sql
show tables;
create table hive_example(a string, b int) partitioned by(c int);
alter table hive_example add partition(c=1);
insert into hive_example partition(c=1) values('a', 1), ('a', 2),('b',3);
select count(distinct a) from hive_example;
select sum(b) from hive_example;
```
