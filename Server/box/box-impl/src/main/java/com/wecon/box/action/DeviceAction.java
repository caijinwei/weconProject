package com.wecon.box.action;

import com.alibaba.fastjson.JSONObject;
import com.wecon.box.api.AccountApi;
import com.wecon.box.api.AccountDirRelApi;
import com.wecon.box.api.DevBindUserApi;
import com.wecon.box.api.DeviceApi;
import com.wecon.box.entity.Account;
import com.wecon.box.entity.AccountDirRel;
import com.wecon.box.entity.DevBindUser;
import com.wecon.box.entity.Device;
import com.wecon.box.enums.ErrorCodeOption;
import com.wecon.restful.annotation.WebApi;
import com.wecon.restful.core.AppContext;
import com.wecon.restful.core.BusinessException;
import com.wecon.restful.core.Output;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by caijinw on 2017/8/8.
 */
@RestController
@RequestMapping(value = "baseInfoAction")
public class DeviceAction {
    @Autowired
    DeviceApi deviceApi;

    @Autowired
    AccountApi accountApi;

    @Autowired
    DevBindUserApi devBindUserApi;

    @Autowired
    AccountDirRelApi accountDirRelApi;

    /**
     * 盒子基本信息的展示
     *
     * @param device_id
     * @return
     */
    @RequestMapping(value = "showBaseInfo")
    @WebApi(forceAuth = true, master = true)
    public Output showBaseInfo(@RequestParam("device_id") Integer device_id) {
        /*
        * 验证信息(不知道怎么写)
        * */
        Device device = deviceApi.getDevice(device_id);
        JSONObject data = new JSONObject();
        data.put("device", device);

        return new Output(data);
    }

    /*
    * 测试 没有用户登入等验证
    * 直接先传入user_id
    * */
    @WebApi(forceAuth = true, master = true, authority = {"1"})
    @RequestMapping(value = "/boundBox")
    public Output boundBox(@RequestParam("machine_code") String machine_code, @RequestParam("password") String password, @RequestParam("name") String name, @RequestParam("ref_id") Integer ref_id) {

        /*
        *验证是否该盒子是否存在
        * */
        Device device = deviceApi.getDevice(machine_code);
        if (device == null) {
            throw new BusinessException(ErrorCodeOption.Device_NotFound.key, ErrorCodeOption.Device_NotFound.value);
        } else if (!device.password.equals(password) || !device.name.equals(name)) {
            throw new BusinessException(ErrorCodeOption.Device_NotFound.key, ErrorCodeOption.Device_NotFound.value);
        }
        long device_id = device.device_id;

        /*
        *该设备没有被别的用户绑定过
        * */
        if (devBindUserApi.findByDevId(device_id) != 0) {

            throw new BusinessException(ErrorCodeOption.Device_AlreadyBind.key, ErrorCodeOption.Device_AlreadyBind.value);
        }

        DevBindUser model = new DevBindUser();
        model.account_id = AppContext.getSession().client.userId;
        model.device_id = device_id;
        devBindUserApi.saveDevBindUser(model);

        /*
        * 有选择分组操作
        * 默认分组 ref_id=0
        *
        * */
        if (ref_id != 0) {
            AccountDirRel accountDirRel = new AccountDirRel();
            accountDirRel.ref_id = ref_id;
            accountDirRel.acc_dir_id = device_id;
            accountDirRelApi.saveAccountDirRel(accountDirRel);
        }
        return new Output();
    }

}
