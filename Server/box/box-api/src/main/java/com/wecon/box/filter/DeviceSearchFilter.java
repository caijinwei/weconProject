package com.wecon.box.filter;

import com.wecon.restful.doc.Label;

/**
 * Created by caijinw on 2017/10/9.
 */
public class DeviceSearchFilter {
    @Label("用户ID")
    public Long accountId;
    @Label("设备状态")
    public int state;
    @Label("盒子绑定类型")
    public Integer bind_state;
    @Label("机器码")
    public String machine_code;
    @Label("设备ID")
    public Long device_id;
}
