package com.wecon.box.api;

import org.springframework.stereotype.Component;
/**
 * @author lanpenghui
 * 2017年8月1日
 */

import com.wecon.box.entity.AlarmTrigger;
@Component
public interface AlarmTriggerApi {
	/**
	 * 保存报警触发条件
	 *
	 * @param model
	 * @return
	 */
	public long saveAlarmTrigger(AlarmTrigger model);

	/**
	 * 更新报警触发条件
	 *
	 * @param model
	 * @return
	 */
	public boolean updateAlarmTrigger(AlarmTrigger model);

	/**
	 * 根据alarmtrig_id取某个报警触发条件
	 *
	 * @param alarmtrig_id
	 * @return
	 */
	public AlarmTrigger getAlarmTrigger(long alarmtrig_id);

	/**
	 * 根据alarmtrig_id删除某个报警触发条件
	 * 
	 * @param alarmtrig_id
	 * 
	 */
	public void delAlarmTrigger(long alarmtrig_id);
}
