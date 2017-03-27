package com.alibaba.otter.node.etl.common.db.dialect.rocketmq;

import com.alibaba.otter.node.etl.common.db.dialect.DbDialect;
import com.alibaba.otter.node.etl.common.db.dialect.SqlTemplate;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import org.apache.ddlutils.model.Table;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Created by zman on 16/8/4.
 */
public class RocketMQDialect implements DbDialect{

    private DefaultMQProducer rocketMQProducer;

    public RocketMQDialect(DefaultMQProducer mqProducer){
        this.rocketMQProducer = mqProducer;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getVersion() {
        return null;
    }

    @Override
    public int getMajorVersion() {
        return 0;
    }

    @Override
    public int getMinorVersion() {
        return 0;
    }

    @Override
    public String getDefaultSchema() {
        return null;
    }

    @Override
    public String getDefaultCatalog() {
        return null;
    }

    @Override
    public boolean isCharSpacePadded() {
        return false;
    }

    @Override
    public boolean isCharSpaceTrimmed() {
        return false;
    }

    @Override
    public boolean isEmptyStringNulled() {
        return false;
    }

    @Override
    public boolean isSupportMergeSql() {
        return false;
    }

    @Override
    public LobHandler getLobHandler() {
        return null;
    }

    @Override
    public JdbcTemplate getJdbcTemplate() {
        return null;
    }

    @Override
    public TransactionTemplate getTransactionTemplate() {
        return null;
    }

    @Override
    public SqlTemplate getSqlTemplate() {
        return null;
    }

    @Override
    public Table findTable(String schema, String table) {
        return null;
    }

    @Override
    public Table findTable(String schema, String table, boolean useCache) {
        return null;
    }

    @Override
    public void reloadTable(String schema, String table) {

    }

    @Override
    public void destory() {

    }

    @Override
    public DefaultMQProducer getRocketMQProducer() {
        return rocketMQProducer;
    }
}
