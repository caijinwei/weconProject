package com.wecon.box.api;

import com.wecon.box.entity.Device;
import com.wecon.box.entity.DeviceUse;
import com.wecon.box.entity.Page;
import com.wecon.box.filter.DeviceDir;
import com.wecon.box.filter.DeviceFilter;
import com.wecon.box.filter.DeviceSearchFilter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

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

    /*
    * 根据machine_code获取deviceDir信息
    * @param machine_code
    * */
    public DeviceDir getDeviceDir(String machine_code);
	/**
	 * 通过用户获取绑定的机器
	 * @param account_id
	 * @return
	 */
	
	public List<Device> getDeviceList(long account_id,long account_dir_id);
	/**
	 * 获取盒子在线离线数
	 * @param account_id
	 * @param state
	 * @return
	 */
	public int getDeviceList(long account_id,int state);

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

    /*
    * 根据device_id解除盒子的绑定（分组，监控点）
    * */
    public boolean unbindDevice(final Integer accountId, final Integer deviceId);


    /**
     * 获取管理员下的所有分组及对应设备
     * @param acc_id
     * @return
     */
    List<Map<String, Object>> getDevicesByGroup(long acc_id, int selAlarm);

    /*
    *超级管理员展示数据
    * */
    public Page<DeviceDir> showAllDeviceDir(String accountId,Integer state,int pageNum,int pageSize);

    /*
    * 更新设备名称和说明
    * */
    boolean updateDeviceName(Integer deviceId,String deviceName,String remark);


    /*
    *@params state
    *@params bound_state
    * 根据绑定状态展示所有device
    * */
    public Page<DeviceDir> getDeviceByBound(Integer state, Integer pageNum, Integer pageSize);

    public Page<DeviceDir> getDeviceByUnbound(Integer state, Integer pageNum, Integer pageSize);

    /*
    * 绑定盒子
    * */
    public void boundDevice(final long device_id,final String name,final long acc_dir_id);

    /*
    * 超管用户搜索
    * */
    public Page<DeviceDir> getAllDeviceByFilter(DeviceSearchFilter filter, Integer pageNum, Integer pageSize);

    /*
    * 添加或者修改 用户行业表
    * */
    public void updateDeviceUse(DeviceUse deviceUse);

    /*
    * 根据设备id获取用户行业表
    * */
    public DeviceUse getDeviceUse(long deviceId);
}
