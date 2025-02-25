### Doris

启动docker容器

```shell
docker compose -p doris up -d
```

数据库默认root和admin用户均没有密码，登录设置密码

```shell
mysql -uroot -P9030 -h127.0.0.1
```

```sql
set password for 'root' = password('123456');
set password for 'admin' = password('123456');
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
