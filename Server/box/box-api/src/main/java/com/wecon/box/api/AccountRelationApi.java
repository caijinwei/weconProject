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
 * 根据视图id,获取管理者id
 * @param view_id
 * @return
 */
	public AccountRelation getAccountRelation(long view_id);

}
