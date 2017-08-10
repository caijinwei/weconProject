package com.wecon.box.entity;

import java.sql.Timestamp;

/**
 * Created by Administrator on 2017/8/10.
 */
public class ViewAccountRoleView extends RealHisCfg {
    public long view_id;
    public int cfg_type;
    public long cfg_id;
    public long role_type;
    public Timestamp create_date;
    public Timestamp update_date;

    @Override
    public String toString() {
        return "ViewAccountRoleView{" +
                "view_id=" + view_id +
                ", cfg_type=" + cfg_type +
                ", cfg_id=" + cfg_id +
                ", role_type=" + role_type +
                ", create_date=" + create_date +
                ", update_date=" + update_date +
                '}';
    }
}
