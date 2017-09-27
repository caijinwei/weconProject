package com.wecon.box.impl;

import com.wecon.box.api.AlarmCfgApi;

import com.wecon.box.entity.AlarmCfg;
import com.wecon.box.entity.AlarmCfgExtend;
import com.wecon.box.entity.AlarmCfgTrigger;
import com.wecon.box.entity.AlarmTrigger;
import com.wecon.box.entity.Page;
import com.wecon.common.util.TimeUtil;
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
import java.util.List;

/**
 * @author lanpenghui 2017年8月9日下午2:20:05
 */
@Component
public class AlarmCfgImpl implements AlarmCfgApi {
	@Autowired
	private JdbcTemplate jdbcTemplate;

	private final String SEL_COL = "alarmcfg_id,data_id,account_id,name,addr,addr_type,text,condition_type,state,plc_id,device_id,rid,data_limit,digit_count,digit_binary,bind_state,create_date,update_date";

	@Override
	public long saveAlarmCfg(final AlarmCfg alarmCfg) {

		KeyHolder key = new GeneratedKeyHolder();
		jdbcTemplate.update(new PreparedStatementCreator() {
			@Override
			public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
				PreparedStatement preState = con.prepareStatement(
						"insert into alarm_cfg(data_id,account_id,plc_id,`name`,addr,addr_type,text,condition_type,state,device_id,rid,data_limit,digit_count,digit_binary,bind_state,create_date,update_date)values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,1,current_timestamp(),current_timestamp());",
						Statement.RETURN_GENERATED_KEYS);
				preState.setLong(1, alarmCfg.data_id);
				preState.setLong(2, alarmCfg.account_id);
				preState.setLong(3, alarmCfg.plc_id);
				preState.setString(4, alarmCfg.name);
				preState.setString(5, alarmCfg.addr);
				preState.setInt(6, alarmCfg.addr_type);
				preState.setString(7, alarmCfg.text);
				preState.setInt(8, alarmCfg.condition_type);
				preState.setInt(9, alarmCfg.state);
				preState.setLong(10, alarmCfg.device_id);
				preState.setString(11, alarmCfg.rid);
				preState.setString(12, alarmCfg.data_limit);
				preState.setString(13, alarmCfg.digit_count);
				preState.setString(14, alarmCfg.digit_binary);
				return preState;
			}
		}, key);
		// 从主键持有者中获得主键值
		return key.getKey().longValue();
	}

	@Override
	public List<AlarmCfg> getAlarmCfg(long account_id) {
		String sql = "select " + SEL_COL + " from alarm_cfg a where account_id=?";
		List<AlarmCfg> list = jdbcTemplate.query(sql, new Object[] { account_id }, new DefaultAlarmCfgRowMapper());
		if (!list.isEmpty()) {
			return list;
		}

		return null;

	}

	@SuppressWarnings("unchecked")
	@Override
	public Page<AlarmCfgTrigger> getAlarmCfgDataList(long account_id, long groupId, long device_id,long bind_state, int pageIndex,
			int pageSize) {
		String sqlCount = "select count(0) from `alarm_cfg` ac,`account_dir` ad,`account_dir_rel` adr where 1=1 and ac.`alarmcfg_id`=adr.`ref_id` and ad.`id`=adr.`acc_dir_id` and ad.`type`=3 and ac.`account_id`=ad.`account_id` AND  ac.`device_id`=ad.`device_id` ";

		String sql = " select ac.alarmcfg_id,ac.data_id,ac.account_id,ac.name,ac.addr,ac.addr_type,ac.text,ac.condition_type,ac.state,ac.device_id,ac.rid,ac.bind_state,ac.plc_id,ac.data_limit,ac.digit_count,ac.digit_binary,ac.create_date,ac.update_date,adr.`ref_alais`,ad.`name` dirname,ad.`id` from `alarm_cfg` ac,`account_dir` ad,`account_dir_rel` adr where 1=1 and ac.`alarmcfg_id`=adr.`ref_id` and ad.`id`=adr.`acc_dir_id` and ad.`type`=3 and ac.`account_id`=ad.`account_id` and  ac.`device_id`=ad.`device_id`";

		StringBuffer condition = new StringBuffer("");
		List<Object> params = new ArrayList<Object>();
		if (account_id > 0) {
			condition.append(" and ac.`account_id`=? ");
			params.add(account_id);
		}
		if (device_id > 0) {
			condition.append(" and ac.`device_id`=? ");
			params.add(device_id);
		}
		if (groupId > 0) {
			condition.append(" and ad.`id`=? ");
			params.add(groupId);
		}
		if (bind_state > -1) {
			condition.append(" and ac.bind_state=? ");
			params.add(bind_state);
		}
		sqlCount += condition;
		int totalRecord = jdbcTemplate.queryForObject(sqlCount, params.toArray(), Integer.class);
		Page<AlarmCfgTrigger> page = new Page<AlarmCfgTrigger>(pageIndex, pageSize, totalRecord);
		String sort = " order by  ac.`alarmcfg_id` desc";
		sql += condition + sort + " limit " + page.getStartIndex() + "," + page.getPageSize();

		@SuppressWarnings("rawtypes")
		List<AlarmCfgTrigger> list = jdbcTemplate.query(sql, params.toArray(), new RowMapper() {

			@Override
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {

				AlarmCfgTrigger model = new AlarmCfgTrigger();
				model.alarmcfg_id = rs.getLong("alarmcfg_id");
				model.data_id = rs.getLong("data_id");
				model.account_id = rs.getLong("account_id");
				model.device_id = rs.getLong("device_id");
				model.name = rs.getString("name");
				model.addr = rs.getString("addr");
				model.addr_type = rs.getInt("addr_type");
				model.text = rs.getString("text");
				model.state = rs.getInt("state");
				model.dirId = rs.getLong("id");
				model.dirName = rs.getString("dirname");
				model.alais = rs.getString("ref_alais");
				model.rid = rs.getString("rid");
				model.plc_id = rs.getLong("plc_id");
				model.data_limit=rs.getString("data_limit");
				model.digit_count=rs.getString("digit_count");
				model.digit_binary=rs.getString("digit_binary");
				model.bind_state = rs.getInt("bind_state");
				model.condition_type = rs.getInt("condition_type");
				model.create_date = rs.getTimestamp("create_date");
				model.update_date = rs.getTimestamp("update_date");

				return model;
			}

		});
		page.setList(list);
		return page;

	}
	@SuppressWarnings("unchecked")
	@Override
	public Page<AlarmCfgTrigger> getAlarmList(long device_id, long bind_state, int pageIndex, int pageSize) {
		String sqlCount = "select count(0) from alarm_cfg where 1=1 ";

		String sql = " select "+ SEL_COL +" from alarm_cfg where 1=1 ";

		StringBuffer condition = new StringBuffer("");
		List<Object> params = new ArrayList<Object>();
		
		if (device_id > 0) {
			condition.append(" and device_id=? ");
			params.add(device_id);
	
		}
		if (bind_state > -1) {
			condition.append(" and bind_state=? ");
			params.add(bind_state);
		}
		sqlCount += condition;
		int totalRecord = jdbcTemplate.queryForObject(sqlCount, params.toArray(), Integer.class);
		Page<AlarmCfgTrigger> page = new Page<AlarmCfgTrigger>(pageIndex, pageSize, totalRecord);
		String sort = " order by  alarmcfg_id desc";
		sql += condition + sort + " limit " + page.getStartIndex() + "," + page.getPageSize();

		@SuppressWarnings("rawtypes")
		List<AlarmCfgTrigger> list = jdbcTemplate.query(sql, params.toArray(), new RowMapper() {

			@Override
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {

				AlarmCfgTrigger model = new AlarmCfgTrigger();
				model.alarmcfg_id = rs.getLong("alarmcfg_id");
				model.data_id = rs.getLong("data_id");
				model.account_id = rs.getLong("account_id");
				model.device_id = rs.getLong("device_id");
				model.name = rs.getString("name");
				model.addr = rs.getString("addr");
				model.addr_type = rs.getInt("addr_type");
				model.text = rs.getString("text");
				model.state = rs.getInt("state");
				model.rid = rs.getString("rid");
				model.plc_id = rs.getLong("plc_id");
				model.data_limit=rs.getString("data_limit");
				model.digit_count=rs.getString("digit_count");
				model.digit_binary=rs.getString("digit_binary");
				model.bind_state = rs.getInt("bind_state");
				model.condition_type = rs.getInt("condition_type");
				model.create_date = rs.getTimestamp("create_date");
				model.update_date = rs.getTimestamp("update_date");

				return model;
			}

		});
		page.setList(list);
		return page;
	}

	@Override
	public AlarmCfg getAlarmcfg(long alarmcfg_id) {
		String sql = "select " + SEL_COL + " from alarm_cfg a where alarmcfg_id=?";
		List<AlarmCfg> list = jdbcTemplate.query(sql, new Object[] { alarmcfg_id }, new DefaultAlarmCfgRowMapper());
		if (!list.isEmpty()) {
			return list.get(0);
		}

		return null;

	}

	@Override
	public List<AlarmCfgExtend> getAlarmCfgExtendListByState(Object... state) {
		String sql = "select a.alarmcfg_id,a.plc_id,a.data_id,a.account_id,a.name,a.addr,a.addr_type,a.text,a.condition_type,a.state,a.create_date,a.update_date,a.rid,a.data_limit,a.digit_count,a.digit_binary,d.machine_code"
				+ " from alarm_cfg a ,device d where a.device_id=d.device_id and d.state=1 ";
		String triSql = "select at.alarmcfg_id,at.type, at.value from alarm_trigger at, alarm_cfg a where at.alarmcfg_id=a.alarmcfg_id";
		if(null != state && state.length > 0){
			sql += " and a.state in (";
			triSql += " and a.state in (";
			StringBuffer inSb = new StringBuffer();
			for(Object o : state){
				inSb.append(",?");
			}
			sql += inSb.substring(1);
			triSql += inSb.substring(1);
			sql += ")";
			triSql += ")";
		}
		List<AlarmCfgExtend> alarmCfgExtendLst = jdbcTemplate.query(sql, state,
				new DefaultAlarmCfgExtendRowMapper());
		List<AlarmTrigger> alarmCfgTriggerLst = jdbcTemplate.query(triSql, state,
				new DefaultAlarmTriggerRowMapper());

		if(null != alarmCfgExtendLst && null != alarmCfgTriggerLst){
			for(AlarmCfgExtend ae : alarmCfgExtendLst){
				List<AlarmTrigger> conditionList = new ArrayList<AlarmTrigger>();
				for (AlarmTrigger at : alarmCfgTriggerLst) {
					if (ae.alarmcfg_id == at.alarmcfg_id) {
						conditionList.add(at);
					}
				}
				ae.setCondition_list(conditionList);
			}
		}
		return alarmCfgExtendLst;
	}

	@Override
	public boolean upAlarmCfg(AlarmCfg alarmCfg) {
		if (alarmCfg == null) {
			return false;
		}
		String sql = "update alarm_cfg set data_id = ?, account_id=?,name=?,addr=?,addr_type=?,text=?,condition_type=?,state=?,update_date=current_timestamp(),bind_state=?,plc_id=?,device_id=?,rid=? ,data_limit=?,digit_count=?  where alarmcfg_id = ?";

		jdbcTemplate.update(sql,
				new Object[] { alarmCfg.data_id, alarmCfg.account_id, alarmCfg.name, alarmCfg.addr, alarmCfg.addr_type,
						alarmCfg.text, alarmCfg.condition_type, alarmCfg.state, alarmCfg.bind_state, alarmCfg.plc_id,
						alarmCfg.device_id, alarmCfg.rid, alarmCfg.data_limit,alarmCfg.digit_count, alarmCfg.alarmcfg_id });
		return true;
	}

	@Override
	public boolean batchUpdateState(final List<String[]> updList) {
		if (null == updList || updList.size() == 0) {
			return false;
		}
		String sql = "update alarm_cfg set state = ? where alarmcfg_id = ? and date_format(update_date,'%Y-%m-%d %H:%i:%s') = ?";
		jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
			public int getBatchSize() {
				return updList.size();
				// 这个方法设定更新记录数，通常List里面存放的都是我们要更新的，所以返回list.size();
			}

			public void setValues(PreparedStatement ps, int i) throws SQLException {
				try {
					String[] arg = updList.get(i);
					ps.setInt(1, Integer.parseInt(arg[0]));
					ps.setInt(2, Integer.parseInt(arg[1]));
					ps.setString(3, arg[2]);
				}catch (Exception e){
					e.printStackTrace();
				}
			}
		});
		return true;
	}

	@Override
	public boolean batchDeleteByPlcId(final List<Long> ids) {
		if (null == ids || ids.size() == 0) {
			return false;
		}

		StringBuilder idSb = new StringBuilder();
		for (long id : ids) {
			idSb.append(",").append(id);
		}
		String sql = "delete from alarm_cfg where plc_id in(" + idSb.substring(1) + ")";
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
		String sql = "delete from alarm_cfg where alarmcfg_id in(" + idSb.substring(1) + ")";
		jdbcTemplate.update(sql);

		return true;
	}
	@SuppressWarnings("unchecked")
	@Override
	public int getAlamBxo(long account_id) {
		String sql="select  count(distinct ac.`device_id`) from `alarm_cfg` ac where ac.`alarmcfg_id`= any(select  distinct  acd.`alarm_cfg_id`  from `alarm_cfg_data` acd,alarm_cfg ac  where  acd.`alarm_cfg_id`=ac.`alarmcfg_id` and ac.account_id=? and acd.`state`=1) and ac.`bind_state`=1";
		if(account_id<0){
			return 0;
		}
		List<Object> params = new ArrayList<Object>();
		 params.add(account_id);
		 
		 
	   int totalRecord = jdbcTemplate.queryForObject(sql, params.toArray(), Integer.class);
		
		return totalRecord;
	
	}

	@Override
	public List<Long> getDeleteIdsByUpdTime(List<String[]> delArgList){
		if(null == delArgList || delArgList.size() == 0){
			return null;
		}

		StringBuilder idSb = new StringBuilder();
		for(String[] args : delArgList){
			idSb.append(",").append(args[0]);
		}
		List<AlarmCfgExtend> list = jdbcTemplate.query("select alarmcfg_id, update_date from alarm_cfg where alarmcfg_id in("+idSb.substring(1)+")", new RowMapper() {
			@Override
			public Object mapRow(ResultSet resultSet, int i) throws SQLException {
				AlarmCfgExtend model = new AlarmCfgExtend();
				model.alarmcfg_id = resultSet.getLong("alarmcfg_id");
				model.upd_time = TimeUtil.getYYYYMMDDHHMMSSDate(resultSet.getTimestamp("update_date"));
				return model;
			}
		});

		if(null != list){
			List<Long> ids = new ArrayList<>();
			for(String[] args : delArgList){
				for(AlarmCfgExtend extend : list){
					if(Integer.parseInt(args[0]) == extend.alarmcfg_id
							&& args[1].equals(extend.upd_time)){
						ids.add(extend.alarmcfg_id);
						break;
					}
				}
			}

			return ids;
		}

		return null;
	}

	@Override
	public List<Long> getAlarmCfgIdsByPlcIds(List<Long> plcIds){
		if (null == plcIds || plcIds.size() == 0) {
			return null;
		}

		StringBuilder idSb = new StringBuilder();
		for (long plcId : plcIds) {
			idSb.append(",").append(plcId);
		}

		List<Long> cfgIds = jdbcTemplate.query("select alarmcfg_id from alarm_cfg where plc_id in(" + idSb.substring(1) + ")", new RowMapper() {
			@Override
			public Object mapRow(ResultSet resultSet, int i) throws SQLException {
				return resultSet.getLong("alarmcfg_id");
			}
		});

		return cfgIds;
	}

	public static final class DefaultAlarmTriggerRowMapper implements RowMapper<AlarmTrigger> {
		@Override
		public AlarmTrigger mapRow(ResultSet rs, int i) throws SQLException {
			AlarmTrigger model = new AlarmTrigger();
			model.alarmcfg_id = rs.getLong("alarmcfg_id");
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
			model.com = model.plc_id + "";
			model.data_id = rs.getLong("data_id");
			model.account_id = rs.getLong("account_id");
			model.name = rs.getString("name");
			model.addr = rs.getString("addr");
			model.addr_type = rs.getInt("addr_type");
			model.text = rs.getString("text");
			model.state = rs.getInt("state");
			model.data_limit = rs.getString("data_limit");
			model.digit_count = rs.getString("digit_count");
			model.digit_binary = rs.getString("digit_binary");
			model.condition_type = rs.getInt("condition_type");
			model.create_date = rs.getTimestamp("create_date");
			model.update_date = rs.getTimestamp("update_date");
			model.upd_time = TimeUtil.getYYYYMMDDHHMMSSDate(model.update_date);
			model.machine_code = rs.getString("machine_code");
			model.rid = rs.getString("rid");
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
			model.device_id = rs.getLong("device_id");
			model.name = rs.getString("name");
			model.addr = rs.getString("addr");
			model.addr_type = rs.getInt("addr_type");
			model.text = rs.getString("text");
			model.state = rs.getInt("state");
			model.rid = rs.getString("rid");
			model.data_limit = rs.getString("data_limit");
			model.digit_count = rs.getString("digit_count");
			model.digit_binary = rs.getString("digit_binary");
			model.plc_id = rs.getLong("plc_id");
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
				Integer model = resultSet.getInt("alarmcfg_id");
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

	public boolean updatePointAccAndState(long accountId, long deviceId) {

		Object[] args = new Object[] { accountId, deviceId };
		String sql = "UPDATE alarm_cfg a SET a.account_id = ?, a.bind_state = 1 WHERE a.device_id = ?";
		Integer count = jdbcTemplate.update(sql, args);
		if (count <= 0) {
			return false;
		}
		return true;
	}

	@Override
	public void deleteAlarmCfg(long plc_id) {
		Object[] args=new Object[]{plc_id};
		String sql="delete from alarm_cfg where plc_id=?";
		jdbcTemplate.update(sql,args);
	}


}
