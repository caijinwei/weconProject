package com.wecon.box.api;

import org.springframework.stereotype.Component;
import com.wecon.box.entity.Page;
import com.wecon.box.entity.ViewAccountRole;
import com.wecon.box.filter.ViewAccountRoleFilter;
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
}
