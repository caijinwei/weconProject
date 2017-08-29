package com.wecon.box.api;

import com.wecon.box.entity.AlarmCfg;
import com.wecon.box.entity.AlarmCfgExtend;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lanpenghui
 * 2017年8月1日
 */

@Component
public interface AlarmCfgApi {

	/**
	 * 根据account_id取报警数据配置
	 *
	 * @return
	 */
	public List<AlarmCfg> getAlarmCfg(long account_id);

	/**
	 * 根据状态获取报警数据配置
	 * @param state
	 * @return
     */
	List<AlarmCfgExtend> getAlarmCfgExtendListByState(int state);

	/**
	 * 批量更新状态
	 * @param updList
	 * @return
	 */
	boolean batchUpdateState(final List<int[]> updList);

	/*
	* 根据device_id 设备号ID查询alarmCfgId
	* */
	public ArrayList<Integer> findAlarmCfgIdSBydevice_id(Integer device_id);

	/*
	* 根据alramsCfgId 批量设置bind_state
	* */
	public void setBind_state(final int[] alaramCfgId, final Integer state);
}
