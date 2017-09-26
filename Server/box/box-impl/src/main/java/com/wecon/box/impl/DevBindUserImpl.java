package com.wecon.box.impl;

import com.wecon.box.api.DevBindUserApi;
import com.wecon.box.entity.DevBindUser;
import com.wecon.box.filter.DevBindUserFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lanpenghui 2017年8月7日下午3:20:16
 */
@Component
public class DevBindUserImpl implements DevBindUserApi {
	@Autowired
	private JdbcTemplate jdbcTemplate;
	private final String SEL_COL = "account_id,device_id,create_date";

	@Override
	public void saveDevBindUser(final DevBindUser model) {
		String sql = "insert into dev_bind_user (account_id,device_id,create_date)values(?,?,current_timestamp())";
		jdbcTemplate.update(sql, new Object[] { model.account_id, model.device_id });
	}

	@Override
	public List<DevBindUser> getDevBindUser(DevBindUserFilter filter) {
		String sql = "select " + SEL_COL + " from dev_bind_user where 1=1 ";

		StringBuffer condition = new StringBuffer("");
		List<Object> params = new ArrayList<Object>();

		if (filter.account_id > 0) {
			condition.append("and account_id=? ");
			params.add(filter.account_id);
		}
		if (filter.device_id > 0) {
			condition.append("and device_id=? ");
			params.add(filter.device_id);
		}
		sql += condition;
		List<DevBindUser> list = jdbcTemplate.query(sql, params.toArray(), new DefaultDevBindUserRowMapper());
		if (!list.isEmpty()) {
			return list;
		}
		return null;
	}

	@Override
	public int getDevBindUserCount(long account_id) {
		String sql = "select count(0) from dev_bind_user where account_id=?";
		if (account_id < 0) {
			return 0;
		}
		List<Object> params = new ArrayList<Object>();
		params.add(account_id);

		int totalRecord = jdbcTemplate.queryForObject(sql, params.toArray(), Integer.class);

		return totalRecord;
	}

	@Override
	public void delDevBindUser(long account_id, long device_id) {
		Object[] args = new Object[] { account_id, device_id };
		String sql = "DELETE FROM dev_bind_user  WHERE  account_id=? AND device_id=?";
		jdbcTemplate.update(sql, args);
	}

	@Override
	public int findByDevId(long device_id) {
		Object[] args = new Object[] { device_id };
		String sql = "select count(1) from dev_bind_user where device_id=?";
		return jdbcTemplate.queryForObject(sql, args, Integer.class);
	}

	public static final class DefaultDevBindUserRowMapper implements RowMapper<DevBindUser> {

		@Override
		public DevBindUser mapRow(ResultSet rs, int i) throws SQLException {
			DevBindUser model = new DevBindUser();
			model.device_id = rs.getLong("device_id");
			model.account_id = rs.getLong("account_id");
			model.create_date = rs.getTimestamp("create_date");
			return model;
		}
	}

	public boolean isRecord(Integer device_id, long account_id) {
		Object[] args = new Object[] { device_id, account_id };
		String sql = "SELECT COUNT(*) FROM dev_bind_user WHERE device_id=? AND account_id=?";
		Integer count = jdbcTemplate.queryForObject(sql, args, Integer.class);
		if (count == 1) {
			return true;
		}
		return false;
	}

}
