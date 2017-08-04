package com.wecon.box.api;

import org.springframework.stereotype.Component;
/**
 * @author lanpenghui
 * 2017年8月1日
 */

import com.wecon.box.entity.AlarmCfg;

@Component
public interface AlarmCfgApi {
	/**
	 * 保存报警数据配置
	 *
	 * @param model
	 * @return
	 */
	public long SaveAlarmCfg(AlarmCfg model);

	/**
	 * 更新报警数据配置
	 *
	 * @param model
	 * @return
	 */
	public boolean updateAlarmCfg(AlarmCfg model);

	/**
	 * 根据alarmcfg_id取某个报警数据配置
	 *
	 * @param alarmcfg_id
	 * @return
	 */
	public AlarmCfg getAlarmCfg(long alarmcfg_id);

	/**
	 * 根据alarmcfg_id删除某个报警数据配置
	 * 
	 * @param alarmcfg_id
	 * 
	 */
	public void delAlarmCfg(long alarmcfg_id);
}
