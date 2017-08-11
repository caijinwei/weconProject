package com.wecon.box.impl;

import com.wecon.box.api.ViewAccountRoleApi;
import com.wecon.box.entity.Page;
import com.wecon.box.entity.RealHisCfg;
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
	public void delViewAccountRole(long view_id) {
	}
	@Override
	public Page<ViewAccountRole> getViewAccountRoleList(ViewAccountRoleFilter filter, int pageIndex, int pageSize) {
		return null;
	}
	/*
	* @Param view_id  account_id
	* 		视图账号ID  管理员账户ID
	*
	*  获取 该视图账户中未有的监控点
	*  	需要分页
	*  	id   state  name  digit_count addr  addr_type desceibe
	* */
	@Override
	public List<RealHisCfg> getViewRealHisCfgByViewAndAccId(long view_id,long account_id,Integer pageIndex, Integer pageSize)
	{
		/*
		* 默认pageIndex为1
		* 默认pageSize为5
		*
		* */
		if(pageIndex==null)
		{
			pageIndex=1;
		}else if(pageSize==null)
		{
			pageSize=5;
		}
		Object[] args=new Object[]{account_id,view_id,pageIndex,pageSize};
		String sql="select a.id, a.state,a.name , a.digit_count,a.addr , a.addr_type,a.describe from real_his_cfg a WHERE account_id=? AND id NOT IN(SELECT cfg_id FROM view_account_role where view_id=?)LIMIT ?,?";
		return jdbcTemplate.query(sql, args, new RowMapper()
		{
			@Override
			public Object mapRow(ResultSet resultSet, int i) throws SQLException {
				RealHisCfg model=new RealHisCfg();
				model.id=resultSet.getLong("id");
				model.state=resultSet.getInt("state");
				model.name=resultSet.getString("name");
				model.digit_count=resultSet.getString("digit_count");
				model.addr=resultSet.getString("addr");
				model.addr_type=resultSet.getInt("addr_type");
				model.describe=resultSet.getString("describe");
				return model;
			}
		});
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
