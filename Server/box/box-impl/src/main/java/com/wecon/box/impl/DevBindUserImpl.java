package com.wecon.box.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import com.wecon.box.api.DevBindUserApi;
import com.wecon.box.entity.DevBindUser;
import com.wecon.box.filter.DevBindUserFilter;
import com.wecon.common.util.CommonUtils;

/**
 * @author lanpenghui 2017年8月7日下午3:20:16
 */
@Component
public class DevBindUserImpl implements DevBindUserApi {
	@Autowired
	private JdbcTemplate jdbcTemplate;
	private final String SEL_COL = "account_id,device_id,create_date";

	@Override
	public long saveDevBindUser(final DevBindUser model) {

		KeyHolder key = new GeneratedKeyHolder();
		jdbcTemplate.update(new PreparedStatementCreator() {
			@Override
			public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
				PreparedStatement preState = con.prepareStatement(
						"insert into dev_bind_user (account_id,device_id,create_date)values(?,?,current_timestamp());",
						Statement.RETURN_GENERATED_KEYS);
				preState.setLong(1, model.account_id);
				preState.setLong(1, model.device_id);

				return preState;
			}
		}, key);
		// 从主键持有者中获得主键值
		return key.getKey().longValue();

	}

	@Override
	public List<DevBindUser> getDevBindUser(DevBindUserFilter filter ) {
		String sql = "select " + SEL_COL + " from dev_bind_user where 1=1 ";

		StringBuffer condition = new StringBuffer("");
		List<Object> params = new ArrayList<Object>();

		if (filter.account_id>0) {
			condition.append("and account_id=? ");
			params.add(filter.account_id);

		}
		if (filter.device_id>0) {
			condition.append("and device_id=? ");
			params.add(filter.device_id);
		}
		sql+=condition;
		List<DevBindUser> list = jdbcTemplate.query(sql, params.toArray(),
				new DefaultDevBindUserRowMapper());
		if (!list.isEmpty()) {
			return list;
		}
		return null;
	}

	@Override
	public void delDevBindUser(long account_id, long device_id) {

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
}
