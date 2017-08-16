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
		String sql = "select count(1) from account_dir where account_id = ? and `name` = ? and `type` = ?  ";

		int ret = jdbcTemplate.queryForObject(sql, new Object[] { model.account_id, model.name, model.type },
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
						"insert into account_dir(account_id,`name`,`type`,create_date,update_date) values (?,?,?,current_timestamp(),current_timestamp());",
						Statement.RETURN_GENERATED_KEYS);
				String secret_key = DigestUtils.md5Hex(UUID.randomUUID().toString());
				preState.setLong(1, model.account_id);
				preState.setString(2, model.name);
				preState.setInt(3, model.type);

				return preState;
			}
		}, key);
		// 从主键持有者中获得主键值
		long dir_id = key.getKey().longValue();
		return dir_id;
	}

	@Override
	public boolean updateAccountDir(AccountDir model) {
		// 只能更新分组名
		String sql = "select count(1) from account_dir where account_id = ? and `name` = ? and `type` = ? and id <> ? ";
		int ret = jdbcTemplate.queryForObject(sql, new Object[] { model.account_id, model.name, model.type, model.id },
				Integer.class);
		if (ret > 0) {
			throw new BusinessException(ErrorCodeOption.CanAddTheSameGroup.key,
					ErrorCodeOption.CanAddTheSameGroup.value);
		}
		sql = "update account_dir set `name` = ?,update_date=current_timestamp()  where id=?";
		jdbcTemplate.update(sql, new Object[] { model.name, model.id });
		return true;
	}

	@Override
	public boolean delAccountDir(final long id) {
		TransactionTemplate tt = new TransactionTemplate(transactionManager);
		try {
			tt.execute(new TransactionCallback() {
				@Override
				public Object doInTransaction(TransactionStatus ts) {
					jdbcTemplate.update("DELETE FROM account_dir WHERE id=?;", new Object[] { id });
					jdbcTemplate.update("DELETE FROM account_dir_rel WHERE acc_dir_id=?;", new Object[] { id });

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
		List<AccountDir> list = jdbcTemplate.query(sql, new Object[] { id }, new DefaultAccountDirRowMapper());
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

//	@Override
//	public List<AccountDir> getAccountDirList(long account_id,long device_id) {
//		String sql = " select ad.id,ad.account_id,ad.name,ad.type,ad.create_date,ad.update_date from account_dir ad,account_dir_rel adr ,real_his_cfg  rhc,plc_info pli where 1=1 and ad.`id`=adr.`acc_dir_id` and pli.`plc_id`=rhc.`plc_id`and rhc.`id`=adr.`ref_id` and ad.`TYPE` =1";
//		StringBuffer condition = new StringBuffer("");
//		List<Object> params = new ArrayList<Object>();
//		if (account_id > 0) {
//			condition.append(" and ad.account_id = ? ");
//			params.add(account_id);
//
//		}
//		if (device_id > 0) {
//			condition.append(" and pli.device_id = ? ");
//			params.add(device_id);
//
//		}
//		sql += condition;
//		List<AccountDir> list = jdbcTemplate.query(sql, params.toArray(), new DefaultAccountDirRowMapper());
//		return list;
//	}

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

			return model;
		}
	}

}
