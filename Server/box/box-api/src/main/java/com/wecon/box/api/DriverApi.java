package com.wecon.box.api;

import com.wecon.box.entity.Driver;
import com.wecon.box.entity.DriverDetail;
import com.wecon.box.entity.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Created by zengzhipeng on 2017/9/16.
 */
@Component
public interface DriverApi {
    /**
     * 批量增加驱动
     *
     * @param driverList
     * @return
     */
    boolean batchAddDriver(List<Driver> driverList);

    /**
     * 保存驱动，根据驱动名唯一
     *
     * @param model
     * @return
     */
    boolean saveDriver(Driver model);

    /**
     * 更新驱动，根据id
     *
     * @param model
     * @return
     */
    boolean updateDriver(Driver model);

    /**
     * 获取驱动的分页列表
     *
     * @param type
     * @param driver
     * @param pageIndex
     * @param pageSize
     * @return
     */
    Page<Driver> getDriverList(String type, String driver, int pageIndex, int pageSize);

    /**
     * 获取驱动信息
     *
     * @param driver_id
     * @return
     */
    Driver getDriver(Long driver_id);

    /**
     * 获取驱动信息
     *
     * @param type
     * @return
     */
    Driver getDriver(String type);

    /*
    * 获取驱动信息
    *
    * @param driver
    * @return
    * */
    Driver getDriverBydriver(String driver);

    /**
     * 获取驱动详情
     *
     * @param driver_id
     * @return
     */
    DriverDetail getDriverDetail(Long driver_id);

    /**
     * 获取下发给盒子的数据
     * @param plcId
     * @return
     */
    Map<String, Object> getDriverExtend(long plcId);

    /**
     * 删除驱动
     *
     * @param model
     * @return
     */
    boolean delDriver(Driver model);
}
