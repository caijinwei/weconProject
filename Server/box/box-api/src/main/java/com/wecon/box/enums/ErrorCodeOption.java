package com.wecon.box.enums;

/**
 * 错误代码定义
 * Created by zengzhipeng on 2017/8/2.
 */
public enum ErrorCodeOption {
    Unknown("未知错误", 10000),
    //用户操作相关
    AccountExisted("用户已经存在,不能注册", 11000),
    EmailErorr("邮箱格式有错", 11001),
    Login_NotAccount("登录帐号不存在", 11002),
    Login_PwdError("密码错误", 11003),

    Account_Permissions_No("账户权限不足，无法新增盒子", 30001),
    Device_NotFound("该设备不存在",30002),
    Device_AlreadyBind("该设备已经被别的用户绑定",300023);


    public int value;
    public String key;

    ErrorCodeOption(String _key, int _value) {
        this.key = _key;
        this.value = _value;
    }
}
