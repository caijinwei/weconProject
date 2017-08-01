package com.wecon.box.impl;

import com.wecon.box.api.AccountApi;
import com.wecon.box.entity.Account;
import com.wecon.box.entity.Page;
import com.wecon.box.filter.AccountFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zengzhipeng on 2017/8/1.
 */
@Component
public class AccountImpl implements AccountApi {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final String SEL_COL = " ACCOUNT_ID,USERNAME,`PASSWORD`,PHONENUM,EMAIL,CREATE_DATE,`TYPE`,STATE,UPDATE_DATE ";

    @Override
    public long registerAccount(final Account model) {
        String sql = "SELECT COUNT(1) FROM ACCOUNT WHERR USERNAME = ? OR EMAIL = ? OR PHONENUM = ?";

        int ret = jdbcTemplate.queryForObject(sql,
                new Object[]{model.user_name, model.email, model.phone_num},
                Integer.class);
        if (ret > 0) {
            return -1;
        }
        KeyHolder key = new GeneratedKeyHolder();
        jdbcTemplate.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                PreparedStatement preState = con.prepareStatement("INSERT INTO ACCOUNT(USERNAME,`PASSWORD`,PHONENUM,EMAIL,CREATE_DATE,`TYPE`,STATE,UPDATE_DATE) VALUES (?,?,?,?,CURRENT_TIMESTAMP(),?,?,CURRENT_TIMESTAMP() );", Statement.RETURN_GENERATED_KEYS);
                preState.setString(1, model.user_name);
                preState.setString(2, model.password);
                preState.setString(3, model.phone_num);
                preState.setString(4, model.email);
                preState.setInt(5, model.type);
                preState.setInt(6, model.state);

                return preState;
            }
        }, key);
        //从主键持有者中获得主键值
        return key.getKey().longValue();
    }

    @Override
    public boolean updateAccount(Account model) {
        String sql = "UPDATE ACCOUNT SET `PASSWORD`=?,PHONENUM=?,EMAIL=?,STATE=?,UPDATE_DATE=CURRENT_TIMESTAMP()  WHERE ACCOUNT_ID=?";
        jdbcTemplate.update(sql, new Object[]{model.password, model.phone_num, model.email, model.state, model.account_id});
        return true;
    }

    @Override
    public Account getAccount(long account_id) {
        String sql = "SELECT " + SEL_COL + " FROM ACCOUNT WHERE ACCOUNT_ID=?";
        List<Account> list = jdbcTemplate.query(sql,
                new Object[]{account_id},
                new DefaultAccountRowMapper());
        if (!list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    @Override
    public Page<Account> getAccountList(AccountFilter filter, int pageIndex, int pageSize) {
        String sqlCount = "SELECT count(0) FROM ACCOUNT WHERE 1=1";
        String sql = "SELECT " + SEL_COL + " FROM ACCOUNT WHERE 1=1";
        String condition = "";
        List<Object> params = new ArrayList<Object>();
        if (filter.account_id > 0) {
            condition += " and ACCOUNT_ID = ? ";
            params.add(filter.account_id);
        }
        if (filter.user_name != null && !filter.user_name.isEmpty()) {
            condition += " and USERNAME like ? ";
            params.add("%" + filter.user_name + "%");
        }
        if (filter.phone_num != null && !filter.phone_num.isEmpty()) {
            condition += " and PHONENUM like ? ";
            params.add("%" + filter.phone_num + "%");
        }
        if (filter.email != null && !filter.email.isEmpty()) {
            condition += " and EMAIL like ? ";
            params.add("%" + filter.email + "%");
        }
        if (filter.type > -1) {
            condition += " and TYPE = ? ";
            params.add(filter.type);
        }
        if (filter.state > -1) {
            condition += " and STATE = ? ";
            params.add(filter.state);
        }


        sqlCount += condition;
        int totalRecord = jdbcTemplate.queryForObject(sqlCount,
                params.toArray(),
                Integer.class);
        Page<Account> page = new Page<Account>(pageIndex, pageSize, totalRecord);
        String sort = " ORDER BY ACCOUNT_ID DESC";
        sql += condition + sort + " LIMIT " + page.getStartIndex() + "," + page.getPageSize();
        List<Account> list = jdbcTemplate.query(sql,
                params.toArray(),
                new DefaultAccountRowMapper());
        page.setList(list);
        return page;
    }

    public static final class DefaultAccountRowMapper implements RowMapper<Account> {

        @Override
        public Account mapRow(ResultSet rs, int i) throws SQLException {
            Account model = new Account();
            model.account_id = rs.getLong("ACCOUNT_ID");
            model.user_name = rs.getString("USERNAME");
            model.password = rs.getString("PASSWORD");
            model.phone_num = rs.getString("PHONENUM");
            model.email = rs.getString("EMAIL");
            model.type = rs.getInt("TYPE");
            model.state = rs.getInt("STATE");
            model.create_date = rs.getTimestamp("CREATE_DATE");
            model.update_date = rs.getTimestamp("UPDATE_DATE");

            return model;
        }
    }
}
