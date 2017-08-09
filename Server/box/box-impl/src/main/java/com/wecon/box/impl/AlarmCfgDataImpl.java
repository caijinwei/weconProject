package com.wecon.box.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import com.wecon.box.api.AlarmCfgDataApi;
import com.wecon.box.entity.AlarmCfgData;
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
	private final String SEL_COL = "alarm_cfg_id,monitor_time,`value`,create_date,state";

	@Override
	public long saveAlarmCfgData(final AlarmCfgData model) {
		KeyHolder key = new GeneratedKeyHolder();
		jdbcTemplate.update(new PreparedStatementCreator() {
			@Override
			public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
				PreparedStatement preState = con.prepareStatement(
						"insert into alarm_cfg_data (alarm_cfg_id,monitor_time,`value`,create_date,state)values(?,?,?,current_timestamp(),?);",
						Statement.RETURN_GENERATED_KEYS);
				preState.setLong(1, model.alarm_cfg_id);
				preState.setTimestamp(2, model.monitor_time);
				preState.setString(3, model.value);
				preState.setInt(4, model.state);
				return preState;
			}
		}, key);
		// 从主键持有者中获得主键值
		return key.getKey().longValue();
	}

	@Override
	public void SaveAlarmCfgData(final List<AlarmCfgData> listmodel) {
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
	public List<AlarmCfgData> getAlarmCfgData(long alarm_cfg_id) {
		String sql = "select " + SEL_COL + " from alarm_cfg_data where alarm_cfg_id=?";
		List<AlarmCfgData> list = jdbcTemplate.query(sql, new Object[] { alarm_cfg_id },
				new DefaultAlarmCfgDataRowMapper());
		if (!list.isEmpty()) {
			return list;
		}

		return null;

	}

	@Override
	public AlarmCfgData getAlarmCfgData(long alarm_cfg_id, Timestamp monitor_time) {
		String sql = "select " + SEL_COL + " from alarm_cfg_data where alarm_cfg_id=? and monitor_time=?";
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
	public Page<AlarmCfgData> getRealHisCfgDataList(AlarmCfgDataFilter filter, int pageIndex, int pageSize) {
		String sqlCount = "select count(0) from alarm_cfg_data where 1=1";
		String sql = "select " + SEL_COL + " from alarm_cfg_data where 1=1";
		StringBuffer condition = new StringBuffer("");
		List<Object> params = new ArrayList<Object>();
		if (filter.alarm_cfg_id>0) {
			condition.append(" and alarm_cfg_id = ? ");
			params.add(filter.alarm_cfg_id);
		}
		if (!CommonUtils.isNullOrEmpty(filter.value)) {
			condition.append(" and value like ? ");
			params.add("%" + filter.value + "%");
		}
		if (filter.state>-1) {
			condition.append(" and state = ? ");
			params.add(filter.state);
		}
		// 操作时间起
		if (!CommonUtils.isNullOrEmpty(filter.start_date)) {
			condition.append(" and date_format(alarm_cfg_data.monitor_time,'%Y-%m-%d %H:%i') >= ");
			condition.append(" date_format(str_to_date(?,'%Y-%m-%d %H:%i'),'%Y-%m-%d %H:%i') ");
			params.add(CommonUtils.trim(filter.start_date));

		}
		// 操作时间止
		if (!CommonUtils.isNullOrEmpty(filter.end_date)) {
			condition.append(" and date_format(alarm_cfg_data.monitor_time,'%Y-%m-%d %H:%i') <= ");
			condition.append(" date_format(str_to_date(?,'%Y-%m-%d %H:%i'),'%Y-%m-%d %H:%i') ");
			params.add(CommonUtils.trim(filter.end_date));

		}
		sqlCount += condition;
		int totalRecord = jdbcTemplate.queryForObject(sqlCount, params.toArray(), Integer.class);
		Page<AlarmCfgData> page = new Page<AlarmCfgData>(pageIndex, pageSize, totalRecord);
		String sort = " order by alarm_cfg_id desc";
		sql += condition + sort + " limit " + page.getStartIndex() + "," + page.getPageSize();
		List<AlarmCfgData> list = jdbcTemplate.query(sql, params.toArray(), new DefaultAlarmCfgDataRowMapper());
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

}
