package com.wecon.box.impl;

import com.wecon.box.api.ViewAccountRoleApi;
import com.wecon.box.entity.Page;
import com.wecon.box.entity.ViewAccountRole;
import com.wecon.box.entity.ViewAccountRoleView;
import com.wecon.box.filter.ViewAccountRoleFilter;
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

	@Override
	public long saveViewAccountRole(ViewAccountRole model) {
		return 0;
	}

	@Override
	public boolean updateViewAccountRole(ViewAccountRole model) {
		return false;
	}

	@Override
	public ViewAccountRole getViewAccountRole(long view_id) {
		return null;
	}

	@Override
	public void delViewAccountRole(long view_id) {

	}

	@Override
	public Page<ViewAccountRole> getViewAccountRoleList(ViewAccountRoleFilter filter, int pageIndex, int pageSize) {
		return null;
	}

	/*
        * SELECT * FROM view_account_role a,real_his_cfg b WHERE a.cfg_id=b.id AND a.view_id="1" AND a.cfg_type='1'AND b.data_type='0';
        * cgf_type ：
        *           1实时历史监控点
        *           2报警监控点
        * data_type
        *           0：实时监控点
        *           1：历史监控点
        * */
			@Override
			public List<ViewAccountRoleView> getViewAccountRoleViewByViewID (Integer data_type,long view_id) {
				if (data_type == null || data_type < 0 || data_type > 1) {
					return null;
				}
				String sql = "SELECT a.cfg_id,a.role_type,b.name,b.addr,b.state FROM view_account_role a INNER JOIN real_his_cfg b ON a.cfg_id = b.id WHERE a.cfg_type = '1' AND b.data_type = ? AND a.view_id =?";


				Object[] args = new Object[]{data_type, view_id};
				List<ViewAccountRoleView> list = new ArrayList<ViewAccountRoleView>();
				return jdbcTemplate.query(sql, args, new RowMapper() {
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
			}
	}
