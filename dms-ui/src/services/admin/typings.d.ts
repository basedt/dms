declare namespace DMS {
  type SysDictType = {
    id?: number;
    dictTypeCode: string;
    dictTypeName: string;
    remark?: string;
    createTime?: Date;
    updateTime?: Date;
  };

  type SysDictTypeParam = DMS.QueryParam & {
    dictTypeCode?: string;
    dictTypeName?: string;
  };

  type SysDictData = {
    id?: number;
    sysDictType: SysDictType;
    dictCode?: string;
    dictValue?: string;
    remark?: string;
  };

  type SysDictDataParam = DMS.QueryParam & {
    dictTypeCode?: string;
    dictCode?: string;
    dictValue?: string;
  };

  type SysUser = {
    id?: string | number;
    userName?: string;
    nickName?: string;
    realName?: string;
    email?: string;
    mobilePhone?: string;
    password?: string;
    userStatus?: DMS.Dict;
    summary?: string;
    registerChannel?: DMS.Dict;
    registerTime?: Date;
    registerIp?: string;
    createTime?: Date;
    updateTime?: Date;
    roles?: DMS.SysRole[];
  };

  type SysUserParam = DMS.QueryParam & {
    userName?: string;
    email?: string;
    userStatus?: string;
  };

  type SysRole = {
    id?: string;
    roleCode?: string;
    roleName?: string;
    roleType?: DMS.Dict;
    roleStatus?: DMS.Dict;
    roleDesc?: string;
    privileges?: DMS.SysPrivilege[];
  };

  type SysRoleParam = DMS.QueryParam & {
    roleCode?: string;
    roleName?: string;
    roleStatus?: string;
  };

  type SysPrivilege = {
    id?: number;
    moduleCode: string;
    moduleName: string;
    pageCode?: string;
    pageName?: string;
    blockCode?: string;
    blockName?: string;
    privilegeCode: string;
    privilegeName: string;
  };

  type EmailConfig = {
    email: string;
    password: string;
    host: string;
    port: number;
  };

  type SysMessage = {
    id?: number;
    title: string;
    messageType: DMS.Dict;
    receiver: string;
    sender: string;
    content: string;
    isRead: DMS.Dict;
    isDelete: DMS.Dict;
    createTime:Date;
  };

  type SysMessageParam = {
    messageType?: string;
    isRead?: string;
    isDelete?: string;
  };
}
