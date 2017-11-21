package com.wecon.box.entity;

/**
 * @author lanpenghui
 * 2017年8月1日
 */
import com.alibaba.fastjson.JSON;

import java.sql.Timestamp;

public class RealHisCfgData {
	/**
	 * `real_his_cfg_id` bigint(20) NOT NULL COMMENT '监控点配置id',
	 * `monitor_time` datetime NOT NULL COMMENT '监控时间，将终端上传的时间转成datetime，便于查询', `value`
	 * varchar(64) DEFAULT NULL, `create_date` datetime DEFAULT NULL,
	 * `state`int(11) DEFAULT NULL COMMENT '监控点状态',
	 */
	public long real_his_cfg_id;// 监控点配置ID
	public Timestamp monitor_time;// 监控时间，将终端上传的时间转成datetime，便于查询
	public String value;
	public Timestamp create_date;
	public int state;

	public String toStringee(){
		return JSON.toJSONString(5);
	}
}
