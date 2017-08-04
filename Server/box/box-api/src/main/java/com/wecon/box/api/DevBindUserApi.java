package com.wecon.box.api;

import org.springframework.stereotype.Component;
/**
 * @author lanpenghui
 * 2017年8月1日
 */

import com.wecon.box.entity.DevBindUser;
@Component
public interface DevBindUserApi {
	/**
	 * 创建用户关联设备
	 *
	 * @param model
	 * @return
	 */
	public long SaveDevBindUser(DevBindUser model);

	/**
	 * 更新用户关联设备
	 *
	 * @param model
	 * @return
	 */
	public boolean updateDevBindUser(DevBindUser model);

	
	/**
	 * 查询某条用户关联设备信息
	 * @param account_id
	 * @param device_id
	 * @return
	 */
	public DevBindUser getDevBindUser(long account_id,long device_id);

	/**
	 * 删除某条用户关联设备信息
	 * @param account_id
	 * @param device_id
	 */
	public void delDevBindUser(long account_id,long device_id);
}
