import { ClientSideRowModelModule } from '@ag-grid-community/client-side-row-model';
import { ColDef, ModuleRegistry } from '@ag-grid-community/core';
import { AgGridReact } from '@ag-grid-community/react';
import '@ag-grid-community/styles/ag-grid.css';
import '@ag-grid-community/styles/ag-theme-quartz.css';
import { useIntl, useModel } from '@umijs/max';
import { Button, Input, message, Modal } from 'antd';
import copy from 'copy-to-clipboard';
import React, { useMemo, useRef, useState } from 'react';
import CustomHeader from './customHeader';
import { IItem, IOlympicData } from './DmsGrid.type';
import './style/ag-grid.css'; // Core CSS
import './style/ag-theme-balham.css'; // Theme
import './style/style.less';

// Register the required feature modules with the Grid
ModuleRegistry.registerModules([ClientSideRowModelModule]);

const DmsAgGrid: React.FC<{ item: IItem; visibleColumns: string[] }> = ({
  item,
  visibleColumns,
}) => {
  const intl = useIntl();
  const containerStyle = useMemo(
    () => ({
      width: '100%',
      height: `calc(100% - 34px)`,
    }),
    [],
  );
  const gridStyle = useMemo(() => ({ height: '98%', width: '100%' }), []);
  const gridRef = useRef<AgGridReact>(null);
  const { columns, data: dataList } = item?.data || {};
  const [dataListShow, setDataListShow] = useState<any>(dataList);
  const { agGridkey } = useModel('global');

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
      hide: !visibleColumns.includes(column?.dataIndex),
      headerName: column.dataIndex,
      cellDataType: column.dataType?.toLowerCase(),
    }));

    cols.length &&
      cols.unshift({
        headerName: '',
        sortable: false,
        width: 60,
        pinned: 'left',
        suppressMovable: true,
        valueGetter: (params: any) => {
          return params.node.rowIndex + 1;
        },
      });

    return cols;
  }, [columns, visibleColumns]);

  const components = useMemo(() => {
    return {
      agColumnHeader: (props: any) => (
        <CustomHeader {...props} dataList={dataList} setDataListShow={setDataListShow} />
      ),
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

  const copyTableCell = () => {
    copy(isModalOpen.data);
    message.success(
      intl.formatMessage({ id: 'dms.common.operate.copy' }) +
        intl.formatMessage({ id: 'dms.common.modal.successful' }),
    );
  };

  // ctrl + c event
  const onCellKeyDown = (e: any) => {
    if (!e.event) {
      return;
    }
    const kbEvent = e.event;
    if ((kbEvent.ctrlKey || kbEvent.metaKey) && kbEvent.key == 'c') {
      copy(e.value);
    }
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
            event.colDef.field &&
              setIsModalOpen({
                isHidden: true,
                data: event.value,
              });
          }}
          onCellKeyDown={(event) => {
            onCellKeyDown(event);
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
            isModalOpen.data?.toString() && (
              <Button onClick={copyTableCell} key="copyTableCell">
                {intl.formatMessage({ id: 'dms.common.operate.copy' })}
              </Button>
            ),
            <Button
              key="close"
              type="primary"
              onClick={() => {
                setIsModalOpen({
                  isHidden: false,
                  data: '',
                });
              }}
            >
              {intl.formatMessage({ id: 'dms.common.tabs.card.close' })}
            </Button>,
          ]}
        >
          <Input.TextArea autoSize={{ minRows: 6, maxRows: 6 }} value={isModalOpen.data} />
        </Modal>
      )}
    </div>
  );
};

export default DmsAgGrid;
