package com.wecon.box.param;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Created by zengzhipeng on 2017/9/16.
 */
public class DriverBatchParam {
    @NotNull
    public List<DriverParam> driverParams;

    public void setDriverParams(List<DriverParam> driverParams) {
        this.driverParams = driverParams;
    }
}
