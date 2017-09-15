package com.wecon.box.impl;

import com.wecon.box.api.RealHisCfgDataApi;
import com.wecon.box.entity.Page;
import com.wecon.box.entity.RealHisCfgData;
import com.wecon.box.entity.RealHisCfgDevice;
import com.wecon.box.filter.RealHisCfgDataFilter;
import com.wecon.box.filter.RealHisCfgFilter;
import com.wecon.box.filter.ViewAccountRoleFilter;
import com.wecon.common.util.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lanpenghui 2017年8月2日
 */
@Component
public class RealHisCfgDataImpl implements RealHisCfgDataApi {
	@Autowired
	private JdbcTemplate jdbcTemplate;
	private final String SEL_COL = "real_his_cfg_id,monitor_time,`value`,create_date,state";

	@Override
	public long saveRealHisCfgData(final RealHisCfgData model) {
		KeyHolder key = new GeneratedKeyHolder();
		jdbcTemplate.update(new PreparedStatementCreator() {
			@Override
			public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
				PreparedStatement preState = con.prepareStatement(
						"insert into real_his_cfg_data (real_his_cfg_id,monitor_time,`value`,create_date,state)values(?,?,?,current_timestamp(),?);",
						Statement.RETURN_GENERATED_KEYS);
				preState.setLong(1, model.real_his_cfg_id);
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
	public void saveRealHisCfgData(final List<RealHisCfgData> listmodel) {
		String sql = "insert into real_his_cfg_data (real_his_cfg_id,monitor_time,`value`,create_date,state)values(?,?,?,current_timestamp(),?);";
		jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				ps.setLong(1, listmodel.get(i).real_his_cfg_id);
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
	public RealHisCfgData getRealHisCfgData(long real_his_cfg_id, Timestamp monitor_time) {

		String sql = "select " + SEL_COL + " from real_his_cfg_data where real_his_cfg_id=? and monitor_time=?";
		List<RealHisCfgData> list = jdbcTemplate.query(sql, new Object[] { real_his_cfg_id, monitor_time },
				new DefaultRealHisCfgDataRowMapper());
		if (!list.isEmpty()) {
			return list.get(0);
		}

		return null;
	}

	@Override
	public void delRealHisCfgData(long real_his_cfg_id) {
		String sql = "delete from  real_his_cfg_data  where real_his_cfg_id=?";
		jdbcTemplate.update(sql, new Object[] { real_his_cfg_id });
	}

	@Override
	public boolean batchDeleteByPlcId(List<Long> ids) {
		if (null == ids || ids.size() == 0) {
			return false;
		}

		StringBuilder idSb = new StringBuilder();
		for(long id : ids){
			idSb.append(",").append(id);
		}
		String sql = "delete from real_his_cfg_data where real_his_cfg_id in(select id from real_his_cfg where plc_id in("+idSb.substring(1)+"))";
		jdbcTemplate.update(sql);

		return true;
	}

	@Override
	public boolean batchDeleteById(final List<Integer> ids) {
		if (null == ids || ids.size() == 0) {
			return false;
		}

		StringBuilder idSb = new StringBuilder();
		for(int id : ids){
			idSb.append(",").append(id);
		}
		String sql = "delete from real_his_cfg_data where real_his_cfg_id in("+idSb.substring(1)+")";
		jdbcTemplate.update(sql);

		return true;
	}

	@Override
	public Page<RealHisCfgData> getRealHisCfgDataList(RealHisCfgDataFilter filter, int pageIndex, int pageSize) {
		String sqlCount = "select count(0) from real_his_cfg_data where 1=1";
		String sql = "select " + SEL_COL + " from real_his_cfg_data where 1=1";
		StringBuffer condition = new StringBuffer("");
		List<Object> params = new ArrayList<Object>();
		if (filter.real_his_cfg_id > 0) {
			condition.append(" and real_his_cfg_id = ? ");
			params.add(filter.real_his_cfg_id);
		}
		if (!CommonUtils.isNullOrEmpty(filter.value)) {
			condition.append(" and value like ? ");
			params.add("%" + filter.value + "%");
		}
		if (filter.state > -1) {
			condition.append(" and state = ? ");
			params.add(filter.state);
		}
		// 操作时间起
		if (!CommonUtils.isNullOrEmpty(filter.start_date)) {
			condition.append(" and date_format(real_his_cfg_data.monitor_time,'%Y-%m-%d %H:%i') >= ");
			condition.append(" date_format(str_to_date(?,'%Y-%m-%d %H:%i'),'%Y-%m-%d %H:%i') ");
			params.add(CommonUtils.trim(filter.start_date));

		}
		// 操作时间止
		if (!CommonUtils.isNullOrEmpty(filter.end_date)) {
			condition.append(" and date_format(real_his_cfg_data.monitor_time,'%Y-%m-%d %H:%i') <=  ");
			condition.append(" date_format(str_to_date(?,'%Y-%m-%d %H:%i'),'%Y-%m-%d %H:%i') ");
			params.add(CommonUtils.trim(filter.end_date));

		}
		sqlCount += condition;
		int totalRecord = jdbcTemplate.queryForObject(sqlCount, params.toArray(), Integer.class);
		Page<RealHisCfgData> page = new Page<RealHisCfgData>(pageIndex, pageSize, totalRecord);
		String sort = " order by real_his_cfg_id desc";
		sql += condition + sort + " limit " + page.getStartIndex() + "," + page.getPageSize();
		List<RealHisCfgData> list = jdbcTemplate.query(sql, params.toArray(), new DefaultRealHisCfgDataRowMapper());
		page.setList(list);
		return page;

	}

	@Override
	public Page<Map<String, Object>> getRealHisCfgDataPage(RealHisCfgFilter filter, Map<String, Object> bParams, int pageIndex, int pageSize) {
		String fromStr = "from real_his_cfg_data rd, real_his_cfg r";
		String whereStr = " where rd.real_his_cfg_id=r.id ";
		StringBuffer condition = new StringBuffer();
		List<Object> params = new ArrayList<Object>();

		if (filter.account_id > 0) {
			condition.append(" and r.account_id = ? ");
			params.add(filter.account_id);
		}
		if (filter.data_type > -1) {
			condition.append(" and r.data_type = ? ");
			params.add(filter.data_type);
		}
		// 操作时间起
		if (!CommonUtils.isNullOrEmpty(bParams.get("monitorBeginTime"))) {
			condition.append(" and date_format(real_his_cfg_data.monitor_time,'%Y-%m-%d %H:%i') >= ");
			condition.append(" date_format(str_to_date(?,'%Y-%m-%d %H:%i'),'%Y-%m-%d %H:%i') ");
			params.add(CommonUtils.trim(bParams.get("monitorBeginTime").toString()));

		}
		// 操作时间止
		if (!CommonUtils.isNullOrEmpty(bParams.get("monitorEndTime"))) {
			condition.append(" and date_format(real_his_cfg_data.monitor_time,'%Y-%m-%d %H:%i') <=  ");
			condition.append(" date_format(str_to_date(?,'%Y-%m-%d %H:%i'),'%Y-%m-%d %H:%i') ");
			params.add(CommonUtils.trim(bParams.get("monitorEndTime").toString()));
		}

		Object boxId = bParams.get("boxId");
		Object monitorId = bParams.get("monitorId");
		if(null != boxId){
			condition.append(" and r.device_id = ? ");
			params.add(boxId);
		}
		if(null != monitorId){
			condition.append(" and rd.real_his_cfg_id = ?");
			params.add(monitorId);
		}
		String sqlCount = "select count(distinct r.id) " + fromStr + whereStr;
		String sql = "select distinct r.id, r.name, rd.value, rd.monitor_time" + "  "+fromStr + whereStr;
		sqlCount += condition;
		int totalRecord = jdbcTemplate.queryForObject(sqlCount, params.toArray(), Integer.class);
		Page<Map<String, Object>> page = new Page<Map<String, Object>>(pageIndex, pageSize, totalRecord);
		String sort = " order by r.id desc";
		sql += condition + sort + " limit " + page.getStartIndex() + "," + page.getPageSize();
		List<Map<String, Object>> list = jdbcTemplate.query(sql, params.toArray(), new DefaultRealHisCfgMapRowMapper());
		page.setList(list);
		return page;

	}

	@Override
	public Page<Map<String, Object>> getRealHisCfgDataPage(ViewAccountRoleFilter filter, Map<String, Object> bParams, int pageIndex, int pageSize) {
		String fromStr = " from real_his_cfg_data rd, real_his_cfg r, view_account_role v ";
		String whereStr = " where v.cfg_id=r.id and v.cfg_type=1 and rd.real_his_cfg_id=r.id ";
		StringBuffer condition = new StringBuffer();
		List<Object> params = new ArrayList<Object>();

		if (filter.view_id > 0) {
			condition.append(" and v.view_id = ? ");
			params.add(filter.view_id);
		}

		if (filter.data_type > -1) {
			condition.append(" and r.data_type = ? ");
			params.add(filter.data_type);
		}

		// 操作时间起
		if (!CommonUtils.isNullOrEmpty(bParams.get("monitorBeginTime"))) {
			condition.append(" and date_format(rd.monitor_time,'%Y-%m-%d %H:%i') >= ");
			condition.append(" date_format(str_to_date(?,'%Y-%m-%d %H:%i'),'%Y-%m-%d %H:%i') ");
			params.add(CommonUtils.trim(bParams.get("monitorBeginTime").toString()));

		}
		// 操作时间止
		if (!CommonUtils.isNullOrEmpty(bParams.get("monitorEndTime"))) {
			condition.append(" and date_format(rd.monitor_time,'%Y-%m-%d %H:%i') <=  ");
			condition.append(" date_format(str_to_date(?,'%Y-%m-%d %H:%i'),'%Y-%m-%d %H:%i') ");
			params.add(CommonUtils.trim(bParams.get("monitorEndTime").toString()));

		}

		Object boxId = bParams.get("boxId");
		Object monitorId = bParams.get("monitorId");
		if(null != boxId){
			condition.append(" and r.device_id = ? ");
			params.add(boxId);
		}
		if(null != monitorId){
			condition.append(" and rd.real_his_cfg_id = ?");
			params.add(monitorId);
		}
		String sqlCount = "select count(distinct r.id) " + fromStr + whereStr;
		String sql = "select distinct r.id, r.name, rd.value, rd.monitor_time" + fromStr+ whereStr;
		sqlCount += condition;
		int totalRecord = jdbcTemplate.queryForObject(sqlCount, params.toArray(), Integer.class);
		Page<Map<String, Object>> page = new Page<Map<String, Object>>(pageIndex, pageSize, totalRecord);
		String sort = " order by r.id desc";
		sql += condition + sort + " limit " + page.getStartIndex() + "," + page.getPageSize();
		List<Map<String, Object>> list = jdbcTemplate.query(sql, params.toArray(), new DefaultRealHisCfgMapRowMapper());
		page.setList(list);
		return page;

	}

	public static final class DefaultRealHisCfgDataRowMapper implements RowMapper<RealHisCfgData> {

		@Override
		public RealHisCfgData mapRow(ResultSet rs, int i) throws SQLException {
			RealHisCfgData model = new RealHisCfgData();
			model.real_his_cfg_id = rs.getLong("real_his_cfg_id");
			model.monitor_time = rs.getTimestamp("monitor_time");
			model.value = rs.getString("value"); 
			model.create_date = rs.getTimestamp("create_date");
			model.state = rs.getInt("state");

			return model;
		}
	}

	public static final class DefaultRealHisCfgMapRowMapper implements RowMapper<Map<String, Object>> {
		@Override
		public Map<String, Object> mapRow(ResultSet rs, int i) throws SQLException {
			Map<String, Object> row = new HashMap<String, Object>();
			row.put("monitorId", rs.getString("id"));
			row.put("monitorName", rs.getString("name"));
			row.put("number", rs.getString("value"));
			row.put("monitorTime", rs.getTimestamp("monitor_time"));
			return row;
		}
	}

}
