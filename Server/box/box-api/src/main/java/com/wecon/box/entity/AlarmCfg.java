package com.wecon.box.entity;

import java.sql.Timestamp;
/**
 * @author lanpenghui
 * 2017年8月1日
 */
public class AlarmCfg {
/**
 * `ALARMCFG_ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `DATA_ID` bigint(20) DEFAULT NULL,
  `ACCOUNT_ID` bigint(20) DEFAULT NULL,
  `NAME` varchar(64) NOT NULL COMMENT '报警名称',
  `ADDR` varchar(64) NOT NULL COMMENT '0：位地址1：字地址2：双字',
  `ADDR_TYPE` int(11) DEFAULT NULL COMMENT '地址',
  `TEXT` varchar(256) DEFAULT NULL COMMENT '报警内容',
  `CONDITION_TYPE` int(11) NOT NULL COMMENT '0：一个条件1：与 2：或',
  `STATE` int(11) DEFAULT NULL COMMENT '状态:1-启用; 0-未启用',
  `CREATE_DATE` datetime DEFAULT NULL,
  `UPDATE_DATE` datetime DEFAULT NULL,
  `bind_state` int(11) DEFAULT '1' COMMENT '1.绑定状态 0.解绑状态',
 */
	public long alarmcfg_id;
	public long data_id;
	public long  account_id;
	public long plc_id;
	public String name;
	public String addr;
	public int addr_type;
	public String text;
	public int condition_type;
	public int state;
	public int bind_state;//1.绑定状态 0.解绑状态
	public Timestamp create_date;
	public Timestamp update_date;
	
	
}
