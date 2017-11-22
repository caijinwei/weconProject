package com.wecon.box.impl;

import com.wecon.box.api.AccountDirApi;
import com.wecon.box.entity.AccountDir;
import com.wecon.box.enums.ErrorCodeOption;
import com.wecon.restful.core.BusinessException;
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
 * Created by zengzhipeng on 2017/8/8.
 */
@Component
public class AccountDirImpl implements AccountDirApi {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public PlatformTransactionManager transactionManager;

    @Override
    public long addAccountDir(final AccountDir model) {
        // 相同用户，相同类型，不允许重名
        String sql = "select count(1) from account_dir where account_id = ? and `name` = ? and `type` = ? and `device_id` = ?  ";

        int ret = jdbcTemplate.queryForObject(sql, new Object[]{model.account_id, model.name, model.type, model.device_id},
                Integer.class);
        if (ret > 0) {
            throw new BusinessException(ErrorCodeOption.CanAddTheSameGroup.key,
                    ErrorCodeOption.CanAddTheSameGroup.value);
        }

        KeyHolder key = new GeneratedKeyHolder();
        jdbcTemplate.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                PreparedStatement preState = con.prepareStatement(
                        "insert into account_dir(account_id,`name`,`type`,create_date,update_date,device_id) values (?,?,?,current_timestamp(),current_timestamp(),?);",
                        Statement.RETURN_GENERATED_KEYS);
                String secret_key = DigestUtils.md5Hex(UUID.randomUUID().toString());
                preState.setLong(1, model.account_id);
                preState.setString(2, model.name);
                preState.setInt(3, model.type);
                preState.setLong(4, model.device_id);

                return preState;
            }
        }, key);
        // 从主键持有者中获得主键值
        long dir_id = key.getKey().longValue();
        model.id = dir_id;
        return dir_id;
    }

    @Override
    public boolean updateAccountDir(AccountDir model) {
        // 只能更新分组名
        String sql = "select count(1) from account_dir where account_id = ? and `name` = ? and `type` = ? and `device_id` = ? and id <> ? ";
        int ret = jdbcTemplate.queryForObject(sql, new Object[]{model.account_id, model.name, model.type, model.device_id, model.id},
                Integer.class);
        if (ret > 0) {
            throw new BusinessException(ErrorCodeOption.CanAddTheSameGroup.key,
                    ErrorCodeOption.CanAddTheSameGroup.value);
        }
        sql = "update account_dir set `name` = ?,update_date=current_timestamp()  where id=?";
        jdbcTemplate.update(sql, new Object[]{model.name, model.id});
        return true;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public boolean delAccountDir(final long id) {
        TransactionTemplate tt = new TransactionTemplate(transactionManager);
        try {
            tt.execute(new TransactionCallback() {
                @Override
                public Object doInTransaction(TransactionStatus ts) {
                    jdbcTemplate.update("DELETE FROM account_dir WHERE id=?;", new Object[]{id});
                    jdbcTemplate.update("DELETE FROM account_dir_rel WHERE acc_dir_id=?;", new Object[]{id});

                    return true;
                }
            });
        } catch (Exception e) {
            Logger.getLogger(AccountImpl.class.getName()).log(Level.SEVERE, null, e);
            throw new RuntimeException(e);
        }
        return true;
    }

    @Override
    public AccountDir getAccountDir(long id) {
        String sql = "select * from account_dir where id = ? ";
        List<AccountDir> list = jdbcTemplate.query(sql, new Object[]{id}, new DefaultAccountDirRowMapper());
        if (!list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    @Override
    public List<AccountDir> getAccountDirList(long account_id, int type) {
        String sql = "select * from account_dir where 1=1";
        StringBuffer condition = new StringBuffer("");
        List<Object> params = new ArrayList<Object>();
        if (account_id > 0) {
            condition.append(" and account_id = ? ");
            params.add(account_id);

        }
        if (type > -1) {
            condition.append(" and type = ? ");
            params.add(type);

        }
        sql += condition;
        List<AccountDir> list = jdbcTemplate.query(sql, params.toArray(), new DefaultAccountDirRowMapper());
        return list;
    }

    @Override
    public List<AccountDir> getAccountDirList(long account_id, int type, long device_id) {
        String sql = " select  *from account_dir where 1=1 ";
        StringBuffer condition = new StringBuffer("");
        List<Object> params = new ArrayList<Object>();
        if (account_id > 0) {
            condition.append(" and account_id = ? ");
            params.add(account_id);

        }
        if (device_id > 0 || device_id == -100 || device_id == -200) {//-100表示是管理者自定义监控点的设备id -200视图账号的设备id
            condition.append(" and device_id = ? ");
            params.add(device_id);

        }
        if (type > -1) {
            condition.append(" and type = ? ");
            params.add(type);

        }
        sql += condition;
        List<AccountDir> list = jdbcTemplate.query(sql, params.toArray(), new DefaultAccountDirRowMapper());
        return list;
    }

    @Override
    public void updateAccountBydeviceAndType(long accountId,long deviceId) {
        String sql="UPDATE account_dir SET account_id=? WHERE device_id=?";
        Object[] args=new Object[]{accountId,deviceId};
        jdbcTemplate.update(sql,args);
    }

    public static final class DefaultAccountDirRowMapper implements RowMapper<AccountDir> {

        @Override
        public AccountDir mapRow(ResultSet rs, int i) throws SQLException {
            AccountDir model = new AccountDir();
            model.id = rs.getLong("id");
            model.account_id = rs.getLong("account_id");
            model.name = rs.getString("name");
            model.type = rs.getInt("type");
            model.create_date = rs.getTimestamp("create_date");
            model.update_date = rs.getTimestamp("update_date");
            model.device_id = rs.getLong("device_id");

            return model;
        }
    }


}
