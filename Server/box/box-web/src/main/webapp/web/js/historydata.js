var appModule = angular.module('weconweb', []);
appModule
		.controller(
				"infoController",
				function($scope, $http, $compile, $filter) {
					$scope.onInit = function() {
						$scope.deviceid = T.common.util
								.getParameter("device_id");
						$scope.devicename = T.common.util
								.getParameter("device_name");

						$scope.commointor_submit();

						$('.form_datetime').datetimepicker({
							// language: 'fr',
							weekStart : 1,
							todayBtn : 1,
							autoclose : 1,
							todayHighlight : 1,
							startView : 2,
							forceParse : 0,
							showMeridian : 1,
							pickerPosition : "bottom-left"
						});

					}

					/**
					 * 提交接口请求
					 */
					$scope.commointor_submit = function() {
						$("#loadingModal").modal("show");
						var params = {
							device_id : $scope.deviceid

						};
						T.common.ajax.request("WeconBox",
								"hisDataAction/getComMonitor", params,
								function(data, code, msg) {
									if (code == 200) {
										$scope.commonitors = data.monitors;
										$scope.accounttype = data.type;

										if ($scope.commonitors == "") {
											$("#searchid").attr("disabled",
													true);
										}
										$scope.$apply();

										$scope.paginationConf = {
											currentPage : 1,
											itemsPerPage : 10,
											totalItems : $scope.count,
											pagesLength : 15,
											perPageOptions : [ 5, 10, 20, 50,
													100 ],
											rememberPerPage : 'perPageItems',
											onChange : function() {
												if (this.currentPage != 0) {
													$scope.searchHisData(
															this.currentPage,
															this.itemsPerPage);
												}
											}
										}
										$scope.paginationConf_register = {
											currentPage : 1,
											itemsPerPage : 10,
											totalItems : $scope.count,
											pagesLength : 15,
											perPageOptions : [ 5, 10, 20, 50,
													100 ],
											rememberPerPage : 'perPageItems',
											onChange : function() {
												if (this.currentPage != 0) {
													$scope.showhisconf(
															this.currentPage,
															this.itemsPerPage);
												}
											}
										}

										$("#loadingModal").modal("hide");

									} else {
										$("#loadingModal").modal("hide");

										alert(code + "-" + msg);
									}
								}, function() {
									$("#loadingModal").modal("hide");

									alert("ajax error");
								});
					}
					$scope.paginationConf = {
						totalItems : $scope.count,
					}
					$scope.paginationConf_register = {
						totalItems : $scope.count,
					}
					$scope.searchHisData = function(pageIndex, pageSize) {
						if (pageIndex == 0)
							pageIndex = 1;
						$("#loadingModal").modal("show");
						var params = {
							real_his_cfg_id : $("#monitorid").val(),
							start_date : $("#startdateid").val(),
							end_date : $("#enddateid").val(),
							pageIndex : pageIndex,
							pageSize : pageSize

						};
						T.common.ajax
								.request(
										"WeconBox",
										"hisDataAction/getHisData",
										params,
										function(data, code, msg) {
											if (code == 200) {
												$scope.paginationConf.totalItems = data.realHisCfgDataList.totalRecord;
												$scope.Hisdatas = data.realHisCfgDataList.list;
												var xval = new Array();
												var yval = new Array();
												angular
														.forEach(
																data.realHisCfgDataList.list,
																function(data,
																		index,
																		array) {

																	xval
																			.push($filter(
																					'date')
																					(
																							data.monitor_time,
																							"yyyy-MM-dd HH:mm:ss"));

																	yval
																			.push(parseFloat(data.value));

																});
												angular
														.forEach(
																$scope.commonitors,
																function(data,
																		index,
																		array) {
																	if ($(
																			"#monitorid")
																			.val() == data.id) {
																		$scope.monitorName = data.name;
																	}

																});

												var chart = new Highcharts.Chart(
														'original-graph-container',
														{
															title : {
																text : '平均数值',
																x : -20
															},
															subtitle : {
																text : '数据来源: we-con.com.cn',
																x : -20
															},
															xAxis : {
																categories : xval
															},
															yAxis : {
																title : {
																	text : '值'
																},
																plotLines : [ {
																	value : 0,
																	width : 1,
																	color : '#808080'
																} ]
															},
															tooltip : {
																valueSuffix : ''
															},
															legend : {
																layout : 'vertical',
																align : 'right',
																verticalAlign : 'middle',
																borderWidth : 0
															},
															series : [ {
																name : $scope.monitorName,
																data : yval

															} ]
														});

												$scope.$apply();
												$("#loadingModal")
														.modal("hide");
											} else {

												alert(code + "-" + msg);
												$("#loadingModal")
														.modal("hide");
											}
										}, function() {
											alert("ajax error");
										});
					}
					// 原始数据界面，列表视图、曲线视图
					$scope.showListOrCurves = function(btnId) {
						var checkType = btnId; // 查看类型
						$('#btn-list,#btn-curves').attr('class',
								'btn btn-default');
						$('#list-view,#curves-view').css('display', 'none');
						switch (btnId) {
						case 'btn-list':
							$('#btn-list').attr('class', 'btn btn-primary');
							$('#list-view').css('display', 'block');
							break;
						case 'btn-curves':
							$('#btn-curves').attr('class', 'btn btn-primary');
							$('#curves-view').css('display', 'block');

							break;
						default:
							alert("视图切换异常！");
							break;
						}
					}
					/*
					 * 盒子下plc配置展示
					 */
					var mtype = 0;
					$scope.showAllPlcConf = function(dealtype) {
						$scope.getDataType();
						mtype = dealtype;
						var params = {
							device_id : $scope.deviceid
						};
						T.common.ajax
								.request(
										"WeconBox",
										"plcInfoAction/showAllPlcConf",
										params,
										function(data, code, msg) {
											if (code == 200) {
												$scope.infoDatas = data.infoDatas;
												$scope.$apply();
												if (dealtype == 0) {
													$scope.showtype = 0;
													$("#datatypeid")
															.val(
																	$scope.dataTypes[0].value);

													mid = -1;

													if (data.infoDatas != "") {
														$scope
																.condevice(data.infoDatas[0].plcId);
														$("#nameid ").val("");
														$("#addrid").val("");
														$("#describeid")
																.val("");

													}
												} else {
													$scope.showtype = 1;
													$("#nameid ").val(
															minfo.name);
													$("#conid ").val(
															minfo.plc_id);
													$scope
															.condevice(minfo.plc_id);

													$("#datatypeid").val(
															minfo.data_id);
													$("#hiscycleid").val(
															minfo.his_cycle);

													$("#addrid")
															.val(minfo.addr);
													$("#describeid").val(
															minfo.describe);

												}

											} else {
												alert(code + "-" + msg);
											}
										}, function() {
											alert("ajax error");
										});
					}
					/**
					 * 获取数据类型
					 */
					$scope.getDataType = function() {
						var params = {};
						T.common.ajax.request("WeconBox",
								"actDataAction/getDataType", params, function(
										data, code, msg) {
									if (code == 200) {
										$scope.dataTypes = data.DataTypeOption;
										$scope.$apply();
									} else {
										alert(code + "-" + msg);
									}
								}, function() {
									alert("ajax error");
								});

					}
					/**
					 * 连接设备点击响应
					 */
					$("#conid").change(function() {

						$scope.condevice($("#conid").val());

					});

					var plcId;
					$scope.condevice = function(clickplc) {
						plcId = clickplc;
						var params = {
							plc_id : clickplc
						};
						T.common.ajax
								.request(
										"WeconBox",
										"actDataAction/getAddrType",
										params,
										function(data, code, msg) {
											if (code == 200) {
												$scope.allAddrs = data.allAddr;

												if (data.allAddr != "") {

													$scope.addrvalues = data.allAddr[0].addrRid;
													if (data.allAddr[0].addrRid != null) {
														$("#rangid")
																.val(
																		data.allAddr[0].addrRid[0].range);
													}
													$scope.$apply();
													if (mtype == 1) {

														$("#addrtypeid")
																.val(
																		minfo.addr_type);
														angular
																.forEach(
																		$scope.allAddrs,
																		function(
																				data,
																				index,
																				array) {
																			if ($(
																					"#addrtypeid")
																					.val() == data.addrkey) {
																				$scope.addrvalues = data.addrRid;
																				$scope
																						.$apply();

																			}

																		})

														$("#registerid").val(
																minfo.rid);
														if ($("#registerid")
																.val() == null) {
															$("#addrtypeid")
																	.val(
																			data.allAddr[0].addrkey);

															if ($scope.addrvalues != null) {
																$("#registerid")
																		.val(
																				$scope.addrvalues[0].addrvalue);

																$("#rangid")
																		.val(
																				$scope.addrvalues[0].range);

															}

														} else {
															$("#rangid")
																	.val(
																			minfo.data_limit);

														}
													}
												}

											} else {
												alert(code + "-" + msg);
											}
										}, function() {
											alert("ajax error");
										});

					}
					// 地址类型点击
					$("#addrtypeid").change(function() {

						$scope.changeaddrtype($("#addrtypeid").val());

					});
					$scope.changeaddrtype = function(value) {
						angular.forEach($scope.allAddrs, function(data, index,
								array) {
							if (value == data.addrkey) {
								$scope.addrvalues = data.addrRid;
								$("#rangid").val(data.addrRid[0].range);
								$scope.$apply();
							}

						})

					}

					$("#registerid").change(function() {

						$scope.changeaddr($("#registerid").val());

					});
					// 寄存器地址点击
					$scope.changeaddr = function(value) {
						angular.forEach($scope.addrvalues, function(data,
								index, array) {
							if (value == data.addrvalue) {
								$("#rangid").val(data.range);

							}

						})

					}
					var mid = -1;
					var minfo = null;
					// 获取修改监控点的信息
					$scope.editmonitor = function(model) {
						minfo = model;
						mid = model.id;
						$scope.showAllPlcConf(1);

					}
					$scope.addmonitor = function() {

						mid = -1;
						$scope.showAllPlcConf(0);

					}
					// 保存添加/修改监控点
					$scope.saveupmonitor = function() {
						if ($("#nameid").val() == ""
								|| $("#addrid").val() == "") {
							alert("参数未配置完整！");
							return;
						}
						if ($("#hiscycleid").val() == ""
								|| $("#hiscycleid").val() < 1) {
							alert("请输入大于0的采集周期");
							return;
						}
						var params = {
							id : mid,
							plc_id : plcId,
							device_id : $scope.deviceid,
							name : $("#nameid").val(),
							data_id : $("#datatypeid").val(),
							addr_type : $("#addrtypeid").val(),
							addr : $("#addrid").val(),
							rid : $("#registerid").val(),
							rang : $("#rangid").val(),
							describe : $("#describeid").val(),
							data_type : "1",
							his_cycle : $("#hiscycleid").val()
						};

						T.common.ajax
								.request(
										"WeconBox",
										"actDataAction/addUpdataMonitor",
										params,
										function(data, code, msg) {
											if (code == 200) {
												$("#dataRecord").modal("hide");
												$scope
														.showhisconf(
																$scope.paginationConf_register.currentPage,
																$scope.paginationConf_register.itemsPerPage);

												if (mtype == 0) {
													alert("数据登记成功");
												} else {
													alert("修改数据成功");
												}

											} else {
												alert(code + "-" + msg);
											}
										}, function() {
											alert("ajax error");
										});

					}
					// 删除监控点
					$scope.delmonitor = function(model) {
						$scope.delmonitorid = model.id;// 监控点id
						$("#delgroupid").html("确定要删除【" + model.name + "】数据吗？");
					}
					// 移除监控点
					$scope.del_monitor_group = function() {

						var params = {
							monitorid : $scope.delmonitorid,
						};

						T.common.ajax
								.request(
										"WeconBox",
										"hisDataAction/delHisMonitor",
										params,
										function(data, code, msg) {
											if (code == 200) {
												$("#deletehispoint").modal(
														"hide");
												$scope
														.showhisconf(
																$scope.paginationConf_register.currentPage,
																$scope.paginationConf_register.itemsPerPage);
												alert("删除成功！");

											} else {

												alert(code + "-" + msg);
											}
										}, function() {
											alert("ajax error");
										});
					}

					/**
					 * 获取历史数据配置信息
					 */
					$scope.showhisconf = function(pageIndex, pageSize) {
						if (pageIndex == 0)
							pageIndex = 1;
						var params = {
							device_id : $scope.deviceid,
							pageIndex : pageIndex,
							pageSize : pageSize

						};
						T.common.ajax
								.request(
										"WeconBox",
										"hisDataAction/getHisConfig",
										params,
										function(data, code, msg) {
											if (code == 200) {
												$scope.paginationConf_register.totalItems = data.HisAllotData.totalRecord;
												$scope.hisConfs = data.HisAllotData.list;
												$scope.$apply();

											} else {
												alert(code + "-" + msg);
											}
										}, function() {
											alert("ajax error");
										});

					}

				})