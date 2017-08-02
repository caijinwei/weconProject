package com.wecon.box.api;

import com.wecon.box.entity.LogAccount;

/**
 * Created by zengzhipeng on 2017/8/1.
 */
public interface LogAccountApi {
    /**
     * 增加日志
     *
     * @param log
     * @return
     */
    long addLog(LogAccount log);

    /**
     * 获取日志的一些初始化数据
     *
     * @return
     */
    LogAccount getLogInit();
}
