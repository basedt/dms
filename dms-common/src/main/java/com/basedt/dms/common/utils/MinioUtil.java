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
package com.basedt.dms.common.utils;

import cn.hutool.core.util.StrUtil;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class MinioUtil {

    @Value("${minio.bucketName}")
    private String bucket;

    @Value("${minio.expiry}")
    private Integer expiry;

    final private MinioClient minioClient;

    public MinioUtil(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @PostConstruct
    private void init() {
        if (!bucketExists(this.bucket)) {
            bucketCreate(this.bucket, this.expiry);
        }
    }

    @SneakyThrows
    public boolean bucketExists(String bucketName) {
        return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
    }

    @SneakyThrows
    public void bucketCreate(String bucketName) {
        minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
    }

    @SneakyThrows
    public void bucketCreate(String bucketName, int expiry) {
        bucketCreate(bucketName);
        List<LifecycleRule> lifecycleRules = new ArrayList<>();
        lifecycleRules.add(new LifecycleRule(Status.ENABLED,
                        null,
                        new Expiration((ResponseDate) null, expiry, null),
                        new RuleFilter("/"),
                        bucketName + "DeleteRule",
                        null,
                        null,
                        null
                )
        );
        minioClient.setBucketLifecycle(SetBucketLifecycleArgs
                .builder()
                .config(new LifecycleConfiguration(lifecycleRules))
                .bucket(bucketName)
                .build()
        );
    }

    @SneakyThrows
    public void bucketDelete(String bucketName) {
        minioClient.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
    }

    @SneakyThrows
    public List<Bucket> listBuckets() {
        return minioClient.listBuckets();
    }

    public boolean isObjectExist(String bucketName, String objectName) {
        try {
            minioClient.statObject(StatObjectArgs.builder().bucket(bucketName).object(objectName).build());
            return true;
        } catch (Exception e) {
            log.debug("{} not exists in bucket {},exception info is {}", objectName, bucketName, e.getMessage());
            return false;
        }
    }

    public boolean isFolderExist(String bucketName, String folderName) {
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket(bucketName)
                    .prefix(folderName)
                    .recursive(false)
                    .build()
            );
            for (Result<Item> result : results) {
                Item item = result.get();
                if (item.isDir() && folderName.equals(item.objectName())) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @SneakyThrows
    public InputStream downloadObject(String bucketName, String objectName) {
        return minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .build());
    }

    @SneakyThrows
    public InputStream downloadObject(String bucketName, String objectName, long offset, long length) {
        return minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .offset(offset)
                .length(length)
                .build());
    }

    @SneakyThrows
    public ObjectWriteResponse uploadObject(String bucketName, String objectName, MultipartFile file) {
        InputStream inputStream = file.getInputStream();
        return minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .stream(inputStream, file.getSize(), -1)
                .contentType(file.getContentType())
                .build()
        );
    }

    @SneakyThrows
    public ObjectWriteResponse uploadObject(String bucketName, String objectName, String filePath) {
        return minioClient.uploadObject(UploadObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .filename(filePath)
                .build());
    }

    @SneakyThrows
    public ObjectWriteResponse uploadObject(String bucketName, String objectName, InputStream inputStream) {
        return minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .stream(inputStream, inputStream.available(), -1)
                .build());
    }

    @SneakyThrows
    public void deleteObject(String bucketName, String objectName) {
        minioClient.removeObject(RemoveObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .build());
    }

    @SneakyThrows
    public ObjectWriteResponse createFolder(String bucketName, String folderName) {
        return minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucketName)
                .object(folderName)
                .stream(new ByteArrayInputStream(new byte[]{}), 0, -1)
                .build());
    }

    @SneakyThrows
    public String getPresignedUrl(String bucketName, String objectName, int expiry) {
        return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .method(Method.GET)
                .expiry(expiry)
                .build());
    }

    public String getObjectURI(String bucketName, String objectName) {
        return StrUtil.concat(true, "minio://", bucketName, "/", objectName);
    }

    public String getBucketName(String objectURI) {
        if (StrUtil.isBlank(objectURI)) {
            return null;
        } else if (StrUtil.startWith(objectURI, "minio://", true)) {
            String tmpStr = StrUtil.replace(objectURI, "minio://", "");
            return StrUtil.subBefore(tmpStr, "/", false);
        } else {
            return null;
        }
    }

    public String getObjectName(String objectURI) {
        if (StrUtil.isBlank(objectURI)) {
            return null;
        } else if (StrUtil.startWith(objectURI, "minio://", true)) {
            String bucketName = getBucketName(objectURI);
            return StrUtil.replace(objectURI, "minio://" + bucketName + "/", "");
        } else {
            return null;
        }
    }

    public String getFileName(String objectURI) {
        if (StrUtil.isBlank(objectURI)) {
            return null;
        } else {
            return StrUtil.subAfter(objectURI, "/", true);
        }
    }
}
