drop table if exists sys_dict_type;
create table sys_dict_type
(
    id             serial primary key,
    dict_type_code varchar(32)  not null,
    dict_type_name varchar(128) not null,
    remark         varchar(512),
    creator        varchar(32)  not null,
    create_time    timestamptz  not null default current_timestamp,
    editor         varchar(32)  not null,
    update_time    timestamptz  not null default current_timestamp
);
comment on table sys_dict_type is 'dict type table';
comment on column sys_dict_type.id is 'auto increment primary key';
comment on column sys_dict_type.dict_type_code is 'dict type code';
comment on column sys_dict_type.dict_type_name is 'dict type name';
comment on column sys_dict_type.remark is 'remark';
comment on column sys_dict_type.creator is 'creator';
comment on column sys_dict_type.create_time is 'create time';
comment on column sys_dict_type.editor is 'editor';
comment on column sys_dict_type.update_time is 'update time';
create unique index un_sys_dict_type on sys_dict_type (dict_type_code);
create index idx_sys_dict_type_name on sys_dict_type (dict_type_name);
/* init data */
insert into sys_dict_type(dict_type_code, dict_type_name, remark, creator, editor)
values ('bool', 'boolean', '', 'sys', 'sys');
insert into sys_dict_type(dict_type_code, dict_type_name, remark, creator, editor)
values ('user_status', 'user status', '', 'sys', 'sys');
insert into sys_dict_type(dict_type_code, dict_type_name, remark, creator, editor)
values ('register_channel', 'register channel', '', 'sys', 'sys');
insert into sys_dict_type(dict_type_code, dict_type_name, remark, creator, editor)
values ('role_type', 'role type', '', 'sys', 'sys');
insert into sys_dict_type(dict_type_code, dict_type_name, remark, creator, editor)
values ('role_status', 'role status', '', 'sys', 'sys');
insert into sys_dict_type(dict_type_code, dict_type_name, remark, creator, editor)
values ('login_type', 'login type', '', 'sys', 'sys');
insert into sys_dict_type(dict_type_code, dict_type_name, remark, creator, editor)
values ('message_type', 'message type', '', 'sys', 'sys');
insert into sys_dict_type(dict_type_code, dict_type_name, remark, creator, editor)
values ('is_read', 'is read', '', 'sys', 'sys');
insert into sys_dict_type(dict_type_code, dict_type_name, remark, creator, editor)
values ('datasource_type', 'datasource type', '', 'sys', 'sys');
insert into sys_dict_type(dict_type_code, dict_type_name, remark, creator, editor)
values ('sql_status', 'sql status', '', 'sys', 'sys');
insert into sys_dict_type(dict_type_code, dict_type_name, remark, creator, editor)
values ('file_type', 'file type', '', 'sys', 'sys');
insert into sys_dict_type(dict_type_code, dict_type_name, remark, creator, editor)
values ('file_status', 'file status', '', 'sys', 'sys');
insert into sys_dict_type(dict_type_code, dict_type_name, remark, creator, editor)
values ('task_status', 'task status', '', 'sys', 'sys');
insert into sys_dict_type(dict_type_code, dict_type_name, remark, creator, editor)
values ('task_type', 'task type', '', 'sys', 'sys');
insert into sys_dict_type(dict_type_code, dict_type_name, remark, creator, editor)
values ('file_encoding', 'file encoding', '', 'sys', 'sys');

