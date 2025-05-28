import { useIntl } from '@umijs/max';
import { Form, Modal } from 'antd';
import { useState } from 'react';

const DbCreateModal: React.FC<DMS.ModalProps<DMS.DataSource>> = (props) => {
  const intl = useIntl();
  const [form] = Form.useForm();
  const { open, data, handleOk, handleCancel } = props;
  const [loading, setLoading] = useState<boolean>(false);
  return (
    <Modal
      title={intl.formatMessage({
        id: 'dms.console.workspace.dataquery.database.newDatabase',
      })}
      open={open}
      onOk={() => {
        setLoading(true);
        form.validateFields().then((values) => {
          //todo submit
        });

        setLoading(false);
      }}
      destroyOnHidden={true}
      confirmLoading={loading}
      onCancel={handleCancel}
      styles={{ body: { paddingTop: 8 } }}
      width="540px"
    >
      hello
    </Modal>
  );
};

export default DbCreateModal;
