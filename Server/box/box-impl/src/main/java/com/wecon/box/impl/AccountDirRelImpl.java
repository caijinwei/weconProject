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

import com.wecon.box.api.AccountDirRelApi;
import com.wecon.box.entity.AccountDirRel;

/**
 * @author lanpenghui 2017年8月7日下午1:51:25
 */
@Component
public class AccountDirRelImpl implements AccountDirRelApi {
	@Autowired
	private JdbcTemplate jdbcTemplate;

	private final String SEL_COL = "acc_dir_id,ref_id,ref_alais,create_date";

	@Override
	public long saveAccountDirRel(final AccountDirRel model) {
		KeyHolder key = new GeneratedKeyHolder();
		jdbcTemplate.update(new PreparedStatementCreator() {
			@Override
			public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
				PreparedStatement preState = con.prepareStatement(
						"insert into account_dir_rel(acc_dir_id,ref_id,ref_alais,create_date) values (?,?,?current_timestamp());",
						Statement.RETURN_GENERATED_KEYS);
				preState.setLong(1, model.acc_dir_id);
				preState.setLong(2, model.ref_id);
				preState.setString(3, model.ref_alais);
				return preState;
			}
		}, key);
		// 从主键持有者中获得主键值
		return key.getKey().longValue();
	}

	@Override
	public AccountDirRel getAccountDirRel(long acc_dir_id, long ref_id) {
		
		String sql = "select " + SEL_COL + " from account_dir where id=?";
		List<AccountDirRel> list = jdbcTemplate.query(sql, new Object[] { acc_dir_id,ref_id }, new DefaultAccountDirRelRowMapper());
		if (!list.isEmpty()) {
			return list.get(0);
		}
		return null;
		
		
	}

	@Override
	public void delAccountDir(long acc_dir_id, long ref_id) {
		
			String sql = "delete from  account_dir  where acc_dir_id=? and ref_id=?";
			jdbcTemplate.update(sql, new Object[] { acc_dir_id,ref_id });

		

	}
	public static final class DefaultAccountDirRelRowMapper implements RowMapper<AccountDirRel> {

		@Override
		public AccountDirRel mapRow(ResultSet rs, int i) throws SQLException {
			AccountDirRel model = new AccountDirRel();
			model.acc_dir_id = rs.getLong("acc_dir_id");
			model.ref_id = rs.getLong("ref_id");
			model.ref_alais = rs.getString("ref_alais");
			model.create_date = rs.getTimestamp("create_date");
			return model;
		}
	}

}
