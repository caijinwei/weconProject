package com.wecon.box.api;

import com.wecon.box.entity.Account;
import com.wecon.box.entity.Page;
import com.wecon.box.filter.AccountFilter;
import org.springframework.stereotype.Component;

/**
 * Created by zengzhipeng on 2017/7/31.
 */
@Component
public interface AccountApi {


    /**
     * 邮箱注册
     *
     * @param username
     * @param email
     * @param password
     * @return
     */
    Account signupByEmail(String username, String email, String password);

    /**
     * 创建会话
     *
     * @param user
     * @param productId
     * @param fuid
     * @param loginIp
     * @param loginTime
     * @param seconds
     * @return
     */
    String createSession(Account user, int productId, String fuid, long loginIp, long loginTime, int seconds);

    /**
     * 更新用户信息
     *
     * @param model
     * @return
     */
    boolean updateAccount(Account model);

    /**
     * 根据ID取帐号信息
     *
     * @param account_id
     * @return
     */
    Account getAccount(long account_id);

    /**
     * 获取用户的分页列表
     *
     * @param filter
     * @param pageIndex
     * @param pageSize
     * @return
     */
    Page<Account> getAccountList(AccountFilter filter, int pageIndex, int pageSize);
}
