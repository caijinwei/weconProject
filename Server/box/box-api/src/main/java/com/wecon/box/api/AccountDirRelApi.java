package com.wecon.box.api;

import com.wecon.box.entity.AccountDirRel;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author lanpenghui 2017年8月1日
 */
@Component
public interface AccountDirRelApi {
	/**
	 * 创建分组所关联的监控点
	 *
	 * @param model
	 * @return
	 */
	public void saveAccountDirRel(AccountDirRel model);

	public void saveAccountDirRel(List<AccountDirRel> model);

	/**
	 * 根据acc_dir_id取某个分组所关联的监控点
	 *
	 * @param acc_dir_id
	 * @param ref_id
	 * @return
	 */
	public AccountDirRel getAccountDirRel(long acc_dir_id, long ref_id);
	public List<AccountDirRel> getAccountDirRel(long acc_dir_id);

	/**
	 * 更新别名
	 * 
	 * @param model
	 * @return
	 */
	public boolean upAccountDirRel(AccountDirRel model);

	/**
	 * 根据acc_dir_id，ref_id删除某个分组所关联的监控点
	 *
	 * @param acc_dir_id
	 * @param ref_id
	 */
	public void delAccountDir(long acc_dir_id, long ref_id);

	/*
	 * 删除用户绑定下的全部盒子
	 * 
	 * @param device_id account_id 盒子id 用户id delete FROM account_dir_rel where
	 * ref_id=1 AND acc_dir_id IN (SELECT id FROM account_dir WHERE
	 * account_id=1)
	 */
	public void deleteDeviceRel(Integer device_id, long account_id);

	/*
	 * 删除AccountDirRel记录
	 */
	public int deleteAccountDirRel(AccountDirRel accountDirRel);

	/*
	 * 更新分组信息
	 */
	public int updateAccountDirRel(AccountDirRel newAccDirRel, AccountDirRel oldAccDirRel);

	   /*
    * 解除视图账户和监控点分组下的关系
    * DELETE FROM	account_dir_rel WHERE	acc_dir_id IN (SELECT	id	FROM	account_dir	WHERE	type=1 AND	account_id IN(SELECT view_id FROM account_relation WHERE manager_id =11)) AND ref_id IN (1,2);
    * */

	public void deleteViewAccAndPointRel(Integer type,Integer managerId,List<Integer> points);


	/*
  * 删除管理员与盒子分组关系
  * DELETE FROM account_dir_rel WHERE ref_id=1111 AND acc_dir_id IN (SELECT id FROM account_dir WHERE type=0 AND account_id=12);
   * */
	public void deleteAccDeviceRel(Integer deviceId, Integer accountId);
}
