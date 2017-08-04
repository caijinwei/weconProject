package com.wecon.box.api;

import org.springframework.stereotype.Component;
/**
 * @author lanpenghui
 * 2017年8月1日
 */

import com.wecon.box.entity.Configinfo;
@Component
public interface ConfiginfoApi {
	/**
	 * 保存配置信息
	 *
	 * @param model
	 * @return
	 */
	public long SaveConfiginfo(Configinfo model);

	/**
	 * 更新配置信息
	 *
	 * @param model
	 * @return
	 */
	public boolean updateConfiginfo(Configinfo model);

	/**
	 * 根据id取某个配置信息
	 *
	 * @param id
	 * @return
	 */
	public Configinfo getConfiginfo(long id);

	/**
	 * 根据id删除某个配置信息
	 * 
	 * @param id
	 * 
	 */
	public void delConfiginfo(long id);
}
