package com.wecon.box.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.wecon.box.api.AlarmCfgApi;
import com.wecon.box.entity.AlarmCfg;

/**
 * @author lanpenghui
 * 2017年8月9日下午2:20:05
 */
@Component
public class AlarmCfgImpl implements AlarmCfgApi {
	@Autowired
	private JdbcTemplate jdbcTemplate;
	private final String SEL_COL ="alarmcfg_id,data_id,account_id,name,addr,addr_type,text,condition_type,state,create_date,update_date";

	@Override
	public List<AlarmCfg> getAlarmCfg(long account_id) {
		String sql = "select " + SEL_COL + " from alarm_cfg where account_id=?";
		List<AlarmCfg> list = jdbcTemplate.query(sql, new Object[] {account_id },
				new DefaultAlarmCfgRowMapper());
		if (!list.isEmpty()) {
			return list;
		}

		return null;
		
	
	}
	public static final class DefaultAlarmCfgRowMapper implements RowMapper<AlarmCfg> {

		@Override
		public AlarmCfg mapRow(ResultSet rs, int i) throws SQLException {
			AlarmCfg model = new AlarmCfg();
			model.alarmcfg_id = rs.getLong("alarmcfg_id");
			model.data_id = rs.getLong("data_id");
			model.account_id = rs.getLong("account_id");
			model.name = rs.getString("name");
			model.addr = rs.getString("addr");
			model.addr_type = rs.getInt("addr_type");
			model.text=rs.getString("text");
			model.state = rs.getInt("state");
			model.condition_type = rs.getInt("condition_type");
			model.create_date = rs.getTimestamp("create_date");
			model.update_date = rs.getTimestamp("update_date");

			return model;
		}
	}


}
