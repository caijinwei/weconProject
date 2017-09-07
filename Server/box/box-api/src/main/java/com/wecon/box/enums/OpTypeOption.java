package com.wecon.box.enums;

import com.wecon.common.enums.EnumVal;

/**
 * Created by zengzhipeng on 2017/8/30.
 */
public enum OpTypeOption implements EnumVal {
    Unknown("未定义", 0),
    //用户相关
    SignupEmail("邮箱注册", 100),
    EmailActive("邮箱激活", 101),
    SignupPhone("手机号码注册", 102),
    Signin("登录", 103),
    Signout("登出", 104),
    ChgPwd("修改密码", 105),
    ChgEmail("修改邮箱", 106),
    ChgPhone("修改手机号码", 107),
    AddViewUser("新增视图帐号", 108),
    UpdViewUser("修改视图帐号", 109),

    //分组操作
    AddDir("新增分组", 200),
    UpdDir("修改分组", 201),
    DelDir("删除分组", 202),

    //盒子操作
    BindDevice("绑定盒子", 300),
    UnBindDevice("解除绑定盒子", 301),
    DragDeviceDir("盒子分组修改", 302),
    UpdateDeviceInfo("修改盒子基本信息", 303),

    //权限操作
    AddViewRole("添加视图帐号权限", 400),
    UpdViewRole("更新视图帐号权限", 401),
    DelViewRole("删除视图帐号权限", 402),

    //通讯口操作
    AddPlc("添加通讯口", 501),
    UpdatePlc("更新通讯口配置", 502),
    DelPlc("删除通讯口配置", 503),
    //实时监控点操作
    AddAct("新增实时数据配置", 600),
    UpdAct("修改实时数据配置", 601),
    DelAct("删除实时数据配置", 602),

    //历史监控点操作
    AddHis("新增历史数据配置", 700),
    UpdHis("修改历史数据配置", 701),
    DelHis("删除历史数据配置", 702),

    //报警监控点
    AddAlarm("新增报警数据配置", 800),
    UpdAlarm("修改报警数据配置", 801),
    DelAlarm("删除报警数据配置", 802);

    private String key;

    private int value;

    OpTypeOption(String _key, int _value) {
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
