package com.wecon.box.api;

import java.util.List;

import org.springframework.stereotype.Component;
/**
 * @author lanpenghui
 * 2017年8月1日
 */

import com.wecon.box.entity.AlarmTrigger;
import com.wecon.box.filter.AlarmTriggerFilter;

@Component
public interface AlarmTriggerApi {

	/**
	 * 根据filter取报警触发条件
	 * 
	 * @param filter
	 * @return
	 */
	public List<AlarmTrigger> getAlarmTrigger(AlarmTriggerFilter filter);
	
	
	public AlarmTrigger getAlarmTrigger(long alarmtrig_id);
	public boolean upAlarmTrigger(AlarmTrigger alarmTrigger);
	public void saveAlarmTrigger(List<AlarmTrigger> listalarmTrigger);
	public void delAlarmTrigger(long alarmcfg_id);

}
