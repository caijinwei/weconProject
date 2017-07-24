package com.wecon.box.param;

import javax.validation.constraints.NotNull;

/**
 * Apollo测试工具请求参数
 * Created by zengzhipeng on 2017/7/21.
 */
public class MqttTestParam {
    @NotNull
    public String mqtt_host;
    @NotNull
    public String mqtt_username;
    @NotNull
    public String mqtt_pwd;
    @NotNull
    public String mqtt_topics;

}
