/* init privilege data */
truncate table sys_privilege;
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('sys:dic:dct:0','dms.p.sys.dic.dct.show','sys:dic:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('ws:wss:wpi:2','dms.p.ws.wss.wpi.edit','ws:wss:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('dst:dft:dft:0','dms.p.dst.dft.dft.show','dg:dft:dft:0','1','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('ws:wsh:dft:0','dms.p.ws.wsh.dft.show','ws:dft:dft:0','2','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('ws:dft:dft:0','dms.p.ws.dft.dft.show','root','1','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('sys:rol:rli:0','dms.p.sys.rol.rli.show','sys:rol:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('sys:rol:rli:4','dms.p.sys.rol.rli.grant','sys:rol:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('ws:wss:wpi:1','dms.p.ws.wss.wpi.add','ws:wss:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('sys:usr:uli:2','dms.p.sys.usr.uli.edit','sys:usr:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('sys:usr:uli:3','dms.p.sys.usr.uli.delete','sys:usr:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('sys:dic:dcd:1','dms.p.sys.dic.dcd.add','sys:dic:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('sys:dic:dcd:3','dms.p.sys.dic.dcd.delete','sys:dic:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('dg:dgd:ddi:0','dms.p.dg.dgd.ddi.show','dg:dgd:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('ws:wsd:wdl:2','dms.p.ws.wsd.wdl.edit','ws:wsd:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('ws:wse:dft:0','dms.p.ws.wse.dft.show','ws:dft:dft:0','2','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('sys:usr:dft:0','dms.p.sys.usr.dft.show','sys:dft:dft:0','2','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('sys:dic:dct:1','dms.p.sys.dic.dct.add','sys:dic:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('ws:wsd:dft:0','dms.p.ws.wsd.dft.show','ws:dft:dft:0','2','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('sys:rol:rli:1','dms.p.sys.rol.rli.add','sys:rol:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('ws:wss:wpi:3','dms.p.ws.wss.wpi.delete','ws:wss:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('dg:dgm:dft:0','dms.p.dg.dgm.dft.show','dst:dft:dft:0','2','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('sys:usr:uli:1','dms.p.sys.usr.uli.add','sys:usr:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('dg:dgd:ddi:2','dms.p.dg.dgd.ddi.edit','dg:dgd:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('sys:usr:uli:0','dms.p.sys.usr.uli.show','sys:usr:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('sys:dic:dcd:0','dms.p.sys.dic.dcd.show','sys:dic:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('sys:dft:dft:0','dms.p.sys.dft.dft.show','root','1','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('dg:dgm:dmi:0','dms.p.dg.dgm.dmi.show','dg:dgm:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('dg:dge:dei:0','dms.p.dg.dge.dei.show','dg:dge:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('ws:wss:wpi:0','dms.p.ws.wss.wpi.show','ws:wss:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('dg:dgm:dmi:3','dms.p.dg.dgm.dmi.delete','dg:dgm:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('ws:wss:dft:0','dms.p.ws.wss.dft.show','ws:dft:dft:0','2','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('sys:dic:dft:0','dms.p.sys.dic.dft.show','sys:dft:dft:0','2','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('ws:wse:whe:0','dms.p.ws.wse.whe.show','ws:wse:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('dg:dgm:dmi:1','dms.p.dg.dgm.dmi.add','dg:dgm:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('dg:dgd:dft:0','dms.p.dg.dgd.dft.show','dst:dft:dft:0','2','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('sys:rol:rli:2','dms.p.sys.rol.rli.edit','sys:rol:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('dg:dgs:dgi:0','dms.p.dg.dgs.dgi.show','dg:dgs:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('dg:dgd:ddi:3','dms.p.dg.dgd.ddi.delete','dg:dgd:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('ws:wsd:wdl:0','dms.p.ws.wsd.wdl.show','ws:wsd:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('ws:wsd:wdl:3','dms.p.ws.wsd.wdl.delete','ws:wsd:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('ws:wso:dft:0','dms.p.ws.wso.dft.show','ws:dft:dft:0','2','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('ws:wsd:wdl:1','dms.p.ws.wsd.wdl.add','ws:wsd:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('dg:dge:dft:0','dms.p.dg.dge.dft.show','dst:dft:dft:0','2','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('sys:dic:dct:2','dms.p.sys.dic.dct.edit','sys:dic:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('dg:dft:dft:0','dms.p.dg.dft.dft.show','root','1','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('dg:dge:dei:1','dms.p.dg.dge.dei.add','dg:dge:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('sys:rol:rli:3','dms.p.sys.rol.rli.delete','sys:rol:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('dg:dgs:dgi:3','dms.p.dg.dgs.dgi.delete','dg:dgs:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('ws:wsh:whl:0','dms.p.ws.wsh.whl.show','ws:wsh:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('sys:dic:dcd:2','dms.p.sys.dic.dcd.edit','sys:dic:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('dg:dgs:dgi:2','dms.p.dg.dgs.dgi.edit','dg:dgs:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('dg:dgs:dft:0','dms.p.dg.dgs.dft.show','dst:dft:dft:0','2','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('ws:wsi:whi:0','dms.p.ws.wsi.whi.show','ws:wsi:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('dg:dgm:dmi:2','dms.p.dg.dgm.dmi.edit','dg:dgm:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('sys:dic:dct:3','dms.p.sys.dic.dct.delete','sys:dic:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('sys:set:dft:0','dms.p.sys.set.dft.show','sys:dft:dft:0','2','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('dg:dgs:dgi:1','dms.p.dg.dgs.dgi.add','dg:dgs:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('ws:wsi:dft:0','dms.p.ws.wsi.dft.show','ws:dft:dft:0','2','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('dg:dge:dei:3','dms.p.dg.dge.dei.delete','dg:dge:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('sys:rol:dft:0','dms.p.sys.rol.dft.show','sys:dft:dft:0','2','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('sys:dic:dct:7','dms.p.sys.dic.dct.detail','sys:dic:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('dg:dgd:ddi:1','dms.p.dg.dgd.ddi.add','dg:dgd:dft:0','3','sys','sys');
insert into sys_privilege (privilege_code,privilege_name,parent_code,level,creator,editor) values ('dg:dge:dei:2','dms.p.dg.dge.dei.edit','dg:dge:dft:0','3','sys','sys');

/* init role privilege */
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