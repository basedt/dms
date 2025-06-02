package com.basedt.dms.plugins.datasource.impl.hive;

import cn.hutool.core.util.StrUtil;
import com.basedt.dms.common.utils.DateTimeUtil;
import com.basedt.dms.plugins.datasource.dto.TableDTO;
import com.basedt.dms.plugins.datasource.enums.DbObjectType;
import com.basedt.dms.plugins.datasource.impl.jdbc.JdbcTableHandler;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.metastore.HiveMetaStoreClient;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.thrift.TException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.basedt.dms.plugins.datasource.enums.DbObjectType.TABLE;
import static com.basedt.dms.plugins.datasource.impl.hive.HivePluginImpl.METASTORE_URIS;

public class HiveTableHandler extends JdbcTableHandler {

    @Override
    public List<TableDTO> listTableDetails(String catalog, String schemaPattern, String tablePattern, DbObjectType type) throws SQLException {
        try {
            List<TableDTO> result = new ArrayList<>();
            String uris = this.config.get(METASTORE_URIS);
            Configuration conf = new Configuration();
            conf.set("hive.metastore.uris", uris);
            HiveMetaStoreClient client = new HiveMetaStoreClient(conf);
            List<String> tables = client.getAllTables(null, schemaPattern);
            List<Table> tableList = client.getTableObjectsByName(null, schemaPattern, tables);
            for (Table table : tableList) {
                if ("MANAGED_TABLE".equals(table.getTableType()) || "EXTERNAL_TABLE".equals(table.getTableType())) {
                    TableDTO tableDTO = new TableDTO();
                    tableDTO.setCatalogName(catalog);
                    tableDTO.setSchemaName(table.getDbName());
                    tableDTO.setObjectName(table.getTableName());
                    tableDTO.setObjectType(TABLE.name());
                    tableDTO.setCreateTime(DateTimeUtil.toLocalDateTime(table.getCreateTime()));
                    tableDTO.setLastAccessTime(DateTimeUtil.toLocalDateTime(table.getLastAccessTime()));
                    Map<String, String> params = table.getParameters();
                    tableDTO.setRemark(params.get("comment"));
                    tableDTO.setTableRows(Long.parseLong(Objects.isNull(params.get("numRows")) ? "0" : params.get("numRows")));
                    tableDTO.setDataBytes(Long.parseLong(Objects.isNull(params.get("totalSize")) ? "0" : params.get("totalSize")));
                    String lastDdlTime = params.get("transient_lastDdlTime");
                    tableDTO.setLastDdlTime(DateTimeUtil.toLocalDateTime(Integer.parseInt(Objects.isNull(lastDdlTime) ? "0" : lastDdlTime)));
                    if (Objects.isNull(tablePattern) || StrUtil.contains(tableDTO.getTableName(), tablePattern)) {
                        result.add(tableDTO);
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
