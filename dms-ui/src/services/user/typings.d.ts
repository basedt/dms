declare namespace DMS {
  type LogLogin = {
    id?: number;
    userName: string;
    loginTime: Date;
    ipAddress: string;
    loginType: DMS.Dict;
    clientInfo?: string;
    osInfo?: string;
    browserInfo?: string;
    actionInfo?: string;
    createTime?: Date;
    updateTime?: Date;
  };
}
