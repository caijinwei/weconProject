package com.wecon.box.api;

import org.springframework.stereotype.Component;
/**
 * @author lanpenghui
 * 2017年8月1日
 */

import com.wecon.box.entity.AccountDirRel;
@Component
public interface AccountDirRelApi {
	/**
	 * 创建分组所关联的监控点
	 *
	 * @param model
	 * @return
	 */
	public long saveAccountDirRel(AccountDirRel model);

	/**
	 * 更新分组所关联的监控点
	 *
	 * @param model
	 * @return
	 */
	public boolean updateAccountDirRel(AccountDirRel model);

	/**
	 * 根据acc_dir_id取某个分组所关联的监控点
	 * @param acc_dir_id
	 * @param ref_id
	 * @return
	 */
	public AccountDirRel getAccountDirRel(long acc_dir_id,long ref_id);

	/**
	 * 根据acc_dir_id删除某个分组所关联的监控点
	 * @param acc_dir_id
	 * @param ref_id
	 */
	public void delAccountDir(long acc_dir_id,long ref_id);

/*
*查询是否有分组记录
* @param acc_dir_id
*
* */
	public int findRecordingByAccDirid(long acc_dir_id);

}
