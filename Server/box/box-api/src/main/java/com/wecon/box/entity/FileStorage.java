package com.wecon.box.entity;

import java.sql.Timestamp;

/**
 * Created by zengzhipeng on 2017/9/13.
 */
public class FileStorage {
    public long file_id;
    public String file_name;
    public String file_md5;
    public String file_type;
    public long file_size;
    public byte[] file_data;
    public Timestamp create_date;
}
