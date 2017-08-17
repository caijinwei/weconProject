package com.wecon.box.enums;

/**
 * 错误代码定义
 * Created by zengzhipeng on 2017/8/2.
 */
public enum ErrorCodeOption {
    Unknown("未知错误", 10000),
    //用户操作相关
    AccountExisted("用户已经存在,不能注册", 11000),
    EmailError("邮箱格式有错", 11001),
    Login_NotAccount("登录帐号不存在", 11002),
    Login_PwdError("密码错误", 11003),
    Login_NotAllow("禁止登录", 11004),
    Login_NotActive("帐号未激活", 11005),
    EmailActiveError("邮箱激活失败", 11006),
    AccountNotExisted("操作的用户不存在", 11007),
    OldPwdError("旧密码错误", 11008),
    EmailIsSame("邮箱没有变化", 11009),
    CanAddTheSameGroup("分组命名不能重复", 11010),
    UserGroupTypeIsUndefined("分组类型未定义", 11011),
    OnlyOperateOneselfGroup("只能操作自己创建的分组", 11012),
    PhonenumError("手机号码格式有错", 11013),
    SmsVercodeError("手机验证码有错", 11014),
    AccountEmailExisted("邮箱已经被使用", 11015),
    AccountPhoneExisted("邮箱已经被使用", 11016),

    Account_Permissions_No("账户权限不足，无法新增盒子", 30001),
    Device_NotFound("该设备不存在",30002),
    Device_AlreadyBind("该设备已经被别的用户绑定",300023),
    Viewpoint_Dlete_False("视图账户监控点解绑失败",300003),
    ViewpointRoleTypePrams_Update_False("视图账号监控点权限分配失败(参数不能为空)",300004),
	Get_DeviceList_Error("获取盒子列表失败",30005),
    Get_HisList_Error("获取历史数据配置列表失败",30006),
    Get_AlarmList_Error("获取报警数据配置列表失败",30007),
	Get_ActList_Error("获取实时数据配置列表失败",30008);

    public int value;
    public String key;

    ErrorCodeOption(String _key, int _value) {
        this.key = _key;
        this.value = _value;
    }
}
