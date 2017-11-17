package com.wecon.box.action;

import com.alibaba.fastjson.JSONObject;
import com.wecon.box.api.AppVersionApi;
import com.wecon.box.entity.AppVerInfo;
import com.wecon.common.enums.MobileOption;
import com.wecon.common.util.CommonUtils;
import com.wecon.restful.annotation.WebApi;
import com.wecon.restful.core.AppContext;
import com.wecon.restful.core.Client;
import com.wecon.restful.core.Output;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by whp on 2017/10/11.
 */
@RestController
@RequestMapping(value = "appveraction")
public class AppVersionAction {
    @Autowired
    AppVersionApi appVersionApi;

    @Description("获取版本号")
    @RequestMapping(value = "/getversion")
    @WebApi(forceAuth = true, master = true, authority = {"0"})
    public Output getVersion() {
        String[] versions = appVersionApi.getVersions();
        JSONObject data = new JSONObject();
        data.put("versions", versions);
        return new Output(data);
    }


    @Description("更新app版本号")
    @RequestMapping(value = "/updateversion")
    @WebApi(forceAuth = true, master = true, authority = {"0"})
    public Output updateVersion(@RequestParam("androidVersion") String androidVersion, @RequestParam("iosVersion") String iosVersion,
                                @RequestParam("updateContent") String updateContent, @RequestParam("isforce") String isforce) {
        AppVerInfo androidVerInfo = new AppVerInfo();
        androidVerInfo.version_code = androidVersion;
        androidVerInfo.platform = MobileOption.ANDROID.value;
        androidVerInfo.updateContent = updateContent;
        androidVerInfo.isforce = Integer.parseInt(isforce);
        AppVerInfo iosVerInfo = new AppVerInfo();
        iosVerInfo.version_code = iosVersion;
        iosVerInfo.platform = MobileOption.IPHONE.value;
        iosVerInfo.updateContent = updateContent;
        iosVerInfo.isforce = Integer.parseInt(isforce);
        appVersionApi.saveOrUpdate(androidVerInfo);
        appVersionApi.saveOrUpdate(iosVerInfo);
        JSONObject data = new JSONObject();
        return new Output(data);
    }

    @Description("获取app版本号及更新包下载地址")
    @RequestMapping(value = "/checkupdate")
    @WebApi(master = true)
    public Output checkUpdate() {
        Client client = AppContext.getSession().client;
        MobileOption platform = client.platform;
        JSONObject data = new JSONObject();
        String[] versions = appVersionApi.getVersions();
        if(null != versions && versions.length > 0){
            if(MobileOption.ANDROID.value == platform.value){
                data.put("version", versions[0]);
                data.put("apkUrl", "web/apk/V-Box.apk");
            }else if(MobileOption.IPHONE.value == platform.value){
                data.put("version", versions[1]);
            }
            data.put("updateContent", versions[2]);
            data.put("updateFlag", versions[3]);
        }
        return new Output(data);
    }

}
