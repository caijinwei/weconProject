package com.wecon.box.filter;

import java.sql.Timestamp;

public class ViewAccountRoleFilter {
	public long view_id;
	public int cfg_type;//1.实时/历史 2.报警
	public int data_type;//0：实时数据  1：历史数据
	public int state;//配置状态 0-未启用 1-启用
	public long cfg_id;
	public long role_type;
	public long dirId;//分组id
	public Timestamp create_date;
	public Timestamp update_date;
}
