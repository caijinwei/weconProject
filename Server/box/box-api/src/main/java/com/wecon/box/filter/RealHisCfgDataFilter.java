package com.wecon.box.filter;

import java.sql.Timestamp;

public class RealHisCfgDataFilter {
	public long real_his_cfg_id;// 监控点配置ID
	public Timestamp monitor_time;// 监控时间，将终端上传的时间转成datetime，便于查询
	public String value;
	public Timestamp create_date;
	public int state;
	public String start_date;// 开始时间
	public String end_date;// 结束时间

}
