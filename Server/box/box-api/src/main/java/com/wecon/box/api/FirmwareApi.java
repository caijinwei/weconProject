package com.wecon.box.api;

import com.wecon.box.entity.Firmware;
import com.wecon.box.entity.FirmwareDetail;
import com.wecon.box.entity.Page;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by zengzhipeng on 2017/9/12.
 */
@Component
public interface FirmwareApi {

    /**
     * 获取固件
     *
     * @param firmware_id
     * @return
     */
    Firmware getFirmware(long firmware_id);

    /**
     * 获取详情
     *
     * @param firmware_id
     * @return
     */
    FirmwareDetail getFirmwareDetail(long firmware_id);

    /**
     * 获取分页列表
     *
     * @param dev_model
     * @param firm_info
     * @param pageIndex
     * @param pageSize
     * @return
     */
    Page<FirmwareDetail> getFirmwareDetailList(String dev_model, String firm_info, int pageIndex, int pageSize);

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
     * 删除固件
     *
     * @param model
     * @return
     */
    boolean deleteFirmware(Firmware model);

    /**
     * 获取指定设备类型的所有可用固件
     *
     * @param dev_model
     * @return
     */
    List<FirmwareDetail> getFirmwareDetailList(String dev_model);
}
