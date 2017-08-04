package com.wecon.box.api;

import org.springframework.stereotype.Component;
/**
 * @author lanpenghui
 * 2017年8月1日
 */

import com.wecon.box.entity.DataType;

@Component
public interface DataTypeApi {
	/**
	 * 保存数据类型
	 * 
	 * @param model
	 * @return
	 */
	public long saveDataType(DataType model);

	/**
	 * 更新数据类型
	 * 
	 * @param model
	 * @return
	 */
	public boolean updateDataType(DataType model);

	/**
	 * 获取某条数据类型
	 * 
	 * @param data_id
	 * @return
	 */
	public DataType getDataType(long data_id);

	/**
	 * 根据id删除某条数据类型
	 * 
	 * @param data_id
	 */
	public void delDataType(long data_id);

}
