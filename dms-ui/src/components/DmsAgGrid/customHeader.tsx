import { Icon, useIntl } from '@umijs/max';
import { Button, Input, Popover, Space, Tree } from 'antd';
import { MouseEvent, useEffect, useState } from 'react';
import { CustomHeaderProps } from './DmsGrid.type';
import styles from './style/style.less';

const CustomHeader: React.FC<CustomHeaderProps> = (props) => {
  const [treeDataList, setTreeDataList] = useState<{ title: string; value: string; key: string }[]>(
    [],
  );
  const [treeData, setTreeData] = useState<{ title: string; value: string; key: string }[]>([]);
  const [treeDataCheckout, setTreeDataCheckout] = useState<string[]>([]);
  const [ascSort, setAscSort] = useState<boolean>(false);
  const [descSort, setDescSort] = useState<boolean>(false);
  const [open, setOpen] = useState<boolean>(false);
  const intl = useIntl();

  const [filterButton, setFilterButton] = useState<boolean>(false); //点击确认按钮

  const onSortRequested = (order: string, event: MouseEvent) => {
    props.setSort(order, event.shiftKey);
  };

  const onSortChanged = () => {
    setAscSort(props.column.isSortAscending());
    setDescSort(props.column.isSortDescending());
  };

  useEffect(() => {
    if (props) {
      let filterDataL = [
        ...new Set(props?.dataList?.map((item: any) => item[props?.column?.colId])),
      ];
      let filterDataList = filterDataL
        ?.filter((item) => item)
        ?.map((item: unknown, index: number) => {
          return {
            title: item as string,
            value: item as string,
            key: item as string,
          };
        });
      setTreeDataList(filterDataList || []);
      setTreeData(filterDataList || []);
      setTreeDataCheckout(filterDataList?.map((item) => item.key) || []);
    }
    props.column.addEventListener('sortChanged', onSortChanged);
  }, [props]);

  const onCheck = (checkedKeysValue: string[]) => {
    setTreeDataCheckout(checkedKeysValue);
  };

  const onChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { value } = e.target;
    setTreeData(
      treeDataList.filter((item: { title: string }) => {
        if (item.title === null) {
          return false;
        }
        const itemTitle = item.title.toString().toLowerCase();
        return itemTitle.includes(value.toLowerCase());
      }),
    );
  };

  const handleOpenChange = (newOpen: boolean) => {
    setOpen(newOpen);
  };
  //点击确认过滤方法
  const filtrationMethod = () => {
    const filterDataList: any = props?.dataList?.filter((item) =>
      treeDataCheckout.includes(item[props?.column?.colId]),
    );
    props?.setDataListShow(filterDataList);
    setFilterButton(false);
    if (props?.dataList?.length === filterDataList.length) {
      setFilterButton(true);
    }
  };

  const content = (
    <div className={styles.sorterPopover}>
      <div className={styles.sorterPopover_Top}>
        <span
          onClick={(event) => {
            onSortRequested('desc', event), setOpen(false);
          }}
        >
          <span>
            <Icon icon="local:sortDes" height="15" width="15" />
          </span>
          {intl.formatMessage({ id: 'dms.common.operate.desOrder' })}
        </span>
        <span
          onClick={(event) => {
            onSortRequested('asc', event), setOpen(false);
          }}
        >
          <span>
            <Icon icon="local:sortAsc" height="15" width="15" />
          </span>
          {intl.formatMessage({ id: 'dms.common.operate.ascOrder' })}
        </span>
      </div>
      <div className={styles.sorterPopover_Bottom}>
        <Input className={styles.sorterPopover_Bottom_input} onChange={onChange} />
        <Space size="small" style={{ marginBottom: 3 }}>
          <Button
            size="small"
            onClick={() => {
              onCheck(treeData.map((item) => item.key));
            }}
          >
            {intl.formatMessage({ id: 'dms.common.operate.select.all' })}
          </Button>{' '}
          <Button
            size="small"
            onClick={() => {
              onCheck([]);
            }}
          >
            {intl.formatMessage({ id: 'dms.common.operate.select.cancel.all' })}
          </Button>
        </Space>
        <Tree
          treeData={treeData}
          checkable
          onCheck={(item: any) => onCheck(item)}
          checkedKeys={treeDataCheckout as string[]}
          style={{ border: '1px solid #ccc', maxHeight: 163, minHeight: 163, overflowY: 'scroll' }}
        />
        <Space size="small" style={{ float: 'right', marginTop: 4 }} align="end">
          <Button
            size="small"
            type="primary"
            onClick={() => {
              filtrationMethod();
              setOpen(false);
            }}
          >
            {intl.formatMessage({ id: 'dms.common.operate.confirm' })}
          </Button>
          <Button
            size="small"
            onClick={() => {
              setOpen(false);
            }}
          >
            {intl.formatMessage({ id: 'dms.common.operate.cancel' })}
          </Button>
        </Space>
      </div>
    </div>
  );

  return (
    <div className={styles.headerIcon}>
      <div className="customHeaderLabel" style={{ marginRight: 5 }}>
        {props.column.userProvidedColDef.headerName || ''}
      </div>
      {props.displayName && (
        <Popover
          content={content}
          placement="bottomLeft"
          trigger="click"
          open={open}
          onOpenChange={handleOpenChange}
        >
          {descSort ? (
            <Icon icon="local:sortDes" height="15" width="15" />
          ) : ascSort ? (
            <Icon icon="local:sortAsc" height="15" width="15" />
          ) : null}
          {!descSort && !ascSort && <Icon icon="local:collect" height="13" width="13" />}
        </Popover>
      )}
    </div>
  );
};
export default CustomHeader;
