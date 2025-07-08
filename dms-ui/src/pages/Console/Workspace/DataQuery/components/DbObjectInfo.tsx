import { MetaDataService } from '@/services/meta/metadata.service';
import { Editor } from '@monaco-editor/react';
import { useIntl } from '@umijs/max';
import { Button, message, Space, Spin } from 'antd';
import { useEffect, useState } from 'react';

export type DbObjectInfoProps = {
  workspaceId: string | number;
  datasource: DMS.DataSource;
  maxHeight: number;
  node: DMS.CatalogTreeNode<string>;
  action: string; //"create"|"edit"
};

const DbObjectInfoView: React.FC<DbObjectInfoProps> = (props) => {
  const intl = useIntl();
  const [loading, setLoading] = useState<boolean>(false);
  const { workspaceId, datasource, maxHeight, node, action } = props;
  const [script, setScript] = useState<string>('');

  useEffect(() => {
    generateScript();
  }, []);

  const generateScript = () => {
    setLoading(true);
    const objInfo: string[] = node.identifier.split('.') as string[];
    if (action === 'create' && (node.type === 'G_VIEW' || node.type === 'VIEW')) {
      const createViewScript = `CREATE VIEW ${objInfo[1]}.newView AS\nSELECT \n\t* \nFROM \n;`;
      setScript(createViewScript);
    } else if (
      action === 'create' &&
      (node.type === 'G_MATERIALIZED_VIEW' || node.type === 'MATERIALIZED_VIEW')
    ) {
      const createMviewScript = `CREATE MATERIALIZED VIEW ${objInfo[1]}.newMaterializedView AS\nSELECT \n\t* \nFROM \n;`;
      setScript(createMviewScript);
    } else if (action === 'edit' && (node.type === 'VIEW' || node.type === 'MATERIALIZED_VIEW')) {
      MetaDataService.generateDDL(datasource.id as string, node.identifier, node.type).then(
        (resp) => {
          if (resp.success) {
            setScript(resp.data || '');
          }
        },
      );
    }
    setLoading(false);
    return node.type;
  };

  return (
    <div>
      <div
        style={{
          paddingBottom: 6,
          borderBottom: '1px solid #eee',
        }}
      >
        <Space wrap>
          <Button
            size="small"
            type="primary"
            style={{ height: 22, fontSize: 12, marginLeft: 6 }}
            onClick={() => {
              setLoading(true);
              MetaDataService.executeDDL({
                workspaceId: workspaceId,
                dataSourceId: datasource.id as string,
                script: script,
              }).then((resp) => {
                if (resp.success) {
                  message.success(
                    intl.formatMessage({
                      id: 'dms.common.message.operate.success',
                    }),
                  );
                }
              });
              setLoading(false);
            }}
          >
            {intl.formatMessage({ id: 'dms.common.operate.confirm' })}
          </Button>
          <Button
            size="small"
            style={{ height: 22, fontSize: 12, marginLeft: 6 }}
            onClick={() => {
              generateScript();
              message.success(
                intl.formatMessage({
                  id: 'dms.common.message.operate.success',
                }),
              );
            }}
          >
            {intl.formatMessage({ id: 'dms.common.operate.refresh' })}
          </Button>
        </Space>
      </div>
      <div style={{ maxHeight: maxHeight - 36, overflowY: 'auto' }}>
        <div style={{ padding: '0px 6px', textAlign: 'left' }}>
          <Spin spinning={loading}>
            <Editor
              width={'100%'}
              height={maxHeight - 100}
              value={script}
              language="sql"
              onChange={(value) => {
                setScript(value || '');
              }}
            ></Editor>
          </Spin>
        </div>
      </div>
    </div>
  );
};
export default DbObjectInfoView;
