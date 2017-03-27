/*
 * Copyright (C) 2010-2101 Alibaba Group Holding Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.otter.node.etl.common.rocketmq;

import com.alibaba.otter.node.etl.common.datasource.DataSourceService;
import com.alibaba.otter.node.etl.common.db.dialect.DbDialect;
import com.alibaba.otter.node.etl.common.db.dialect.DbDialectGenerator;
import com.alibaba.otter.node.etl.common.db.dialect.rocketmq.RocketMQDialect;
import com.alibaba.otter.shared.common.model.config.data.db.DbMediaSource;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.google.common.base.Function;
import com.google.common.collect.GenericMapMaker;
import com.google.common.collect.MapEvictionListener;
import com.google.common.collect.MapMaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * @author jianghang 2011-10-27 下午02:12:06
 * @version 4.0.0
 */
public class RocketMQDialectFactory implements DisposableBean {

    private static final Logger                      logger = LoggerFactory.getLogger(RocketMQDialectFactory.class);
    private DataSourceService                        dataSourceService;
    private DbDialectGenerator                       dbDialectGenerator;

    // 第一层pipelineId , 第二层DbMediaSource id
    private Map<Long, Map<DbMediaSource, DbDialect>> dialects;

    // message max size 10M
    private static final int MESSAGE_MAX_SIZE = 10*1024*1024;

    public RocketMQDialectFactory(){
        // 构建第一层map
        GenericMapMaker mapMaker = null;
        mapMaker = new MapMaker().softValues().evictionListener(new MapEvictionListener<Long, Map<DbMediaSource, DbDialect>>() {

                                                                    public void onEviction(Long pipelineId,
                                                                                           Map<DbMediaSource, DbDialect> dialect) {
                                                                        if (dialect == null) {
                                                                            return;
                                                                        }

                                                                        for (DbDialect dbDialect : dialect.values()) {
                                                                            dbDialect.destory();
                                                                        }
                                                                    }
                                                                });

        dialects = mapMaker.makeComputingMap(new Function<Long, Map<DbMediaSource, DbDialect>>() {

            public Map<DbMediaSource, DbDialect> apply(final Long pipelineId) {
                // 构建第二层map
                return new MapMaker().makeComputingMap(new Function<DbMediaSource, DbDialect>() {

                    public DbDialect apply(final DbMediaSource source) {

                        String mqUrl = source.getUrl();
                        logger.info("rocketmq nameserveraddr:{}", mqUrl);

                        DefaultMQProducer rocketmqProducer = new DefaultMQProducer(source.getName()+ "_"+UUID.randomUUID().toString());
                        rocketmqProducer.setNamesrvAddr(mqUrl);
                        rocketmqProducer.setMaxMessageSize(MESSAGE_MAX_SIZE);

                        try {
                            rocketmqProducer.start();
                        } catch (MQClientException e) {
                            logger.error("RocketMQ producer启动失败,host:"+mqUrl,e);
                            throw new UnsupportedOperationException("RocketMQ producer启动失败,host:"+mqUrl,e);
                        }
                        RocketMQDialect rocketMQDialect = new RocketMQDialect(rocketmqProducer);
                        return rocketMQDialect;
                    }
                });
            }
        });

    }

    public DbDialect getDbDialect(Long pipelineId, DbMediaSource source) {
        return dialects.get(pipelineId).get(source);
    }

    public void destory(Long pipelineId) {
        Map<DbMediaSource, DbDialect> dialect = dialects.remove(pipelineId);
        if (dialect != null) {
            for (DbDialect dbDialect : dialect.values()) {
                dbDialect.destory();
            }
        }
    }

    public void destroy() throws Exception {
        Set<Long> pipelineIds = new HashSet<Long>(dialects.keySet());
        for (Long pipelineId : pipelineIds) {
            destory(pipelineId);
        }
    }

    // =============== setter / getter =================

    public void setDataSourceService(DataSourceService dataSourceService) {
        this.dataSourceService = dataSourceService;
    }

    public void setDbDialectGenerator(DbDialectGenerator dbDialectGenerator) {
        this.dbDialectGenerator = dbDialectGenerator;
    }

}
