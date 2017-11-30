package com.wecon.box.param;

import com.wecon.restful.doc.Label;

/**
 * Created by win7 on 2017/11/29.
 */
public class BusinessDataParam {
    @Label("盒子ID")
    public long boxId;
    @Label("分组ID")
    public long groupId;
    @Label("监控点ID")
    public long monitorId;
    @Label("监控开始时间")
    public String monitorBeginTime;
    @Label("监控结束时间")
    public String monitorEndTime;
    @Label("当前页")
    public int pageIndex;
    @Label("每页条数")
    public int pageSize;
    @Label("监控点分组类型")
    public int type;
    @Label("状态")
    public int state;
    @Label("盒子是否需要查询报警数据")
    public int selAlarm;

    public void setBoxId(long boxId) {
        this.boxId = boxId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    public void setMonitorBeginTime(String monitorBeginTime) {
        this.monitorBeginTime = monitorBeginTime;
    }

    public void setMonitorId(long monitorId) {
        this.monitorId = monitorId;
    }

    public void setMonitorEndTime(String monitorEndTime) {
        this.monitorEndTime = monitorEndTime;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setState(int state) {
        this.state = state;
    }

    public void setSelAlarm(int selAlarm) {
        this.selAlarm = selAlarm;
    }
}
