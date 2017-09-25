package com.wecon.box.entity;

import java.sql.Timestamp;
import java.util.List;

/**
 * Created by win7 on 2017/8/28.
 */
public class AlarmCfgExtend extends AlarmCfg{
    public String com;
    public long addr_id;
    public String upd_time;
    public String machine_code;
    public List<AlarmTrigger> condition_list;

    public static String[] UPDATE_ALARM_FIELD_FILTER = new String[]{"com","addr_id", "upd_time", "data_id", "name", "addr", "addr_type",
            "text", "condition_type", "condition_list", "rid", "digit_count", "digit_binary", "data_limit"};

    public String getMachine_code() {
        return machine_code;
    }

    public void setMachine_code(String machine_code) {
        this.machine_code = machine_code;
    }

    public List<AlarmTrigger> getCondition_list() {
        return condition_list;
    }

    public void setCondition_list(List<AlarmTrigger> condition_list) {
        this.condition_list = condition_list;
    }

}
