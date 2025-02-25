import { DataTaskService } from "@/services/workspace/data.task";
import { Editor } from "@monaco-editor/react";
import { useIntl } from "@umijs/max";
import { Button, Modal } from "antd";
import moment from "moment";
import { useEffect, useState } from "react";

const TaskLogModal: React.FC<DMS.ModalProps<{ taskId: number | string }>> = (
  props
) => {
  const intl = useIntl();
  const { open, data, handleOk, handleCancel } = props;
  const [loading, setLoading] = useState<boolean>(false);
  const [logInfo, setLogInfo] = useState<string>("");

  useEffect(() => {
    refreshLog();
  }, []);

  const refreshLog = () => {
    DataTaskService.viewLog(data?.taskId as string).then((resp) => {
      let log = "";
      if (resp.success) {
        resp.data?.forEach((item) => {
          log +=
            "[" +
            moment(item.createTime).format("YYYY-MM-DD HH:mm:ss") +
            "] " +
            item.logInfo +
            "\n";
        });
      }
      setLogInfo(log);
    });
  };

  return (
    <Modal
      title={intl.formatMessage({
        id: "dms.console.workspace.export.taskLog",
      })}
      open={open}
      onOk={() => {
        setLoading(true);
        refreshLog();
        setLoading(false);
      }}
      destroyOnClose={true}
      confirmLoading={loading}
      onCancel={handleCancel}
      styles={{ body: { paddingTop: 8 } }}
      width="680px"
      footer={
        <>
          <Button onClick={handleCancel}>
            {intl.formatMessage({ id: "dms.common.operate.close" })}
          </Button>
          <Button
            type="primary"
            onClick={() => {
              refreshLog();
            }}
          >
            {intl.formatMessage({ id: "dms.common.operate.refresh" })}
          </Button>
        </>
      }
    >
      <Editor height={"500px"} width={"100%"} value={logInfo} />
    </Modal>
  );
};

export default TaskLogModal;
