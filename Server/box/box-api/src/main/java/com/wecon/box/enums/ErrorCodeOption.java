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
    AccountPhoneExisted("手机号码已经被使用", 11016),
    PhonenumAndEmailError("手机号码和邮箱格式有错", 11017),

    //文件操作相关
    UploadFileError("上传文件异常", 12000),
    UploadFileParamEmpty("上传文件参数为空", 12001),
    UploadFileExtError("上传文件格式有误", 12002),

    PiBoxDevice_IsNot_Found("盒子不存在",300009),
    Account_Permissions_No("账户权限不足，无法新增盒子", 30001),
    Device_NotFound("该设备不存在",30002),
    Device_AlreadyBind("该设备已经被别的用户绑定",300023),
    Viewpoint_Dlete_False("视图账户监控点解绑失败",300003),
    ViewpointRoleTypePrams_Update_False("视图账号监控点权限分配失败(参数不能为空)",300004),
    Get_DeviceList_Error("获取盒子列表失败",30005),
    AlarmViewpoint_IsNULL("报警监控点为空",300008),
    Get_HisList_Error("获取历史数据配置列表失败",30006),
    Get_AlarmList_Error("获取报警数据配置列表失败",30007),
    Get_ActList_Error("获取实时数据配置列表失败",30008),
    Is_Exist_PlcPort("通讯协议被占用",300012),
    Is_Not_Params_DeviceID("没有输入参数",300013),
    UpdateFalse_DeviceName_Remark("设备别名和备注保存失败",300014),
    PIBox_Bound_False("设备绑定失败",300015),
    PIBox_Bound_PointMove_False("设备绑定失败",300016),
    Delete_AccDevice_Rel_False("设备解绑失败",300017),
    DeviceId_Is_Unknown("盒子设备ID非法",300018),


    Get_Groupid_Error("获取数组ID错误",50001),
	
    Monitor_Existed("该分组下已经存在该监控点",60001),
	Get_Data_Error("获取参数异常",60002);

    public int value;
    public String key;

    ErrorCodeOption(String _key, int _value) {
        this.key = _key;
        this.value = _value;
    }
}
