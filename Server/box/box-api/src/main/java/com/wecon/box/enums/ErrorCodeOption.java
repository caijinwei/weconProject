package com.wecon.box.enums;

/**
 * 错误代码定义
 * Created by zengzhipeng on 2017/8/2.
 */
public enum ErrorCodeOption {
    Unknown("未知错误", 10000),
    //用户操作相关
    AccountExisted("用户已经存在,不能注册",11000);

    public int value;
    public String key;

    ErrorCodeOption(String _key, int _value) {
        this.key = _key;
        this.value = _value;
    }
}
