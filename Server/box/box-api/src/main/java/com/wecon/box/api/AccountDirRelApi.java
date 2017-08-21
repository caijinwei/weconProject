package com.wecon.box.api;

import com.wecon.box.entity.AccountDirRel;
import org.springframework.stereotype.Component;

/**
 * @author lanpenghui
 * 2017年8月1日
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


	/**
	 * 根据acc_dir_id取某个分组所关联的监控点
	 * @param acc_dir_id
	 * @param ref_id
	 * @return
	 */
	public AccountDirRel getAccountDirRel(long acc_dir_id,long ref_id);
	/**
	 * 更新别名
	 * @param acc_dir_id
	 * @param ref_id
	 * @return
	 */
	public boolean upAccountDirRel(AccountDirRel model);

	/**
	 * 根据acc_dir_id，ref_id删除某个分组所关联的监控点
	 * @param acc_dir_id
	 * @param ref_id
	 */
	public void delAccountDir(long acc_dir_id,long ref_id);

	/*
	* 删除用户绑定下的全部盒子
	* @param device_id       account_id
	* 		盒子id			用户id
	* delete FROM account_dir_rel where ref_id=1 AND acc_dir_id IN (SELECT id FROM account_dir WHERE account_id=1)
	* */
	public void deleteDeviceRel(Integer device_id,long account_id);
}
