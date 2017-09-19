package com.wecon.box.param;

import javax.validation.constraints.NotNull;

/**
 * Created by zengzhipeng on 2017/9/16.
 */
public class DriverParam {
    @NotNull
    public Long driver_id;
    @NotNull
    public Long file_id;
    @NotNull
    public String file_md5;
    @NotNull
    public String type;
    @NotNull
    public String driver;

    public void setDriver_id(Long driver_id) {
        this.driver_id = driver_id;
    }

    public void setFile_id(Long file_id) {
        this.file_id = file_id;
    }

    public void setFile_md5(String file_md5) {
        this.file_md5 = file_md5;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }
}
