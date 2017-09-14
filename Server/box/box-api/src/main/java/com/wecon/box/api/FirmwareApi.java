package com.wecon.box.api;

import com.wecon.box.entity.Firmware;
import com.wecon.box.entity.FirmwareDetail;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by zengzhipeng on 2017/9/12.
 */
@Component
public interface FirmwareApi {

    /**
     * 获取详情
     *
     * @param firmware_id
     * @return
     */
    FirmwareDetail getFirmwareDetail(long firmware_id);

    /**
     * 新增固件
     *
     * @param model
     * @return
     */
    long addFirmware(Firmware model);

    /**
     * 更新固件
     *
     * @param model
     * @return
     */
    boolean updateFirmware(Firmware model);

    /**
     * 获取指定设备类型的所有可用固件
     *
     * @param dev_model
     * @return
     */
    List<FirmwareDetail> getFirmwareDetailList(String dev_model);
}
