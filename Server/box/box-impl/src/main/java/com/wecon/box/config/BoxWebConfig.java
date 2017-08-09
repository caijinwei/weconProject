package com.wecon.box.config;

/**
 * 站点的一些配置信息
 * Created by zengzhipeng on 2017/8/4.
 */
public class BoxWebConfig {
    /**
     * 邮箱激活帐号地址
     */
    private String emailActiveUrl;

    public String getEmailActiveUrl() {
        return emailActiveUrl;
    }

    public void setEmailActiveUrl(String emailActiveUrl) {
        this.emailActiveUrl = emailActiveUrl;
    }
}
