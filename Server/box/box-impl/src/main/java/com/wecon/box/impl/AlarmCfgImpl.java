package com.wecon.box.impl;

import com.wecon.box.api.AlarmCfgApi;
import com.wecon.box.entity.AlarmCfg;
import com.wecon.box.entity.AlarmCfgExtend;
import com.wecon.box.entity.AlarmTrigger;
import com.wecon.common.util.CommonUtils;
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
		String sql = "select " + SEL_COL + " from alarm_cfg a where account_id=?";
		List<AlarmCfg> list = jdbcTemplate.query(sql, new Object[] { account_id }, new DefaultAlarmCfgRowMapper());
		if (!list.isEmpty()) {
			return list;
		}

		return null;

	}

	@Override
	public List<AlarmCfgExtend> getAlarmCfgExtendListByState(int state){
		String sql = "select a.alarmcfg_id,a.data_id,a.account_id,a.name,a.addr,a.addr_type,a.text,a.condition_type,a.state,a.create_date,a.update_date,d.machine_code" +
				" from alarm_cfg a ,device d where a.device_id=d.device_id and a.state = ?";
		String triSql = "select at.type, at.value from alarm_trigger at, alarm_cfg ac where at.alarmcfg_id=ac.alarmcfg_id and ac.state = ?";
		List<AlarmCfgExtend> alarmCfgExtendLst = jdbcTemplate.query(sql, new Object[]{state}, new DefaultAlarmCfgExtendRowMapper());
		List<AlarmTrigger> alarmCfgTriggerLst = jdbcTemplate.query(triSql, new Object[]{state}, new DefaultAlarmTriggerRowMapper());
		if(null != alarmCfgExtendLst && null != alarmCfgTriggerLst){
			for(AlarmCfgExtend ae : alarmCfgExtendLst){
				List<AlarmTrigger> conditionList = new ArrayList<AlarmTrigger>();
				for(AlarmTrigger at : alarmCfgTriggerLst){
					if(ae.alarmcfg_id == at.alarmcfg_id){
						conditionList.add(at);
					}
				}
				ae.setCondition_list(conditionList);
			}
		}
		return alarmCfgExtendLst;
	}

	@Override
	public boolean batchUpdateState(final List<int[]> updList){
		if(null == updList || updList.size() == 0){
			return false;
		}
		String sql = "update alarm_cfg set state = ? where alarmcfg_id = ?";
		jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
			public int getBatchSize() {
				return updList.size();
				//这个方法设定更新记录数，通常List里面存放的都是我们要更新的，所以返回list.size();
			}
			public void setValues(PreparedStatement ps, int i)throws SQLException {
				int[] arg = updList.get(i);
				ps.setInt(1, arg[0]);
				ps.setInt(2, arg[1]);
			}
		});
		return true;
	}

	public static final class DefaultAlarmTriggerRowMapper implements RowMapper<AlarmTrigger> {
		@Override
		public AlarmTrigger mapRow(ResultSet rs, int i) throws SQLException {
			AlarmTrigger model = new AlarmTrigger();
			model.type = rs.getInt("type");
			model.value = rs.getString("value");
			return model;
		}
	}

	public static final class DefaultAlarmCfgExtendRowMapper implements RowMapper<AlarmCfgExtend> {

		@Override
		public AlarmCfgExtend mapRow(ResultSet rs, int i) throws SQLException {
			AlarmCfgExtend model = new AlarmCfgExtend();
			model.alarmcfg_id = rs.getLong("alarmcfg_id");
			model.addr_id = model.alarmcfg_id;
			model.plc_id = rs.getLong("plc_id");
			model.com = model.plc_id+"";
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
			model.upd_time = model.update_date;
			model.machine_code = rs.getString("machine_code");
			return model;
		}
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
