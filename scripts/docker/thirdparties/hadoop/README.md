# hadoop

启动：

```shell
docker compose up -d
```

启动后，可在浏览器访问如下地址：

* namenode。http://localhost:9870
* resource manager。http://localhost:8088

hadoop 运行在容器中，如果在容器外通过 spark 等连接 hadoop，向 hdfs 写入文件时，会遇到写入失败问题。因为 namenode 创建完路径后，客户端需向 datanode 写入数据，datanode 的地址为 namenode 返回的容器内地址，在容器外无法访问。在网上的使用 docker compose 创建 hadoop 环境的文档中，多是在创建完成后登录 namenode、datanode 等容器，在容器内进行操作。如果要在容器外正常使用，需编辑本地 hosts 文件，添加如下配置：

```
127.0.0.1 namenode
127.0.0.1 datanode
127.0.0.1 resourcemanager
127.0.0.1 nodemanager
127.0.0.1 historyserver

也可以使用如下配置
127.0.0.1	localhost namenode datanode resourcemanager nodemanager historyserver
::1       localhost namenode datanode resourcemanager nodemanager historyserver
```

注意：这里的 namenode、datanode 和 `docker-compose.yml` 文件中的 namenode、datanode 都是对应的，如果修改 `docker-compose.yml`，比如新增 datanode2，也需新增 `127.0.0.1 datanode2`。

除此之外，在向 yarn 提交任务时也会遇到相关问题。比如提交 flink 任务，yarn 会向启动的容器（yarn 的容器，不是 docker 的容器）随机分配一个端口号。但是 nodemanager 运行在 docker 容器内，nodemanager docker 容器暴露的端口号在启动时就已经决定，而运行在 nodemanager 上的 yarn 容器随机分配的端口号，无法在 docker 容器外访问。

连接 hadoop 时，需自己创建 `core-site.xml` 和 `hdfs-site.xml` 文件，内容分别如下：

core-site.xml：

```xml
<configuration>
    <property>
        <name>fs.defaultFS</name>
        <value>hdfs://localhost:9000</value>
    </property>
</configuration>
```

hdfs-site.xml：

```xml
<configuration>
    <property>
        <name>dfs.replication</name>
        <value>1</value>
    </property>
</configuration>
```

