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

    private final String SEL_COL = " account_id,username,`password`,phonenum,email,create_date,`type`,state,update_date ";

    @Override
    public long registerAccount(final Account model) {
        String sql = "select count(1) from account wherr username = ? or email = ? or phonenum = ?";

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
                PreparedStatement preState = con.prepareStatement("insert into account(username,`password`,phonenum,email,create_date,`type`,state,update_date) values (?,?,?,?,current_timestamp(),?,?,current_timestamp() );", Statement.RETURN_GENERATED_KEYS);
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
        String sql = "update account set `password`=?,phonenum=?,email=?,state=?,update_date=current_timestamp()  where account_id=?";
        jdbcTemplate.update(sql, new Object[]{model.password, model.phone_num, model.email, model.state, model.account_id});
        return true;
    }

    @Override
    public Account getAccount(long account_id) {
        String sql = "select " + SEL_COL + " from account where account_id=?";
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
        String sqlCount = "select count(0) from account where 1=1";
        String sql = "select " + SEL_COL + " from account where 1=1";
        String condition = "";
        List<Object> params = new ArrayList<Object>();
        if (filter.account_id > 0) {
            condition += " and account_id = ? ";
            params.add(filter.account_id);
        }
        if (filter.user_name != null && !filter.user_name.isEmpty()) {
            condition += " and username like ? ";
            params.add("%" + filter.user_name + "%");
        }
        if (filter.phone_num != null && !filter.phone_num.isEmpty()) {
            condition += " and phonenum like ? ";
            params.add("%" + filter.phone_num + "%");
        }
        if (filter.email != null && !filter.email.isEmpty()) {
            condition += " and email like ? ";
            params.add("%" + filter.email + "%");
        }
        if (filter.type > -1) {
            condition += " and type = ? ";
            params.add(filter.type);
        }
        if (filter.state > -1) {
            condition += " and state = ? ";
            params.add(filter.state);
        }


        sqlCount += condition;
        int totalRecord = jdbcTemplate.queryForObject(sqlCount,
                params.toArray(),
                Integer.class);
        Page<Account> page = new Page<Account>(pageIndex, pageSize, totalRecord);
        String sort = " order by account_id desc";
        sql += condition + sort + " limit " + page.getStartIndex() + "," + page.getPageSize();
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
            model.account_id = rs.getLong("account_id");
            model.user_name = rs.getString("username");
            model.password = rs.getString("password");
            model.phone_num = rs.getString("phonenum");
            model.email = rs.getString("email");
            model.type = rs.getInt("type");
            model.state = rs.getInt("state");
            model.create_date = rs.getTimestamp("create_date");
            model.update_date = rs.getTimestamp("update_date");

            return model;
        }
    }
}
