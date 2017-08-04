package com.wecon.box.entity;

import java.sql.Timestamp;
/**
 * @author lanpenghui
 * 2017年8月1日
 */
public class DevFirm {
/**
 *  `F_ID` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `DEVICE_ID` bigint(20) DEFAULT NULL COMMENT '设备ID',
  `F_NAME` varchar(64) NOT NULL COMMENT '固件名称',
  `F_VER` varchar(64) NOT NULL COMMENT '固件版本号',
  `CREATE_DATE` datetime DEFAULT NULL,
  `UPDATE_DATE` datetime DEFAULT NULL,
 */
	public long f_id;
	public long device_id;
	public String f_name;
	public String f_ver;
	public Timestamp create_date;
	public Timestamp update_date;
}
