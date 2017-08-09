package com.wecon.box.api;

import org.springframework.stereotype.Component;
/**
 * @author lanpenghui
 * 2017年8月1日
 */

import com.wecon.box.entity.AccountRelation;
@Component
public interface AccountRelationApi {
	/**
	 * 创建帐号之间关系描述
	 *
	 * @param model
	 * @return
	 */
	public long saveAccountRelation(AccountRelation model);

	/**
	 * 更新帐号之间关系描述
	 *
	 * @param model
	 * @return
	 */
	public boolean updateAccountRelation(AccountRelation model);

	/**
	 * 根据manager_id取某个帐号之间关系描述
	 * 
	 * @param manager_id
	 * @param view_id
	 * @return
	 */
	public AccountRelation getAccountRelation(long manager_id, long view_id);

	/**
	 * 根据manager_id删除某个帐号之间关系描述
	 * 
	 * @param manager_id
	 * @param view_id
	 */
	public void delAccountRelation(long manager_id, long view_id);
}
