package com.wecon.box.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import com.wecon.box.api.AlarmTriggerApi;
import com.wecon.box.entity.AlarmTrigger;
import com.wecon.box.filter.AlarmTriggerFilter;
import com.wecon.common.util.CommonUtils;

/**
 * @author lanpenghui 2017年8月9日下午2:31:37
 */
@Component
public class AlarmTriggerImpl implements AlarmTriggerApi {
	@Autowired
	private JdbcTemplate jdbcTemplate;
	private final String SEL_COL = "alarmtrig_id,alarmcfg_id,type,value,create_date,update_date";

	@Override
	public void saveAlarmTrigger(final List<AlarmTrigger> listalarmTrigger) {

		String sql = "insert into alarm_trigger (alarmcfg_id,type,value,create_date,update_date)values(?,?,?,current_timestamp(),current_timestamp());";
		jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				ps.setLong(1, listalarmTrigger.get(i).alarmcfg_id);
				ps.setInt(2, listalarmTrigger.get(i).type);
				ps.setString(3, listalarmTrigger.get(i).value);
			}

			@Override
			public int getBatchSize() {
				return listalarmTrigger.size();
			}
		});
	}

	@Override
	public List<AlarmTrigger> getAlarmTrigger(AlarmTriggerFilter filter) {

		String sql = "select " + SEL_COL + " from alarm_trigger where 1=1";
		StringBuffer condition = new StringBuffer("");
		List<Object> params = new ArrayList<Object>();
		if (filter.alarmtrig_id > 0) {
			condition.append(" and alarmtrig_id = ? ");
			params.add(filter.alarmtrig_id);
		}
		if (filter.alarmcfg_id > 0) {
			condition.append(" and alarmcfg_id = ? ");
			params.add(filter.alarmcfg_id);
		}
		if (filter.type > -1) {
			condition.append(" and type = ? ");
			params.add(filter.type);
		}
		if (!CommonUtils.isNullOrEmpty(filter.value)) {
			condition.append(" and value like ? ");
			params.add("%" + filter.value + "%");

		}
		sql += condition;
		List<AlarmTrigger> list = jdbcTemplate.query(sql, params.toArray(), new DefaultAlarmTriggerRowMapper());
		return list;

	}

	@Override
	public AlarmTrigger getAlarmTrigger(long alarmtrig_id) {

		String sql = "select" + SEL_COL + "from alarm_trigger where alarmtrig_id=?";

		List<AlarmTrigger> list = jdbcTemplate.query(sql, new Object[] { alarmtrig_id },
				new DefaultAlarmTriggerRowMapper());
		if (!list.isEmpty()) {
			return list.get(0);
		}

		return null;
	}

	@Override
	public boolean upAlarmTrigger(AlarmTrigger alarmTrigger) {
		if (alarmTrigger == null) {
			return false;
		}
		String sql = "update alarm_trigger set alarmcfg_id=?,type=?,value=?,update_date=current_timestamp() where alarmtrig_id = ?";

		jdbcTemplate.update(sql, new Object[] { alarmTrigger.alarmcfg_id, alarmTrigger.type, alarmTrigger.value,
				alarmTrigger.alarmtrig_id });

		return true;
	}

	public static final class DefaultAlarmTriggerRowMapper implements RowMapper<AlarmTrigger> {

		@Override
		public AlarmTrigger mapRow(ResultSet rs, int i) throws SQLException {
			AlarmTrigger model = new AlarmTrigger();
			model.alarmtrig_id = rs.getLong("alarmtrig_id");
			model.alarmcfg_id = rs.getLong("alarmcfg_id");
			model.type = rs.getInt("type");
			model.value = rs.getString("value");
			model.create_date = rs.getTimestamp("create_date");
			model.update_date = rs.getTimestamp("update_date");

			return model;
		}
	}

	@Override
	public void delAlarmTrigger(long alarmcfg_id) {
		String sql = "delete from  alarm_trigger where alarmcfg_id=?";
		jdbcTemplate.update(sql, new Object[] { alarmcfg_id });

	}

}
