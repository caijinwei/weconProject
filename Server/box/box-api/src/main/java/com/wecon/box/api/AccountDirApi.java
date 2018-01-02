package com.wecon.box.api;

import com.wecon.box.entity.AccountDir;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author lanpenghui
 *         2017年8月1日
 */
@Component
public interface AccountDirApi {
    /**
     * 创建监控点分组
     *
     * @param model
     * @return
     */
    public long addAccountDir(AccountDir model);

    /**
     * 更新监控点分组
     *
     * @param model
     * @return
     */
    public boolean updateAccountDir(AccountDir model);

    /**
     * 删除分组，同时会删除绑定关系
     *
     * @param id
     * @return
     */
    boolean delAccountDir(long id);

    /**
     * 获取分组详情
     *
     * @param id
     * @return
     */
    AccountDir getAccountDir(long id);

    /**
     * 获取指定分类的用户自定义分组
     *
     * @param account_id
     * @param type
     * @return
     */
    List<AccountDir> getAccountDirList(long account_id, int type);

    List<AccountDir> getAccountDirList(long account_id, int type, long device_id);

    /*
    * 更新用户id
    * */
    public void updateAccountBydeviceAndType(long accountId,long deviceId);

    /*
    * 复制分组信息
    * */
    public Map<Long,Long> copyAccountDir(long accountId,long fromDeviceId,long toDeviceId,int type);

    /*
    * 复制分组（添加）
    * 允许同名分组
    * */
    public long copyAddAccountDir(AccountDir model);
}
