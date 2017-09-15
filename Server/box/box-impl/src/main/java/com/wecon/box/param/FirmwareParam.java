package com.wecon.box.param;

import javax.validation.constraints.NotNull;

/**
 * Created by zengzhipeng on 2017/9/14.
 */
public class FirmwareParam {
    @NotNull
    public long firmware_id;
    @NotNull
    public long file_id;
    @NotNull
    public String dev_model;
    @NotNull
    public String firm_info;
    @NotNull
    public String version_code;
    @NotNull
    public String version_name;
    public String description;

    public void setFirmware_id(long firmware_id) {
        this.firmware_id = firmware_id;
    }

    public void setFile_id(long file_id) {
        this.file_id = file_id;
    }

    public void setDev_model(String dev_model) {
        this.dev_model = dev_model;
    }

    public void setFirm_info(String firm_info) {
        this.firm_info = firm_info;
    }

    public void setVersion_code(String version_code) {
        this.version_code = version_code;
    }

    public void setVersion_name(String version_name) {
        this.version_name = version_name;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
