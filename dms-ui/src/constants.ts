export const DICT_TYPE = {
  bool: "bool",
  roleStatus: "role_status",
  userStatus: "user_status",
  messageType: "message_type",
  datasourceType: "datasource_type",
  sqlStatus: "sql_status",
  fileEncoding: "file_encoding",
  taskStatus: "task_status",
};

export const PRIVILEGES = {
  roleSuperAdmin: "_super_admin",
  sysDicDctShow: "sys:dic:dct:0",
  wsWssWpiEdit: "ws:wss:wpi:2",
  sysRolRliEdit: "sys:rol:rli:2",
  wsWshDftShow: "ws:wsh:dft:0",
  wsDftDftShow: "ws:dft:dft:0",
  wsWsdWdlShow: "ws:wsd:wdl:0",
  wsWsdWdlDelete: "ws:wsd:wdl:3",
  wsWsoDftShow: "ws:wso:dft:0",
  sysRolRliShow: "sys:rol:rli:0",
  sysRolRliGrant: "sys:rol:rli:4",
  wsWsdWdlAdd: "ws:wsd:wdl:1",
  sysDicDctEdit: "sys:dic:dct:2",
  wsWssWpiAdd: "ws:wss:wpi:1",
  sysUsrUliEdit: "sys:usr:uli:2",
  sysUsrUliDelete: "sys:usr:uli:3",
  sysDicDcdAdd: "sys:dic:dcd:1",
  sysDicDcdDelete: "sys:dic:dcd:3",
  sysRolRliDelete: "sys:rol:rli:3",
  wsWsdWdlEdit: "ws:wsd:wdl:2",
  sysUsrDftShow: "sys:usr:dft:0",
  sysDicDctAdd: "sys:dic:dct:1",
  wsWshWhlShow: "ws:wsh:whl:0",
  sysDicDcdEdit: "sys:dic:dcd:2",
  wsWsdDftShow: "ws:wsd:dft:0",
  wsWseDftShow: "ws:wse:dft:0",
  wsWsiDftShow: "ws:wsi:dft:0",
  sysRolRliAdd: "sys:rol:rli:1",
  wsWssWpiDelete: "ws:wss:wpi:3",
  sysUsrUliAdd: "sys:usr:uli:1",
  sysUsrUliShow: "sys:usr:uli:0",
  sysDicDctDelete: "sys:dic:dct:3",
  sysSetDftShow: "sys:set:dft:0",
  sysDicDcdShow: "sys:dic:dcd:0",
  sysDftDftShow: "sys:dft:dft:0",
  wsWssWpiShow: "ws:wss:wpi:0",
  wsWssDftShow: "ws:wss:dft:0",
  sysRolDftShow: "sys:rol:dft:0",
  sysDicDftShow: "sys:dic:dft:0",
  sysDicDctDetail: "sys:dic:dct:7",
};

export const PATTERNS = {
  characterWord: /^[a-zA-Z0-9_]+$/,
  excludeSpecialChar:
    /^[^\s\\\.\[\]\{\}\(\)\<\>\*\+\-\=\!\?\^\$\|~、/,【】《》。·`：；“”"':;]+$/,
};

export const DB_STORES = {
  tableMeta: "tableMeta",
};
