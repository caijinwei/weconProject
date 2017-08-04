package com.wecon.box.entity;
/**
 * @author lanpenghui
 * 2017年8月1日
 */
import java.sql.Timestamp;

public class ViewAccountRole {
	/**
	 *  `VIEW_ID` bigint(20) NOT NULL COMMENT '视图帐号ID',
  		`CFG_TYPE` int(11) NOT NULL COMMENT '监控点类型：1-历史，实时监控点； 2-报警监控点',
  		`CFG_ID` bigint(20) NOT NULL COMMENT '监控点配置ID',
  		`ROLE_TYPE` int(11) DEFAULT NULL COMMENT '权限类型：0-无权限，1-只读，2-只写，3-读写',
  		`CREATE_DATE` datetime DEFAULT NULL,
  		`UPDATE_DATE` datetime DEFAULT NULL
	 */
	public long view_id;
	public int cfg_type;
	public long cfg_id;
	public long role_type;
	public Timestamp create_date;
	public Timestamp update_date;

}
