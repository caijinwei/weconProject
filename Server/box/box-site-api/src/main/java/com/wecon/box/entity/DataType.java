package com.wecon.box.entity;

import java.sql.Timestamp;
/**
 * @author lanpenghui
 * 2017年8月1日
 */
public class DataType {
	/**
	 * `DATA_ID` bigint(20) NOT NULL AUTO_INCREMENT, `DATA_NAME` varchar(64) NOT
	 * NULL, `DATA_VALUE` int(11) NOT NULL, `CREATE_DATE` datetime DEFAULT NULL,
	 * `UPDATE_DATE` datetime DEFAULT NULL,
	 */
	public long data_id;
	public String data_name;
	public int data_value;
	public Timestamp create_date;
	public Timestamp update_date;

}
