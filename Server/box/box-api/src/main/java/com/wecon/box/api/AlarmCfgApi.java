package com.wecon.box.api;

import com.wecon.box.entity.AlarmCfg;
import com.wecon.box.entity.AlarmCfgExtend;
import com.wecon.box.entity.AlarmCfgTrigger;
import com.wecon.box.entity.Page;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lanpenghui 2017年8月1日
 */

@Component
public interface AlarmCfgApi {

	/**
	 * 保存报警配置
	 * 
	 * @param alarmCfg
	 * @return
	 */
	public long saveAlarmCfg(AlarmCfg alarmCfg);

	/**
	 * 根据account_id取报警数据配置
	 *
	 * @return
	 */
	public List<AlarmCfg> getAlarmCfg(long account_id);

	/**
	 * 获取报警配置对象
	 * 
	 * @param alarmcfg_id
	 * @return
	 */
	public AlarmCfg getAlarmcfg(long alarmcfg_id);

	/**
	 * 根据状态获取报警数据配置
	 * 
	 * @param state
	 * @return
	 */
	List<AlarmCfgExtend> getAlarmCfgExtendListByState(Object...  state);

	public boolean upAlarmCfg(AlarmCfg alarmCfg);

	/**
	 * 批量更新状态
	 * 
	 * @param updList
	 * @return
	 */
	boolean batchUpdateState(final List<String[]> updList);

	/**
	 * 批量删除报警配置根据plc_id
	 * @param ids
	 * @return
     */
	boolean batchDeleteByPlcId(final List<Long> ids);

	/**
	 * 批量删除报警配置根据alaramcfg_id
	 * @param ids
	 * @return
     */
	boolean batchDeleteById(final List<Long> ids);

	/**
	 * 根据update_date查找需要删除的id列表
	 * @param delArgList
	 * @return
	 */
	List<Long> getDeleteIdsByUpdTime(List<String[]> delArgList);
	/*
	 * 根据device_id 设备号ID查询alarmCfgId
	 */
	public ArrayList<Integer> findAlarmCfgIdSBydevice_id(Integer device_id);

	/*
	 * 根据alramsCfgId 批量设置bind_state
	 */
	public void setBind_state(final int[] alaramCfgId, final Integer state);

	Page<AlarmCfgTrigger> getAlarmCfgDataList(long account_id,long groupId,long device_id, long bind_state,int pageIndex, int pageSize);
	Page<AlarmCfgTrigger> getAlarmList(long device_id, long bind_state,int pageIndex, int pageSize);

	/*
	* 盒子跟换用户  监控点迁移
	* */
	public boolean updatePointAccAndState(long accountId,long deviceId) ;
	/**
	 * 通过用户获取当前报警盒子数
	 * 
	 * @param account_id
	 * @return
	 */
	public int getAlamBxo(long account_id);
}
