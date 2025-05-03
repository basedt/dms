/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.basedt.dms.service.hms;

import com.basedt.dms.DmsApplication;
import com.basedt.dms.plugins.datasource.impl.hive.HivePluginImpl;
import com.basedt.dms.service.workspace.DmsDataSourceService;
import com.basedt.dms.service.workspace.dto.DmsDataSourceDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.metastore.HiveMetaStoreClient;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.thrift.TException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@Slf4j
@SpringBootTest(classes = DmsApplication.class)
public class HmsTest {

    @Autowired
    private DmsDataSourceService dataSourceService;

    @Test
    public void simpleChatTest() throws TException {
        DmsDataSourceDTO datasource = this.dataSourceService.selectOne(9L);
        String hmsUris = datasource.getAttrs().get(HivePluginImpl.METASTORE_URIS).toString();
        log.info("hello {}", hmsUris);
        Configuration conf = new Configuration();
        conf.set("hive.metastore.uris", hmsUris);
        HiveMetaStoreClient client = new HiveMetaStoreClient(conf);
        List<String> catalogs = client.getCatalogs();
        log.info("catalog list {}", catalogs);
        List<String> databases = client.getAllDatabases(null);
        log.info("database list {}", databases);
        List<String> tableList = client.getAllTables(null, "default");
        List<Table> tables = client.getTableObjectsByName(null, "default", tableList);
        for (Table table : tables) {
            log.info("table {}", table);
        }
    }

}
