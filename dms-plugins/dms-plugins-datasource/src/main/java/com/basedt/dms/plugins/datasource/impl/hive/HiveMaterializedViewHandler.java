package com.basedt.dms.plugins.datasource.impl.hive;

import cn.hutool.core.util.StrUtil;
import com.basedt.dms.common.utils.DateTimeUtil;
import com.basedt.dms.plugins.datasource.dto.MaterializedViewDTO;
import com.basedt.dms.plugins.datasource.dto.TableDTO;
import com.basedt.dms.plugins.datasource.impl.jdbc.JdbcDataTypeMapper;
import com.basedt.dms.plugins.datasource.impl.jdbc.JdbcIndexHandler;
import com.basedt.dms.plugins.datasource.impl.jdbc.JdbcMaterializedViewHandler;
import com.basedt.dms.plugins.datasource.impl.jdbc.JdbcTableHandler;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.metastore.HiveMetaStoreClient;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.thrift.TException;

import java.sql.SQLException;
import java.util.*;

import static com.basedt.dms.plugins.datasource.enums.DbObjectType.MATERIALIZED_VIEW;
import static com.basedt.dms.plugins.datasource.impl.hive.HivePluginImpl.METASTORE_URIS;

public class HiveMaterializedViewHandler extends JdbcMaterializedViewHandler {

    @Override
    public List<MaterializedViewDTO> listMViews(String catalog, String schemaPattern, String mViewPattern) throws SQLException {
        List<MaterializedViewDTO> result = new ArrayList<>();
        JdbcTableHandler tableHandler = new JdbcTableHandler();
        JdbcIndexHandler indexHandler = new JdbcIndexHandler();
        indexHandler.initialize(this.dataSource, new HashMap<>());
        tableHandler.initialize(this.dataSource, new HashMap<>(),new JdbcDataTypeMapper(),indexHandler);
        List<TableDTO> tables = tableHandler.listTables(catalog, schemaPattern, mViewPattern, MATERIALIZED_VIEW);
        for (TableDTO table : tables) {
            MaterializedViewDTO mvDTO = new MaterializedViewDTO();
            mvDTO.setCatalogName(catalog);
            mvDTO.setSchemaName(table.getSchemaName());
            mvDTO.setObjectName(table.getObjectName());
            mvDTO.setObjectType(MATERIALIZED_VIEW.name());
            result.add(mvDTO);
        }
        return result;
    }

    @Override
    public List<MaterializedViewDTO> listMViewDetails(String catalog, String schemaPattern, String mViewPattern) throws SQLException {
        try {
            List<MaterializedViewDTO> result = new ArrayList<>();
            String uris = this.config.get(METASTORE_URIS);
            Configuration conf = new Configuration();
            conf.set("hive.metastore.uris", uris);
            HiveMetaStoreClient client = new HiveMetaStoreClient(conf);
            List<String> tables = client.getAllTables(null, schemaPattern);
            List<Table> tableList = client.getTableObjectsByName(null, schemaPattern, tables);
            for (Table table : tableList) {
                if ("MATERIALIZED_VIEW".equals(table.getTableType())) {
                    MaterializedViewDTO mvDTO = new MaterializedViewDTO();
                    mvDTO.setCatalogName(catalog);
                    mvDTO.setSchemaName(table.getDbName());
                    mvDTO.setObjectName(table.getTableName());
                    mvDTO.setObjectType(MATERIALIZED_VIEW.name());
                    mvDTO.setCreateTime(DateTimeUtil.toLocalDateTime(table.getCreateTime()));
                    mvDTO.setQuerySql(table.getViewOriginalText());
                    Map<String, String> params = table.getParameters();
                    mvDTO.setRemark(params.get("comment"));
                    mvDTO.setDataBytes(Long.parseLong(Objects.isNull(params.get("totalSize")) ? "0" : params.get("totalSize")));
                    String lastDdlTime = params.get("transient_lastDdlTime");
                    mvDTO.setLastDdlTime(DateTimeUtil.toLocalDateTime(Integer.parseInt(Objects.isNull(lastDdlTime) ? "0" : lastDdlTime)));
                    if (Objects.isNull(mViewPattern) || StrUtil.contains(mvDTO.getMViewName(), mViewPattern)) {
                        result.add(mvDTO);
                    }
                }
            }
            client.close();
            return result;
        } catch (TException e) {
            throw new SQLException(e.getMessage());
        }
    }
}
