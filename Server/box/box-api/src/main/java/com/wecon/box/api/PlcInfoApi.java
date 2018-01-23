package com.wecon.box.api;

import com.wecon.box.entity.PlcExtend;
import com.wecon.box.entity.PlcInfo;
import com.wecon.box.entity.PlcInfoDetail;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

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

	/**
	 * 根据状态获取PlcExtend列表
	 * @param state
	 * @return
     */
	List<PlcExtend> getPlcExtendListByState(Object... state);

	/**
	 * 批量更新状态
	 * @param updList
	 * @return
     */
	boolean batchUpdateState(final List<String[]> updList);

	/**
	 * 批量更新file_md5
	 * @param updList
	 * @return
     */
	boolean batchUpdateFileMd5(final List<String[]> updList);

	/**
	 * 批量删除Plc
	 * @param ids
	 * @return
     */
	boolean batchDeletePlc(final List<Long> ids);

	/**
	 * 根据update_date查找需要删除的id列表
	 * @param delArgList
	 * @return
     */
	List<Long> getDeleteIdsByUpdTime(List<String[]> delArgList);

	/*
	* 通讯协议是否已经存在
	* */
	public List<PlcInfo> getPortState(long device_id, String port);

	/*
	* 解绑plc
	* */
	public void unBundledPlc(Integer plcId);

    /*
    * 展示单个plc
    * @Params plc_id
    * */
    public PlcInfo findPlcInfoByPlcId(long plcId);

	/*
	* 根据设备id查看plcInfoDtail
	* */
	public List<PlcInfoDetail> getListPlcInfoDetail(long device_id);

	/*
	* 更新plcdetaled
	* */
	public void savePlcInfoDetail(PlcInfoDetail detail);

	/*
	* 查找plcDetail
	* */
	public PlcInfoDetail getPlcInfoDetail(long plc_id);

	/*
	* 获取 非(删除配置)state!=3  的plcDetail
	* */
	public List<PlcInfoDetail> getActiveListPlcInfoDetail(long device_id);

	/*
	* 复制盒子通讯口配置
	*
	* */
	public Map<Long,Long> copyDeviceCom(long fromDeviceId, long toDeviceId);

}
