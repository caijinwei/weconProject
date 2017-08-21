package com.wecon.box.api;

import com.wecon.box.entity.DevBindUser;
import com.wecon.box.filter.DevBindUserFilter;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author lanpenghui
 * 2017年8月1日
 */
@Component
public interface DevBindUserApi {
	/**
	 * 创建用户关联设备
	 *
	 * @param model
	 * @return
	 */
	public void saveDevBindUser(DevBindUser model);


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

	/*
	* 根据设备号查询记录
	* */
	public int findByDevId(long device_id);

	/*
	* 查看是否绑定（表中是否有记录）
	* @param    device_id     account_id
	* 		    设备号id	用户账户id
	* */
	public boolean isRecord(Integer device_id,long account_id);
}