drop table if exists sys_dict;
create table sys_dict
(
    id             serial primary key,
    dict_type_code varchar(32)  not null,
    dict_code      varchar(128) not null,
    dict_value     varchar(128) not null,
    remark         varchar(512),
    creator        varchar(32)  not null,
    create_time    timestamptz  not null default current_timestamp,
    editor         varchar(32)  not null,
    update_time    timestamptz  not null default current_timestamp
);
comment on table sys_dict is 'dict data table';
comment on column sys_dict.id is 'auto increment primary key';
comment on column sys_dict.dict_type_code is 'dict type code';
comment on column sys_dict.dict_code is 'dict code';
comment on column sys_dict.dict_value is 'dict value';
comment on column sys_dict.remark is 'remark';
comment on column sys_dict.creator is 'creator';
comment on column sys_dict.create_time is 'create time';
comment on column sys_dict.editor is 'editor';
comment on column sys_dict.update_time is 'update time';
create unique index un_sys_dict on sys_dict (dict_type_code, dict_code);
create index idx_sys_dict_name on sys_dict (dict_value);
/* init data */
insert into sys_dict(dict_type_code, dict_code, dict_value, remark, creator, editor)
values ('bool', '1', 'true', '', 'sys', 'sys');
insert into sys_dict(dict_type_code, dict_code, dict_value, remark, creator, editor)
values ('bool', '0', 'false', '', 'sys', 'sys');
insert into sys_dict(dict_type_code, dict_code, dict_value, remark, creator, editor)
values ('user_status', 'normal', 'normal', '', 'sys', 'sys');
insert into sys_dict(dict_type_code, dict_code, dict_value, remark, creator, editor)
values ('user_status', 'forbidden', 'forbidden', '', 'sys', 'sys');
insert into sys_dict(dict_type_code, dict_code, dict_value, remark, creator, editor)
values ('user_status', 'cancellation', 'cancellation', '', 'sys', 'sys');
insert into sys_dict(dict_type_code, dict_code, dict_value, remark, creator, editor)
values ('register_channel', '01', 'register', '', 'sys', 'sys');
insert into sys_dict(dict_type_code, dict_code, dict_value, remark, creator, editor)
values ('register_channel', '02', 'backend', '', 'sys', 'sys');
insert into sys_dict(dict_type_code, dict_code, dict_value, remark, creator, editor)
values ('role_status', '01', 'enable', '', 'sys', 'sys');
insert into sys_dict(dict_type_code, dict_code, dict_value, remark, creator, editor)
values ('role_status', '02', 'disable', '', 'sys', 'sys');
insert into sys_dict(dict_type_code, dict_code, dict_value, remark, creator, editor)
values ('role_type', '01', 'system build-in', '', 'sys', 'sys');
insert into sys_dict(dict_type_code, dict_code, dict_value, remark, creator, editor)
values ('role_type', '02', 'user created', '', 'sys', 'sys');
insert into sys_dict(dict_type_code, dict_code, dict_value, remark, creator, editor)
values ('login_type', '1', 'Login', '', 'sys', 'sys');
insert into sys_dict(dict_type_code, dict_code, dict_value, remark, creator, editor)
values ('login_type', '2', 'Logout', '', 'sys', 'sys');
insert into sys_dict(dict_type_code, dict_code, dict_value, remark, creator, editor)
values ('login_type', '0', 'Unknown', '', 'sys', 'sys');
insert into sys_dict(dict_type_code, dict_code, dict_value, remark, creator, editor)
values ('message_type', 'sys', 'System Message', '', 'sys', 'sys');
insert into sys_dict(dict_type_code, dict_code, dict_value, remark, creator, editor)
values ('is_read', '1', 'Read', '', 'sys', 'sys');
insert into sys_dict(dict_type_code, dict_code, dict_value, remark, creator, editor)
values ('is_read', '0', 'UnRead', '', 'sys', 'sys');
insert into sys_dict(dict_type_code, dict_code, dict_value, remark, creator, editor)
values ('datasource_type', 'mysql', 'Mysql', '', 'sys', 'sys');
insert into sys_dict(dict_type_code, dict_code, dict_value, remark, creator, editor)
values ('datasource_type', 'oracle', 'Oracle', '', 'sys', 'sys');
insert into sys_dict(dict_type_code, dict_code, dict_value, remark, creator, editor)
values ('datasource_type', 'postgreSQL', 'PostgreSQL', '', 'sys', 'sys');
insert into sys_dict(dict_type_code, dict_code, dict_value, remark, creator, editor)
values ('datasource_type', 'mssql', 'SQL Server', '', 'sys', 'sys');
insert into sys_dict(dict_type_code, dict_code, dict_value, remark, creator, editor)
values ('datasource_type', 'doris', 'Doris', '', 'sys', 'sys');
insert into sys_dict(dict_type_code, dict_code, dict_value, remark, creator, editor)
values ('datasource_type', 'hologres', 'Hologres', '', 'sys', 'sys');
insert into sys_dict(dict_type_code, dict_code, dict_value, remark, creator, editor)
values ('datasource_type', 'gaussdb', 'GaussDB', '', 'sys', 'sys');
insert into sys_dict(dict_type_code, dict_code, dict_value, remark, creator, editor)
values ('datasource_type', 'clickhouse', 'ClickHouse', '', 'sys', 'sys');
insert into sys_dict(dict_type_code, dict_code, dict_value, remark, creator, editor)
values ('datasource_type', 'mariadb', 'MariaDB', '', 'sys', 'sys');
insert into sys_dict(dict_type_code, dict_code, dict_value, remark, creator, editor)
values ('datasource_type', 'polardb_mysql', 'PolarDB Mysql', '', 'sys', 'sys');
insert into sys_dict(dict_type_code, dict_code, dict_value, remark, creator, editor)
values ('datasource_type', 'polardb_postgre', 'PolarDB Postgre', '', 'sys', 'sys');
insert into sys_dict(dict_type_code, dict_code, dict_value, remark, creator, editor)
values ('datasource_type', 'greenplum', 'Greenplum', '', 'sys', 'sys');
insert into sys_dict(dict_type_code, dict_code, dict_value, remark, creator, editor)
values ('datasource_type', 'apachehive', 'Apache Hive', '', 'sys', 'sys');
insert into sys_dict(dict_type_code, dict_code, dict_value, remark, creator, editor)
values ('sql_status', '01', 'dms.common.operate.sql.success', '', 'sys', 'sys');
insert into sys_dict(dict_type_code, dict_code, dict_value, remark, creator, editor)
values ('sql_status', '02', 'dms.common.operate.sql.failure', '', 'sys', 'sys');
insert into sys_dict(dict_type_code, dict_code, dict_value, remark, creator, editor)
values ('file_type', 'sql', 'SQL', '', 'sys', 'sys');
insert into sys_dict(dict_type_code, dict_code, dict_value, remark, creator, editor)
values ('file_type', 'csv', 'CSV', '', 'sys', 'sys');
insert into sys_dict(dict_type_code, dict_code, dict_value, remark, creator, editor)
values ('file_type', 'xlsx', 'XLSX', '', 'sys', 'sys');
insert into sys_dict(dict_type_code, dict_code, dict_value, remark, creator, editor)
values ('file_type', 'orc', 'ORC', '', 'sys', 'sys');
insert into sys_dict(dict_type_code, dict_code, dict_value, remark, creator, editor)
values ('file_status', 'd', 'Draft', '', 'sys', 'sys');
insert into sys_dict(dict_type_code, dict_code, dict_value, remark, creator, editor)
values ('file_status', 'p', 'Publish', '', 'sys', 'sys');
insert into sys_dict(dict_type_code, dict_code, dict_value, remark, creator, editor)
values ('task_status', '1', 'dms.common.operate.task.wait', '', 'sys', 'sys');
insert into sys_dict(dict_type_code, dict_code, dict_value, remark, creator, editor)
values ('task_status', '2', 'dms.common.operate.task.running', '', 'sys', 'sys');
insert into sys_dict(dict_type_code, dict_code, dict_value, remark, creator, editor)
values ('task_status', '3', 'dms.common.operate.task.success', '', 'sys', 'sys');
insert into sys_dict(dict_type_code, dict_code, dict_value, remark, creator, editor)
values ('task_status', '4', 'dms.common.operate.task.failure', '', 'sys', 'sys');
insert into sys_dict(dict_type_code, dict_code, dict_value, remark, creator, editor)
values ('task_type', 'i', 'Import', '', 'sys', 'sys');
insert into sys_dict(dict_type_code, dict_code, dict_value, remark, creator, editor)
values ('task_type', 'e', 'Export', '', 'sys', 'sys');
insert into sys_dict(dict_type_code, dict_code, dict_value, remark, creator, editor)
values ('file_encoding', 'UTF-8', 'UTF-8', '', 'sys', 'sys');
insert into sys_dict(dict_type_code, dict_code, dict_value, remark, creator, editor)
values ('file_encoding', 'GBK', 'GBK', '', 'sys', 'sys');

