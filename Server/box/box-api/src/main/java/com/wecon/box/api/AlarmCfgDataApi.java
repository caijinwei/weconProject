package com.wecon.box.api;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import com.wecon.box.entity.AlarmCfgData;
import com.wecon.box.entity.AlarmCfgDataAlarmCfg;
import com.wecon.box.entity.Page;
import com.wecon.box.entity.RealHisCfg;
import com.wecon.box.filter.AlarmCfgDataFilter;

/**
 * @author lanpenghui 2017年8月1日
 */
@Component
public interface AlarmCfgDataApi {

	/**
	 * 批量保存报警数据
	 * 
	 * @param listmodel
	 * @return
	 */
	public void saveAlarmCfgData(List<AlarmCfgData> listmodel);

	/**
	 * 更新报警数据
	 * 
	 * @param alarmCfgData
	 * @return
	 */
	public boolean updateAlarmCfgData(AlarmCfgData alarmCfgData);

	/**
	 * 根据filter取列表报警数据
	 *
	 * @param filter
	 * @return
	 */
	public List<AlarmCfgDataAlarmCfg> getAlarmCfgData(AlarmCfgDataFilter filter);

	/**
	 * 根据alarm_cfg_id,monitor_time取某个报警数据配置
	 *
	 * @param alarm_cfg_id
	 * @return
	 */
	public AlarmCfgData getAlarmCfgData(long alarm_cfg_id, Timestamp monitor_time);

	/**
	 * 根据alarm_cfg_id删除某个报警数据
	 * 
	 * @param alarm_cfg_id
	 * 
	 */
	public void delAlarmCfgData(long alarm_cfg_id);

	/**
	 * 批量删除报警配置数据根据plc_id
	 * 
	 * @param ids
	 * @return
	 */
	boolean batchDeleteByPlcId(List<Long> ids);

	/**
	 * 批量删除报警配置数据根据alarm_cfg_id
	 * 
	 * @param ids
	 * @return
	 */
	boolean batchDeleteById(final List<Long> ids);

	

	/**
	 * 分页查询报警数据
	 * 
	 * @param filter
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 */
	Page<AlarmCfgDataAlarmCfg> getRealHisCfgDataList(AlarmCfgDataFilter filter, int pageIndex, int pageSize);

	Page<AlarmCfgDataAlarmCfg> getViewRealHisCfgDataList(AlarmCfgDataFilter filter, int pageIndex, int pageSize);

	Page<AlarmCfgDataAlarmCfg> getAdminAlarmCfgDataPage(AlarmCfgDataFilter filter, Map<String, Object> bParams,
			int pageIndex, int pageSize);

	Page<AlarmCfgDataAlarmCfg> getViewAlarmCfgDataPage(AlarmCfgDataFilter filter, Map<String, Object> bParams,
			int pageIndex, int pageSize);
}
