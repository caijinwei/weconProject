package com.wecon.box.impl;

import com.wecon.box.api.ViewAccountRoleApi;
import com.wecon.box.entity.*;
import com.wecon.box.enums.ErrorCodeOption;
import com.wecon.box.filter.ViewAccountRoleFilter;
import com.wecon.restful.core.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by caijinw on 2017/8/10.
 */
@Component
public class ViewAccountRoleImpl implements ViewAccountRoleApi {
	@Autowired
	JdbcTemplate jdbcTemplate;
	@Autowired
	public PlatformTransactionManager transactionManager;

	@Override
	public long saveViewAccountRole(ViewAccountRole model) {
		return 0;
	}

	@Override
	public boolean updateViewAccountRole(ViewAccountRole model) {
		return false;
	}

	@Override
	public void delViewAccountRole(long view_id) {
	}

	@Override
	public Page<ViewAccountRole> getViewAccountRoleList(ViewAccountRoleFilter filter, int pageIndex, int pageSize) {
		return null;
	}

	/*
	 * @Param view_id account_id 视图账号ID 管理员账户ID
	 *
	 * 获取 该视图账户中未有的监控点 需要分页 id state name digit_count addr addr_type desceibe
	 */
	/*
	 * 该账户全部余下监控点
	 */
	@Override
	public Page<RealHisCfg> getViewRealHisCfgByViewAndAccId(long view_id, long account_id, Integer type,
			Integer pageIndex, Integer pageSize) {
		Object[] args0 = new Object[] { account_id, type, view_id };
		String sqlCount = "select count(*) from real_his_cfg a WHERE account_id=? AND a.data_type=? AND a.bind_state=1  AND id NOT IN(SELECT cfg_id FROM view_account_role where view_id=?);";
		int totalRecord = jdbcTemplate.queryForObject(sqlCount, args0, Integer.class);
		Page<RealHisCfg> page = new Page<RealHisCfg>(pageIndex, pageSize, totalRecord);

		Object[] args = new Object[] { account_id, type, view_id, page.getStartIndex(), page.getPageSize() };
		String sql = "select a.id, a.state,a.name , a.digit_count,a.addr , a.addr_type,a.describe from real_his_cfg a WHERE account_id=?  AND a.data_type=? AND a.bind_state=1  AND id NOT IN(SELECT cfg_id FROM view_account_role where view_id=?)LIMIT ?,?";
		List<RealHisCfg> list = jdbcTemplate.query(sql, args, new RowMapper() {
			@Override
			public Object mapRow(ResultSet resultSet, int i) throws SQLException {
				RealHisCfg model = new RealHisCfg();
				model.id = resultSet.getLong("id");
				model.state = resultSet.getInt("state");
				model.name = resultSet.getString("name");
				model.digit_count = resultSet.getString("digit_count");
				model.addr = resultSet.getString("addr");
				model.addr_type = resultSet.getInt("addr_type");
				model.describe = resultSet.getString("describe");
				return model;
			}
		});
		page.setList(list);
		return page;
	}

	/*
	 * 该盒子下的余下监控点（实时历史） SELECT a.id, a.state, a. NAME, a.digit_count, a.addr,
	 * a.addr_type, a.`describe` FROM real_his_cfg a WHERE a.data_type = 0 AND
	 * a.plc_id IN ( SELECT plc_id FROM plc_info WHERE device_id = '1')AND id
	 * NOT IN ( SELECT cfg_id FROM view_account_role WHERE view_id =
	 * '1000021')LIMIT 1, 5;
	 */
	public Page<RealHisCfg> getViewRealHisCfgByViewAndDeivceId(long account_id, long view_id, long device_id,
			Integer type, Integer pageIndex, Integer pageSize) {
		Object[] args0 = new Object[] { account_id, type, device_id, view_id };
		String sqlCount = "SELECT COUNT(*) FROM real_his_cfg a WHERE  account_id=?  AND a.data_type = ? AND a.bind_state=1 AND a.plc_id IN ( SELECT plc_id FROM plc_info WHERE device_id = ?)AND id NOT IN ( SELECT cfg_id FROM view_account_role WHERE view_id = ?)";
		int totalRecord = jdbcTemplate.queryForObject(sqlCount, args0, Integer.class);
		Page<RealHisCfg> page = new Page<RealHisCfg>(pageIndex, pageSize, totalRecord);

		Object[] args = new Object[] { account_id, type, device_id, view_id, page.getStartIndex(), page.getPageSize() };
		String sql = "SELECT  a.id, a.state,a.name ,a.digit_count,a.addr , a.addr_type,a.describe FROM real_his_cfg a WHERE a.account_id =? AND a.data_type = ?  AND a.bind_state=1 AND a.plc_id IN ( SELECT plc_id FROM plc_info WHERE device_id = ?)AND id NOT IN ( SELECT cfg_id FROM view_account_role WHERE view_id = ?) LIMIT ?,?";
		List<RealHisCfg> list = jdbcTemplate.query(sql, args, new RowMapper() {
			@Override
			public Object mapRow(ResultSet resultSet, int i) throws SQLException {
				RealHisCfg model = new RealHisCfg();
				model.id = resultSet.getLong("id");
				model.state = resultSet.getInt("state");
				model.name = resultSet.getString("name");
				model.digit_count = resultSet.getString("digit_count");
				model.addr = resultSet.getString("addr");
				model.addr_type = resultSet.getInt("addr_type");
				model.describe = resultSet.getString("describe");
				return model;
			}
		});
		page.setList(list);
		System.out.println(list);
		return page;
	}

