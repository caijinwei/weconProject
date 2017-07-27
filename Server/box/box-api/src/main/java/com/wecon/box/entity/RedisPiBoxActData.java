package com.wecon.box.entity;

import java.util.List;

/**
 * redis保存实时数据结构 Created by zengzhipeng on 2017/7/26.
 */
public class RedisPiBoxActData {
	public String machine_code;
	public String time;
	public List<PiBoxCom> act_time_data_list;
	public String getMachine_code() {
		return machine_code;
	}
	public void setMachine_code(String machine_code) {
		this.machine_code = machine_code;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public List<PiBoxCom> getAct_time_data_list() {
		return act_time_data_list;
	}
	public void setAct_time_data_list(List<PiBoxCom> act_time_data_list) {
		this.act_time_data_list = act_time_data_list;
	}


}
