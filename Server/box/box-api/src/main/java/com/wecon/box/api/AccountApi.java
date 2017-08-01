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
     * 注册用户
     *
     * @param model
     * @return
     */
    public long registerAccount(Account model);

    /**
     * 更新用户信息
     *
     * @param model
     * @return
     */
    public boolean updateAccount(Account model);

    /**
     * 根据ID取帐号信息
     *
     * @param account_id
     * @return
     */
    public Account getAccount(long account_id);

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
