package com.wecon.box.entity;

import java.sql.Timestamp;

/**
 * @author lanpenghui 2017年8月1日
 */
public class AccountDirRel {
	/**
	 * `ACC_DIR_ID` bigint(20) NOT NULL, `REF_ID` bigint(20) NOT NULL,
	 * `ref_alais` varchar(64) DEFAULT NULL COMMENT '别名', `CREATE_DATE` datetime
	 * DEFAULT NULL,
	 */
	public long acc_dir_id;
	public long ref_id;// 1.实时数据、历史数据id 2.报警数据id 3.机器id
	public String ref_alais;
	public Timestamp create_date;

}
