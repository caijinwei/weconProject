package com.wecon.box.entity;

import java.sql.Timestamp;

/**
 * @author lanpenghui 2017年8月1日
 */
public class AlarmCfgData {
	/**
	 * `ALARM_CFG_ID` bigint(20) NOT NULL COMMENT '报警点配置ID', `MONITOR_TIME`
	 * datetime NOT NULL COMMENT '监控时间，将终端上传的时间转成datetime，便于查询', `VALUE`
	 * varchar(64) DEFAULT NULL, `CREATE_DATE` datetime DEFAULT NULL, `state`
	 * int(11) DEFAULT NULL COMMENT '状态',
	 */
	public long alarm_cfg_id;
	public Timestamp monitor_time;
	public String value;
	public int state;
	public Timestamp create_date;
	public int alarm_type; //报警类型：1-触发报警，0-解除报警
	
}
