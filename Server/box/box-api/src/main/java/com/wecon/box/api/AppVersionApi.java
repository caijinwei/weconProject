package com.wecon.box.api;

import com.wecon.box.entity.AppVerInfo;
import org.springframework.stereotype.Component;


/**
 * Created by whp on 2017/10/11.
 */
@Component
public interface AppVersionApi {
    /**
     * 保存或更新版本号
     * @param appVerInfo
     */
    void saveOrUpdate(AppVerInfo appVerInfo);

    /**
     * 获取版本号
     * @return
     */
    String[] getVersions();
}
