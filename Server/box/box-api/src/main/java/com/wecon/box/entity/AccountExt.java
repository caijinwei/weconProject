package com.wecon.box.entity;

import java.sql.Timestamp;

/**
 * 帐号的扩展信息
 * Created by zengzhipeng on 2017/10/16.
 */
public class AccountExt {
    public long account_id;
    public String company;
    public String company_business;
    public String company_contact;
    public String company_phone;
    public Timestamp create_date;
    public Timestamp update_date;
}
