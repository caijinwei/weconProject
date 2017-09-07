package com.wecon.box.impl;

import com.wecon.box.api.LogAccountApi;
import com.wecon.box.entity.LogAccount;
import com.wecon.box.entity.LogAccountFilter;
import com.wecon.box.entity.Page;
import com.wecon.box.enums.OpTypeOption;
import com.wecon.box.enums.ResTypeOption;
import com.wecon.common.util.EnumUtil;
import com.wecon.common.web.IpAddrHelper;
import com.wecon.restful.core.AppContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
        final String sql = "insert into log_account(`account_id`, `client_platform`, `client_ip`, `op_type`, `op_date`, `op_time`, `message`, `url`, `res_id`, `res_type`,username) values(?,?,?,?,?,CURRENT_TIMESTAMP(),?,?,?,?,?)";
        //创建一个主键持有者
        KeyHolder key = new GeneratedKeyHolder();

        this.jdbcTemplate.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                PreparedStatement preState = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                preState.setLong(1, log.account_id);
                preState.setInt(2, log.client_platform);
                preState.setString(3, log.client_ip);
                preState.setInt(4, log.op_type.getValue());
                preState.setLong(5, log.op_date);
                preState.setString(6, log.message);
                preState.setString(7, log.url);
                preState.setLong(8, log.res_id);
                preState.setInt(9, log.res_type.getValue());
                preState.setString(10, log.username);
                return preState;
            }
        }, key);
        //从主键持有者中获得主键值
        return key.getKey().longValue();
    }

    @Override
    public Page<LogAccount> getLogList(LogAccountFilter filter, int pageIndex, int pageSize) {
        String sqlCount = "select count(0) from log_account where 1=1";
        String sql = "select * from log_account where 1=1";
        String condition = "";
        List<Object> params = new ArrayList<Object>();
        if (filter.username != null && !filter.username.isEmpty()) {
            condition += " and username=? ";
            params.add(filter.username);
        }
        if (filter.op_date_begin > 0) {
            condition += " and op_date>=?";
            params.add(filter.op_date_begin);
        }
        if (filter.op_date_end > 0) {
            condition += " and op_date<=? ";
            params.add(filter.op_date_end);
        }
        if (filter.op_type != null && filter.op_type.getValue() > -1) {
            condition += " and op_type=? ";
            params.add(filter.op_type.getValue());
        }
        if (filter.res_id != null && filter.res_id > -1) {
            condition += " and res_id=? ";
            params.add(filter.res_id);
        }
        if (filter.res_type != null && filter.res_type.getValue() > -1) {
            condition += " and res_type=? ";
            params.add(filter.res_type.getValue());
        }
        if (filter.account_id != null && filter.account_id > -1) {
            condition += " and account_id=? ";
            params.add(filter.account_id);
        }
        if (filter.id != null && filter.id > -1) {
            condition += " and id=? ";
            params.add(filter.id);
        }
        if (filter.client_platform != null && filter.client_platform > -1) {
            condition += " and client_platform=? ";
            params.add(filter.client_platform);
        }

        sqlCount += condition;
        int totalRecord = jdbcTemplate.queryForObject(sqlCount,
                params.toArray(),
                Integer.class);
        Page<LogAccount> page = new Page<LogAccount>(pageIndex, pageSize, totalRecord);
        String sort = " order by id desc";
        sql += condition + sort + " limit " + page.getStartIndex() + "," + page.getPageSize();
        List<LogAccount> list = jdbcTemplate.query(sql,
                params.toArray(),
                new DefaultOpLogRowMapper());
        page.setList(list);
        return page;
    }

    @Override
    public LogAccount getLogInit() {
        LogAccount log = new LogAccount();
        //<editor-fold desc="通用数据赋值">
        Date now = new Date();
        log.account_id = AppContext.getSession().client.userId;
        log.username = AppContext.getSession().client.account;
        log.url = AppContext.getSession().request.getRequestURL().toString();
        log.client_platform = AppContext.getSession().client.platform.value;
        log.client_ip = IpAddrHelper.convertLongToIP(AppContext.getSession().client.ip);
        log.op_date = Integer.valueOf(dateFormat.format(now));
        //</editor-fold>
        return log;
    }

    public static final class DefaultOpLogRowMapper implements RowMapper<LogAccount> {

        @Override
        public LogAccount mapRow(ResultSet rs, int i) throws SQLException {
            LogAccount log = new LogAccount();
            log.account_id = rs.getLong("account_id");
            log.id = rs.getLong("id");
            log.username = rs.getString("username");
            log.message = rs.getString("message");
            log.op_date = rs.getInt("op_date");
            log.op_time = rs.getTimestamp("op_time");
            log.op_type = EnumUtil.toEnum(OpTypeOption.class, rs.getInt("op_type"));
            log.res_id = rs.getLong("res_id");
            log.res_type = EnumUtil.toEnum(ResTypeOption.class, rs.getInt("res_type"));
            log.client_ip = rs.getString("client_ip");
            log.client_platform = rs.getInt("client_platform");
            log.url = rs.getString("url");
            log.res_type_name = log.res_type.getKey();
            log.op_type_name = log.op_type.getKey();

            return log;
        }
    }
}
