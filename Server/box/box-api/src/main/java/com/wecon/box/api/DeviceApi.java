package com.wecon.box.api;

import org.springframework.stereotype.Component;
import com.wecon.box.entity.Device;
import com.wecon.box.entity.Page;
import com.wecon.box.filter.DeviceFilter;

import java.util.List;

/**
 * @author lanpenghui 2017年8月1日
 */
@Component
public interface DeviceApi {
    /**
     * 添加设备
     *
     * @param model
     * @return
     */
    public long saveDevice(Device model);

    /**
     * 更新设备信息
     *
     * @param model
     * @return
     */
    public boolean updateDevice(Device model);

    /**
     * 根据device_id获取设备信息
     *
     * @param device_id
     * @return
     */
    public Device getDevice(long device_id);

    /**
     * 根据machine_code获取设备信息
     *
     * @param machine_code
     * @return
     */
    public Device getDevice(String machine_code);

    /**
     * 根据device_id删除设备信息
     *
     * @param device_id
     */
    public void delDevice(long device_id);

    /**
     * 获取用户的分页列表
     *
     * @param filter
     * @param pageIndex
     * @param pageSize
     * @return
     */
    Page<Device> getDeviceList(DeviceFilter filter, int pageIndex, int pageSize);

    /*
* 查询管理账户的盒子
* @Params acc_id
*         管理账户id
* sql:SELECT a.device_id, b.`name` FROM dev_bind_user a INNER JOIN device b ON a.device_id=b.device_id WHERE a.account_id='1000017';
* */
    public List<Device> getDeviceNameByAccId(long acc_id);
}
