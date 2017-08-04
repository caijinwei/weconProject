package com.wecon.box.constant;

/**
 * Created by zengzhipeng on 2017/8/3.
 */
public class ConstKey {
    /**
     * 保存实时数据的key，由machine_code组成
     */
    public final static String REDIS_PIBOX_ACT_DATA_KEY = "pibox:actdata:%s";

    /**
     * redis配置名
     */
    public final static String REDIS_GROUP_NAME = "pibox";

    /**
     * session默认过期时间 单位秒
     */
    public final static int SESSION_EXPIRE_TIME = 3600 * 12;
}
