package com.wecon.box.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import com.wecon.box.api.AlarmCfgDataApi;
import com.wecon.box.entity.AlarmCfgData;
import com.wecon.box.entity.AlarmCfgDataAlarmCfg;
import com.wecon.box.entity.Page;
import com.wecon.box.filter.AlarmCfgDataFilter;
import com.wecon.common.util.CommonUtils;

/**
 * @author lanpenghui 2017年8月4日上午9:22:54
 */
@Component
public class AlarmCfgDataImpl implements AlarmCfgDataApi {
	@Autowired
	private JdbcTemplate jdbcTemplate;
	private final String SEL_COL = "acd.alarm_cfg_id,acd.monitor_time,acd.value,acd.create_date,acd.state,acd.alarm_type";

	@Override
	public void saveAlarmCfgData(final List<AlarmCfgData> listmodel) {
		String sql = "insert into alarm_cfg_data (alarm_cfg_id,monitor_time,`value`,create_date,state,alarm_type)values(?,?,?,current_timestamp(),?,?);";
		jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				ps.setLong(1, listmodel.get(i).alarm_cfg_id);
				ps.setTimestamp(2, listmodel.get(i).monitor_time);
				ps.setString(3, listmodel.get(i).value);
				ps.setInt(4, listmodel.get(i).state);
				ps.setInt(5, listmodel.get(i).alarm_type);
			}

