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

	public List<AlarmTrigger> listAlarmTrigger = new ArrayList<AlarmTrigger>();

}
