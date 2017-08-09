package com.wecon.box.entity;

import java.sql.Timestamp;
/**
 * @author lanpenghui
 * 2017年8月1日
 */
public class DevBindUser {
	/**
	 * `ACCOUNT_ID` bigint(20) NOT NULL, `DEVICE_ID` bigint(20) NOT NULL COMMENT
	 * '设备ID', `CREATE_DATE` datetime DEFAULT NULL
	 */
	public long account_id;
	public long device_id;
	public Timestamp create_date;

}
