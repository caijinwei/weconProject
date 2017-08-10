package com.wecon.box.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
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

}