	/*
	 * 该盒子下的余下监控点（报警） SELECT COUNT(*) FROM alarm_cfg a WHERE account_id =1000017
	 * AND a.plc_id IN ( SELECT plc_id FROM plc_info WHERE device_id = 1)AND
	 * a.alarmcfg_id NOT IN ( SELECT cfg_id FROM view_account_role WHERE view_id
	 * = 1000021)
	 */
	public Page<AlarmCfg> getViewAlarmCfgByViewAndDeivceId(long account_id, long view_id, Integer device_id,
			Integer pageIndex, Integer pageSize) {
		Object[] args0 = new Object[] { account_id, device_id, view_id };
		String sqlCount = "SELECT COUNT(*) FROM alarm_cfg a WHERE account_id =?  AND a.bind_state=1 AND a.plc_id IN ( SELECT plc_id FROM plc_info WHERE device_id = ?)AND a.alarmcfg_id NOT IN (SELECT cfg_id FROM view_account_role WHERE view_id = ?)";
		int totalRecord = jdbcTemplate.queryForObject(sqlCount, args0, Integer.class);
		Page<AlarmCfg> page = new Page<AlarmCfg>(pageIndex, pageSize, totalRecord);
		Object[] args = new Object[] { account_id, device_id, view_id, page.getStartIndex(), page.getPageSize() };
		String sql = "SELECT  a.alarmcfg_id, a.state,a.name ,a.addr , a.addr_type,a.text FROM alarm_cfg a WHERE account_id =? AND a.bind_state=1 AND a.plc_id IN ( SELECT plc_id FROM plc_info WHERE device_id = ?)AND a.alarmcfg_id NOT IN (SELECT cfg_id FROM view_account_role WHERE view_id = ?)LIMIT ?,?";
		List<AlarmCfg> list = jdbcTemplate.query(sql, args, new RowMapper() {
			@Override
			public Object mapRow(ResultSet resultSet, int i) throws SQLException {
				AlarmCfg model = new AlarmCfg();
				model.alarmcfg_id = resultSet.getLong("alarmcfg_id");
				model.state = resultSet.getInt("state");
				model.name = resultSet.getString("name");
				model.addr = resultSet.getString("addr");
				model.addr_type = resultSet.getInt("addr_type");
				model.text = resultSet.getString("text");
				return model;
			}
		});
		page.setList(list);
		return page;
	}

	/*
	 * 该账户下的余下监控点（报警） select count(*) from alarm_cfg a WHERE a.account_id=? AND
	 * a.alarmcfg_id NOT IN(SELECT cfg_id FROM view_account_role where
	 * view_id=?)
	 */
	public Page<AlarmCfg> getViewAlarmCfgByView(long account_id, long view_id, Integer pageIndex, Integer pageSize) {

		Object[] args0 = new Object[] { account_id, view_id };
		String sqlCount = "select count(*) from alarm_cfg a WHERE  a.account_id=? AND a.bind_state=1 AND a.alarmcfg_id NOT IN(SELECT cfg_id FROM view_account_role where view_id=?)";
		int totalRecord = jdbcTemplate.queryForObject(sqlCount, args0, Integer.class);
		Page<AlarmCfg> page = new Page<AlarmCfg>(pageIndex, pageSize, totalRecord);
		Object[] args = new Object[] { account_id, view_id, page.getStartIndex(), page.getPageSize() };
		String sql = "SELECT  a.alarmcfg_id, a.state,a.name ,a.addr , a.addr_type,a.text FROM alarm_cfg a  WHERE  a.account_id=? AND a.bind_state=1 AND a.alarmcfg_id NOT IN(SELECT cfg_id FROM view_account_role where view_id=?)LIMIT ?,?";
		List<AlarmCfg> list = jdbcTemplate.query(sql, args, new RowMapper() {
			@Override
			public Object mapRow(ResultSet resultSet, int i) throws SQLException {
				AlarmCfg model = new AlarmCfg();
				model.alarmcfg_id = resultSet.getLong("alarmcfg_id");
				model.state = resultSet.getInt("state");
				model.name = resultSet.getString("name");
				model.addr = resultSet.getString("addr");
				model.addr_type = resultSet.getInt("addr_type");
				model.text = resultSet.getString("text");
				return model;
			}
		});
		page.setList(list);
		return page;
	}

