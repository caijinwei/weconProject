package com.wecon.box.api;

import com.wecon.box.entity.AccountRelation;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author lanpenghui
 * 2017年8月1日
 */

@Component
public interface AccountRelationApi {
    /**
     * 根据视图id,获取管理者id
     *
     * @param view_id
     * @return
     */
    public AccountRelation getAccountRelation(long view_id);

	/*
	* 根据管理账户获取视图账户全部id
	* @param managerId
	* @return AccountRelation
	* */
    public List<AccountRelation> getAccountRelationByManagerAccId(long managerId);

}