			@Override
			public int getBatchSize() {
				return listmodel.size();
			}
		});

	}

	@Override
	public boolean updateAlarmCfgData(AlarmCfgData alarmCfgData) {
		String sql = "update alarm_cfg_data SET `value`=?,create_date=?,state=?,alarm_type=? where alarm_cfg_id=? and monitor_time=?";
		jdbcTemplate.update(sql, new Object[] { alarmCfgData.value, alarmCfgData.create_date, alarmCfgData.state,
				alarmCfgData.alarm_type, alarmCfgData.alarm_cfg_id, alarmCfgData.monitor_time });

		return true;
	}

	@Override
	public List<AlarmCfgDataAlarmCfg> getAlarmCfgData(AlarmCfgDataFilter filter) {

		String sql = " select " + SEL_COL + ",ac.name,ac.text "
				+ " from alarm_cfg ac ,alarm_cfg_data acd,alarm_trigger atr where 1=1 and  ac.alarmcfg_id=atr.alarmcfg_id and  ac.alarmcfg_id=acd.alarm_cfg_id and ac.bind_state=1 ";

		StringBuffer condition = new StringBuffer("");
		List<Object> params = new ArrayList<Object>();
		if (filter.account_id > 0) {
			condition.append(" and ac.account_id = ? ");
			params.add(filter.account_id);
		}
		if (!CommonUtils.isNullOrEmpty(filter.value)) {
			condition.append(" and acd.value like ? ");
			params.add("%" + filter.value + "%");
		}
		if (filter.state > -1) {
			condition.append(" and acd.state = ? ");
			params.add(filter.state);
		}
		if (!CommonUtils.isNullOrEmpty(filter.name)) {
			condition.append(" and ac.name like ? ");
			params.add("%" + filter.name + "%");

		}
		sql += condition;
		List<AlarmCfgDataAlarmCfg> list = jdbcTemplate.query(sql, params.toArray(),
				new DefaultAlarmCfgDataAlarmCfgRowMapper());

		return list;

	}

	@Override
	public AlarmCfgData getAlarmCfgData(long alarm_cfg_id, Timestamp monitor_time) {
		String sql = "select " + SEL_COL + " from alarm_cfg_data acd  where acd.alarm_cfg_id=? and acd.monitor_time=?";
		List<AlarmCfgData> list = jdbcTemplate.query(sql, new Object[] { alarm_cfg_id, monitor_time },
				new DefaultAlarmCfgDataRowMapper());
		if (!list.isEmpty()) {
			return list.get(0);
		}

		return null;
	}

	@Override
	public void delAlarmCfgData(long alarm_cfg_id) {
		String sql = "delete from  alarm_cfg_data  where alarm_cfg_id=?";
		jdbcTemplate.update(sql, new Object[] { alarm_cfg_id });
	}

	@Override
	public boolean batchDeleteByPlcId(List<Long> ids) {
		if (null == ids || ids.size() == 0) {
			return false;
		}

		StringBuilder idSb = new StringBuilder();
		for (long id : ids) {
			idSb.append(",").append(id);
		}
		String sql = "delete from alarm_cfg_data where alarm_cfg_id in(select alarmcfg_id from alarm_cfg where plc_id in("
				+ idSb.substring(1) + "))";
		jdbcTemplate.update(sql);

		return true;
	}

	@Override
	public boolean batchDeleteById(final List<Long> ids) {
		if (null == ids || ids.size() == 0) {
			return false;
		}

		StringBuilder idSb = new StringBuilder();
		for (long id : ids) {
			idSb.append(",").append(id);
		}
		String sql = "delete from alarm_cfg_data where alarm_cfg_id in(" + idSb.substring(1) + ")";
		jdbcTemplate.update(sql);

		return true;
	}

	@Override
	public Page<AlarmCfgDataAlarmCfg> getRealHisCfgDataList(AlarmCfgDataFilter filter, int pageIndex, int pageSize) {
		String sqlCount = "select count(0) from alarm_cfg ac ,alarm_cfg_data acd,plc_info pli where 1=1  and  ac.alarmcfg_id=acd.alarm_cfg_id and ac.bind_state=1 and pli.`plc_id`=ac.plc_id and ac.bind_state=1";
		String sql = " select " + SEL_COL + ",ac.name,ac.text,ac.alarm_level "
				+ " from alarm_cfg ac ,alarm_cfg_data acd,plc_info pli where 1=1 and  ac.alarmcfg_id=acd.alarm_cfg_id and ac.bind_state=1 and pli.`plc_id`=ac.plc_id and ac.bind_state=1";

		StringBuffer condition = new StringBuffer("");
		List<Object> params = new ArrayList<Object>();
		if (filter.account_id > 0) {
			condition.append(" and ac.account_id = ? ");
			params.add(filter.account_id);
		}
		if (filter.alarm_cfg_id > 0) {
			condition.append(" and acd.alarm_cfg_id = ? ");
			params.add(filter.alarm_cfg_id);
		}
		if (filter.device_id > 0) {
			condition.append(" and ac.device_id = ? ");
			params.add(filter.device_id);
		}
		if (!CommonUtils.isNullOrEmpty(filter.value)) {
			condition.append(" and acd.value like ? ");
			params.add("%" + filter.value + "%");
		}
		if (filter.state > -1) {
			condition.append(" and acd.state = ? ");
			params.add(filter.state);
		}
		if (!CommonUtils.isNullOrEmpty(filter.name)) {
			condition.append(" and ac.name like ? ");
			params.add("%" + filter.name + "%");
		}
		if (filter.event_id > -1) {
			condition.append(" and acd.alarm_type = ? ");
			params.add(filter.event_id);
		}
		if (filter.grade_id > -1) {
			condition.append(" and ac.alarm_level = ? ");
			params.add(filter.grade_id);
		}
		// 操作时间起
		if (!CommonUtils.isNullOrEmpty(filter.start_date)) {
			condition.append(" and date_format(acd.monitor_time,'%Y-%m-%d %H:%i') >= ");
			condition.append(" date_format(str_to_date(?,'%Y-%m-%d %H:%i'),'%Y-%m-%d %H:%i') ");
			params.add(CommonUtils.trim(filter.start_date));

		}
		// 操作时间止
		if (!CommonUtils.isNullOrEmpty(filter.end_date)) {
			condition.append(" and date_format(acd.monitor_time,'%Y-%m-%d %H:%i') <= ");
			condition.append(" date_format(str_to_date(?,'%Y-%m-%d %H:%i'),'%Y-%m-%d %H:%i') ");
			params.add(CommonUtils.trim(filter.end_date));

		}
		sqlCount += condition;
		int totalRecord = jdbcTemplate.queryForObject(sqlCount, params.toArray(), Integer.class);
		Page<AlarmCfgDataAlarmCfg> page = new Page<AlarmCfgDataAlarmCfg>(pageIndex, pageSize, totalRecord);
		String sort = " order by acd.monitor_time desc";
		if (pageIndex > 0 && pageSize > 0) {
			sql += condition + sort + " limit " + page.getStartIndex() + "," + page.getPageSize();
		} else {
			sql += condition + sort;// 不分页
		}

		List<AlarmCfgDataAlarmCfg> list = jdbcTemplate.query(sql, params.toArray(),
				new DefaultAlarmCfgDataAlarmCfgRowMapper());
		page.setList(list);
		return page;

	}

	@Override
	public Page<AlarmCfgDataAlarmCfg> getViewRealHisCfgDataList(AlarmCfgDataFilter filter, int pageIndex,
			int pageSize) {
		String sqlCount = "select count(0) from view_account_role var,alarm_cfg ac,alarm_cfg_data  acd,plc_info pli where 1=1 and ac.alarmcfg_id=var.cfg_id and ac.alarmcfg_id=acd.alarm_cfg_id and var.cfg_type=2 and ac.bind_state=1 and pli.`plc_id`=ac.plc_id ";
		String sql = " select " + SEL_COL + ",ac.name,ac.text,ac.alarm_level "
				+ " from view_account_role var,alarm_cfg ac,alarm_cfg_data  acd,plc_info pli where 1=1 and ac.alarmcfg_id=var.cfg_id  and ac.alarmcfg_id=acd.alarm_cfg_id and var.cfg_type=2 and ac.bind_state=1 and pli.`plc_id`=ac.plc_id";

		StringBuffer condition = new StringBuffer("");
		List<Object> params = new ArrayList<Object>();
		if (filter.account_id > 0) {
			condition.append(" and var.view_id = ? ");
			params.add(filter.account_id);
		}
		if (filter.alarm_cfg_id > 0) {
			condition.append(" and acd.alarm_cfg_id = ? ");
			params.add(filter.alarm_cfg_id);
		}
		if (filter.device_id > 0) {
			condition.append(" and ac.device_id = ? ");
			params.add(filter.device_id);
		}
		if (!CommonUtils.isNullOrEmpty(filter.value)) {
			condition.append(" and acd.value like ? ");
			params.add("%" + filter.value + "%");
		}
		if (filter.state > -1) {
			condition.append(" and acd.state = ? ");
			params.add(filter.state);
		}
		if (!CommonUtils.isNullOrEmpty(filter.name)) {
			condition.append(" and ac.name like ? ");
			params.add("%" + filter.name + "%");

		}
		if (filter.event_id > -1) {
			condition.append(" and acd.alarm_type = ? ");
			params.add(filter.event_id);
		}
		if (filter.grade_id > -1) {
			condition.append(" and ac.alarm_level = ? ");
			params.add(filter.grade_id);
		}
		// 操作时间起
		if (!CommonUtils.isNullOrEmpty(filter.start_date)) {
			condition.append(" and date_format(acd.monitor_time,'%Y-%m-%d %H:%i') >= ");
			condition.append(" date_format(str_to_date(?,'%Y-%m-%d %H:%i'),'%Y-%m-%d %H:%i') ");
			params.add(CommonUtils.trim(filter.start_date));

		}
		// 操作时间止
		if (!CommonUtils.isNullOrEmpty(filter.end_date)) {
			condition.append(" and date_format(acd.monitor_time,'%Y-%m-%d %H:%i') <= ");
			condition.append(" date_format(str_to_date(?,'%Y-%m-%d %H:%i'),'%Y-%m-%d %H:%i') ");
			params.add(CommonUtils.trim(filter.end_date));

		}
		sqlCount += condition;
		int totalRecord = jdbcTemplate.queryForObject(sqlCount, params.toArray(), Integer.class);
		Page<AlarmCfgDataAlarmCfg> page = new Page<AlarmCfgDataAlarmCfg>(pageIndex, pageSize, totalRecord);
		String sort = " order by acd.monitor_time desc";
		sql += condition + sort + " limit " + page.getStartIndex() + "," + page.getPageSize();
		List<AlarmCfgDataAlarmCfg> list = jdbcTemplate.query(sql, params.toArray(),
				new DefaultAlarmCfgDataAlarmCfgRowMapper());
		page.setList(list);
		return page;
	}

	@Override
	public Page<AlarmCfgDataAlarmCfg> getViewAlarmCfgDataPage(AlarmCfgDataFilter filter, Map<String, Object> bParams,
			int pageIndex, int pageSize) {
		String fromStr = " from view_account_role var, alarm_cfg ac,alarm_cfg_data acd ";
		String whereStr = " where  ac.alarmcfg_id=var.cfg_id and ac.alarmcfg_id=acd.alarm_cfg_id and ac.bind_state=1 and var.cfg_type=2 ";
		StringBuffer condition = new StringBuffer("");
		List<Object> params = new ArrayList<Object>();
		if (filter.account_id > 0) {
			condition.append(" and var.view_id = ? ");
			params.add(filter.account_id);
		}

		if (filter.state > -1) {
			condition.append(" and acd.state = ? ");
			params.add(filter.state);
		}

		// 操作时间起
		if (!CommonUtils.isNullOrEmpty(filter.start_date)) {
			condition.append(" and date_format(acd.monitor_time,'%Y-%m-%d %H:%i') >= ");
			condition.append(" date_format(str_to_date(?,'%Y-%m-%d %H:%i'),'%Y-%m-%d %H:%i') ");
			params.add(CommonUtils.trim(filter.start_date));
		}
		// 操作时间止
		if (!CommonUtils.isNullOrEmpty(filter.end_date)) {
			condition.append(" and date_format(acd.monitor_time,'%Y-%m-%d %H:%i') <= ");
			condition.append(" date_format(str_to_date(?,'%Y-%m-%d %H:%i'),'%Y-%m-%d %H:%i') ");
			params.add(CommonUtils.trim(filter.end_date));
		}

		Object boxId = bParams.get("boxId");
		Object alarmType = bParams.get("alarmType");
		Object alarmLevel = bParams.get("alarmLevel");
		if (null != boxId) {
			condition.append(" and ac.device_id = ? ");
			params.add(boxId);
		}
		if(null != alarmType){
			condition.append(" and acd.alarm_type = ? ");
			params.add(alarmType);
		}
		if(null != alarmLevel){
			condition.append(" and ac.alarm_level = ? ");
			params.add(alarmLevel);
		}
		String sqlCount = "select count(0) " + fromStr + whereStr;
		String sql = " select " + SEL_COL + ",ac.name, ac.text,ac.alarm_level " + fromStr + whereStr;
		sqlCount += condition;
		int totalRecord = jdbcTemplate.queryForObject(sqlCount, params.toArray(), Integer.class);
		Page<AlarmCfgDataAlarmCfg> page = new Page<AlarmCfgDataAlarmCfg>(pageIndex, pageSize, totalRecord);
		String sort = " order by alarm_cfg_id desc, monitor_time desc";
		sql += condition + sort + " limit " + page.getStartIndex() + "," + page.getPageSize();
		List<AlarmCfgDataAlarmCfg> list = jdbcTemplate.query(sql, params.toArray(),
				new DefaultAlarmCfgDataAlarmCfgRowMapper());
		page.setList(list);
		return page;
	}

	@Override
	public Page<AlarmCfgDataAlarmCfg> getAdminAlarmCfgDataPage(AlarmCfgDataFilter filter, Map<String, Object> bParams,
			int pageIndex, int pageSize) {
		String fromStr = " from alarm_cfg ac, alarm_cfg_data acd ";
		String whereStr = " where ac.alarmcfg_id=acd.alarm_cfg_id and ac.bind_state=1 ";
		StringBuffer condition = new StringBuffer("");
		List<Object> params = new ArrayList<Object>();
		if (filter.account_id > 0) {
			condition.append(" and ac.account_id = ? ");
			params.add(filter.account_id);
		}

		if (filter.state > -1) {
			condition.append(" and acd.state = ? ");
			params.add(filter.state);
		}

		// 操作时间起
		if (!CommonUtils.isNullOrEmpty(filter.start_date)) {
			condition.append(" and date_format(acd.monitor_time,'%Y-%m-%d %H:%i') >= ");
			condition.append(" date_format(str_to_date(?,'%Y-%m-%d %H:%i'),'%Y-%m-%d %H:%i') ");
			params.add(CommonUtils.trim(filter.start_date));

		}
		// 操作时间止
		if (!CommonUtils.isNullOrEmpty(filter.end_date)) {
			condition.append(" and date_format(acd.monitor_time,'%Y-%m-%d %H:%i') <= ");
			condition.append(" date_format(str_to_date(?,'%Y-%m-%d %H:%i'),'%Y-%m-%d %H:%i') ");
			params.add(CommonUtils.trim(filter.end_date));
		}

		Object boxId = bParams.get("boxId");
		Object alarmType = bParams.get("alarmType");
		Object alarmLevel = bParams.get("alarmLevel");
		if (null != boxId) {
			condition.append(" and ac.device_id = ? ");
			params.add(boxId);
		}
		if(null != alarmType){
			condition.append(" and acd.alarm_type = ? ");
			params.add(alarmType);
		}
		if(null != alarmLevel){
			condition.append(" and ac.alarm_level = ? ");
			params.add(alarmLevel);
		}
		String sqlCount = "select count(0) " + fromStr + whereStr;
		String sql = " select " + SEL_COL + ",ac.name,ac.text,ac.alarm_level " + fromStr + whereStr;
		sqlCount += condition;
		int totalRecord = jdbcTemplate.queryForObject(sqlCount, params.toArray(), Integer.class);
		Page<AlarmCfgDataAlarmCfg> page = new Page<AlarmCfgDataAlarmCfg>(pageIndex, pageSize, totalRecord);
		String sort = " order by alarm_cfg_id desc, monitor_time desc";
		sql += condition + sort + " limit " + page.getStartIndex() + "," + page.getPageSize();
		List<AlarmCfgDataAlarmCfg> list = jdbcTemplate.query(sql, params.toArray(),
				new DefaultAlarmCfgDataAlarmCfgRowMapper());
		page.setList(list);
		return page;

	}

	public static final class DefaultAlarmCfgDataRowMapper implements RowMapper<AlarmCfgData> {

		@Override
		public AlarmCfgData mapRow(ResultSet rs, int i) throws SQLException {
			AlarmCfgData model = new AlarmCfgData();
			model.alarm_cfg_id = rs.getLong("alarm_cfg_id");
			model.monitor_time = rs.getTimestamp("monitor_time");
			model.value = rs.getString("value");
			model.create_date = rs.getTimestamp("create_date");
			model.state = rs.getInt("state");
			if(!CommonUtils.isNullOrEmpty(rs.getObject("alarm_type"))){
				model.alarm_type = rs.getInt("alarm_type");
			}else{
				model.alarm_type = -1;
			}

			return model;
		}
	}

	public static final class DefaultAlarmCfgDataAlarmCfgRowMapper implements RowMapper<AlarmCfgDataAlarmCfg> {

		@Override
		public AlarmCfgDataAlarmCfg mapRow(ResultSet rs, int i) throws SQLException {
			AlarmCfgDataAlarmCfg model = new AlarmCfgDataAlarmCfg();
			model.alarm_cfg_id = rs.getLong("alarm_cfg_id");
			model.monitor_time = rs.getTimestamp("monitor_time");
			model.value = rs.getString("value");
			model.create_date = rs.getTimestamp("create_date");
			model.state = rs.getInt("state");
			model.alarm_level = rs.getInt("alarm_level");
			model.name = rs.getString("name");
			model.text = rs.getString("text");
			if(!CommonUtils.isNullOrEmpty(rs.getObject("alarm_type"))){
				model.alarm_type = rs.getInt("alarm_type");
			}else{
				model.alarm_type = -1;
			}
			return model;
		}
	}

}
