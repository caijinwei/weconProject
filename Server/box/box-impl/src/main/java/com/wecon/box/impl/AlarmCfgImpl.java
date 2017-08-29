package com.wecon.box.impl;

import com.wecon.box.api.AlarmCfgApi;
import com.wecon.box.entity.AlarmCfg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lanpenghui 2017年8月9日下午2:20:05
 */
@Component
public class AlarmCfgImpl implements AlarmCfgApi {
	@Autowired
	private JdbcTemplate jdbcTemplate;
	private final String SEL_COL = "alarmcfg_id,data_id,account_id,name,addr,addr_type,text,condition_type,state,device_id,rid,create_date,update_date";

	@Override
	public List<AlarmCfg> getAlarmCfg(long account_id) {
		String sql = "select " + SEL_COL + " from alarm_cfg where account_id=?";
		List<AlarmCfg> list = jdbcTemplate.query(sql, new Object[] { account_id }, new DefaultAlarmCfgRowMapper());
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
			model.text = rs.getString("text");
			model.state = rs.getInt("state");
			model.condition_type = rs.getInt("condition_type");
			model.create_date = rs.getTimestamp("create_date");
			model.update_date = rs.getTimestamp("update_date");

			return model;
		}
	}

	/*
	 * 查询盒子下的监控点
	 */
	public ArrayList<Integer> findAlarmCfgIdSBydevice_id(Integer device_id) {
		Object[] args = new Object[] { device_id };
		String sql = "SELECT alarmcfg_id FROM alarm_cfg WHERE device_id=?";
		ArrayList<Integer> list = null;
		list = (ArrayList<Integer>) jdbcTemplate.query(sql, args, new RowMapper<Integer>() {
			public Integer mapRow(ResultSet resultSet, int i) throws SQLException {
				Integer model = resultSet.getInt("id");
				return model;
			}
		});
		return list;

	}

	/*
	 * 设置bind_state=0 解绑
	 */
	public void setBind_state(final int[] alaramCfgId, final Integer state) {
		String sql = "UPDATE alarm_cfg SET alarmcfg_id=? where bind_state=?";
		Object[] args = new Object[] { alaramCfgId, state };
		if (alaramCfgId.length != 0) {
			jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps, int i) throws SQLException {
					ps.setInt(1, alaramCfgId[i]);
					ps.setInt(2, state);
				}

				@Override
				public int getBatchSize() {
					return 0;
				}
			});
		}
	}

}
