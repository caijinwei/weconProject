package com.wecon.box.enums;

import com.wecon.common.enums.EnumVal;

/**
 * Created by zengzhipeng on 2017/8/30.
 */
public enum ResTypeOption implements EnumVal {
    Unknown("未定义", 0),
    Account("帐号", 100),
    Dir("分组", 200),
    Device("盒子", 300),
	Act("实时数据", 600),
	His("历史数据", 700),
	Alarm("报警数据", 800);
	

    private String key;

    private int value;

    ResTypeOption(String _key, int _value) {
        key = _key;
        value = _value;
    }
    @Override
    public int getValue() {
        return value;
    }
    @Override
    public String getKey() {
        return key;
    }
}
