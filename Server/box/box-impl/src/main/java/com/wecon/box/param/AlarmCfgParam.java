package com.wecon.box.param;

import javax.validation.constraints.NotNull;

import com.wecon.restful.doc.Label;

/**
 * @author lanpenghui 2017年8月25日上午10:26:28
 */
public class AlarmCfgParam {

	@Label("alarmcfg_id")
	public long alarmcfg_id;
	@Label("alarmtrig_id")
	public String alarmtrig_id;
	@Label("plcId")
	@NotNull
	public long plc_id;
	@Label("设备device_id")
	@NotNull
	public long device_id;
	@Label("报警名称")
	@NotNull
	public String name;
	@Label("数据格式")
	@NotNull
	public long data_id;
	@Label("地址类型")
	@NotNull
	public int addr_type;
	@Label("地址")
	@NotNull
	public String addr;
	@Label("寄存器类型")
	@NotNull
	public String rid;
	@Label("数据范围")
	@NotNull
	public String rang;
	@Label("数据类型")
	@NotNull
	public int data_type;
	@Label("所在组id")
	public long group_id;
	@Label("0：一个条件 1：与 2：或")
	@NotNull
	public int condition_type;
	@Label("触发值")
	@NotNull
	public String value;
	@Label("0：等于1：不等于2：大于 3：大于等于4：小于5：小于等于6：ON 7：OFF")
	@NotNull
	public String type;

	@Label("报警内容")
	@NotNull
	public String text;
	@Label("整数小数位")
	public String digit_count;
	@Label("进制")
	public String digit_binary;
	@Label("报警级别")
	@NotNull
	public int alarm_level;
	

	public void setDigit_binary(String digit_binary) {
		this.digit_binary = digit_binary;
	}

	public void setDigit_count(String digit_count) {
		this.digit_count = digit_count;
	}

	public void setAlarmcfg_id(long alarmcfg_id) {
		this.alarmcfg_id = alarmcfg_id;
	}

	public void setAlarmtrig_id(String alarmtrig_id) {
		this.alarmtrig_id = alarmtrig_id;
	}

	public void setPlc_id(long plc_id) {
		this.plc_id = plc_id;
	}

	public void setDevice_id(long device_id) {
		this.device_id = device_id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setData_id(long data_id) {
		this.data_id = data_id;
	}

	public void setAddr_type(int addr_type) {
		this.addr_type = addr_type;
	}

	public void setAddr(String addr) {
		this.addr = addr;
	}

	public void setRid(String rid) {
		this.rid = rid;
	}

	public void setRang(String rang) {
		this.rang = rang;
	}

	public void setData_type(int data_type) {
		this.data_type = data_type;
	}

	public void setGroup_id(long group_id) {
		this.group_id = group_id;
	}

	public void setCondition_type(int condition_type) {
		this.condition_type = condition_type;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setText(String text) {
		this.text = text;
	}

	public void setAlarm_level(int alarm_level) {
		this.alarm_level = alarm_level;
	}
	

}
