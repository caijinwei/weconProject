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
import com.wecon.box.api.DevFirmApi;
import com.wecon.box.entity.DevFirm;

/**
 * @author lanpenghui 2017年8月4日上午11:21:26
 */
@Component
public class DevFirmImpl implements DevFirmApi {
	@Autowired
	private JdbcTemplate jdbcTemplate;
	private final String SEL_COL = "f_id,device_id,f_name,f_ver,create_date,update_date";

	@Override
	public long SaveDevFirm(final DevFirm model) {
		KeyHolder key = new GeneratedKeyHolder();
		jdbcTemplate.update(new PreparedStatementCreator() {
			@Override
			public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
				PreparedStatement preState = con.prepareStatement(
						"insert into dev_firm (device_id,f_name,f_ver,create_date)values(?,?,?,current_timestamp());",
						Statement.RETURN_GENERATED_KEYS);
				preState.setLong(1, model.device_id);
				preState.setString(2, model.f_name);
				preState.setString(3, model.f_ver);
				return preState;
			}
		}, key);
		// 从主键持有者中获得主键值
		return key.getKey().longValue();
	}

	@Override
	public boolean updateDevFirm(DevFirm model) {
		String sql = "update dev_firm set f_name=?,f_ver=?,update_date=current_timestamp() where device_id=?";

		jdbcTemplate.update(sql, new Object[] { model.f_name, model.f_ver, model.device_id });
		return true;
	}

	@Override
	public DevFirm getDevFirm(long f_id) {
		String sql = "select " + SEL_COL + " from dev_firm where f_id=?";
		List<DevFirm> list = jdbcTemplate.query(sql, new Object[] { f_id }, new DefaultDevFirmRowMapper());
		if (!list.isEmpty()) {
			return list.get(0);
		}
		return null;
	}

	@Override
	public DevFirm getDevFirm_device_id(long device_id) {
		String sql = "select " + SEL_COL + " from dev_firm where device_id=?";
		List<DevFirm> list = jdbcTemplate.query(sql, new Object[] { device_id }, new DefaultDevFirmRowMapper());
		if (!list.isEmpty()) {
			return list.get(0);
		}
		return null;
	}

	@Override
	public void delDevFirm(long f_id) {
		String sql = "delete from  dev_firm  where f_id=?";
		jdbcTemplate.update(sql, new Object[] { f_id });
	}

	public static final class DefaultDevFirmRowMapper implements RowMapper<DevFirm> {

		@Override
		public DevFirm mapRow(ResultSet rs, int i) throws SQLException {
			DevFirm model = new DevFirm();
			model.f_id = rs.getLong("f_id");
			model.device_id = rs.getLong("device_id");
			model.f_name = rs.getString("f_name");
			model.f_ver = rs.getString("f_ver");
			model.create_date = rs.getTimestamp("create_date");
			model.update_date = rs.getTimestamp("update_date");

			return model;
		}
	}

}
