package com.wecon.box.entity;

import java.sql.Timestamp;

/**
 * Created by zengzhipeng on 2017/9/12.
 */
public class Firmware {
    public long firmware_id;
    public long file_id;
    public String dev_model;
    public String firm_info;
    public String version_code;
    public String version_name;
    public String description;
    public Timestamp create_date;
    public Timestamp update_date;
}