drop table if exists sys_user;
create table sys_user
(
    id               serial primary key,
    user_name        varchar(32)  not null,
    nick_name        varchar(50),
    real_name        varchar(64),
    email            varchar(128) not null,
    mobile_phone     varchar(16),
    password         varchar(64)  not null,
    user_status      varchar(16)  not null,
    summary          varchar(200),
    register_channel varchar(4),
    register_time    date,
    register_ip      varchar(16),
    creator          varchar(32)  not null,
    create_time      timestamptz  not null default current_timestamp,
    editor           varchar(32)  not null,
    update_time      timestamptz  not null default current_timestamp
);
comment on table sys_user is 'user info table';
comment on column sys_user.id is 'auto increment primary key';
comment on column sys_user.user_name is 'user name';
comment on column sys_user.nick_name is 'nick name';
comment on column sys_user.real_name is 'real name';
comment on column sys_user.email is 'email';
comment on column sys_user.mobile_phone is 'mobild phone';
comment on column sys_user.password is 'password';
comment on column sys_user.user_status is 'user status@user_status';
comment on column sys_user.summary is 'user summary';
comment on column sys_user.register_channel is 'register channel@register_channel';
comment on column sys_user.register_time is 'register time';
comment on column sys_user.register_ip is 'register ip';
comment on column sys_user.creator is 'creator';
comment on column sys_user.create_time is 'create time';
comment on column sys_user.editor is 'editor';
comment on column sys_user.update_time is 'update time';
create unique index un_sys_user_user_name on sys_user (user_name);
create unique index un_sys_user_email on sys_user (email);
create unique index un_sys_user_mobile_phone on sys_user (mobile_phone);

