package com.wecon.box.api;

import java.util.List;

import org.springframework.stereotype.Component;

import com.wecon.box.entity.Account;
import com.wecon.box.entity.AccountDir;
/**
 * @author lanpenghui
 * 2017年8月1日
 */
@Component
public interface AccountDirApi {
	/**
	 * 创建监控点分组
	 *
	 * @param model
	 * @return
	 */
	public long SaveAccountDir(AccountDir model);

	/**
	 * 更新监控点分组
	 *
	 * @param model
	 * @return
	 */
	public boolean updateAccountDir(AccountDir model);

	/**
	 * 根据用户查询对应的监控组列表
	 * @param account
	 * @return
	 */
	public List<AccountDir> getAccountDir(Account account);
	/**
	 * 根据id查询对应的监控组
	 * @param id
	 * @return
	 */
	public AccountDir getAccountDir(long id);

	/**
	 * 根据ID删除某个监控点分组
	 * 
	 * @param id
	 */
	public void delAccountDir(long id);
	

}
