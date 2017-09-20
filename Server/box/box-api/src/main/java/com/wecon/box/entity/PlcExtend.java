package com.wecon.box.entity;

import java.sql.Timestamp;

/**
 * Created by win7 on 2017/8/25.
 */
public class PlcExtend extends PlcInfo{
    public String com;
    public String machine_code;
    public String upd_time;
    public String file_md5;

    public static String[] UPDATE_PLC_FIELD_FILTER = new String[]{"com", "upd_time", "type", "driver", "box_stat_no", "plc_stat_no", "port", "comtype", "baudrate", "stop_bit", "data_length",
            "check_bit", "retry_times", "wait_timeout", "rev_timeout", "com_stepinterval", "com_iodelaytime", "retry_timeout", "net_port", "net_type", "net_isbroadcast", "net_broadcastaddr", "net_ipaddr", "file_md5"};
}
