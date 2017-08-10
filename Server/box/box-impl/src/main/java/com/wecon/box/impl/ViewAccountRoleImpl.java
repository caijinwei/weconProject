package com.wecon.box.impl;

import com.wecon.box.api.ViewAccountRoleApi;
import com.wecon.box.entity.AlarmCfgData;
import com.wecon.box.entity.ViewAccountRole;
import com.wecon.box.entity.ViewAccountRoleView;
import com.wecon.box.filter.ViewAccountRoleFilter;
import com.wecon.box.impl.AlarmCfgDataImpl.DefaultAlarmCfgDataRowMapper;
import com.wecon.common.util.CommonUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by caijinw on 2017/8/10.
 */
@Component
public class ViewAccountRoleImpl implements ViewAccountRoleApi {
	@Autowired
	JdbcTemplate jdbcTemplate;
	private final String SEL_COL ="";

	@Override
	public List<ViewAccountRole> getViewAccountRole(ViewAccountRoleFilter filter) {
		String sql = "select " + SEL_COL + " from alarm_cfg_data where 1=1";
		String alarmcfgsql="select name from ";
		StringBuffer condition = new StringBuffer("");
		List<Object> params = new ArrayList<Object>();
		if (filter.view_id > 0) {
			condition.append(" and view_id = ? ");
			params.add(filter.view_id);
		}
		if (filter.cfg_type > -1) {
			condition.append(" and cfg_type = ? ");
			params.add(filter.cfg_type);
		}
		if (filter.cfg_id > 0) {
			condition.append(" and cfg_id = ? ");
			params.add(filter.cfg_id);
		}
		if (filter.role_type > -1) {
			condition.append(" and role_type = ? ");
			params.add(filter.role_type);
		}
		sql += condition;
//		List<ViewAccountRole> list = jdbcTemplate.query(sql, params.toArray(), new DefaultAlarmCfgDataRowMapper());
		return null;
		
		
		
		
		
		
		
		
		
		
	}


	/*
	 * SELECT * FROM view_account_role a,real_his_cfg b WHERE a.cfg_id=b.id AND
	 * a.view_id="1" AND a.cfg_type='1'AND b.data_type='0'; cgf_type ： 1实时历史监控点
	 * 2报警监控点 data_type 0：实时监控点 1：历史监控点
	 */
	@Override
	public List<ViewAccountRoleView> getViewAccountRoleViewByViewID(Integer data_type, long view_id) {
		if (data_type == null || data_type < 0 || data_type > 1) {
			return null;
		}
		String sql = "SELECT * FROM view_account_role a,real_his_cfg b WHERE a.cfg_id=b.id  AND a.cfg_type='1'AND b.data_type=? AND a.view_id=?";
		Object[] args = new Object[] { data_type, view_id };
		List<ViewAccountRoleView> list = new ArrayList<ViewAccountRoleView>();
		return jdbcTemplate.query(sql, args, new RowMapper() {
			@Override
			public Object mapRow(ResultSet resultSet, int i) throws SQLException {
				ViewAccountRoleView model = new ViewAccountRoleView();
				model.role_type = resultSet.getInt("role_type");
				model.name = resultSet.getString("name");
				model.addr = resultSet.getString("addr");
				model.state = resultSet.getInt("state");
				return model;
			}
		});

	}

}
