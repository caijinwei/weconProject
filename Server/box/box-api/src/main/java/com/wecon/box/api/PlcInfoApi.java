package com.wecon.box.api;

import java.util.List;

import org.springframework.stereotype.Component;
import com.wecon.box.entity.PlcInfo;

import java.util.List;

/**
 * @author lanpenghui
 * 2017年8月1日
 */
@Component
public interface PlcInfoApi {
	/**
	 * 保存通讯口信息
	 *
	 * @param model
	 * @return
	 */
	public long savePlcInfo(PlcInfo model);

	/**
	 * 更新通讯口信息
	 *
	 * @param model
	 * @return
	 */
	public boolean updatePlcInfo(PlcInfo model);

	/**
	 * 根据plc_id取某个通讯口信息
	 *
	 * @param plc_id
	 * @return
	 */
	public PlcInfo getPlcInfo(long plc_id);
	
	/**
	 * 根据device_id取通讯口信列表
	 * @param device_id
	 * @return
	 */
	
	public List<PlcInfo>getListPlcInfo(long device_id);

	/**
	 * 根据plc_id删除某个通讯口信息
	 * 
	 * @param plc_id
	 * 
	 */
	public void delPlcInfo(long plc_id);

	/*
	* 展示所有通讯口配置信息的部分信息
	*
	* */
	public List<PlcInfo> showAllPlcInfoByDeviceId(Integer deviceId);
}
