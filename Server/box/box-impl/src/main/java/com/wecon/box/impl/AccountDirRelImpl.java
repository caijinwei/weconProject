package com.wecon.box.impl;

import com.wecon.box.api.AccountDirRelApi;
import com.wecon.box.entity.AccountDirRel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author lanpenghui 2017年8月7日下午1:51:25
 */
@Component
public class AccountDirRelImpl implements AccountDirRelApi {
	@Autowired
	private JdbcTemplate jdbcTemplate;

	private final String SEL_COL = "acc_dir_id,ref_id,ref_alais,create_date";

	@Override
	public void saveAccountDirRel(final AccountDirRel model) {

		String sql="insert into account_dir_rel(acc_dir_id,ref_id,ref_alais,create_date) values (?,?,?,current_timestamp())";
		Object[] args=new Object[]{model.acc_dir_id,model.ref_id,model.ref_alais};
		jdbcTemplate.update(sql,args);
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
	/*
	* 删除用户绑定下的全部盒子
	* delete FROM account_dir_rel where ref_id=1 AND acc_dir_id IN (SELECT id FROM account_dir WHERE account_id=1)
	* */
	public void deleteDeviceRel(Integer device_id,long account_id)
	{
		Object[] args=new Object[]{device_id,account_id};
		String sql="delete FROM account_dir_rel where ref_id=? AND acc_dir_id IN (SELECT id FROM account_dir WHERE account_id=?)";
		jdbcTemplate.update(sql,args);
	}

}
