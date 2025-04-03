declare namespace DMS {
  type Workspace = {
    id: string | number;
    workspaceCode: string;
    workspaceName: string;
    owner: string;
    remark: string;
    createTime: Date;
    updateTime: Date;
  };

  type WorkspaceParam = DMS.QueryParam & {
    workspaceCode?: string;
    workspaceName?: string;
    owner?: string;
  };

  type DataSource = {
    id?: string | number;
    workspaceId: number | string;
    datasourceName?: string;
    datasourceType?: DMS.Dict;
    hostName?: string;
    databaseName?: string;
    port?: number;
    userName?: string;
    password?: string;
    remark?: string;
    attrs?: { [key: string]: any };
    isPasswordChange?: boolean;
    createTime?: Date;
    updateTime?: Date;
  };

  type DataSourceParam = {
    workspaceId: number | string;
    datasourceName?: string;
    datasourceType?: string;
    hostName?: string;
    databaseName?: string;
  };

  type FileCatalog = {
    id?: string | number;
    workspaceId: number | string;
    name?: string;
    pid?: string | number;
  };

  type FileTreeNode<T> = {
    key: T;
    title: string;
    parentId: T;
    type: string;
    children: FileTreeNode<T>[];
  };

  type File = {
    id?: string | number;
    workspaceId: number | string;
    datasourceId?: number | string;
    fileName?: string;
    fileType?: DMS.Dict;
    fileCatalog?: string | number;
    fileStatus?: DMS.Dict;
    content?: string;
    version?: number;
    owner?: string;
    currentEditor?: string;
    remark?: string;
    newFileName?: string;
    newFileCatalog?: number;
  };

  type sqlTopButton = {
    runButton: boolean;
    stopButton: boolean;
    publishButton: boolean;
    saveButton: boolean;
  };

  type LogSqlHistory = {
    id: string | number;
    workspaceId: number | string;
    datasourceId?: number | string;
    sqlScript?: string;
    startTime: Date;
    endTime: Date;
    sqlStatus: DMS.Dict;
    remark: string;
    creator: string;
    createTime: Date;
    updateTime: Date;
  };

  type LogSqlHistoryParam = {
    workspaceId: number | string;
    datasourceId?: number | string;
    startTimeFrom?: string;
    startTimeTo?: string;
    endTimeFrom?: string;
    endTimeTo?: string;
    sqlStatus?: string;
    creator?: string;
  };

  type DataTask = {
    id?: string | number;
    workspaceId: number | string;
    datasourceId: number | string;
    fileName?: string;
    fileType?: DMS.Dict;
    fileSize?: number;
    fileUrl?: string;
    splitRow?: number;
    fileEncoding?: Dms.Dict;
    taskStatus?: DMS.Dict;
    taskType?: DMS.Dict;
    sqlScript: string;
    creator?: string;
    createTime?: Date;
  };

  type DataTaskParam = {
    workspaceId: number | string;
    datasourceId?: number | string;
    fileName?: string;
    taskType?: string;
    taskStatus?: string;
    creator?: string;
    createTimeFrom?: string;
    createTimeTo?: string;
  };

  type DataTaskLog = {
    id?: string | number;
    taskId: number | string;
    logInfo: string;
    creator?: string;
    createTime?: Date;
  };

  type ImportDataTaskParam = {
    workspaceId: number | string;
    datasourceId: number | string;
    catalog?: string;
    schema?: string;
    tableName?: string;
    isTruncate?: boolean;
    file?: any;
    fileType?: string;
    fileEncoding?: string;
    separator?: string;
  };

  type ChatMsg = {
    cid: string;
    messages: string[];
  };
}