drop table if exists sys_role;
create table sys_role
(
    id          serial primary key,
    role_code   varchar(36) not null,
    role_name   varchar(64) not null,
    role_type   varchar(4)  not null,
    role_status varchar(4)  not null,
    role_desc   varchar(512),
    creator     varchar(32) not null,
    create_time timestamptz not null default current_timestamp,
    editor      varchar(32) not null,
    update_time timestamptz not null default current_timestamp
);
comment on table sys_role is 'user role table';
comment on column sys_role.id is 'auto increment primary key';
comment on column sys_role.role_code is 'role code';
comment on column sys_role.role_name is 'role name';
comment on column sys_role.role_type is 'role type@role_type';
comment on column sys_role.role_status is 'role status@role_status';
comment on column sys_role.role_desc is 'role remark';
comment on column sys_role.creator is 'creator';
comment on column sys_role.create_time is 'create time';
comment on column sys_role.editor is 'editor';
comment on column sys_role.update_time is 'update time';
create unique index un_sys_role on sys_role (role_code);
/* init data */
insert into sys_role(role_code, role_name, role_type, role_status, role_desc, creator, editor)
values ('_super_admin', 'super admin', '01', '01', 'super admin', 'sys', 'sys');
insert into sys_role(role_code, role_name, role_type, role_status, role_desc, creator, editor)
values ('_normal', 'normal user', '01', '01', 'normal user', 'sys', 'sys');

drop table if exists sys_privilege;
create table sys_privilege
(
    id             serial primary key,
    privilege_code varchar(32) not null,
    privilege_name varchar(32) not null,
    parent_code    varchar(32) not null,
    level          int,
    creator        varchar(32) not null,
    create_time    timestamptz not null default current_timestamp,
    editor         varchar(32) not null,
    update_time    timestamptz not null default current_timestamp
);
comment on table sys_privilege is 'privilege resource table';
comment on column sys_privilege.id is 'auto increment primary key';
comment on column sys_privilege.privilege_code is 'privilege code';
comment on column sys_privilege.privilege_name is 'privilege name';
comment on column sys_privilege.parent_code is 'parent privilege code';
comment on column sys_privilege.level is 'level';
comment on column sys_privilege.creator is 'creator';
comment on column sys_privilege.create_time is 'create time';
comment on column sys_privilege.editor is 'editor';
comment on column sys_privilege.update_time is 'update time';
create unique index un_sys_privilege on sys_privilege (privilege_code);

