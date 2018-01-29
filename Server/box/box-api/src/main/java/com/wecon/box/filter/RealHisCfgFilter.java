package com.wecon.box.filter;

import java.sql.Timestamp;

public class RealHisCfgFilter {
	public long id;
	public long data_id;
	public long account_id;
	public long plc_id;
	public String name;// 名称
	public String addr;// 地址
	public int addr_type;// 0：位地址 1：字地址 2：双字
	public String describe;// 描述
	public String digit_count;// 整数位数，小数位数
	public String data_limit;// 数据范围
	public int his_cycle;// 历史数据采集周期
	public int data_type;// 0：实时数据 1：历史数据
	public int state;// 状态:1-启用; 0-未启用
	public int bind_state;// 1.绑定状态 0.解绑状态'
	public long device_id;
	public String rid;// 寄存器类型
	public long dirId;// 分组id
	public String ext_unit;//单位
	public String dead_set;
	public Timestamp create_date;
	public Timestamp update_date;
}
