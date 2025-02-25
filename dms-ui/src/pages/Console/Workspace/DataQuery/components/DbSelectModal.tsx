import { Button, Divider, Modal, Select } from "antd";
import { useEffect, useState } from "react";
import { DataSourceService } from "@/services/workspace/datasource.service";
import { useIntl, Link, history } from "@umijs/max";
import { PlusOutlined } from "@ant-design/icons";

interface DbSelectModalProps {
  open: boolean;
  data: any;
  handleOk: (isOpen: boolean, value: any) => void;
  handleCancel: () => void;
}

const DbSelectModal = (props: DbSelectModalProps) => {
  const intl = useIntl();
  const { open, data, handleOk, handleCancel } = props;
  const [dbList, setDbList] = useState<DMS.Dict[]>([]);
  const [selectDb, setSelectDb] = useState<string | number>();
  useEffect(() => {
    if (data?.defaultValue) {
      setSelectDb(data?.defaultValue);
    }
    DataSourceService.listByWorkspace(data?.workspaceId as string).then(
      (resp) => {
        if (resp.success) {
          setDbList(resp.data as DMS.Dict[]);
        }
      }
    );
  }, []);

  return (
    <Modal
      title={intl.formatMessage({
        id: "dms.console.workspace.dataquery.select",
      })}
      open={open}
      onOk={() => {
        handleOk ? handleOk(false, selectDb) : null;
      }}
      destroyOnClose={true}
      maskClosable={false}
      onCancel={handleCancel}
      styles={{ body: { paddingTop: 8 } }}
      width={540}
    >
      <Select
        style={{ width: "100%" }}
        allowClear={false}
        showSearch={true}
        placeholder={intl.formatMessage({
          id: "dms.console.workspace.dataquery.select",
        })}
        defaultValue={data?.defaultValue}
        onChange={(value) => {
          setSelectDb(value);
        }}
        dropdownRender={(menu) => {
          return (
            <>
              {menu}
              <Divider style={{ margin: "8px 0" }} />
              <Link
                to={`/workspace/${data?.workspaceId as string
                  }?m=datasource&n=true`}
                reloadDocument={true}
                onClick={() => sessionStorage.setItem('selectOpen', 'true')}
              >
                <Button
                  type="text"
                  block
                  icon={<PlusOutlined />}
                  style={{ marginBottom: 2 }}
                >
                  {intl.formatMessage(
                    { id: "dms.console.workspace.datasource.new" },
                    { type: "" }
                  )}
                </Button>
              </Link>
            </>
          );
        }}
      >
        {dbList &&
          dbList.map((item) => {
            return (
              <Select.Option key={item.value} value={item.value}>
                {item.label}
              </Select.Option>
            );
          })}
      </Select>
    </Modal>
  );
};

export default DbSelectModal;
