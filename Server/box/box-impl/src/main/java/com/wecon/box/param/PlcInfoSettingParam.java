package com.wecon.box.param;

import com.wecon.restful.doc.Label;

import javax.validation.constraints.NotNull;

/**
 * Created by caijinw on 2017/8/5.
 */
public class PlcInfoSettingParam {

    @Label("plcId")
    @NotNull
    public long plc_id;

    @Label("设备id")
    @NotNull
    public long device_id;

    @Label("驱动名称")
    @NotNull
    public String type;

    @Label("盒子站号")
    @NotNull
    public int box_stat_no;

    @Label("PLC站号")
    @NotNull
    public int plc_stat_no;

    @Label("串口")
    @NotNull
    public String port;

    @Label("串口协议")
    public int comtype;

    @Label("波特率")
    public String baudrate;

    @Label("停止位")
    public int stop_bit;

    @Label("数据位")
    public int data_length;


    @Label("校验位")
    public String check_bit;

    @Label("重试次数")
    public int retry_times;

    @Label("等待超时")
    public int wait_timeout;

    @Label("接收超时")
    public int rev_timeout;

    @Label("连续长度")
    public int com_stepinterval;

    @Label("通讯延迟时间")
    public int com_iodelaytime;

    @Label("重试超时")
    public int retry_timeout;

    @Label("端口")
    public Integer net_port;

    @Label("网络类型")
    public Integer net_type;

    @Label("使用广播地址")
    public Integer net_isbroadcast;

    @Label("广播地址站号")
    public Integer net_broadcastaddr;

    @Label("IP地址")
    public String net_ipaddr;

    @Label("驱动配置文件名")
    @NotNull
    public String driver;

    public int state;

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public void setDevice_id(long device_id) {
        this.device_id = device_id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setPlc_id(long plc_id) {
        this.plc_id = plc_id;
    }

    public void setNet_port(Integer net_port) {
        this.net_port = net_port;
    }

    public void setNet_type(Integer net_type) {
        this.net_type = net_type;
    }

    public void setNet_isbroadcast(Integer net_isbroadcast) {
        this.net_isbroadcast = net_isbroadcast;
    }

    public void setNet_broadcastaddr(Integer net_broadcastaddr) {
        this.net_broadcastaddr = net_broadcastaddr;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public void setBox_stat_no(int box_stat_no) {
        this.box_stat_no = box_stat_no;
    }

    public void setPlc_stat_no(int plc_stat_no) {
        this.plc_stat_no = plc_stat_no;
    }

    public void setComtype(int comtype) {
        this.comtype = comtype;
    }

    public void setBaudrate(String baudrate) {
        this.baudrate = baudrate;
    }

    public void setStop_bit(int stop_bit) {
        this.stop_bit = stop_bit;
    }

    public void setCheck_bit(String check_bit) {
        this.check_bit = check_bit;
    }

    public void setData_length(int data_length) {
        this.data_length = data_length;
    }

    public void setRetry_times(int retry_times) {
        this.retry_times = retry_times;
    }

    public void setWait_timeout(int wait_timeout) {
        this.wait_timeout = wait_timeout;
    }

    public void setRev_timeout(int rev_timeout) {
        this.rev_timeout = rev_timeout;
    }

    public void setCom_stepinterval(int com_stepinterval) {
        this.com_stepinterval = com_stepinterval;
    }

    public void setCom_iodelaytime(int com_iodelaytime) {
        this.com_iodelaytime = com_iodelaytime;
    }

    public void setRetry_timeout(int retry_timeout) {
        this.retry_timeout = retry_timeout;
    }

    public void setNet_port(int net_port) {
        this.net_port = net_port;
    }

    public void setNet_type(int net_type) {
        this.net_type = net_type;
    }

    public void setNet_isbroadcast(int net_isbroadcast) {
        this.net_isbroadcast = net_isbroadcast;
    }

    public void setNet_broadcastaddr(int net_broadcastaddr) {
        this.net_broadcastaddr = net_broadcastaddr;
    }

    public void setNet_ipaddr(String net_ipaddr) {
        this.net_ipaddr = net_ipaddr;
    }

    public void setState(int state) {
        this.state = state;
    }
}