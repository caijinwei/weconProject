package com.wecon.box.filter;

import java.sql.Timestamp;

/**
 * @author lanpenghui 2017年8月4日上午9:25:39
 */
public class AlarmCfgDataFilter {
	public long alarm_cfg_id;
	public Timestamp monitor_time;
	public String value;
	public int state;
	public Timestamp create_date;
	public String name;//配置中的名称
	public long account_id;
	public String start_date;// 开始时间
	public String end_date;// 结束时间
}
