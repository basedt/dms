import { ClientSideRowModelModule } from '@ag-grid-community/client-side-row-model';
import { ColDef, ModuleRegistry } from '@ag-grid-community/core';
import { AgGridReact } from '@ag-grid-community/react';
import '@ag-grid-community/styles/ag-grid.css';
import '@ag-grid-community/styles/ag-theme-quartz.css';
import './style/ag-grid.css'; // Core CSS
import './style/ag-theme-balham.css'; // Theme

import { Modal, Button, message, Input } from 'antd';
import React, { useMemo, useRef, useState } from 'react';
import CustomHeader from './customHeader';
import { useIntl, useModel } from '@umijs/max';
import { IItem, IOlympicData } from './DmsGrid.type';
import './style/style.less';

// Register the required feature modules with the Grid
ModuleRegistry.registerModules([ClientSideRowModelModule]);

const GridExample: React.FC<{ item: IItem }> = ({ item }) => {
  const intl = useIntl();
  const containerStyle = useMemo(() => ({
    width: '100%',
    height: `calc(100% - 34px)`,
  }), []);
  const gridStyle = useMemo(() => ({ height: '98%', width: '100%' }), []);
  const gridRef = useRef<AgGridReact>(null);
  const { columns, data: dataList } = item?.data || {};
  const [dataListShow, setDataListShow] = useState<any>(dataList)
  const { agGridkey } = useModel('global')

  const [isModalOpen, setIsModalOpen] = useState({
    isHidden: false,
    data: '',
  });

  const transformedData: ColDef[] = useMemo(() => {
    if (!columns) {
      return [];
    }

    const cols: ColDef[] = columns.map((column) => ({
      field: column.dataIndex,
      editable: column.isReadOnly,
      suppressMovable: true,
      width: 140,
      headerName: column.dataIndex,
      cellDataType: column.dataType?.toLowerCase(),
    }));

    cols.length && cols.unshift({
      headerName: '',
      sortable: false,
      width: 60,
      pinned: 'left',
      suppressMovable: true,
      valueGetter: (params: any) => {
        return params.node.rowIndex + 1;
      },
    });

    return cols
  }, [columns]);



  const components = useMemo(() => {
    return {
      agColumnHeader: (props: any) => <CustomHeader {...props} dataList={dataList} setDataListShow={setDataListShow} />
    };
  }, [dataList]);

  const defaultColDef = useMemo<ColDef>(() => {
    return {
      editable: false,
      filter: true,
      headerComponentParams: {
        menuIcon: 'fa-bars',
      },
    };
  }, []);

  // 复制表格单元格内容到剪贴板
  const copyTableCell = () => {
    if (!isModalOpen.data) return
    navigator.clipboard.writeText(isModalOpen.data);
    message.success(intl.formatMessage({ id: 'dms.common.modal.copy' }) + intl.formatMessage({ id: 'dms.common.modal.successful' }));
  };

  return (
    <div style={containerStyle}>
      <div style={gridStyle} className={'ag-theme-balham'}>
        <AgGridReact<IOlympicData>
          key={agGridkey}
          ref={gridRef}
          rowData={dataListShow}
          columnDefs={transformedData}
          suppressMenuHide={true}
          components={components}
          defaultColDef={defaultColDef}
          onSortChanged={() => {
            if (gridRef.current) {
              gridRef.current.api.redrawRows();
            }
          }}
          onCellDoubleClicked={(event) => {
            event.colDef.field && setIsModalOpen({
              isHidden: true,
              data: event.value,
            });
          }}
        />
      </div>
      {isModalOpen.isHidden && (
        <Modal
          title={intl.formatMessage({ id: 'dms.common.modal.currentFieldValue' })}
          open={isModalOpen.isHidden}
          centered
          onCancel={() => {
            setIsModalOpen({
              isHidden: false,
              data: '',
            });
          }}
          styles={{ body: { height: '150px', overflow: 'auto' } }}
          footer={[
            <Button onClick={copyTableCell} key="copyTableCell">
              {intl.formatMessage({ id: 'dms.common.modal.copy' })}
            </Button>,
            <Button key="close" type="primary" onClick={() => {
              setIsModalOpen({
                isHidden: false,
                data: '',
              });
            }} >{intl.formatMessage({ id: 'dms.common.tabs.card.close' })}

            </Button>
          ]}
        >
          <Input.TextArea autoSize={{ minRows: 6, maxRows: 6 }} value={isModalOpen.data} />
        </Modal>
      )}
    </div>
  );
};

export default GridExample;
