package com.wecon.box.impl;

import com.wecon.box.api.AccountApi;
import com.wecon.box.entity.Account;
import com.wecon.box.entity.Page;
import com.wecon.box.enums.ErrorCodeOption;
import com.wecon.box.filter.AccountFilter;
import com.wecon.restful.core.BusinessException;
import com.wecon.restful.core.SessionManager;
import com.wecon.restful.core.SessionState;
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
import java.util.UUID;

/**
 * Created by zengzhipeng on 2017/8/1.
 */
@Component
public class AccountImpl implements AccountApi {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final String SEL_COL = " account_id,username,`password`,phonenum,email,create_date,`type`,state,update_date ";

    @Override
    public Account signupByEmail(final String username, final String email, final String password) {
        String sql = "select count(1) from account where username = ? or email = ? ";

        int ret = jdbcTemplate.queryForObject(sql,
                new Object[]{username, email},
                Integer.class);
        if (ret > 0) {
            throw new BusinessException(ErrorCodeOption.AccountExisted.key, ErrorCodeOption.AccountExisted.value);
        }

        KeyHolder key = new GeneratedKeyHolder();
        jdbcTemplate.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                PreparedStatement preState = con.prepareStatement("insert into account(username,`password`,email,create_date,`type`,state,update_date) values (?,?,?,current_timestamp(),?,?,current_timestamp() );", Statement.RETURN_GENERATED_KEYS);
                preState.setString(1, username);
                preState.setString(2, password);
                preState.setString(3, email);
                preState.setInt(4, 1);//注册帐号为管理帐号
                preState.setInt(5, 1);//默认为启用

                return preState;
            }
        }, key);
        //从主键持有者中获得主键值
        long account_id = key.getKey().longValue();
        return getAccount(account_id);
    }

    @Override
    public String createSession(Account user, int productId, String fuid, long loginIp, long loginTime, int seconds) {
        SessionState.UserInfo.Builder builder = SessionState.UserInfo.newBuilder();
        builder.setCuid(fuid);
        builder.setProductId(productId);
        builder.setLoginIp(loginIp);
        builder.setLoginTime(loginTime);

        builder.setUserID(user.account_id);
        builder.setAccount(user.user_name);
        SessionState.UserInfo builderUrser = builder.build();

        String sid = UUID.randomUUID().toString().replace("-", "");
        SessionManager.persistSession(sid, builderUrser, seconds);//3600 * 24 * 30
        return sid;
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
