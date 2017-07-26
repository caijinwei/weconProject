package com.wecon.box.api;

import com.wecon.box.entity.RedisPiBoxActData;
import org.springframework.stereotype.Component;

/**
 * Created by zengzhipeng on 2017/7/26.
 */
@Component
public interface RedisPiBoxApi {
    /**
     * 获取指定机器码的实时数据
     *
     * @param machine_code
     * @return
     */
    RedisPiBoxActData getRedisPiBoxActData(String machine_code);

    /**
     * 保存实时数据
     *
     * @param model
     * @return
     */
    boolean saveRedisPiBoxActData(RedisPiBoxActData model);
}
