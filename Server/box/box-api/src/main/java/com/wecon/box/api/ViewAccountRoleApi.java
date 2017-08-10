package com.wecon.box.api;

import com.wecon.box.entity.ViewAccountRoleView;
import org.springframework.stereotype.Component;
import com.wecon.box.entity.Page;
import com.wecon.box.entity.ViewAccountRole;
import com.wecon.box.filter.ViewAccountRoleFilter;

import java.util.List;

/**
 * @author lanpenghui
 * 2017年8月1日
 */
@Component
public interface ViewAccountRoleApi {
	/**
	 * 保存视图账号
	 *
	 * @param model
	 * @return
	 */
	public long SaveViewAccountRole(ViewAccountRole model);

	/**
	 * 更新视图账号
	 *
	 * @param model
	 * @return
	 */
	public boolean updateViewAccountRole(ViewAccountRole model);

	/**
	 * 根据view_id取某个视图账号
	 *
	 * @param view_id
	 * @return
	 */
	public ViewAccountRole getViewAccountRole(long view_id);

	/**
	 * 根据view_id删除某个视图账号
	 * 
	 * @param view_id
	 * 
	 */
	public void delViewAccountRole(long view_id);
	   /**
     * 获取用户的分页列表
     *
     * @param filter
     * @param pageIndex
     * @param pageSize
     * @return
     */
    Page<ViewAccountRole> getViewAccountRoleList(ViewAccountRoleFilter filter, int pageIndex, int pageSize);

	/*
	* @param view_id
	* 根据视图账号ID获取
	* 	viewAccountRole 视图监控点 连接 realHisCfg 获取监控点name
	*
    * SELECT * FROM view_account_role a,real_his_cfg b WHERE a.cfg_id=b.id AND a.view_id="1" AND a.cfg_type='1'AND b.data_type='0';
    * cgf_type ：
    *           1实时历史监控点
    *           2报警监控点
    * data_type
    *           0：实时监控点
    *           1：历史监控点
    * */
	public List<ViewAccountRoleView> getViewAccountRoleViewByViewID(Integer data_type, long view_id);

}
