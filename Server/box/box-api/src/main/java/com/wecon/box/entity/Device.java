package com.wecon.box.entity;

import java.sql.Timestamp;
/**
 * @author lanpenghui
 * 2017年8月1日
 */
public class Device {
/**
 * `DEVICE_ID` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '设备ID',
  `MACHINE_CODE` varchar(64) NOT NULL COMMENT '机器码',
  `PASSWORD` varchar(64) NOT NULL COMMENT '密码，md5加密',
  `DEV_MODEL` varchar(32) NOT NULL COMMENT '设备型号',
  `NAME` varchar(32) NOT NULL COMMENT '盒子名称',
  `REMARK` varchar(32) DEFAULT NULL COMMENT '备注',
  `MAP` varchar(32) DEFAULT NULL COMMENT '地图信息 经度,纬度',
  `STATE` int(11) NOT NULL COMMENT '设备状态 0：离线 1：在线',
  `DIR_ID` bigint(20) DEFAULT NULL COMMENT '目录ID',
  `CREATE_DATE` datetime DEFAULT NULL,
  `UPDATE_DATE` datetime DEFAULT NULL,
 */
	public long device_id;
	public String machine_code;
	public String password;
	public String dev_model;
	public String name;
	public String remark;
	public String map;
	public int state;
	public long dir_id;
	public Timestamp create_date;
	public Timestamp update_date;
}
