package com.wecon.box.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lanpenghui 2017年8月31日上午8:42:20
 */
public class AlarmCfgTrigger extends AlarmCfg {
	public String triggerValue;// 触发值拼接
	public long dirId;// 分组id
	public String dirName;// 分组名称
	public String alais;// 监控点别名
	public String data_value;//数据类型值
	public long id;// 显示方便
	public String num;// 整数位数
	public String dec;// 小数位数
	public String main_addr;//主编号地址
	public String child_addr;//子编号地址
	public String main_limit;//主编号范围
	public String child_limit;//子编号范围
	public String main_binary;//主编号进制
	public String child_binary;//子编号进制

	public List<AlarmTrigger> listAlarmTrigger = new ArrayList<AlarmTrigger>();

}
