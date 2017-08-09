package com.wecon.box.impl;

import com.wecon.box.api.AccountApi;
import com.wecon.box.entity.Account;
import com.wecon.box.entity.Page;
import com.wecon.box.enums.ErrorCodeOption;
import com.wecon.box.filter.AccountFilter;
import com.wecon.restful.core.BusinessException;
import com.wecon.restful.core.SessionManager;
import com.wecon.restful.core.SessionState;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by zengzhipeng on 2017/8/1.
 */
@Component
public class AccountImpl implements AccountApi {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public PlatformTransactionManager transactionManager;

    private final String SEL_COL = " account_id,username,`password`,phonenum,email,create_date,`type`,state,update_date,secret_key ";

    @Override
    public Account signupByEmail(final String username, final String email, final String password) {
        String sql = "select count(1) from account where username = ? or username = ? or email = ?  or email = ?  ";

        int ret = jdbcTemplate.queryForObject(sql,
                new Object[]{username, email, username, email},
                Integer.class);
        if (ret > 0) {
            throw new BusinessException(ErrorCodeOption.AccountExisted.key, ErrorCodeOption.AccountExisted.value);
        }

        KeyHolder key = new GeneratedKeyHolder();
        jdbcTemplate.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                PreparedStatement preState = con.prepareStatement("insert into account(username,`password`,email,secret_key,create_date,`type`,state,update_date) values (?,?,?,?,current_timestamp(),?,?,current_timestamp() );", Statement.RETURN_GENERATED_KEYS);
                String secret_key = DigestUtils.md5Hex(UUID.randomUUID().toString());
                preState.setString(1, username);
                preState.setString(2, DigestUtils.md5Hex(password + secret_key));
                preState.setString(3, email);
                preState.setString(4, secret_key);
                preState.setInt(5, 1);//注册帐号为管理帐号
                preState.setInt(6, -1);//未激活

                return preState;
            }
        }, key);
        //从主键持有者中获得主键值
        long account_id = key.getKey().longValue();
        return getAccount(account_id);
    }

    @Override
    public void signout(String sid) {
        SessionManager.deleteSession(sid);
    }

    @Override
    public String createSession(Account user, int productId, String fuid, long loginIp, long loginTime, int seconds) {
        SessionState.UserInfo.Builder builder = SessionState.UserInfo.newBuilder();
        builder.setCuid(fuid);
        builder.setProductId(productId);
        builder.setLoginIp(loginIp);
        builder.setLoginTime(loginTime);

        builder.setUserID(user.account_id);
        builder.setAccount(user.username);
        builder.setUserType(user.type);
        SessionState.UserInfo builderUser = builder.build();

        String sid = UUID.randomUUID().toString().replace("-", "");
        SessionManager.persistSession(sid, builderUser, seconds);//3600 * 24 * 30
        return sid;
    }

    @Override
    public boolean updateAccount(Account model) {
        String sql = "update account set phonenum=?,email=?,state=?,update_date=current_timestamp()  where account_id=?";
        jdbcTemplate.update(sql, new Object[]{model.phonenum, model.email, model.state, model.account_id});
        return true;
    }

    @Override
    public boolean updateAccountPwd(long account_id, String oldpwd, String newpwd) {
        Account model = getAccount(account_id);
        if (model == null) {
            throw new BusinessException(ErrorCodeOption.AccountNotExisted.key, ErrorCodeOption.AccountNotExisted.value);
        }
        String pwd = DigestUtils.md5Hex(oldpwd + model.secret_key);
        if (!pwd.equals(model.password)) {
            throw new BusinessException(ErrorCodeOption.OldPwdError.key, ErrorCodeOption.OldPwdError.value);
        }
        pwd = DigestUtils.md5Hex(newpwd + model.secret_key);
        String sql = "update account set password=?,update_date=current_timestamp()  where account_id=?";
        jdbcTemplate.update(sql, new Object[]{pwd, model.account_id});

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
    public Account getAccount(String alias) {
        String sql = "select " + SEL_COL + " from account where username=? or phonenum=? or email=?";
        List<Account> list = jdbcTemplate.query(sql,
                new Object[]{alias, alias, alias},
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
        if (filter.username != null && !filter.username.isEmpty()) {
            condition += " and username like ? ";
            params.add("%" + filter.username + "%");
        }
        if (filter.phonenum != null && !filter.phonenum.isEmpty()) {
            condition += " and phonenum like ? ";
            params.add("%" + filter.phonenum + "%");
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

    @Override
    public Page<Account> getViewAccountList(long managerId, int pageIndex, int pageSize) {
        String sqlCount = "SELECT count(1) FROM account a " +
                "INNER JOIN account_relation b on b.view_id=a.account_id ";
        String sql = "SELECT  a.* FROM account a " +
                "INNER JOIN account_relation b on b.view_id=a.account_id ";
        String condition = "WHERE b.manager_id=?";
        List<Object> params = new ArrayList<Object>();
        params.add(managerId);
        sqlCount += condition;
        int totalRecord = jdbcTemplate.queryForObject(sqlCount,
                params.toArray(),
                Integer.class);
        Page<Account> page = new Page<Account>(pageIndex, pageSize, totalRecord);
        String sort = " order by a.account_id desc";
        sql += condition + sort + " limit " + page.getStartIndex() + "," + page.getPageSize();
        List<Account> list = jdbcTemplate.query(sql,
                params.toArray(),
                new DefaultAccountRowMapper());
        page.setList(list);
        return page;
    }

    @Override
    public boolean addViewAccount(final long managerId, final Account viewAccount) {
        TransactionTemplate tt = new TransactionTemplate(transactionManager);
        try {
            tt.execute(new TransactionCallback() {
                @Override
                public Object doInTransaction(TransactionStatus ts) {
                    //视图帐号，只能管理员用用户名注册
                    String sql = "select count(1) from account where username = ? or email = ?  or phonenum = ?  ";

                    int ret = jdbcTemplate.queryForObject(sql,
                            new Object[]{viewAccount.username, viewAccount.username, viewAccount.username},
                            Integer.class);
                    if (ret > 0) {
                        throw new BusinessException(ErrorCodeOption.AccountExisted.key, ErrorCodeOption.AccountExisted.value);
                    }

                    KeyHolder key = new GeneratedKeyHolder();
                    jdbcTemplate.update(new PreparedStatementCreator() {
                        @Override
                        public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                            PreparedStatement preState = con.prepareStatement("insert into account(username,`password`,secret_key,create_date,`type`,state,update_date) values (?,?,?,current_timestamp(),?,?,current_timestamp() );", Statement.RETURN_GENERATED_KEYS);
                            String secret_key = DigestUtils.md5Hex(UUID.randomUUID().toString());
                            preState.setString(1, viewAccount.username);
                            preState.setString(2, DigestUtils.md5Hex(viewAccount.password + secret_key));
                            preState.setString(3, secret_key);
                            preState.setInt(4, 2);//视图帐号
                            preState.setInt(5, viewAccount.state);

                            return preState;
                        }
                    }, key);
                    //从主键持有者中获得主键值
                    long view_id = key.getKey().longValue();

                    //增加关联
                    jdbcTemplate.update("insert into account_relation(manager_id,view_id) values (?,?);", new Object[]{managerId, view_id});

                    return true;
                }
            });
        } catch (Exception e) {
            Logger.getLogger(AccountImpl.class.getName()).log(Level.SEVERE, null, e);
            throw new BusinessException(ErrorCodeOption.AccountExisted.key, ErrorCodeOption.AccountExisted.value);
//            throw new RuntimeException(e);
        }
        return true;
    }

    public static final class DefaultAccountRowMapper implements RowMapper<Account> {

        @Override
        public Account mapRow(ResultSet rs, int i) throws SQLException {
            Account model = new Account();
            model.account_id = rs.getLong("account_id");
            model.username = rs.getString("username");
            model.password = rs.getString("password");
            model.phonenum = rs.getString("phonenum");
            model.email = rs.getString("email");
            model.type = rs.getInt("type");
            model.state = rs.getInt("state");
            model.create_date = rs.getTimestamp("create_date");
            model.update_date = rs.getTimestamp("update_date");
            model.secret_key = rs.getString("secret_key");

            return model;
        }
    }
}