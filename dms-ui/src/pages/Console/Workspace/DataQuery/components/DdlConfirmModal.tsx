import { MetaDataService } from '@/services/meta/metadata.service';
import { Editor } from '@monaco-editor/react';
import { useIntl } from '@umijs/max';
import { message, Modal } from 'antd';
import { useState } from 'react';

const DdlConfirmModal: React.FC<
  DMS.ModalProps<{ workspaceId: string | number; dataSourceId: string | number; script: string }>
> = (props) => {
  const intl = useIntl();
  const { open, data, handleOk, handleCancel } = props;
  const [loading, setLoading] = useState<boolean>(false);
  return (
    <Modal
      title={intl.formatMessage({ id: 'dms.console.workspace.table.ddl.priview' })}
      open={open}
      onOk={() => {
        setLoading(true);
        MetaDataService.executeDDL({
          workspaceId: data?.workspaceId as string,
          dataSourceId: data?.dataSourceId as string,
          script: data?.script as string,
        }).then((resp) => {
          if (resp.success) {
            message.success(
              intl.formatMessage({
                id: 'dms.common.message.operate.success',
              }),
            );
            handleOk?.(false);
          }
        });
        setLoading(false);
      }}
      onCancel={handleCancel}
      confirmLoading={loading}
      destroyOnHidden={true}
      styles={{
        body: {
          overflowY: 'scroll',
          maxHeight: '640px',
          paddingBottom: 12,
          height: 480,
        },
      }}
      width="780px"
    >
      <Editor width={'100%'} height={480} value={data?.script} language="sql"></Editor>
    </Modal>
  );
};

export default DdlConfirmModal;
