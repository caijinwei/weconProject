package com.wecon.box.param;

import javax.validation.constraints.NotNull;

import com.wecon.restful.doc.Label;

/**
 * @author lanpenghui 2017年9月5日上午8:43:25
 */
public class AlarmDataParam {
	@Label("设备device_id")
	@NotNull
	public String device_id;
	@Label("alarm_cfg_id")
	public String alarm_cfg_id;
	@Label("级别")
	public String grade_id;
	@Label("事件")
	public String event_id;
	@Label("状态")
	public String state;
	@Label("名称")
	public String name;
	@Label("开始时间")
	public String start_date;
	@Label("结束时间")
	public String end_date;
	@Label("索引")
	public Integer pageIndex;
	@Label("分页大小")
	public Integer pageSize;

	public void setDevice_id(String device_id) {
		this.device_id = device_id;
	}

	public void setAlarm_cfg_id(String alarm_cfg_id) {
		this.alarm_cfg_id = alarm_cfg_id;
	}

	public void setState(String state) {
		this.state = state;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setStart_date(String start_date) {
		this.start_date = start_date;
	}

	public void setEnd_date(String end_date) {
		this.end_date = end_date;
	}

	public void setPageIndex(Integer pageIndex) {
		this.pageIndex = pageIndex;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	public void setGrade_id(String grade_id) {
		this.grade_id = grade_id;
	}

	public void setEvent_id(String event_id) {
		this.event_id = event_id;
	}

}
