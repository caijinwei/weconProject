package com.wecon.box.param;

import javax.validation.constraints.NotNull;

import com.wecon.restful.doc.Label;

/**
 * @author lanpenghui 2017年8月25日上午10:26:28
 */
public class RealHisCfgParam {

	@Label("id")
	public long id;
	@Label("plcId")
	@NotNull
	public long plc_id;
	@Label("设备id")
	@NotNull
	public long device_id;
	@Label("监控点名称")
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
	@Label("描述")
	public String describe;
	@Label("进制")
	public String digit_binary;
	@Label("整数小数位数")
	public String digit_count;
	@Label("数据类型")
	@NotNull
	public int data_type;
	@Label("所在组id")
	public long group_id;
	@Label("历史采集周期")
	public int his_cycle;
	@Label("批量个数")
	public int batch;
	@Label("增量")
	public String increase;

	public void setBatch(int batch) {
		this.batch = batch;
	}

	public void setIncrease(String increase) {
		this.increase = increase;
	}

	public void setDigit_count(String digit_count) {
		this.digit_count = digit_count;
	}

	public void setDigit_binary(String digit_binary) {
		this.digit_binary = digit_binary;
	}

	public void setId(long id) {
		this.id = id;
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

	public void setDescribe(String describe) {
		this.describe = describe;
	}

	public void setData_type(int data_type) {
		this.data_type = data_type;
	}

	public void setGroup_id(long group_id) {
		this.group_id = group_id;
	}

	public void setHis_cycle(int his_cycle) {
		this.his_cycle = his_cycle;
	}

}
