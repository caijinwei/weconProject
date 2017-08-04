package com.wecon.box.api;

import org.springframework.stereotype.Component;
/**
 * @author lanpenghui
 * 2017年8月1日
 */

import com.wecon.box.entity.DevFirm;
@Component
public interface DevFirmApi {

	/**
	 * 保存设备固件版本信息
	 *
	 * @param model
	 * @return
	 */
	public long SaveDevFirm(DevFirm model);

	/**
	 * 更新设备固件版本信息
	 *
	 * @param model
	 * @return
	 */
	public boolean updateDevFirm(DevFirm model);

	/**
	 * 根据f_id取某个设备固件版本信息
	 *
	 * @param f_id
	 * @return
	 */
	public DevFirm getDevFirm(long f_id);
	/**
	 * 根据device_id取某个设备固件版本信息
	 *
	 * @param device_id
	 * @return
	 */
	public DevFirm getDevFirm_device_id(long device_id);


	/**
	 * 根据f_id删除某个设备固件版本信息
	 * 
	 * @param f_id
	 * 
	 */
	public void delDevFirm(long f_id);
}
