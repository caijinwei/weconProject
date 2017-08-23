package com.wecon.box.impl;

import com.wecon.box.api.LogAccountApi;
import com.wecon.box.entity.LogAccount;
import com.wecon.common.web.IpAddrHelper;
import com.wecon.restful.core.AppContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by zengzhipeng on 2017/8/1.
 */
@Component
public class LogAccountImpl implements LogAccountApi {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());

    @Override
    public long addLog(final LogAccount log) {
        final String sql = "insert into log_account(`account_id`, `client_platform`, `client_ip`, `op_type`, `op_date`, `op_time`, `message`, `url`, `res_id`, `res_type`) values(?,?,?,?,?,CURRENT_TIMESTAMP(),?,?,?,?)";
        //创建一个主键持有者
        KeyHolder key = new GeneratedKeyHolder();

        this.jdbcTemplate.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                PreparedStatement preState = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                preState.setLong(1, log.account_id);
                preState.setInt(2, log.client_platform);
                preState.setString(3, log.client_ip);
                preState.setInt(4, log.op_type);
                preState.setLong(5, log.op_date);
                preState.setString(6, log.message);
                preState.setString(7, log.url);
                preState.setLong(8, log.res_id);
                preState.setInt(9, log.res_type);
                return preState;
            }
        }, key);
        //从主键持有者中获得主键值
        return key.getKey().longValue();
    }

    @Override
    public LogAccount getLogInit() {
        LogAccount log = new LogAccount();
        //<editor-fold desc="通用数据赋值">
        Date now = new Date();
        log.account_id = AppContext.getSession().client.userId;
        log.url = AppContext.getSession().request.getRequestURL().toString();
        log.client_platform = AppContext.getSession().client.platform.value;
        log.client_ip = IpAddrHelper.convertLongToIP(AppContext.getSession().client.ip);
        log.op_date = Integer.valueOf(dateFormat.format(now));
        //</editor-fold>
        return log;
    }
}
