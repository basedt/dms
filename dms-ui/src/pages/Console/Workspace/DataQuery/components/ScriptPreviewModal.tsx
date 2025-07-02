import { Editor } from '@monaco-editor/react';
import { useIntl } from '@umijs/max';
import { Modal } from 'antd';
import copy from 'copy-to-clipboard';
import { useState } from 'react';

const ScriptPreviewModal: React.FC<DMS.ModalProps<{ script: string }>> = (props) => {
  const intl = useIntl();
  const { open, data, handleOk, handleCancel } = props;
  const [loading, setLoading] = useState<boolean>(false);
  const [sqlScript, setSqlScript] = useState<any>(data?.script);
  return (
    <Modal
      title={intl.formatMessage({ id: 'dms.console.workspace.table.ddl.priview' })}
      open={open}
      onOk={() => {
        setLoading(true);
        copy(sqlScript);
        handleOk?.(false);
        setLoading(false);
      }}
      okText={intl.formatMessage({ id: 'dms.common.operate.copy' })}
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
      <Editor
        width={'100%'}
        height={480}
        value={data?.script}
        language="sql"
        options={{ readOnly: true }}
        onChange={(value) => {
          setSqlScript(value || '');
        }}
      ></Editor>
    </Modal>
  );
};

export default ScriptPreviewModal;
