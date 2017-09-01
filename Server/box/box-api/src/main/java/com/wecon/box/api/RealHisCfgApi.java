package com.wecon.box.api;

import com.wecon.box.entity.Page;
import com.wecon.box.entity.RealHisCfg;
import com.wecon.box.entity.RealHisCfgDevice;
import com.wecon.box.entity.RealHisCfgExtend;
import com.wecon.box.filter.RealHisCfgFilter;
import com.wecon.box.filter.ViewAccountRoleFilter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author lanpenghui 2017年8月1日
 */
@Component
public interface RealHisCfgApi {

	/**
	 * 保存历史（实时）数据配置信息
	 * 
	 * @param model
	 * @return
	 */
	public long saveRealHisCfg(RealHisCfg model);

	/**
	 * 更新历史（实时）数据配置信息
	 * 
	 * @param model
	 * @return
	 */
	public boolean updateRealHisCfg(RealHisCfg model);

	/**
	 * 获取某条历史（实时）数据配置信息
	 * 
	 * @param id
	 * @return
	 */
	public RealHisCfg getRealHisCfg(long id);

	/**
	 * 通过filter获取历史（实时）数据配置信息列表
	 * 
	 * @param
	 * @return
	 */
	Page<RealHisCfgDevice> getRealHisCfg(RealHisCfgFilter filter,int pageIndex, int pageSize);
	public List<RealHisCfgDevice> getRealHisCfg(RealHisCfgFilter filter);
	/**
	 * 通过filter获取历史（实时）数据配置信息列表
	 * 
	 * @param filter
	 * @return
	 */
	Page<RealHisCfgDevice> getRealHisCfg(ViewAccountRoleFilter filter,int pageIndex, int pageSize);
	public List<RealHisCfgDevice> getRealHisCfg(ViewAccountRoleFilter filter);

	/**
	 * 通过通讯口id和启用状态获取列表历史（实时）数据配置信息
	 * 
	 * @param plc_id
	 * @param state
	 * @return
	 */
	public List<RealHisCfg> getRealHisCfg(long plc_id, int state);

	/**
	 * 删除某个id的历史（实时）数据配置信息
	 * 
	 * @param id
	 */
	public void delRealHisCfg(long id);

	/**
	 * 获取某条历史（实时）数据配置的分页列表
	 * 
	 * @param filter
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 */
	Page<RealHisCfgDevice> getRealHisCfgList(RealHisCfgFilter filter, int pageIndex, int pageSize);
	Page<RealHisCfgDevice> getRealHisCfgList(ViewAccountRoleFilter filter, int pageIndex, int pageSize);

	/**
	 * 通过filter及外部参数分页获取历史（实时）数据配置信息列表
	 *
	 * @param filter
	 * @return
	 */
	 Page<RealHisCfgDevice> getRealHisCfgDevicePage(RealHisCfgFilter filter, Map<String, Object> bParams, int pageIndex, int pageSize);

	/**
	 * 通过filter及外部参数分页获取历史（实时）数据配置信息列表
	 *
	 * @param filter
	 * @return
	 */
	 Page<RealHisCfgDevice> getRealHisCfgDevicePage(ViewAccountRoleFilter filter, Map<String, Object> bParams, int pageIndex, int pageSize);

	/**
	 * 根据状态获取历史（实时）数据配置信息列表
	 * @param state
	 * @return
     */
	List<RealHisCfgExtend> getRealHisCfgListByState(int state);

	/**
	 * 批量更新状态
	 * @param updList
	 * @return
	 */
	boolean batchUpdateState(final List<int[]> updList);

	/*
	* 查找id根据device_id
	* */
	public ArrayList<Integer> findRealHisCfgIdSBydevice_id(Integer device_id);

	/*
	* 解绑device
	* */
	public void setBind_state(final int[] realHisCfg, final Integer state);
	/*
	* 盒子用户改变  监控点迁移
	* */
	public boolean updatePointAccAndState(long accountId,long deviceId);

	}
