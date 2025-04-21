import { useEmotionCss } from '@ant-design/use-emotion-css';
import { useIntl } from '@umijs/max';
import { Card, Col, Image, Modal, Row, Typography } from 'antd';

type DataSourceTypeSelectProps = {
  open?: boolean;
  data?: DMS.DataSource;
  handleOk?: (isOpen: boolean, ds: DMS.DataSource) => void;
  handleCancel?: () => void;
};

const DataSourceTypeSelect: React.FC<DataSourceTypeSelectProps> = (props) => {
  const intl = useIntl();
  const { open, data, handleOk, handleCancel } = props;

  const groupContainerStyle = useEmotionCss(() => {
    return {
      marginLeft: 12,
      marginRight: 12,
    };
  });

  const dbCardStyle = useEmotionCss(() => {
    return {
      width: '100%',
      height: '100px',
    };
  });

  const groupLableStyle = useEmotionCss(() => {
    return {
      marginTop: 12,
    };
  });

  const getDefaultPort = (dbType: string) => {
    switch (dbType) {
      case 'mysql':
      case 'mariadb':
        return 3306;
      case 'oracle':
        return 1521;
      case 'postgreSQL':
      case 'greenplum':
      case 'gaussdb':
        return 5432;
      case 'mssql':
        return 1433;
      case 'doris':
        return 9030;
      case 'hologres':
        return 80;

      case 'clickhouse':
        return 8123;
      case 'redis':
        return 6379;
      case 'kafka':
        return 9092;
      case 'hive':
        return 10000;
      case 'hdfs':
        return 9000;
      default:
        return null;
    }
  };

  const dbCard = (dbType: DMS.Dict, imageSrc: string) => {
    return (
      <Card
        className={dbCardStyle}
        hoverable
        styles={{ body: { paddingTop: 8 } }}
        cover={
          <Image src={imageSrc} preview={false} height={60} style={{ paddingTop: 12 }}></Image>
        }
        onClick={() => {
          if (
            dbType.value == 'hive' ||
            dbType.value == 'hdfs' ||
            dbType.value == 'redis' ||
            dbType.value == 'kafka'
          ) {
            // message.info("not suppor");
          } else {
            handleOk
              ? handleOk(false, {
                  workspaceId: data?.workspaceId as string,
                  datasourceType: dbType,
                  port: getDefaultPort(dbType.value as string) as number,
                })
              : null;
          }
        }}
      >
        <Card.Meta
          description={<Typography.Text>{dbType.label}</Typography.Text>}
          style={{ textAlign: 'center' }}
        ></Card.Meta>
      </Card>
    );
  };

  return (
    <Modal
      title={intl.formatMessage({ id: 'dms.console.workspace.datasource.new' }, { type: '' })}
      open={open}
      onOk={() => {}}
      destroyOnClose={true}
      onCancel={handleCancel}
      styles={{
        body: {
          overflowY: 'scroll',
          maxHeight: '640px',
          paddingBottom: 12,
          height: 560,
        },
      }}
      width="780px"
      footer={false}
    >
      <Typography.Title level={5} className={groupLableStyle}>
        {intl.formatMessage({
          id: 'dms.console.workspace.datasource.type.rdbms',
        })}
      </Typography.Title>
      <div className={groupContainerStyle}>
        <Row gutter={[12, 12]}>
          <Col span={6}>
            {dbCard({ value: 'mysql', label: 'Mysql' }, '/images/databases/mysql.svg')}
          </Col>
          <Col span={6}>
            {dbCard(
              { value: 'postgreSQL', label: 'PostgreSQL' },
              '/images/databases/postgresql.svg',
            )}
          </Col>
          <Col span={6}>
            {dbCard({ value: 'oracle', label: 'Oracle' }, '/images/databases/oracle.svg')}
          </Col>
          <Col span={6}>
            {dbCard({ value: 'mssql', label: 'SQL Server' }, '/images/databases/mssql.svg')}
          </Col>
          <Col span={6}>
            {dbCard({ value: 'mariadb', label: 'MariaDB' }, '/images/databases/mariadb.svg')}
          </Col>
          <Col span={6}>
            {dbCard({ value: 'polardb', label: 'PolarDB' }, '/images/databases/polardb.svg')}
          </Col>
        </Row>
      </div>
      <Typography.Title level={5} className={groupLableStyle}>
        {intl.formatMessage({
          id: 'dms.console.workspace.datasource.type.mpp',
        })}
      </Typography.Title>
      <div className={groupContainerStyle}>
        <Row gutter={[12, 12]}>
          <Col span={6}>
            {dbCard({ value: 'doris', label: 'Doris' }, '/images/databases/doris.svg')}
          </Col>
          <Col span={6}>
            {dbCard({ value: 'hologres', label: 'Hologres' }, '/images/databases/hologres.svg')}
          </Col>
          <Col span={6}>
            {dbCard({ value: 'gaussdb', label: 'GaussDB' }, '/images/databases/gaussdb.svg')}
          </Col>
          <Col span={6}>
            {dbCard(
              { value: 'clickhouse', label: 'ClickHouse' },
              '/images/databases/clickhouse.svg',
            )}
          </Col>
          <Col span={6}>
            {dbCard({ value: 'greenplum', label: 'Greenplum' }, '/images/databases/greenplum.svg')}
          </Col>

          {/* <Col span={6}>
            {dbCard(
              { value: "hive", label: "Hive" },
              "/images/databases/apachehive.svg"
            )}
          </Col>
          <Col span={6}>
            {dbCard(
              { value: "hdfs", label: "HDFS" },
              "/images/databases/apachehadoop.svg"
            )}
          </Col> */}
        </Row>
      </div>
      {/* <Typography.Title level={5} className={groupLableStyle}>
        {intl.formatMessage({
          id: "dms.console.workspace.datasource.type.nosql",
        })}
      </Typography.Title>
      <div className={groupContainerStyle}>
        <Row gutter={[12, 12]}>
          <Col span={6}>
            {dbCard(
              { value: "redis", label: "Redis" },
              "/images/databases/redis.svg"
            )}
          </Col>
        </Row>
      </div>
      <Typography.Title level={5} className={groupLableStyle}>
        {intl.formatMessage({
          id: "dms.console.workspace.datasource.type.mq",
        })}
      </Typography.Title>
      <div className={groupContainerStyle}>
        <Row gutter={[12, 12]}>
          <Col span={6}>
            {dbCard(
              { value: "kafka", label: "Kafka" },
              "/images/databases/apachekafka.svg"
            )}
          </Col>
        </Row>
      </div> */}
    </Modal>
  );
};

export default DataSourceTypeSelect;