	/*
	 * 视图账户实时历史监控点展示 SELECT * FROM view_account_role a,real_his_cfg b WHERE
	 * a.cfg_id=b.id AND a.view_id="1" AND a.cfg_type='1'AND b.data_type='0';
	 * cgf_type ： 1实时历史监控点 2报警监控点 data_type 0：实时监控点 1：历史监控点
	 */
	@Override
	public Page<ViewAccountRoleView> getViewAccountRoleViewByViewID(Integer data_type, long view_id, Integer pageIndex,
			Integer pageSize) {
		if (data_type == null || data_type < 0 || data_type > 1) {
			return null;
		}
		Object[] args = new Object[] { data_type, view_id };
		String sqlCount = "SELECT COUNT(*) FROM view_account_role a INNER JOIN real_his_cfg b ON a.cfg_id = b.id WHERE a.cfg_type = '1' AND b.bind_state=1 AND b.data_type = ? AND a.view_id =?";
		int totalCount = jdbcTemplate.queryForObject(sqlCount, args, Integer.class);
		Page<ViewAccountRoleView> page = new Page<ViewAccountRoleView>(pageIndex, pageSize, totalCount);
		Object[] args1 = new Object[] { data_type, view_id, page.getStartIndex(), page.getPageSize() };
		String sql = "SELECT a.cfg_id,a.role_type,b.name,b.addr,b.state FROM view_account_role a INNER JOIN real_his_cfg b ON a.cfg_id = b.id WHERE a.cfg_type = '1' AND b.data_type = ? AND a.view_id =? LIMIT ?,?";
		List<ViewAccountRoleView> list = new ArrayList<ViewAccountRoleView>();
		list = jdbcTemplate.query(sql, args1, new RowMapper() {
			@Override
			public Object mapRow(ResultSet resultSet, int i) throws SQLException {
				ViewAccountRoleView model = new ViewAccountRoleView();
				model.id = resultSet.getInt("cfg_id");
				model.role_type = resultSet.getInt("role_type");
				model.name = resultSet.getString("name");
				model.addr = resultSet.getString("addr");
				model.state = resultSet.getInt("state");
				return model;
			}
		});
		page.setList(list);
		return page;
	}

	/*
	 * 视图账户报警监控点展示 SELECT a.cfg_id,a.role_type,b.name,b.addr,b.state FROM
	 * view_account_role a INNER JOIN alarm_cfg b ON a.cfg_id = b.alarmcfg_id
	 * WHERE a.cfg_type = '2' AND a.view_id=1000021 LIMIT 1,5;
	 */
	public Page<ViewAccountRoleView> getAlarmViewAccountRoleViewByViewID(long view_id, Integer pageIndex,
			Integer pageSize) {
		Object[] args = new Object[] { view_id };
		String sqlCount = "SELECT COUNT(*) FROM view_account_role a INNER JOIN alarm_cfg b ON a.cfg_id = b.alarmcfg_id WHERE a.cfg_type = '2' AND b.bind_state=1 AND a.view_id=?";
		int totalCount = jdbcTemplate.queryForObject(sqlCount, args, Integer.class);
		Page<ViewAccountRoleView> page = new Page<ViewAccountRoleView>(pageIndex, pageSize, totalCount);
		Object[] args1 = new Object[] { view_id, page.getStartIndex(), page.getPageSize() };
		String sql = "SELECT a.cfg_id,a.role_type,b.name,b.addr,b.state FROM view_account_role a INNER JOIN alarm_cfg b ON a.cfg_id = b.alarmcfg_id WHERE a.cfg_type = '2'  AND a.view_id=? LIMIT ?,?";
		List<ViewAccountRoleView> list = new ArrayList<ViewAccountRoleView>();
		list = jdbcTemplate.query(sql, args1, new RowMapper() {
			@Override
			public Object mapRow(ResultSet resultSet, int i) throws SQLException {
				ViewAccountRoleView model = new ViewAccountRoleView();
				model.id = resultSet.getInt("cfg_id");
				model.role_type = resultSet.getInt("role_type");
				model.name = resultSet.getString("name");
				model.addr = resultSet.getString("addr");
				model.state = resultSet.getInt("state");
				return model;
			}
		});
		page.setList(list);
		return page;
	}

