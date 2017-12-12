package com.wecon.box.action;

import com.wecon.box.api.AlarmCfgDataApi;
import com.wecon.box.api.RealHisCfgDataApi;
import com.wecon.box.entity.AlarmCfgDataAlarmCfg;
import com.wecon.box.entity.Page;
import com.wecon.box.entity.RealHisCfgData;
import com.wecon.box.filter.AlarmCfgDataFilter;
import com.wecon.box.filter.RealHisCfgDataFilter;
import com.wecon.box.param.AlarmDataParam;
import com.wecon.box.util.ExcelUtils;
import com.wecon.common.util.CommonUtils;
import com.wecon.restful.core.AppContext;
import com.wecon.restful.core.Client;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by whp on 2017/10/25.
 */
@RestController
@RequestMapping("/excelact")
public class ExcelExportAction {
	private static final Logger logger = LogManager.getLogger(ExcelExportAction.class.getName());

	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

	private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

	@Autowired
	private RealHisCfgDataApi realHisCfgDataApi;

	@Autowired
	private AlarmCfgDataApi alarmCfgDataApi;

	@Description("excel导出历史数据")
	@RequestMapping(value = "/filedownloadExportHis", method = RequestMethod.POST)
	@ResponseBody
	public void exportHis(@RequestParam("real_his_cfg_id") String real_his_cfg_id,
			@RequestParam("start_date") String start_date, @RequestParam("end_date") String end_date,
			@RequestParam("pageIndex") Integer pageIndex, @RequestParam("pageSize") Integer pageSize) throws Exception {
		Page<RealHisCfgData> realHisCfgDataList = null;
		RealHisCfgDataFilter realHisCfgDataFilter = new RealHisCfgDataFilter();
		if (!CommonUtils.isNullOrEmpty(real_his_cfg_id)) {
			realHisCfgDataFilter.real_his_cfg_id = Long.parseLong(real_his_cfg_id);
			realHisCfgDataFilter.start_date = start_date;
			realHisCfgDataFilter.end_date = end_date;
			realHisCfgDataFilter.state = -1;
			realHisCfgDataList = realHisCfgDataApi.getRealHisCfgDataList(realHisCfgDataFilter, pageIndex, pageSize);
		}

		String[] rowName = new String[] { "状态", "时间", "数值" };
		HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
				.getResponse();
		List<Object[]> dataList = new ArrayList<>();
		if (null != realHisCfgDataList && null != realHisCfgDataList.getList()) {
			List<RealHisCfgData> rList = realHisCfgDataList.getList();
			for (RealHisCfgData r : rList) {
				String stateText = "超时";
				if (0 == r.state) {
					stateText = "离线";
				} else if (1 == r.state) {
					stateText = "在线";
				}
				dataList.add(new Object[] { stateText, dateFormat.format(r.monitor_time), r.value });
			}
		}

		ExcelUtils.exportExcel(response, sdf.format(new Date()) + "历史数据", rowName, dataList);

	}

	@Description("excel导出报警数据")
	@RequestMapping(value = "/filedownloadExportAlarm", method = RequestMethod.POST)
	@ResponseBody
	public void exportAlarm(@Valid AlarmDataParam alarmDataParam) throws Exception {
		Client client = AppContext.getSession().client;
		Page<AlarmCfgDataAlarmCfg> alarmCfgDataAlarmCfgList = null;
		AlarmCfgDataFilter filter = new AlarmCfgDataFilter();
		if (!CommonUtils.isNullOrEmpty(alarmDataParam.alarm_cfg_id)) {
			filter.alarm_cfg_id = Long.parseLong(alarmDataParam.alarm_cfg_id);
		}
		if (!CommonUtils.isNullOrEmpty(alarmDataParam.name)) {
			filter.name = alarmDataParam.name;
		}
		if (!CommonUtils.isNullOrEmpty(alarmDataParam.device_id)) {
			filter.device_id = Long.parseLong(alarmDataParam.device_id);
		}
		if (!CommonUtils.isNullOrEmpty(alarmDataParam.start_date)) {
			filter.start_date = alarmDataParam.start_date;
		}

		if (!CommonUtils.isNullOrEmpty(alarmDataParam.end_date)) {
			filter.end_date = alarmDataParam.end_date;
		}
		if (!CommonUtils.isNullOrEmpty(alarmDataParam.state)) {
			filter.state = Integer.parseInt(alarmDataParam.state);
		}
		if (!CommonUtils.isNullOrEmpty(alarmDataParam.grade_id)) {
			filter.grade_id = Integer.parseInt(alarmDataParam.grade_id);
		}
		if (!CommonUtils.isNullOrEmpty(alarmDataParam.event_id)) {
			filter.event_id = Integer.parseInt(alarmDataParam.event_id);
		}

		filter.account_id = client.userId;

		if (client.userInfo.getUserType() == 1) {
			/** 管理 **/

			alarmCfgDataAlarmCfgList = alarmCfgDataApi.getRealHisCfgDataList(filter, alarmDataParam.pageIndex,
					alarmDataParam.pageSize);

		} else if (client.userInfo.getUserType() == 2) {
			/** 视图 **/
			alarmCfgDataAlarmCfgList = alarmCfgDataApi.getViewRealHisCfgDataList(filter, alarmDataParam.pageIndex,
					alarmDataParam.pageSize);

		}

		String[] rowName = new String[] { "编码", "名称", "报警等级", "信息", "状态", "值", "事件", "时间" };
		HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
				.getResponse();
		List<Object[]> dataList = new ArrayList<>();
		if (null != alarmCfgDataAlarmCfgList && null != alarmCfgDataAlarmCfgList.getList()) {
			List<AlarmCfgDataAlarmCfg> rList = alarmCfgDataAlarmCfgList.getList();
			for (AlarmCfgDataAlarmCfg r : rList) {

				dataList.add(new Object[] { r.alarm_cfg_id, r.name,
						r.alarm_level == 1 ? "一般报警" : r.alarm_level == 2 ? "严重报警" : "特别严重报警", r.text,
						r.state == 2 ? "确认" : "未确认", r.value, r.alarm_type == 0 ? "恢复" : "触发",
						dateFormat.format(r.monitor_time) });
			}
		}

		ExcelUtils.exportExcel(response, sdf.format(new Date()) + "报警数据", rowName, dataList);

	}
}