/* init data */
truncate table sys_privilege;
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('sys:dic:dct:0','dms.p.sys.dic.dct.show','sys:dic:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('ws:wss:wpi:2','dms.p.ws.wss.wpi.edit','ws:wss:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('sys:rol:rli:2','dms.p.sys.rol.rli.edit','sys:rol:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('ws:wsh:dft:0','dms.p.ws.wsh.dft.show','ws:dft:dft:0','2','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('ws:dft:dft:0','dms.p.ws.dft.dft.show','root','1','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('ws:wsd:wdl:0','dms.p.ws.wsd.wdl.show','ws:wsd:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('ws:wsd:wdl:3','dms.p.ws.wsd.wdl.delete','ws:wsd:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('ws:wso:dft:0','dms.p.ws.wso.dft.show','ws:dft:dft:0','2','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('sys:rol:rli:0','dms.p.sys.rol.rli.show','sys:rol:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('sys:rol:rli:4','dms.p.sys.rol.rli.grant','sys:rol:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('ws:wsd:wdl:1','dms.p.ws.wsd.wdl.add','ws:wsd:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('sys:dic:dct:2','dms.p.sys.dic.dct.edit','sys:dic:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('ws:wss:wpi:1','dms.p.ws.wss.wpi.add','ws:wss:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('sys:usr:uli:2','dms.p.sys.usr.uli.edit','sys:usr:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('sys:usr:uli:3','dms.p.sys.usr.uli.delete','sys:usr:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('sys:dic:dcd:1','dms.p.sys.dic.dcd.add','sys:dic:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('sys:dic:dcd:3','dms.p.sys.dic.dcd.delete','sys:dic:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('sys:rol:rli:3','dms.p.sys.rol.rli.delete','sys:rol:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('ws:wsd:wdl:2','dms.p.ws.wsd.wdl.edit','ws:wsd:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('ws:wse:dft:0','dms.p.ws.wse.dft.show','ws:dft:dft:0','2','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('sys:usr:dft:0','dms.p.sys.usr.dft.show','sys:dft:dft:0','2','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('sys:dic:dct:1','dms.p.sys.dic.dct.add','sys:dic:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('ws:wsh:whl:0','dms.p.ws.wsh.whl.show','ws:wsh:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('sys:dic:dcd:2','dms.p.sys.dic.dcd.edit','sys:dic:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('ws:wsd:dft:0','dms.p.ws.wsd.dft.show','ws:dft:dft:0','2','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('sys:rol:rli:1','dms.p.sys.rol.rli.add','sys:rol:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('ws:wsi:whi:0','dms.p.ws.wsi.whi.show','ws:wsi:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('ws:wss:wpi:3','dms.p.ws.wss.wpi.delete','ws:wss:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('sys:usr:uli:1','dms.p.sys.usr.uli.add','sys:usr:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('sys:usr:uli:0','dms.p.sys.usr.uli.show','sys:usr:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('sys:dic:dct:3','dms.p.sys.dic.dct.delete','sys:dic:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('sys:set:dft:0','dms.p.sys.set.dft.show','sys:dft:dft:0','2','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('sys:dic:dcd:0','dms.p.sys.dic.dcd.show','sys:dic:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('sys:dft:dft:0','dms.p.sys.dft.dft.show','root','1','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('ws:wsi:dft:0','dms.p.ws.wsi.dft.show','ws:dft:dft:0','2','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('ws:wss:wpi:0','dms.p.ws.wss.wpi.show','ws:wss:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('ws:wss:dft:0','dms.p.ws.wss.dft.show','ws:dft:dft:0','2','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('sys:rol:dft:0','dms.p.sys.rol.dft.show','sys:dft:dft:0','2','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('sys:dic:dft:0','dms.p.sys.dic.dft.show','sys:dft:dft:0','2','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('ws:wse:whe:0','dms.p.ws.wse.whe.show','ws:wse:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('sys:dic:dct:7','dms.p.sys.dic.dct.detail','sys:dic:dft:0','3','sys','sys');

drop table if exists sys_role_privilege;
create table sys_role_privilege
(
    id           serial primary key,
    role_id      integer     not null,
    privilege_id integer     not null,
    creator      varchar(32) not null,
    create_time  timestamptz not null default current_timestamp,
    editor       varchar(32) not null,
    update_time  timestamptz not null default current_timestamp
);
comment on table sys_role_privilege is 'role privilege relationships table';
comment on column sys_role_privilege.id is 'auto increment primary key';
comment on column sys_role_privilege.role_id is 'role id';
comment on column sys_role_privilege.privilege_id is 'privilege id';
comment on column sys_role_privilege.creator is 'creator';
comment on column sys_role_privilege.create_time is 'create time';
comment on column sys_role_privilege.editor is 'editor';
comment on column sys_role_privilege.update_time is 'update time';
create unique index un_sys_role_privilege on sys_role_privilege (role_id, privilege_id);
/* init data */
insert into sys_role_privilege(role_id, privilege_id, creator, editor)
select r.id as role_id, p.id as privilege_id, 'sys' as creator, 'sys' as editor
from sys_role r,
     sys_privilege p
where r.role_code = '_super_admin';

insert into sys_role_privilege(role_id, privilege_id, creator, editor)
select r.id  as role_id,
       p.id  as privilege_id,
       'sys' as creator,
       'sys' as editor
from sys_role r,
     sys_privilege p
where r.role_code = '_normal'
  and (p.privilege_code in (
                            'ws:dft:dft:0', 'sys:dic:dcd:0', 'sys:dic:dct:0'
    )
    or p.privilege_code like 'ws:wsd%'
    or p.privilege_code like 'ws:wss%'
    or p.privilege_code like 'ws:wsh%'
    or p.privilege_code like 'ws:wsi%'
    or p.privilege_code like 'ws:wse%'
      );

drop table if exists sys_user_role;
create table sys_user_role
(
    id          serial primary key,
    user_id     integer     not null,
    role_id     integer     not null,
    creator     varchar(32) not null,
    create_time timestamptz not null default current_timestamp,
    editor      varchar(32) not null,
    update_time timestamptz not null default current_timestamp
);
comment on table sys_user_role is 'user role relationships table';
comment on column sys_user_role.id is 'auto increment primary key';
comment on column sys_user_role.user_id is 'user id';
comment on column sys_user_role.role_id is 'role id';
comment on column sys_user_role.creator is 'creator';
comment on column sys_user_role.create_time is 'create time';
comment on column sys_user_role.editor is 'editor';
comment on column sys_user_role.update_time is 'update time';
create unique index un_sys_user_role on sys_user_role (user_id, role_id);

drop table if exists sys_config;
create table sys_config
(
    id          serial primary key,
    cfg_code    varchar(64) not null,
    cfg_value   text,
    creator     varchar(32) not null,
    create_time timestamptz not null default current_timestamp,
    editor      varchar(32) not null,
    update_time timestamptz not null default current_timestamp
);
comment on table sys_config is 'system info config table';
comment on column sys_config.id is 'auto increment primary key';
comment on column sys_config.cfg_code is 'config code';
comment on column sys_config.cfg_value is 'config value';
comment on column sys_config.creator is 'creator';
comment on column sys_config.create_time is 'create time';
comment on column sys_config.editor is 'editor';
comment on column sys_config.update_time is 'update time';
create unique index un_sys_config on sys_config (cfg_code);

insert into sys_config(cfg_code, cfg_value, creator, editor)
values ('admin.init', '1', 'sys', 'sys');

drop table if exists sys_message;
create table sys_message
(
    id           serial primary key,
    title        varchar(128) not null,
    message_type varchar(4)   not null,
    receiver     varchar(32)  not null,
    sender       varchar(32)  not null,
    content      text,
    is_read      varchar(1)   not null default '0',
    is_delete    varchar(1)   not null default '0',
    creator      varchar(32)  not null,
    create_time  timestamptz  not null default current_timestamp,
    editor       varchar(32)  not null,
    update_time  timestamptz  not null default current_timestamp
);
comment on table sys_message is 'system messages table';
comment on column sys_message.id is 'auto increment primary key';
comment on column sys_message.title is 'title';
comment on column sys_message.message_type is 'message type@message_type';
comment on column sys_message.receiver is 'receiver';
comment on column sys_message.sender is 'sender';
comment on column sys_message.content is 'content';
comment on column sys_message.is_read is 'is read';
comment on column sys_message.is_delete is 'is delete';
comment on column sys_message.creator is 'creator';
comment on column sys_message.create_time is 'create time';
comment on column sys_message.editor is 'editor';
comment on column sys_message.update_time is 'update time';
create index idx_sys_message_receiver on sys_message (receiver, message_type);
create index idx_sys_message_sender on sys_message (sender, message_type);

drop table if exists log_login;
create table log_login
(
    id           serial primary key,
    user_name    varchar(32),
    login_time   timestamptz not null,
    ip_address   varchar(16),
    login_type   varchar(4)  not null,
    client_info  varchar(512),
    os_info      varchar(128),
    browser_info varchar(512),
    action_info  text,
    creator      varchar(32) not null,
    create_time  timestamptz not null default current_timestamp,
    editor       varchar(32) not null,
    update_time  timestamptz not null default current_timestamp
);
comment on table log_login is 'user login and logout log';
comment on column log_login.id is 'auto increment primary key';
comment on column log_login.user_name is 'user name';
comment on column log_login.login_time is 'login time';
comment on column log_login.ip_address is 'ip address';
comment on column log_login.login_type is 'login type';
comment on column log_login.client_info is 'client info';
comment on column log_login.os_info is 'os info';
comment on column log_login.browser_info is 'browser info';
comment on column log_login.action_info is 'action info';
comment on column log_login.creator is 'creator';
comment on column log_login.create_time is 'create time';
comment on column log_login.editor is 'editor';
comment on column log_login.update_time is 'update time';
create index idx_log_login_time on log_login (login_time);
create index idx_log_login_ip on log_login (ip_address);


drop table if exists log_action;
create table log_action
(
    id           serial primary key,
    user_name    varchar(32),
    action_time  timestamptz not null,
    ip_address   varchar(16),
    action_url   varchar(512),
    token        varchar(128),
    client_info  varchar(512),
    os_info      varchar(128),
    browser_info varchar(512),
    action_info  text,
    creator      varchar(32) not null,
    create_time  timestamptz not null default current_timestamp,
    editor       varchar(32) not null,
    update_time  timestamptz not null default current_timestamp
);
comment on table log_action is 'user operate action log';
comment on column log_action.id is 'auto increment primary key';
comment on column log_action.user_name is 'user name';
comment on column log_action.action_time is 'action time';
comment on column log_action.ip_address is 'ip address';
comment on column log_action.action_url is 'action url';
comment on column log_action.token is 'user token or session id';
comment on column log_action.client_info is 'client info';
comment on column log_action.os_info is 'os info';
comment on column log_action.browser_info is 'browser info';
comment on column log_action.action_info is 'action info';
comment on column log_action.creator is 'creator';
comment on column log_action.create_time is 'create time';
comment on column log_action.editor is 'editor';
comment on column log_action.update_time is 'update time';
create index idx_log_action_time on log_action (action_time);
create index idx_log_action_ip on log_action (ip_address);

drop table if exists log_sql_history;
create table log_sql_history
(
    id            serial primary key,
    workspace_id  bigint      not null,
    datasource_id bigint      not null,
    sql_script    text,
    start_time    timestamptz not null,
    end_time      timestamptz not null,
    sql_status    varchar(4)  not null,
    remark        varchar(1024),
    creator       varchar(32) not null,
    create_time   timestamptz not null default current_timestamp,
    editor        varchar(32) not null,
    update_time   timestamptz not null default current_timestamp
);
comment on table log_sql_history is 'sql execute log history';
comment on column log_sql_history.id is 'auto increment primary key';
comment on column log_sql_history.workspace_id is 'workspace id';
comment on column log_sql_history.datasource_id is 'datasource id';
comment on column log_sql_history.sql_script is 'sql script';
comment on column log_sql_history.start_time is 'start time';
comment on column log_sql_history.end_time is 'end time';
comment on column log_sql_history.sql_status is 'sql execute result status';
comment on column log_sql_history.remark is 'error message when sql status is failure';
comment on column log_sql_history.creator is 'creator';
comment on column log_sql_history.create_time is 'create time';
comment on column log_sql_history.editor is 'editor';
comment on column log_sql_history.update_time is 'update time';
create index log_sql_history_creator on log_sql_history (creator);
create index log_sql_history_ws on log_sql_history (workspace_id);
create index log_sql_history_time on log_sql_history (start_time);

drop table if exists log_data_task;
create table log_data_task
(
    id          serial primary key,
    task_id     bigint      not null,
    log_info    text,
    creator     varchar(32) not null,
    create_time timestamptz not null default current_timestamp,
    editor      varchar(32) not null,
    update_time timestamptz not null default current_timestamp
);
comment on table log_data_task is 'data export and import log info';
comment on column log_data_task.id is 'auto increment primary key';
comment on column log_data_task.task_id is 'data task id';
comment on column log_data_task.log_info is 'log info';
comment on column log_data_task.creator is 'creator';
comment on column log_data_task.create_time is 'create time';
comment on column log_data_task.editor is 'editor';
comment on column log_data_task.update_time is 'update time';
create index log_data_task_task on log_data_task (task_id);

drop table if exists dms_workspace;
create table dms_workspace
(
    id             serial primary key,
    workspace_code varchar(32) not null,
    workspace_name varchar(64) not null,
    owner          varchar(32),
    remark         varchar(512),
    creator        varchar(32) not null,
    create_time    timestamptz not null default current_timestamp,
    editor         varchar(32) not null,
    update_time    timestamptz not null default current_timestamp
);
comment on table dms_workspace is 'dms workspace';
comment on column dms_workspace.id is 'auto increment primary key';
comment on column dms_workspace.workspace_code is 'project code';
comment on column dms_workspace.workspace_name is 'project name';
comment on column dms_workspace.owner is 'project owner';
comment on column dms_workspace.remark is 'remark';
comment on column dms_workspace.creator is 'creator';
comment on column dms_workspace.create_time is 'create time';
comment on column dms_workspace.editor is 'editor';
comment on column dms_workspace.update_time is 'update time';
create unique index un_dms_workspace on dms_workspace (workspace_code);

drop table if exists dms_datasource;
create table dms_datasource
(
    id              serial primary key,
    workspace_id    bigint      not null,
    datasource_name varchar(64) not null,
    datasource_type varchar(32) not null,
    host_name       varchar(256),
    database_name   varchar(64),
    port            int,
    user_name       varchar(256),
    password        varchar(256),
    remark          varchar(512),
    attrs           text,
    creator         varchar(32) not null,
    create_time     timestamptz not null default current_timestamp,
    editor          varchar(32) not null,
    update_time     timestamptz not null default current_timestamp
);
comment on table dms_datasource is 'dms datasource';
comment on column dms_datasource.id is 'auto increment primary key';
comment on column dms_datasource.workspace_id is 'workspace id';
comment on column dms_datasource.datasource_name is 'datasource name';
comment on column dms_datasource.datasource_type is 'datasource type';
comment on column dms_datasource.host_name is 'host name';
comment on column dms_datasource.database_name is 'database name';
comment on column dms_datasource.port is 'database port';
comment on column dms_datasource.user_name is 'user name';
comment on column dms_datasource.password is 'password';
comment on column dms_datasource.remark is 'remark';
comment on column dms_datasource.attrs is 'extended attributes';
comment on column dms_datasource.creator is 'creator';
comment on column dms_datasource.create_time is 'create time';
comment on column dms_datasource.editor is 'editor';
comment on column dms_datasource.update_time is 'update time';
create unique index un_dms_datasource on dms_datasource (datasource_name, workspace_id);

drop table if exists dms_file;
create table dms_file
(
    id            serial primary key,
    workspace_id  bigint       not null,
    datasource_id bigint       not null,
    file_name     varchar(256) not null,
    file_type     varchar(8)   not null,
    file_catalog  bigint       not null,
    file_status   varchar(4)   not null,
    content       text,
    version       bigint       not null default 0,
    owner         varchar(32)  not null,
    remark        varchar(512),
    creator       varchar(32)  not null,
    create_time   timestamptz  not null default current_timestamp,
    editor        varchar(32)  not null,
    update_time   timestamptz  not null default current_timestamp
);
comment on table dms_file is 'dms file';
comment on column dms_file.id is 'auto increment primary key';
comment on column dms_file.workspace_id is 'workspace id';
comment on column dms_file.datasource_id is 'datasource id';
comment on column dms_file.file_name is 'file name';
comment on column dms_file.file_type is 'file type';
comment on column dms_file.file_catalog is 'file catalog';
comment on column dms_file.file_status is 'file status';
comment on column dms_file.content is 'content';
comment on column dms_file.version is 'file version';
comment on column dms_file.owner is 'file owner';
comment on column dms_file.remark is 'remark';
comment on column dms_file.creator is 'creator';
comment on column dms_file.create_time is 'create time';
comment on column dms_file.editor is 'editor';
comment on column dms_file.update_time is 'update time';
create unique index un_dms_file on dms_file (workspace_id, file_catalog, file_name, version);

drop table if exists dms_file_catalog;
create table dms_file_catalog
(
    id           serial primary key,
    workspace_id bigint       not null,
    name         varchar(128) not null,
    pid          bigint,
    creator      varchar(32)  not null,
    create_time  timestamptz  not null default current_timestamp,
    editor       varchar(32)  not null,
    update_time  timestamptz  not null default current_timestamp
);
comment on table dms_file_catalog is 'dms file catalog';
comment on column dms_file_catalog.id is 'auto increment primary key';
comment on column dms_file_catalog.workspace_id is 'workspace id';
comment on column dms_file_catalog.name is 'file catalog name';
comment on column dms_file_catalog.pid is 'parent catalog id';
comment on column dms_file_catalog.creator is 'creator';
comment on column dms_file_catalog.create_time is 'create time';
comment on column dms_file_catalog.editor is 'editor';
comment on column dms_file_catalog.update_time is 'update time';
create index idx_dms_file_catalog_ws on dms_file_catalog (workspace_id);
create index idx_dms_file_catalog_pid on dms_file_catalog (pid);

drop table if exists dms_data_task;
create table dms_data_task
(
    id            serial primary key,
    workspace_id  bigint       not null,
    datasource_id bigint       not null,
    file_name     varchar(256) not null,
    file_type     varchar(8)   not null,
    file_size     bigint,
    file_url      text,
    split_row     bigint,
    file_encoding varchar(8),
    task_status   varchar(4),
    task_type     varchar(8),
    creator       varchar(32)  not null,
    create_time   timestamptz  not null default current_timestamp,
    editor        varchar(32)  not null,
    update_time   timestamptz  not null default current_timestamp
);
comment on table dms_data_task is 'dms data import and export task';
comment on column dms_data_task.id is 'auto increment primary key';
comment on column dms_data_task.workspace_id is 'workspace id';
comment on column dms_data_task.datasource_id is 'datasource id';
comment on column dms_data_task.file_name is 'file name';
comment on column dms_data_task.file_type is 'file type,csv、xls、xlsx';
comment on column dms_data_task.file_size is 'file size';
comment on column dms_data_task.file_url is 'file url';
comment on column dms_data_task.split_row is 'split row';
comment on column dms_data_task.file_encoding is 'file encoding';
comment on column dms_data_task.task_status is 'task status,wait、running、success、failure';
comment on column dms_data_task.task_type is 'task type,export or import';
comment on column dms_data_task.creator is 'creator';
comment on column dms_data_task.create_time is 'create time';
comment on column dms_data_task.editor is 'editor';
comment on column dms_data_task.update_time is 'update time';
create index idx_dms_data_task_ws on dms_data_task (workspace_id, datasource_id);
create index idx_dms_data_task_type on dms_data_task (task_type, task_status);
create index idx_dms_data_task_create_time on dms_data_task (create_time);
create index idx_dms_data_task_creator on dms_data_task (creator);

