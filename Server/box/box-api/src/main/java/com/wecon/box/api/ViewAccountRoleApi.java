package com.wecon.box.api;

import com.wecon.box.entity.*;
import com.wecon.box.filter.ViewAccountRoleFilter;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author lanpenghui 2017年8月1日
 */
@Component
public interface ViewAccountRoleApi {
	/**
	 * 保存视图账号
	 *
	 * @param model
	 * @return
	 */
	public long saveViewAccountRole(ViewAccountRole model);

	/**
	 * 更新视图账号
	 *
	 * @param model
	 * @return
	 */
	public boolean updateViewAccountRole(ViewAccountRole model);

	/*
	 * @Param view_id account_id pageIndex pageSize 视图账号ID 管理员账户ID 当前页 每页显示条数
	 *
	 * 获取 该视图账户中未有的监控点 需要分页
	 */
	public Page<RealHisCfg> getViewRealHisCfgByViewAndAccId(long view_id, long account_id, Integer type,
			Integer pageIndex, Integer pageSize);

	/*
	 * 盒子下的报警监控点
	 */
	public Page<AlarmCfg> getViewAlarmCfgByViewAndDeivceId(long account_id, long view_id, Integer device_id,
			Integer pageIndex, Integer pageSize);

	/*
	 * 账户下的报警监控点
	 */
	public Page<AlarmCfg> getViewAlarmCfgByView(long account_id, long view_id, Integer pageIndex, Integer pageSize);

	/*
	 * 盒子下的余下监控点
	 **/
	public Page<RealHisCfg> getViewRealHisCfgByViewAndDeivceId(long account_id, long view_id, long device_id,
			Integer type, Integer pageIndex, Integer pageSize);

	/**
	 * 根据view_id删除某个视图账号
	 *
	 * @param view_id
	 */
	public void delViewAccountRole(long view_id);

	/**
	 * 获取用户的分页列表
	 *
	 * @param filter
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 */
	Page<ViewAccountRole> getViewAccountRoleList(ViewAccountRoleFilter filter, int pageIndex, int pageSize);

	/*
	 * @param view_id 根据视图账号ID获取 viewAccountRole 视图监控点 连接 realHisCfg 获取监控点name
	 *
	 * SELECT * FROM view_account_role a,real_his_cfg b WHERE a.cfg_id=b.id AND
	 * a.view_id="1" AND a.cfg_type='1'AND b.data_type='0'; cgf_type ： 1实时历史监控点
	 * 2报警监控点 data_type 0：实时监控点 1：历史监控点
	 **/

	public Page<ViewAccountRoleView> getViewAccountRoleViewByViewID(Integer data_type, long view_id, Integer pageIndex,
			Integer pageSize);

	/*
	 * 为视图账号分配监控监控点
	 */
	public void setViewPoint(Integer viewId, String[] ids, String[] rights, Integer cgf_type);

	/*
	 * 视图用户监控点解绑
	 * 
	 * @param viewId ， roleType pointId 视图账号ID 监控点类型 监控点ID
	 */
	public void deletePoint(Integer viewId, Integer roleType, Integer pointId);
	public void deletePoint(Integer roleType, long pointId);

	/*
	 * 视图账户监控点权限设置
	 * 
	 * @param viewId pointId roleType 视图账户ID 监控点ID 权限：0无权限 1只读 3读写 UPDATE
	 * view_account_role SET role_type="1" WHERE view_id=111 AND cfg_id=12 ;
	 */
	public void updateViewPointRoleType(Integer viewId, Integer pointId, Integer roleType);

	/*
	 * 户报警监控点展示 SELECT a.cfg_id,a.role_type,b.name,b.addr,b.state FROM
	 * view_account_role a INNER JOIN alarm_cfg b ON a.cfg_id = b.alarmcfg_id
	 * WHERE a.cfg_type = '2' AND a.view_id=1000021 LIMIT 1,5;
	 */
	public Page<ViewAccountRoleView> getAlarmViewAccountRoleViewByViewID(long view_id, Integer pageIndex,
			Integer pageSize);

	/*
	* 解绑盒子  删除盒子下的视图
	* */
	public void deleteViewAccountRoleByCfgId(final List<Integer> cfgIds,final Integer type);
	/*
	* 根据视图账户ID  监控点ID  监控点类型 获取ViewAccountRoleView
	* */
	public ViewAccountRole findViewAccountRoleById(long accId,Integer cfgType);

	/**
	 * 批量删除数据
	 * @param cfgIds
	 * @param type
     * @return
     */
	boolean batchDeleteViewAccRoleByCfgId(final List<Long> cfgIds, int type);
}
