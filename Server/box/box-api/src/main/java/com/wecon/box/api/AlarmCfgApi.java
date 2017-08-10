package com.wecon.box.api;

import java.util.List;

import org.springframework.stereotype.Component;
/**
 * @author lanpenghui
 * 2017年8月1日
 */

import com.wecon.box.entity.AlarmCfg;

@Component
public interface AlarmCfgApi {

	/**
	 * 根据account_id取报警数据配置
	 *
	 * @param alarmcfg_id
	 * @return
	 */
	public List<AlarmCfg> getAlarmCfg(long account_id);
}
