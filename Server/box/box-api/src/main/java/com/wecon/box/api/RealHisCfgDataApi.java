package com.wecon.box.api;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.stereotype.Component;
import com.wecon.box.entity.Page;
import com.wecon.box.entity.RealHisCfgData;
import com.wecon.box.filter.RealHisCfgDataFilter;

/**
 * @author lanpenghui 2017年8月1日
 */
@Component
public interface RealHisCfgDataApi {
	/**
	 * 保存历史（实时）数据信息
	 * 
	 * @param model
	 * @return
	 */
	public long saveRealHisCfgData(RealHisCfgData model);
	/**
	 * 批量保存历史（实时）数据信息
	 * 
	 * @param listmodel
	 * @return
	 */
	public void saveRealHisCfgData(List<RealHisCfgData> listmodel);
	/**
	 * 获取某条历史（实时）数据信息
	 * @param real_his_cfg_id
	 * @param monitor_time
	 * @return
	 */
	public RealHisCfgData getRealHisCfgData(long real_his_cfg_id,Timestamp monitor_time);
	

	/**
	 * 删除该配置下的所有历史（实时）数据信息
	 * 
	 * @param real_his_cfg_id
	 */
	public void delRealHisCfgData(long real_his_cfg_id);

	/**
	 * 获取某条历史（实时）数据的分页列表
	 * 
	 * @param filter
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 */
	Page<RealHisCfgData> getRealHisCfgDataList(RealHisCfgDataFilter filter, int pageIndex, int pageSize);
}
