package com.wecon.box.impl;

import com.wecon.box.api.RealHisCfgApi;
import com.wecon.box.entity.Page;
import com.wecon.box.entity.RealHisCfg;
import com.wecon.box.entity.RealHisCfgDevice;
import com.wecon.box.entity.RealHisCfgExtend;
import com.wecon.box.filter.RealHisCfgFilter;
import com.wecon.box.filter.ViewAccountRoleFilter;
import com.wecon.common.util.CommonUtils;
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
import java.util.Map;

/**
 * @author lanpenghui 2017年8月2日
 */
@Component
public class RealHisCfgImpl implements RealHisCfgApi {
	@Autowired
	private JdbcTemplate jdbcTemplate;
	private final String SEL_COL = "r.id,r.data_id,r.account_id,r.plc_id,r.name,r.addr,r.addr_type,r.describe,r.digit_count,r.data_limit,r.his_cycle,r.data_type,r.state,r.create_date,r.update_date,r.bind_state,r.device_id,r.rid";

	@Override
	public long saveRealHisCfg(final RealHisCfg model) {
		KeyHolder key = new GeneratedKeyHolder();
		jdbcTemplate.update(new PreparedStatementCreator() {
			@Override
			public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
				PreparedStatement preState = con.prepareStatement(
						"insert into real_his_cfg(data_id,account_id,plc_id,`name`,addr,addr_type,`describe`,digit_count,data_limit,his_cycle,data_type,state,create_date,update_date,bind_state,device_id,rid)values(?,?,?,?,?,?,?,?,?,?,?,?,current_timestamp(),current_timestamp(),1,?,?);",
						Statement.RETURN_GENERATED_KEYS);
				preState.setLong(1, model.data_id);
				preState.setLong(2, model.account_id);
				preState.setLong(3, model.plc_id);
				preState.setString(4, model.name);
				preState.setString(5, model.addr);
				preState.setInt(6, model.addr_type);
				preState.setString(7, model.describe);
				preState.setString(8, model.digit_count);
				preState.setString(9, model.data_limit);
				preState.setInt(10, model.his_cycle);
				preState.setInt(11, model.data_type);
				preState.setInt(12, model.state);
				preState.setLong(13, model.device_id);
				preState.setString(14, model.rid);
				return preState;
			}
		}, key);
		// 从主键持有者中获得主键值
		return key.getKey().longValue();
	}

	@Override
	public boolean updateRealHisCfg(RealHisCfg model) {
		String sql = "update real_his_cfg SET data_id=?,account_id=?,plc_id=?,`name`=?,addr=?,addr_type=?,`describe`=?,digit_count=?,data_limit=?,his_cycle=?,data_type=?,state=?,update_date=current_timestamp(),bind_state=?,device_id=?,rid=? where id=?";
		jdbcTemplate.update(sql,
				new Object[] { model.data_id, model.account_id, model.plc_id, model.name, model.addr, model.addr_type,
						model.describe, model.digit_count, model.data_limit, model.his_cycle, model.data_type,
						model.state, model.bind_state, model.device_id,model.rid,model.id });

		return true;
	}

	@Override
	public RealHisCfg getRealHisCfg(long id) {

		String sql = "select " + SEL_COL + " from real_his_cfg r where r.id=?";
		List<RealHisCfg> list = jdbcTemplate.query(sql, new Object[] { id }, new DefaultRealHisCfgRowMapper());
		if (!list.isEmpty()) {
			return list.get(0);
		}
		return null;
	}

	@Override
	public List<RealHisCfg> getRealHisCfg(long plc_id, int state) {
		String sql = "select " + SEL_COL + " from real_his_cfg r where r.plc_id=? and r.state=?";
		List<RealHisCfg> list = jdbcTemplate.query(sql, new Object[] { plc_id, state },
				new DefaultRealHisCfgRowMapper());
		if (!list.isEmpty()) {
			return list;
		}

		return null;
	}

	@Override
	public void delRealHisCfg(long id) {
		
		String sql = "delete from  real_his_cfg r where r.id=?";
		jdbcTemplate.update(sql, new Object[] { id });
	}

	@Override
	public List<RealHisCfgDevice> getRealHisCfg(RealHisCfgFilter filter) {
		String sql = "select " + SEL_COL + ",d.machine_code"
				+ " from  device d,real_his_cfg r where 1=1 and d.`device_id`=r.device_id and r.bind_state=1 ";
		StringBuffer condition = new StringBuffer("");
		List<Object> params = new ArrayList<Object>();
		if (filter.id > 0) {
			condition.append(" and r.id = ? ");
			params.add(filter.id);
		}
		if (filter.device_id > 0) {
			condition.append(" and r.device_id = ? ");
			params.add(filter.device_id);
		}
		if (filter.data_id > 0) {
			condition.append(" and r.data_id = ? ");
			params.add(filter.data_id);
		}
		if (filter.account_id > 0) {
			condition.append(" and r.account_id = ? ");
			params.add(filter.account_id);
		}
		if (filter.plc_id > 0) {
			condition.append(" and r.plc_id = ? ");
			params.add(filter.plc_id);
		}
		if (!CommonUtils.isNullOrEmpty(filter.name)) {
			condition.append(" and r.name like ? ");
			params.add("%" + filter.name + "%");
		}
		if (!CommonUtils.isNullOrEmpty(filter.addr)) {
			condition.append(" and r.addr like ? ");
			params.add("%" + filter.addr + "%");
		}
		if (filter.addr_type > -1) {
			condition.append(" and r.addr_type = ? ");
			params.add(filter.addr_type);
		}

		if (!CommonUtils.isNullOrEmpty(filter.describe)) {
			condition.append(" and r.describe like ? ");
			params.add("%" + filter.describe + "%");
		}
		if (!CommonUtils.isNullOrEmpty(filter.digit_count)) {
			condition.append(" and r.digit_count like ? ");
			params.add("%" + filter.digit_count + "%");
		}
		if (!CommonUtils.isNullOrEmpty(filter.data_limit)) {
			condition.append(" and r.data_limit like ? ");
			params.add("%" + filter.data_limit + "%");
		}

		if (filter.his_cycle > -1) {
			condition.append(" and r.his_cycle = ? ");
			params.add(filter.his_cycle);
		}
		if (filter.data_type > -1) {
			condition.append(" and r.data_type = ? ");
			params.add(filter.data_type);
		}
		if (filter.state > -1) {
			condition.append(" and r.state = ? ");
			params.add(filter.state);

		}

		sql += condition;
		List<RealHisCfgDevice> list = jdbcTemplate.query(sql, params.toArray(), new DefaultHisCfgDeviceRowMapper());
		return list;
	}

	/**
	 * 管理账号给自己分配实时监控点
	 */
	@Override
	public Page<RealHisCfgDevice> getRealHisCfg(RealHisCfgFilter filter, int pageIndex, int pageSize) {
		String sqlCount = "select count(0) from  device d,real_his_cfg r where 1=1 and d.`device_id`=r.device_id and r.bind_state=1 ";
		String sql = "select " + SEL_COL + ",d.machine_code"
				+ " from  device d,real_his_cfg r where 1=1 and d.`device_id`=r.device_id and r.bind_state=1 ";
		StringBuffer condition = new StringBuffer("");
		List<Object> params = new ArrayList<Object>();
		if (filter.id > 0) {
			condition.append(" and r.id = ? ");
			params.add(filter.id);
		}
		if (filter.device_id > 0) {
			condition.append(" and r.device_id = ? ");
			params.add(filter.device_id);
		}
		if (filter.data_id > 0) {
			condition.append(" and r.data_id = ? ");
			params.add(filter.data_id);
		}
		if (filter.account_id > 0) {
			condition.append(" and r.account_id = ? ");
			params.add(filter.account_id);
		}
		if (filter.plc_id > 0) {
			condition.append(" and r.plc_id = ? ");
			params.add(filter.plc_id);
		}
		if (!CommonUtils.isNullOrEmpty(filter.name)) {
			condition.append(" and r.name like ? ");
			params.add("%" + filter.name + "%");
		}
		if (!CommonUtils.isNullOrEmpty(filter.addr)) {
			condition.append(" and r.addr like ? ");
			params.add("%" + filter.addr + "%");
		}
		if (filter.addr_type > -1) {
			condition.append(" and r.addr_type = ? ");
			params.add(filter.addr_type);
		}

		if (!CommonUtils.isNullOrEmpty(filter.describe)) {
			condition.append(" and r.describe like ? ");
			params.add("%" + filter.describe + "%");
		}
		if (!CommonUtils.isNullOrEmpty(filter.digit_count)) {
			condition.append(" and r.digit_count like ? ");
			params.add("%" + filter.digit_count + "%");
		}
		if (!CommonUtils.isNullOrEmpty(filter.data_limit)) {
			condition.append(" and r.data_limit like ? ");
			params.add("%" + filter.data_limit + "%");
		}

		if (filter.his_cycle > -1) {
			condition.append(" and r.his_cycle = ? ");
			params.add(filter.his_cycle);
		}
		if (filter.data_type > -1) {
			condition.append(" and r.data_type = ? ");
			params.add(filter.data_type);
		}
		if (filter.state > -1) {
			condition.append(" and r.state = ? ");
			params.add(filter.state);

		}
		if (!CommonUtils.isNullOrEmpty(filter.rid)) {
			condition.append(" and r.rid like ? ");
			params.add("%" + filter.rid + "%");
		}
		sqlCount += condition;
		int totalRecord = jdbcTemplate.queryForObject(sqlCount, params.toArray(), Integer.class);
		Page<RealHisCfgDevice> page = new Page<RealHisCfgDevice>(pageIndex, pageSize, totalRecord);
		String sort = " order by id desc";
		sql += condition + sort + " limit " + page.getStartIndex() + "," + page.getPageSize();
		List<RealHisCfgDevice> list = jdbcTemplate.query(sql, params.toArray(), new DefaultHisCfgDeviceRowMapper());
		page.setList(list);
		return page;
	}

	@Override
	public List<RealHisCfgDevice> getRealHisCfg(ViewAccountRoleFilter filter) {
		String sql = "select " + SEL_COL + ",d.machine_code"
				+ " from   device d,real_his_cfg r ,view_account_role v ,account_relation ar where 1=1 and  d.`device_id`=r.device_id and  v.view_id=ar.view_id and ar.manager_id=r.`account_id` and  r.bind_state=1 and r.data_type=1 and  r.`id`=v.cfg_id";
		StringBuffer condition = new StringBuffer("");
		List<Object> params = new ArrayList<Object>();

		if (filter.view_id > 0) {
			condition.append(" and v.view_id = ? ");
			params.add(filter.view_id);
		}
		if (filter.role_type > -1) {
			condition.append(" and v.role_type = ? ");
			params.add(filter.role_type);
		}

		sql += condition;
		List<RealHisCfgDevice> list = jdbcTemplate.query(sql, params.toArray(), new DefaultHisCfgDeviceRowMapper());
		return list;
	}

	/**
	 * 视图账号给自己分配实时监控点
	 */
	@Override
	public Page<RealHisCfgDevice> getRealHisCfg(ViewAccountRoleFilter filter, int pageIndex, int pageSize) {
		String sqlCount = "select count(0) from   device d,real_his_cfg r ,view_account_role v ,account_relation ar where 1=1 and  d.`device_id`=r.device_id and  v.view_id=ar.view_id and ar.manager_id=r.`account_id` and  r.bind_state=1 and r.data_type=0 and  r.`id`=v.cfg_id";
		String sql = "select " + SEL_COL + ",d.machine_code"
				+ " from   device d,real_his_cfg r ,view_account_role v ,account_relation ar where 1=1 and  d.`device_id`=r.device_id and  v.view_id=ar.view_id and ar.manager_id=r.`account_id` and  r.bind_state=1 and r.data_type=0 and  r.`id`=v.cfg_id";
		StringBuffer condition = new StringBuffer("");
		List<Object> params = new ArrayList<Object>();

		if (filter.view_id > 0) {
			condition.append(" and v.view_id = ? ");
			params.add(filter.view_id);
		}
		if (filter.role_type > -1) {
			condition.append(" and v.role_type = ? ");
			params.add(filter.role_type);
		}
		if (filter.state > -1) {
			condition.append(" and r.state = ? ");
			params.add(filter.state);

		}

		sqlCount += condition;
		int totalRecord = jdbcTemplate.queryForObject(sqlCount, params.toArray(), Integer.class);
		Page<RealHisCfgDevice> page = new Page<RealHisCfgDevice>(pageIndex, pageSize, totalRecord);
		String sort = " order by id desc";
		sql += condition + sort + " limit " + page.getStartIndex() + "," + page.getPageSize();
		List<RealHisCfgDevice> list = jdbcTemplate.query(sql, params.toArray(), new DefaultHisCfgDeviceRowMapper());
		page.setList(list);
		return page;

	}

	@Override
	public Page<RealHisCfgDevice> getRealHisCfgList(RealHisCfgFilter filter, int pageIndex, int pageSize) {
		String sqlCount = "select count(0) from account_dir ad,account_dir_rel adr ,real_his_cfg  r,plc_info pli,device d where 1=1 and  ad.`id`=adr.`acc_dir_id` and pli.`plc_id`=r.`plc_id` and pli.`device_id`=d.`device_id` and r.`id`=adr.`ref_id` and ad.`account_id`=r.`account_id`and r.bind_state=1";
		String sql = "select " + SEL_COL + ",d.machine_code,adr.ref_alais"
				+ " from account_dir ad,account_dir_rel adr ,real_his_cfg  r,plc_info pli,device d where 1=1 and  ad.`id`=adr.`acc_dir_id` and pli.`plc_id`=r.`plc_id` and pli.`device_id`=d.`device_id` and r.`id`=adr.`ref_id` and ad.`account_id`=r.`account_id`and r.bind_state=1";
		StringBuffer condition = new StringBuffer("");
		List<Object> params = new ArrayList<Object>();
		if (filter.id > 0) {
			condition.append(" and r.id = ? ");
			params.add(filter.id);
		}
		if (filter.device_id > 0) {
			condition.append(" and d.device_id = ? ");
			params.add(filter.device_id);
		}
		if (filter.dirId > 0) {
			condition.append(" and ad.id = ? ");
			params.add(filter.dirId);
		}
		if (filter.data_id > 0) {
			condition.append(" and r.data_id = ? ");
			params.add(filter.data_id);
		}
		if (filter.account_id > 0) {
			condition.append(" and r.account_id = ? ");
			params.add(filter.account_id);
		}
		if (filter.plc_id > 0) {
			condition.append(" and r.plc_id = ? ");
			params.add(filter.plc_id);
		}
		if (!CommonUtils.isNullOrEmpty(filter.name)) {
			condition.append(" and r.name like ? ");
			params.add("%" + filter.name + "%");
		}
		if (!CommonUtils.isNullOrEmpty(filter.addr)) {
			condition.append(" and r.addr like ? ");
			params.add("%" + filter.addr + "%");
		}
		if (filter.addr_type > -1) {
			condition.append(" and r.addr_type = ? ");
			params.add(filter.addr_type);
		}

		if (!CommonUtils.isNullOrEmpty(filter.describe)) {
			condition.append(" and r.describe like ? ");
			params.add("%" + filter.describe + "%");
		}
		if (!CommonUtils.isNullOrEmpty(filter.digit_count)) {
			condition.append(" and r.digit_count like ? ");
			params.add("%" + filter.digit_count + "%");
		}
		if (!CommonUtils.isNullOrEmpty(filter.data_limit)) {
			condition.append(" and r.data_limit like ? ");
			params.add("%" + filter.data_limit + "%");
		}

		if (filter.his_cycle > -1) {
			condition.append(" and r.his_cycle = ? ");
			params.add(filter.his_cycle);
		}
		if (filter.data_type > -1) {
			condition.append(" and r.data_type = ? ");
			params.add(filter.data_type);
		}
		if (filter.state > -1) {
			condition.append(" and r.state = ? ");
			params.add(filter.state);

		}
		sqlCount += condition;
		int totalRecord = jdbcTemplate.queryForObject(sqlCount, params.toArray(), Integer.class);
		Page<RealHisCfgDevice> page = new Page<RealHisCfgDevice>(pageIndex, pageSize, totalRecord);
		String sort = " order by id desc";
		sql += condition + sort + " limit " + page.getStartIndex() + "," + page.getPageSize();
		List<RealHisCfgDevice> list = jdbcTemplate.query(sql, params.toArray(), new DefaultRealCfgDeviceRowMapper());
		page.setList(list);
		return page;

	}

	public Page<RealHisCfgDevice> getRealHisCfgList(ViewAccountRoleFilter filter, int pageIndex, int pageSize) {
		String sqlCount = "select count(0) from  real_his_cfg r , device d,plc_info p, view_account_role v,account_dir ad,account_dir_rel adr where 1=1 and  p.plc_id=r.plc_id and r.id=adr.`ref_id` and p.device_id=d.device_id and v.cfg_id=r.id and v.cfg_type=1 and ad.`id`=adr.`acc_dir_id` and ad.`account_id`=v.view_id and r.bind_state=1";
		String sql = "select " + SEL_COL + ",d.machine_code,adr.ref_alais,v.role_type"
				+ " from real_his_cfg r , device d,plc_info p, view_account_role v,account_dir ad,account_dir_rel adr where 1=1 and  p.plc_id=r.plc_id and r.id=adr.`ref_id` and p.device_id=d.device_id and v.cfg_id=r.id and v.cfg_type=1 and ad.`id`=adr.`acc_dir_id` and ad.`account_id`=v.view_id and r.bind_state=1";
		StringBuffer condition = new StringBuffer("");
		List<Object> params = new ArrayList<Object>();
		if (filter.view_id > 0) {
			condition.append(" and v.view_id = ? ");
			params.add(filter.view_id);
		}
		if (filter.dirId > 0) {
			condition.append(" and ad.`id` = ? ");
			params.add(filter.dirId);
		}
		if (filter.role_type > -1) {
			condition.append(" and v.role_type = ? ");
			params.add(filter.role_type);
		}
		if (filter.data_type > -1) {
			condition.append(" and r.data_type = ? ");
			params.add(filter.data_type);
		}
		if (filter.state > -1) {
			condition.append(" and r.state = ? ");
			params.add(filter.state);

		}

		sqlCount += condition;
		int totalRecord = jdbcTemplate.queryForObject(sqlCount, params.toArray(), Integer.class);
		Page<RealHisCfgDevice> page = new Page<RealHisCfgDevice>(pageIndex, pageSize, totalRecord);
		String sort = " order by id desc";
		sql += condition + sort + " limit " + page.getStartIndex() + "," + page.getPageSize();
		List<RealHisCfgDevice> list = jdbcTemplate.query(sql, params.toArray(), new DefaultViewRowMapper());
		page.setList(list);
		return page;
	}

	@Override
	public Page<RealHisCfgDevice> getRealHisCfgDevicePage(RealHisCfgFilter filter, Map<String, Object> bParams,
			int pageIndex, int pageSize) {
		String fromStr = "from real_his_cfg r ,device d, plc_info p";
		StringBuffer condition = new StringBuffer();
		List<Object> params = new ArrayList<Object>();

		if (filter.account_id > 0) {
			condition.append(" and r.account_id = ? ");
			params.add(filter.account_id);
		}
		if (filter.addr_type > -1) {
			condition.append(" and r.addr_type = ? ");
			params.add(filter.addr_type);
		}
		if (filter.data_type > -1) {
			condition.append(" and r.data_type = ? ");
			params.add(filter.data_type);
		}
		if (filter.his_cycle > -1) {
			condition.append(" and r.his_cycle = ? ");
			params.add(filter.his_cycle);
		}

		Object boxId = bParams.get("boxId");
		Object groupId = bParams.get("groupId");
		if (null != boxId) {
			condition.append(" and d.device_id = ? ");
			params.add(boxId);
		}
		if (null != groupId) {
			fromStr += ", account_dir_rel f";
			condition.append(" and r.id=f.ref_id and f.acc_dir_id = ?");
			params.add(groupId);
		}
		String sqlCount = "select count(0) " + fromStr
				+ " where 1=1 and  p.`plc_id`=r.plc_id and p.`device_id`=d.device_id";
		String sql = "select " + SEL_COL + ",d.machine_code" + "  " + fromStr
				+ "  where 1=1 and  p.`plc_id`=r.plc_id and p.`device_id`=d.device_id";
		sqlCount += condition;
		int totalRecord = jdbcTemplate.queryForObject(sqlCount, params.toArray(), Integer.class);
		Page<RealHisCfgDevice> page = new Page<RealHisCfgDevice>(pageIndex, pageSize, totalRecord);
		String sort = " order by id desc";
		sql += condition + sort + " limit " + page.getStartIndex() + "," + page.getPageSize();
		List<RealHisCfgDevice> list = jdbcTemplate.query(sql, params.toArray(), new DefaultRealCfgDeviceRowMapper());
		page.setList(list);
		return page;

	}

	@Override
	public Page<RealHisCfgDevice> getRealHisCfgDevicePage(ViewAccountRoleFilter filter, Map<String, Object> bParams,
			int pageIndex, int pageSize) {
		String fromStr = "from real_his_cfg r ,device d, plc_info p, view_account_role v";
		StringBuffer condition = new StringBuffer();
		List<Object> params = new ArrayList<Object>();

		if (filter.view_id > 0) {
			condition.append(" and v.view_id = ? ");
			params.add(filter.view_id);
		}
		if (filter.role_type > -1) {
			condition.append(" and v.role_type = ? ");
			params.add(filter.role_type);
		}
		if (filter.data_type > -1) {
			condition.append(" and r.data_type = ? ");
			params.add(filter.data_type);
		}

		Object groupId = bParams.get("groupId");
		if (null != groupId) {
			fromStr += ", account_dir_rel f";
			condition.append(" and r.id=f.ref_id and f.acc_dir_id = ?");
			params.add(groupId);
		}
		String sqlCount = "select count(0) " + fromStr
				+ " where 1=1 and  p.`plc_id`=r.plc_id and p.`device_id`=d.device_id and v.cfg_id=r.id and v.cfg_type=1";
		String sql = "select " + SEL_COL + ",d.machine_code" + "  " + fromStr
				+ "  where 1=1 and  p.`plc_id`=r.plc_id and p.`device_id`=d.device_id and v.cfg_id=r.id and v.cfg_type=1";
		sqlCount += condition;
		int totalRecord = jdbcTemplate.queryForObject(sqlCount, params.toArray(), Integer.class);
		Page<RealHisCfgDevice> page = new Page<RealHisCfgDevice>(pageIndex, pageSize, totalRecord);
		String sort = " order by id desc";
		sql += condition + sort + " limit " + page.getStartIndex() + "," + page.getPageSize();
		List<RealHisCfgDevice> list = jdbcTemplate.query(sql, params.toArray(), new DefaultRealCfgDeviceRowMapper());
		page.setList(list);
		return page;

	}

	@Override
	public List<RealHisCfgExtend> getRealHisCfgListByState(int state){
		String sql = "select " + SEL_COL + ",d.machine_code from real_his_cfg r ,device d, plc_info p where d.device_id=p.device_id and p.plc_id=r.plc_id and r.state = ?";
		List<RealHisCfgExtend> list = jdbcTemplate.query(sql, new Object[]{state}, new DefaultRealCfgExtendRowMapper());
		return list;
	}

	@Override
	public boolean batchUpdateState(final List<int[]> updList){
		if(null == updList || updList.size() == 0){
			return false;
		}
		String sql = "update real_his_cfg set state = ? where id = ?";
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

	@Override
	public boolean batchDeleteByPlcId(final List<Integer> ids) {
		if (null == ids || ids.size() == 0) {
			return false;
		}

		StringBuilder idSb = new StringBuilder();
		for(int id : ids){
			idSb.append(",").append(id);
		}
		String sql = "delete from real_his_cfg where plc_id in("+idSb.substring(1)+")";
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
		String sql = "delete from real_his_cfg where id in("+idSb.substring(1)+")";
		jdbcTemplate.update(sql);

		return true;
	}

	public static final class DefaultRealHisCfgRowMapper implements RowMapper<RealHisCfg> {

		@Override
		public RealHisCfg mapRow(ResultSet rs, int i) throws SQLException {
			RealHisCfg model = new RealHisCfg();
			model.id = rs.getLong("id");
			model.data_id = rs.getLong("data_id");
			model.account_id = rs.getLong("account_id");
			model.plc_id = rs.getLong("plc_id");
			model.name = rs.getString("name");
			model.addr = rs.getString("addr");
			model.addr_type = rs.getInt("addr_type");
			model.describe = rs.getString("describe");
			model.digit_count = rs.getString("digit_count");
			model.data_limit = rs.getString("data_limit");
			model.his_cycle = rs.getInt("his_cycle");
			model.data_type = rs.getInt("data_type");
			model.bind_state = rs.getInt("bind_state");
			model.state = rs.getInt("state");
			model.create_date = rs.getTimestamp("create_date");
			model.update_date = rs.getTimestamp("update_date");
			model.rid= rs.getString("rid");
			model.device_id = rs.getLong("device_id");
			model.bind_state=rs.getInt("bind_state");

			return model;
		}
	}

	public static final class DefaultRealCfgDeviceRowMapper implements RowMapper<RealHisCfgDevice> {

		@Override
		public RealHisCfgDevice mapRow(ResultSet rs, int i) throws SQLException {
			RealHisCfgDevice model = new RealHisCfgDevice();
			model.id = rs.getLong("id");
			model.data_id = rs.getLong("data_id");
			model.account_id = rs.getLong("account_id");
			model.plc_id = rs.getLong("plc_id");
			model.name = rs.getString("name");
			model.addr = rs.getString("addr");
			model.addr_type = rs.getInt("addr_type");
			model.describe = rs.getString("describe");
			model.digit_count = rs.getString("digit_count");
			model.data_limit = rs.getString("data_limit");
			model.his_cycle = rs.getInt("his_cycle");
			model.data_type = rs.getInt("data_type");
			model.state = rs.getInt("state");
			model.create_date = rs.getTimestamp("create_date");
			model.update_date = rs.getTimestamp("update_date");
			model.machine_code = rs.getString("machine_code");
			model.ref_alais = rs.getString("ref_alais");
			model.rid= rs.getString("rid");
			model.device_id = rs.getLong("device_id");
			model.bind_state=rs.getInt("bind_state");
			return model;
		}
	}

	public static final class DefaultRealCfgExtendRowMapper implements RowMapper<RealHisCfgExtend> {

		@Override
		public RealHisCfgExtend mapRow(ResultSet rs, int i) throws SQLException {
			RealHisCfgExtend model = new RealHisCfgExtend();
			model.id = rs.getLong("id");
			model.addr_id = model.id;
			model.data_id = rs.getLong("data_id");
			model.account_id = rs.getLong("account_id");
			model.plc_id = rs.getLong("plc_id");
			model.com = model.plc_id+"";
			model.name = rs.getString("name");
			model.addr = rs.getString("addr");
			model.addr_type = rs.getInt("addr_type");
			model.describe = rs.getString("describe");
			model.digit_count = rs.getString("digit_count");
			model.data_limit = rs.getString("data_limit");
			model.his_cycle = rs.getInt("his_cycle");
			model.data_type = rs.getInt("data_type");
			model.state = rs.getInt("state");
			model.create_date = rs.getTimestamp("create_date");
			model.update_date = rs.getTimestamp("update_date");
			model.upd_time = TimeUtil.getYYYYMMDDHHMMSSDate(model.update_date);
			model.machine_code = rs.getString("machine_code");
			//model.ref_alais = rs.getString("ref_alais");
			return model;
		}
	}

	public static final class DefaultViewRowMapper implements RowMapper<RealHisCfgDevice> {

		@Override
		public RealHisCfgDevice mapRow(ResultSet rs, int i) throws SQLException {
			RealHisCfgDevice model = new RealHisCfgDevice();
			model.id = rs.getLong("id");
			model.data_id = rs.getLong("data_id");
			model.account_id = rs.getLong("account_id");
			model.plc_id = rs.getLong("plc_id");
			model.name = rs.getString("name");
			model.addr = rs.getString("addr");
			model.addr_type = rs.getInt("addr_type");
			model.describe = rs.getString("describe");
			model.digit_count = rs.getString("digit_count");
			model.data_limit = rs.getString("data_limit");
			model.his_cycle = rs.getInt("his_cycle");
			model.data_type = rs.getInt("data_type");
			model.state = rs.getInt("state");
			model.rid= rs.getString("rid");
			model.create_date = rs.getTimestamp("create_date");
			model.update_date = rs.getTimestamp("update_date");
			model.machine_code = rs.getString("machine_code");
			model.ref_alais = rs.getString("ref_alais");
			model.role_type = rs.getInt("role_type");
			model.device_id = rs.getLong("device_id");
			model.bind_state=rs.getInt("bind_state");
			return model;
		}
	}

	public static final class DefaultHisCfgDeviceRowMapper implements RowMapper<RealHisCfgDevice> {

		@Override
		public RealHisCfgDevice mapRow(ResultSet rs, int i) throws SQLException {
			RealHisCfgDevice model = new RealHisCfgDevice();
			model.id = rs.getLong("id");
			model.data_id = rs.getLong("data_id");
			model.account_id = rs.getLong("account_id");
			model.device_id = rs.getLong("device_id");
			model.plc_id = rs.getLong("plc_id");
			model.name = rs.getString("name");
			model.addr = rs.getString("addr");
			model.addr_type = rs.getInt("addr_type");
			model.describe = rs.getString("describe");
			model.digit_count = rs.getString("digit_count");
			model.data_limit = rs.getString("data_limit");
			model.his_cycle = rs.getInt("his_cycle");
			model.data_type = rs.getInt("data_type");
			model.state = rs.getInt("state");
			model.rid= rs.getString("rid");
			model.bind_state=rs.getInt("bind_state");
			model.create_date = rs.getTimestamp("create_date");
			model.update_date = rs.getTimestamp("update_date");
			model.machine_code = rs.getString("machine_code");
			return model;
		}
	}

	/*
	 * 查询盒子下的监控点
	 */
	public ArrayList<Integer> findRealHisCfgIdSBydevice_id(Integer device_id) {
		Object[] args = new Object[] { device_id };
		String sql = "SELECT id FROM real_his_cfg WHERE device_id=?";
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
	public void setBind_state(int[] realHisCfg, Integer state) {
		String sql = "UPDATE real_his_cfg SET bind_state=? where id =?";
			for(int i=0;i<realHisCfg.length;i++) {
				Object []args =new Object[]{state,realHisCfg[i]};
				jdbcTemplate.update(sql, args);
			}
	}
	/*
	* 盒子用户改变  监控点迁移
	* */
	public boolean updatePointAccAndState(long accountId,long deviceId)
	{

		String sql="UPDATE real_his_cfg a SET a.account_id=?,a.bind_state=1 WHERE a.device_id=?;";
		Object[] args=new Object[]{accountId,deviceId};
		Integer count=jdbcTemplate.update(sql,args);
		if(count<=0)
		{
			return false;
		}
		return true;

	}

}
