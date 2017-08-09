package com.wecon.box.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import com.wecon.box.api.AccountDirApi;
import com.wecon.box.entity.Account;
import com.wecon.box.entity.AccountDir;

/**
 * @author lanpenghui 2017年8月7日上午11:21:11
 */
@Component
public class AccountDirImpl implements AccountDirApi {
	@Autowired
	private JdbcTemplate jdbcTemplate;

	private final String SEL_COL = "id,account_id,name,type,create_date,update_date";

	@Override
	public long SaveAccountDir(final AccountDir model) {

		KeyHolder key = new GeneratedKeyHolder();
		jdbcTemplate.update(new PreparedStatementCreator() {
			@Override
			public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
				PreparedStatement preState = con.prepareStatement(
						"insert into account_dir(account_id,`name`,type,create_date,update_date) values (?,?,?,current_timestamp(),current_timestamp());",
						Statement.RETURN_GENERATED_KEYS);
				preState.setLong(1, model.account_id);
				preState.setString(2, model.name);
				preState.setInt(3, model.type);
				return preState;
			}
		}, key);
		// 从主键持有者中获得主键值
		return key.getKey().longValue();

	}

	@Override
	public boolean updateAccountDir(AccountDir model) {
		String sql = "update account_dir set account_id=?,name=?,type=?,update_date=current_timestamp() where id=?";

		jdbcTemplate.update(sql, new Object[] { model.account_id, model.name, model.type, model.id });
		return true;
	}

	@Override
	public AccountDir getAccountDir(long id) {
		String sql = "select " + SEL_COL + " from account_dir where id=?";
		List<AccountDir> list = jdbcTemplate.query(sql, new Object[] { id }, new DefaultAccountDirRowMapper());
		if (!list.isEmpty()) {
			return list.get(0);
		}
		return null;
	}

	@Override
	public List<AccountDir> getAccountDir(Account account) {
		String sql = "select " + SEL_COL + " from account_dir where account_id=?";
		List<AccountDir> list = jdbcTemplate.query(sql, new Object[] { account.account_id },
				new DefaultAccountDirRowMapper());
		if (!list.isEmpty()) {
			return list;
		}
		return null;
	}

	@Override
	public void delAccountDir(long id) {
		String sql = "delete from  account_dir  where id=?";
		jdbcTemplate.update(sql, new Object[] { id });

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
			return model;
		}
	}

}
