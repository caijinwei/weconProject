package com.wecon.box.api;

import java.util.List;

import org.springframework.stereotype.Component;
/**
 * @author lanpenghui
 * 2017年8月1日
 */

import com.wecon.box.entity.DevBindUser;
import com.wecon.box.filter.DevBindUserFilter;
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
	 * 查询某条用户关联设备信息
	 * @param devBindUser
	 * @return
	 */
	public List<DevBindUser> getDevBindUser(DevBindUserFilter devBindUser);

	/**
	 * 删除某条用户关联设备信息
	 * @param account_id
	 * @param device_id
	 */
	public void delDevBindUser(long account_id,long device_id);
}