	/*
	 * 为视图账号分配监控监控点
	 * 
	 * @Param cgf_id 1)实时历史监控点 2）报警监控点
	 */
	public void setViewPoint(Integer viewId, String[] ids, String[] rights, Integer cgf_type) {
		if (ids.length <= 0) {
			return;
		}

		/*
		 * 历史 报警监控点分配 （没有权限）
		 */
		String[] sqls = new String[ids.length];
		for (int i = 0; i < ids.length; i++) {
			sqls[i] = "INSERT into view_account_role  (view_id,cfg_type,cfg_id,role_type,create_date,update_date) VALUES("
					+ viewId + "," + cgf_type + "," + ids[i] + ",0,NOW(),NOW());";
			System.out.println("执行的sql语句是:" + sqls[i]);
			jdbcTemplate.update(sqls[i]);
		}

	}

	/*
	 * 视图用户监控点解绑
	 * 
	 * @param viewId ， roleType pointId 视图账号ID 监控点类型 监控点ID
	 *
	 * sql:DELETE FROM account_dir_rel WHERE ref_id=10 AND acc_dir_id IN (SELECT
	 * id FROM account_dir WHERE account_id=123 AND type=1);
	 */
	public void deletePoint(final Integer viewId, final Integer roleType, final Integer pointId) {
		TransactionTemplate tt = new TransactionTemplate(transactionManager);
		try {
			tt.execute(new TransactionCallback() {
				@Override
				public Object doInTransaction(TransactionStatus ts) {
					String sql_deleteSqlView_Account_role = "DELETE FROM view_account_role WHERE view_id=? AND cfg_id=?";
					String sql_delect_ref = "DELETE FROM account_dir_rel  WHERE  ref_id=? AND acc_dir_id IN (SELECT id FROM account_dir WHERE account_id=? AND type=?)";
					Object[] args0 = new Object[] { viewId, pointId };
					jdbcTemplate.update(sql_deleteSqlView_Account_role, args0);
					Object[] args1 = new Object[] { pointId, viewId, roleType };
					jdbcTemplate.update(sql_delect_ref, args1);
					return true;
				}
			});
		} catch (Exception e) {
			Logger.getLogger(AccountImpl.class.getName()).log(Level.SEVERE, null, e);
			throw new BusinessException(ErrorCodeOption.Viewpoint_Dlete_False.key,
					ErrorCodeOption.Viewpoint_Dlete_False.value);
		}
	}

	@Override
	public void deletePoint(Integer roleType, long pointId) {

		String sql_deleteSqlView_Account_role = "DELETE FROM view_account_role WHERE cfg_type=? AND cfg_id=?";
		jdbcTemplate.update(sql_deleteSqlView_Account_role, new Object[] { roleType, pointId });

	}

	/*
	 * 视图账户监控点权限设置
	 * 
	 * @param viewId pointId roleType 视图账户ID 监控点ID 权限：0无权限 1只读 3读写 UPDATE
	 * view_account_role SET role_type="1" WHERE view_id=111 AND cfg_id=12 ;
	 */
	public void updateViewPointRoleType(Integer viewId, Integer pointId, Integer roleType) {
		String sql = "UPDATE view_account_role SET role_type=? WHERE view_id=? AND cfg_id=?";
		Object[] args = new Object[] { roleType, viewId, pointId };
		jdbcTemplate.update(sql, args);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.wecon.box.api.ViewAccountRoleApi#deletePoint(java.lang.Integer,
	 * java.lang.Integer)
	 */

	/*
	* 解绑盒子  删除盒子下的视图
	* */
	public void deleteViewAccountRoleByCfgId(final List<Integer> cfgIds,final Integer type)
	{
		String sql="DELETE FROM view_account_role WHERE cfg_id=? AND cfg_type=?";
		if (cfgIds.size() != 0) {
			jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps, int i) throws SQLException {
					ps.setInt(1,cfgIds.get(i));
					ps.setInt(2,type);
				}
				@Override
				public int getBatchSize() {
					return 0;
				}
			});
		}
	}
}