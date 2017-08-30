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
	private final String SEL_COL = "acd.alarm_cfg_id,acd.monitor_time,acd.value,acd.create_date,acd.state";

	@Override
	public void saveAlarmCfgData(final List<AlarmCfgData> listmodel) {
		String sql = "insert into alarm_cfg_data (alarm_cfg_id,monitor_time,`value`,create_date,state)values(?,?,?,current_timestamp(),?);";
		jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				ps.setLong(1, listmodel.get(i).alarm_cfg_id);
				ps.setTimestamp(2, listmodel.get(i).monitor_time);
				ps.setString(3, listmodel.get(i).value);
				ps.setInt(4, listmodel.get(i).state);
			}

			@Override
			public int getBatchSize() {
				return listmodel.size();
			}
		});

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
	public Page<AlarmCfgDataAlarmCfg> getRealHisCfgDataList(AlarmCfgDataFilter filter, int pageIndex, int pageSize) {
		String sqlCount = "select count(0) from alarm_cfg ac ,alarm_cfg_data acd,alarm_trigger atr,plc_info pli where 1=1 and  ac.alarmcfg_id=atr.alarmcfg_id and  ac.alarmcfg_id=acd.alarm_cfg_id and ac.bind_state=1 and pli.`plc_id`=ac.plc_id";
		String sql = " select " + SEL_COL + ",ac.name,ac.text "
				+ " from alarm_cfg ac ,alarm_cfg_data acd,alarm_trigger atr,plc_info pli where 1=1 and  ac.alarmcfg_id=atr.alarmcfg_id and  ac.alarmcfg_id=acd.alarm_cfg_id and ac.bind_state=1 and pli.`plc_id`=ac.plc_id";

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
		String sort = " order by alarm_cfg_id desc";
		sql += condition + sort + " limit " + page.getStartIndex() + "," + page.getPageSize();
		List<AlarmCfgDataAlarmCfg> list = jdbcTemplate.query(sql, params.toArray(),
				new DefaultAlarmCfgDataAlarmCfgRowMapper());
		page.setList(list);
		return page;

	}

	@Override
	public Page<AlarmCfgDataAlarmCfg> getViewRealHisCfgDataList(AlarmCfgDataFilter filter, int pageIndex,
			int pageSize) {
		String sqlCount = "select count(0) from view_account_role var,alarm_cfg ac,alarm_trigger atr ,alarm_cfg_data  acd,plc_info pli where 1=1 and ac.alarmcfg_id=var.cfg_id and ac.alarmcfg_id=atr.alarmcfg_id and ac.alarmcfg_id=acd.alarm_cfg_id and var.cfg_type=2 and ac.bind_state=1 and pli.`plc_id`=ac.plc_id";
		String sql = " select " + SEL_COL + ",ac.name,ac.text "
				+ " from view_account_role var,alarm_cfg ac,alarm_trigger atr ,alarm_cfg_data  acd,plc_info pli where 1=1 and ac.alarmcfg_id=var.cfg_id and ac.alarmcfg_id=atr.alarmcfg_id and ac.alarmcfg_id=acd.alarm_cfg_id and var.cfg_type=2 and ac.bind_state=1 and pli.`plc_id`=ac.plc_id";

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
		String sort = " order by alarm_cfg_id desc";
		sql += condition + sort + " limit " + page.getStartIndex() + "," + page.getPageSize();
		List<AlarmCfgDataAlarmCfg> list = jdbcTemplate.query(sql, params.toArray(),
				new DefaultAlarmCfgDataAlarmCfgRowMapper());
		page.setList(list);
		return page;
	}

	@Override
	public Page<AlarmCfgDataAlarmCfg> getViewAlarmCfgDataPage(AlarmCfgDataFilter filter, Map<String, Object> bParams,
			int pageIndex, int pageSize) {
		String fromStr = "from view_account_role var,alarm_cfg ac,alarm_trigger atr ,alarm_cfg_data  acd";
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
		Object groupId = bParams.get("groupId");
		if (null != groupId) {
			fromStr += ", account_dir_rel f";
			condition.append(" and ac.alarmcfg_id=f.ref_id and f.acc_dir_id = ?");
			params.add(groupId);
		}
		String sqlCount = "select count(0) " + fromStr
				+ " where 1=1 and ac.alarmcfg_id=var.cfg_id and ac.alarmcfg_id=atr.alarmcfg_id and ac.alarmcfg_id=acd.alarm_cfg_id and var.cfg_type=2 ";
		String sql = " select " + SEL_COL + ",ac.name,ac.text " + fromStr
				+ " where 1=1 and ac.alarmcfg_id=var.cfg_id and ac.alarmcfg_id=atr.alarmcfg_id and ac.alarmcfg_id=acd.alarm_cfg_id and var.cfg_type=2 ";
		sqlCount += condition;
		int totalRecord = jdbcTemplate.queryForObject(sqlCount, params.toArray(), Integer.class);
		Page<AlarmCfgDataAlarmCfg> page = new Page<AlarmCfgDataAlarmCfg>(pageIndex, pageSize, totalRecord);
		String sort = " order by alarm_cfg_id desc";
		sql += condition + sort + " limit " + page.getStartIndex() + "," + page.getPageSize();
		List<AlarmCfgDataAlarmCfg> list = jdbcTemplate.query(sql, params.toArray(),
				new DefaultAlarmCfgDataAlarmCfgRowMapper());
		page.setList(list);
		return page;
	}

	@Override
	public Page<AlarmCfgDataAlarmCfg> getAdminAlarmCfgDataPage(AlarmCfgDataFilter filter, Map<String, Object> bParams,
			int pageIndex, int pageSize) {
		String fromStr = "from alarm_cfg ac ,alarm_cfg_data acd,alarm_trigger atr";
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
		Object groupId = bParams.get("groupId");
		if (null != boxId) {
			fromStr += ",device d, plc_info p";
			condition.append(" and d.device_id=p.device_id and p.plc_id=ac.plc_id and d.device_id = ? ");
			params.add(boxId);
		}
		if (null != groupId) {
			fromStr += ", account_dir_rel f";
			condition.append(" and ac.alarmcfg_id=f.ref_id and f.acc_dir_id = ?");
			params.add(groupId);
		}
		String sqlCount = "select count(0) " + fromStr
				+ " where 1=1 and  ac.alarmcfg_id=atr.alarmcfg_id and  ac.alarmcfg_id=acd.alarm_cfg_id";
		String sql = " select " + SEL_COL + ",ac.name,ac.text " + fromStr
				+ " where 1=1 and  ac.alarmcfg_id=atr.alarmcfg_id and  ac.alarmcfg_id=acd.alarm_cfg_id ";
		sqlCount += condition;
		int totalRecord = jdbcTemplate.queryForObject(sqlCount, params.toArray(), Integer.class);
		Page<AlarmCfgDataAlarmCfg> page = new Page<AlarmCfgDataAlarmCfg>(pageIndex, pageSize, totalRecord);
		String sort = " order by alarm_cfg_id desc";
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
			model.name = rs.getString("name");
			model.text = rs.getString("text");

			return model;
		}
	}

}
