package com.wecon.box.entity;

import java.sql.Timestamp;

/**
 * Created by zengzhipeng on 2017/8/1.
 */
public class LogAccount {
    public long id;
    public long account_id;
    public int client_platform;
    public String client_ip;
    public int op_type;
    public int op_date;
    public Timestamp op_time;
    public String message;
    public String url;
    public long res_id;
    public int res_type;
}
