import { saveAs } from 'file-saver';
import * as XLSX from 'xlsx';

// 定义列的类型
type Column = {
  title: string;
  dataIndex: string;
};

// 定义数据列表的类型
type DataList = {
  columns: Column[];
  data: Record<string, any>[];
};

type Node = {
  title: string;
  key: string;
  children?: Node[];
};

interface TreeNode {
  title: string;
  key: string;
  children?: TreeNode[];
}

// 导出数据到 Excel 的函数
export const exportDataToExcel = (dataList: DataList, fileName: string): void => {
  const headers = dataList.columns.map(col => col.title);
  const sheetData = dataList.data.map(item => {
    return dataList.columns.reduce((acc, col) => {
      acc[col.title] = item[col.dataIndex];
      return acc;
    }, {} as Record<string, any>);
  });
  const mergedData = [headers, ...sheetData.map(row => Object.values(row))];
  const ws = XLSX.utils.aoa_to_sheet(mergedData);
  const wb = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(wb, ws, 'Sheet1');
  const wbout = XLSX.write(wb, { bookType: 'xlsx', type: 'array' });
  saveAs(new Blob([wbout], { type: 'application/octet-stream' }), fileName + '.xlsx');
};

//返回当前匹配title的所有父节点的key
export const searchKeysByTitle = (data: Node[], searchTerm: string): string[] => {
  if (!searchTerm) return [];
  const result: string[] = [];

  function traverse(nodes: Node[], path: string[]) {
    nodes.forEach(node => {
      const newPath = [...path, node.key];
      if (node.title.includes(searchTerm.trim())) {
        result.push(...newPath);
      }
      if (node.children) {
        traverse(node.children, newPath);
      }
    });
  }

  traverse(data, []);
  return result;
}

export const findNodeByTitle = (
  data: DMS.CatalogTreeNode<string>[],
  searchTerm: string,
  results: DMS.CatalogTreeNode<string>[] = []
): any => {
  for (const item of data) {
    if (item.title.toLowerCase().includes(searchTerm.trim().toLowerCase())) {
      results.push(item);
    } else if (item.children && item.children.length > 0) {
      const foundInChildren: any = findNodeByTitle(item.children, searchTerm.trim(), []);
      if (foundInChildren.length > 0) {
        const updatedItem: DMS.CatalogTreeNode<string> = { ...item, children: foundInChildren };
        results.push(updatedItem);
      }
    }
  }
  return results;
};
// 防抖方法
export const debounce = <T extends (...args: any[]) => any>(func: T, delay: number) => {
  let timerId: ReturnType<typeof setTimeout> | null = null;

  return function (this: ThisParameterType<T>, ...args: Parameters<T>) {
    if (timerId) {
      clearTimeout(timerId);
    }
    timerId = setTimeout(() => {
      func.apply(this, args);
    }, delay) as ReturnType<typeof setTimeout>;
  } as (this: ThisParameterType<T>, ...args: Parameters<T>) => void;
};