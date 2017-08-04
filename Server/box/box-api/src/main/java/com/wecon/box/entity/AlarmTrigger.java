package com.wecon.box.entity;

import java.sql.Timestamp;
/**
 * @author lanpenghui
 * 2017年8月1日
 */
public class AlarmTrigger {
/**
 * `ALARMTRIG_ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `ALARMCFG_ID` bigint(20) DEFAULT NULL,
  `TYPE` int(11) NOT NULL COMMENT '0：等于 1：不等于2：大于 3：大于等于 4：小于5：小于等于6：ON7：OFF',
  `VALUE` varchar(32) NOT NULL COMMENT '触发值',
  `CREATE_DATE` datetime DEFAULT NULL,
  `UPDATE_DATE` datetime DEFAULT NULL,
 */
	public long alarmtrig_id;
	public long alarmcfg_id;
	public int type;
	public String value;
	public Timestamp create_data;
	public Timestamp update_data;
	
}
