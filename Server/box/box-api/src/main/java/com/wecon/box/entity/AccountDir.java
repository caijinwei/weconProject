package com.wecon.box.entity;

import java.sql.Timestamp;
/**
 * @author lanpenghui
 * 2017年8月1日
 */
public class AccountDir {
	/**
	 * `ID` bigint(20) NOT NULL AUTO_INCREMENT,
	 *  `ACCOUNT_ID` bigint(20) DEFAULT NULL,
	 *  `NAME` varchar(128) NOT NULL,
	 *  `TYPE` int(11) NOT NULL COMMENT '目录分类\r\n 0：机器类型\r\n 1：实时数据\r\n 2：历史数据\r\n 3：报警数据',
	 *  `CREATE_DATE` datetime DEFAULT NULL,
	 *  `UPDATE_DATE` datetime DEFAULT NULL,
	 */
	public long id;
	public long account_id;
	public String name;
	public int type;
	public Timestamp create_date;
	public Timestamp update_date;
}
