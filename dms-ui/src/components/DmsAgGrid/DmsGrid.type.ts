export interface IOlympicData {
  athlete: string;
  age: number;
  country: string;
  year: number;
  date: string;
  sport: string;
  gold: number;
  silver: number;
  bronze: number;
  total: number;
}

export interface IItem {
  data?: {
    columns: Array<{
      dataIndex: string;
      isReadOnly: boolean;
      dataType?: string;
    }>;
    data?: IOlympicData[];
  };
}

export type CustomHeaderProps = {
  displayName: string;
  column: any;
  setSort: (order: string, shiftKey: boolean) => void;
  api: any;
  sortIcon?: any;
  setSortIcon?: any;
  dataList?: Array<any>;
  setDataListShow: any;
};

// 定义标签项的接口
export interface TabItem {
  label: React.ReactNode;
  children: React.ReactNode;
  key: string | null | undefined;
  tooltipTitle?: string;
  data?: any;
}

/**
 * 右侧编辑器结果组件
 * @param dataColumns 数据列数组
 * @param clearListData 清除列表数据回调函数
 */
export interface DmsGridProps {
  dataColumns: Array<{ key: string; data: any; label: string; sql: string }>;
  clearListData: (items: Array<TabItem>) => void;
  consoleList: Array<
    | boolean
    | React.ReactChild
    | React.ReactFragment
    | React.ReactPortal
    | null
    | undefined
  >;
  workspaceId: number | string;
  datasourceId: number | string;
}

export interface DmsGridRef {
  doSomething: () => void;
}
