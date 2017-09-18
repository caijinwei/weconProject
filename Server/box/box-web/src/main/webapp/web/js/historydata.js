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
												$scope.datatype();
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
														$("#child_addrid").val(
																"");
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

													$("#dataid").val(
															minfo.digit_count);
													if ($("#addrtypeid").val() == 0) {// 如果是位地址隐藏
														$('#datadigitid').css(
																'display',
																'none');
														$("#dataid").val("");
														$("#decid").val("");

													} else {
														$('#datadigitid').css(
																'display',
																'block');
													}
													$("#dataid").val(minfo.num);
													$("#decid").val(minfo.dec);

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

														if (data.allAddr[0].addrRid[0].addrvalue == null) {
															$('#registeraddr')
																	.css(
																			'display',
																			'none');
															$(
																	'#child_registeraddr')
																	.css(
																			'display',
																			'none');

														} else {
															if (data.allAddr[0].addrRid[0].bitCount == null) {
																$(
																		'#child_registeraddr')
																		.css(
																				'display',
																				'none');

															} else {

																$(
																		'#child_registeraddr')
																		.css(
																				'display',
																				'block');
																$(
																		"#child_rangid")
																		.html(
																				data.allAddr[0].addrRid[0].bRange);
																$(
																		"#child_scaleid")
																		.html(
																				data.allAddr[0].addrRid[0].bJinzhi);

															}

															$('#registeraddr')
																	.css(
																			'display',
																			'block');
															$("#rangid")
																	.html(
																			data.allAddr[0].addrRid[0].range);
															$("#scaleid")
																	.html(
																			data.allAddr[0].addrRid[0].mJinzhi);
														}

													} else {

														$('#registeraddr').css(
																'display',
																'none');
														$('#child_registeraddr')
																.css('display',
																		'none');
													}
													$scope.$apply();
													if (mtype == 0) {
														if ($("#addrtypeid")
																.val() == 0) {// 如果是位地址隐藏
															$('#divdatatypeid')
																	.css(
																			'display',
																			'none');
															$('#datadigitid')
																	.css(
																			'display',
																			'none');

															$("#dataid")
																	.val("");
															$("#decid").val("");
														} else {
															$('#divdatatypeid')
																	.css(
																			'display',
																			'block');
															$('#datadigitid')
																	.css(
																			'display',
																			'block');
														}

													} else {
														if (minfo.child_addr != null) {
															$(
																	'#child_registeraddr')
																	.css(
																			'display',
																			'block');
															$("#child_addrid")
																	.val(
																			minfo.child_addr);

														} else {
															$(
																	'#child_registeraddr')
																	.css(
																			'display',
																			'none');
														}

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

															if ($("#addrtypeid")
																	.val() == 0) {// 如果是位地址隐藏
																$(
																		'#divdatatypeid')
																		.css(
																				'display',
																				'none');
																$(
																		'#datadigitid')
																		.css(
																				'display',
																				'none');
															} else {
																$(
																		'#divdatatypeid')
																		.css(
																				'display',
																				'block');
																$(
																		'#datadigitid')
																		.css(
																				'display',
																				'block');
															}

															if ($scope.addrvalues != null) {
																$("#registerid")
																		.val(
																				$scope.addrvalues[0].addrvalue);

																$("#rangid")
																		.html(
																				$scope.addrvalues[0].range);
																$("#scaleid")
																		.html(
																				$scope.addrvalues[0].mJinzhi);

															}

														} else {

															$("#rangid")
																	.html(
																			minfo.data_limit);
															if ($("#addrtypeid")
																	.val() == 0) {// 如果是位地址隐藏
																$(
																		'#divdatatypeid')
																		.css(
																				'display',
																				'none');
																$(
																		'#datadigitid')
																		.css(
																				'display',
																				'none');
															} else {
																$(
																		'#divdatatypeid')
																		.css(
																				'display',
																				'block');
																$(
																		'#datadigitid')
																		.css(
																				'display',
																				'block');
															}

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
					/*
					 * $scope.condevice = function(clickplc) { plcId = clickplc;
					 * var params = { plc_id : clickplc }; T.common.ajax
					 * .request( "WeconBox", "actDataAction/getAddrType",
					 * params, function(data, code, msg) { if (code == 200) {
					 * $scope.allAddrs = data.allAddr;
					 * 
					 * if (data.allAddr != "") {
					 * 
					 * $scope.addrvalues = data.allAddr[0].addrRid; if
					 * (data.allAddr[0].addrRid != null) { $("#rangid") .val(
					 * data.allAddr[0].addrRid[0].range); } $scope.$apply();
					 * 
					 * if ($("#addrtypeid").val() == 0) {// 如果是位地址隐藏
					 * $('#datadigitid').css( 'display', 'none'); } else {
					 * $('#datadigitid').css( 'display', 'block'); } if (mtype ==
					 * 1) {
					 * 
					 * $("#addrtypeid") .val( minfo.addr_type);
					 * 
					 * angular .forEach( $scope.allAddrs, function( data, index,
					 * array) { if ($( "#addrtypeid") .val() == data.addrkey) {
					 * $scope.addrvalues = data.addrRid; $scope .$apply(); } })
					 * 
					 * $("#registerid").val( minfo.rid); if ($("#registerid")
					 * .val() == null) { $("#addrtypeid") .val(
					 * data.allAddr[0].addrkey); if ($("#addrtypeid") .val() ==
					 * 0) {// 如果是位地址隐藏 $('#datadigitid') .css( 'display',
					 * 'none'); } else { $('#datadigitid') .css( 'display',
					 * 'block'); }
					 * 
					 * if ($scope.addrvalues != null) { $("#registerid") .val(
					 * $scope.addrvalues[0].addrvalue);
					 * 
					 * $("#rangid") .val( $scope.addrvalues[0].range); } } else {
					 * $("#rangid") .val( minfo.data_limit); } } } } else {
					 * alert(code + "-" + msg); } }, function() { alert("ajax
					 * error"); }); }
					 */
					// 地址类型点击
					$("#addrtypeid").change(function() {

						$scope.changeaddrtype($("#addrtypeid").val());

						if ($("#addrtypeid").val() == 0) {
							$('#divdatatypeid').css('display', 'none');
							$('#datadigitid').css('display', 'none');

							$("#dataid").val("");
							$("#decid").val("");
						} else {
							$('#divdatatypeid').css('display', 'block');
							$('#datadigitid').css('display', 'block');
						}

					});
					/*
					 * $scope.changeaddrtype = function(value) {
					 * angular.forEach($scope.allAddrs, function(data, index,
					 * array) { if (value == data.addrkey) { $scope.addrvalues =
					 * data.addrRid; $("#rangid").val(data.addrRid[0].range);
					 * $scope.$apply(); } }) }
					 */
					$scope.changeaddrtype = function(value) {
						angular
								.forEach(
										$scope.allAddrs,
										function(data, index, array) {
											if (value == data.addrkey) {
												$scope.addrvalues = data.addrRid;

												if (data.addrRid[0].addrvalue == null) {
													$('#registeraddr').css(
															'display', 'none');
													$('#child_registeraddr')
															.css('display',
																	'none');

												} else {
													if (data.addrRid[0].bitCount == null) {
														$('#child_registeraddr')
																.css('display',
																		'none');

													} else {

														$('#child_registeraddr')
																.css('display',
																		'block');
														$("#chlid_rangid")
																.html(
																		data.addrRid[0].bRange);
														$("#chlid_scaleid")
																.html(
																		data.addrRid[0].bJinzhi);
													}

													$('#registeraddr').css(
															'display', 'block');
													$("#rangid")
															.html(
																	data.addrRid[0].range);
													$("#scaleid")
															.html(
																	data.addrRid[0].mJinzhi);
												}

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
								$("#scaleid").html(data.mJinzhi);

							}

						})

					}
					/**
					 * 数据格式设置
					 */
					$("#datatypeid").change(function() {
						$scope.datatype();

					});
					$scope.datatype = function() {
						if ($("#datatypeid").val() == 100) {
							$("#dataid").attr("placeholder", "1~16");
							$("#dataid").attr("disabled", false); // 设置为可编辑
							$("#decid").attr("disabled", true); // 设置为不可编辑
							$("#decid").attr("placeholder", "无小数");
							$("#dataid").val("");
							$("#decid").val("");

						} else if ($("#datatypeid").val() == 101) {
							$("#dataid").attr("placeholder", "1~6");
							$("#dataid").attr("disabled", false); // 设置为可编辑
							$("#decid").attr("disabled", true); // 设置为不可编辑
							$("#decid").attr("placeholder", "无小数");
							$("#dataid").val("");
							$("#decid").val("");
						} else if ($("#datatypeid").val() == 102) {
							$("#dataid").attr("placeholder", "1~4");
							$("#dataid").attr("disabled", false); // 设置为可编辑
							$("#decid").attr("disabled", true); // 设置为不可编辑
							$("#decid").attr("placeholder", "无小数");
							$("#dataid").val("");
							$("#decid").val("");

						} else if ($("#datatypeid").val() == 103) {
							$("#dataid").attr("placeholder", "0~4");
							$("#dataid").attr("disabled", false); // 设置为可编辑
							$("#decid").attr("disabled", false); // 设置为可编辑
							$("#decid").attr("placeholder", "0~4");
							$("#dataid").val("");
							$("#decid").val("");

						} else if ($("#datatypeid").val() == 104) {
							$("#dataid").attr("placeholder", "0~5");
							$("#dataid").attr("disabled", false); // 设置为可编辑
							$("#decid").attr("disabled", false); // 设置为可编辑
							$("#decid").attr("placeholder", "0~5");
							$("#dataid").val("");
							$("#decid").val("");

						} else if ($("#datatypeid").val() == 105) {
							$("#dataid").attr("placeholder", "0~5");
							$("#dataid").attr("disabled", false); // 设置为可编辑
							$("#decid").attr("disabled", false); // 设置为可编辑
							$("#decid").attr("placeholder", "0~5");
							$("#dataid").val("");
							$("#decid").val("");
						} else if ($("#datatypeid").val() == 200) {
							$("#dataid").attr("placeholder", "1~32");
							$("#dataid").attr("disabled", false); // 设置为可编辑
							$("#decid").attr("disabled", true); // 设置为不可编辑
							$("#decid").attr("placeholder", "无小数");
							$("#dataid").val("");
							$("#decid").val("");

						} else if ($("#datatypeid").val() == 201) {
							$("#dataid").attr("placeholder", "1~11");
							$("#dataid").attr("disabled", false); // 设置为可编辑
							$("#decid").attr("disabled", true); // 设置为不可编辑
							$("#decid").attr("placeholder", "无小数");
							$("#dataid").val("");
							$("#decid").val("");
						} else if ($("#datatypeid").val() == 202) {
							$("#dataid").attr("placeholder", "1~8");
							$("#dataid").attr("disabled", false); // 设置为可编辑
							$("#decid").attr("disabled", true); // 设置为不可编辑
							$("#decid").attr("placeholder", "无小数");
							$("#dataid").val("");
							$("#decid").val("");

						} else if ($("#datatypeid").val() == 203) {
							$("#dataid").attr("placeholder", "0~8");
							$("#dataid").attr("disabled", false); // 设置为可编辑
							$("#decid").attr("disabled", false); // 设置为可编辑
							$("#decid").attr("placeholder", "0~8");
							$("#dataid").val("");
							$("#decid").val("");

						} else if ($("#datatypeid").val() == 204) {
							$("#dataid").attr("placeholder", "0~10");
							$("#dataid").attr("disabled", false); // 设置为可编辑
							$("#decid").attr("disabled", false); // 设置为可编辑
							$("#decid").attr("placeholder", "0~10");
							$("#dataid").val("");
							$("#decid").val("");

						} else if ($("#datatypeid").val() == 205) {
							$("#dataid").attr("placeholder", "0~10");
							$("#dataid").attr("disabled", false); // 设置为可编辑
							$("#decid").attr("disabled", false); // 设置为可编辑
							$("#decid").attr("placeholder", "0~10");
							$("#dataid").val("");
							$("#decid").val("");

						} else if ($("#datatypeid").val() == 206) {
							$("#dataid").attr("placeholder", "0~7");
							$("#dataid").attr("disabled", false); // 设置为可编辑
							$("#decid").attr("disabled", false); // 设置为可编辑
							$("#decid").attr("placeholder", "0~7");
							$("#dataid").val("");
							$("#decid").val("");
						} else if ($("#datatypeid").val() == 400) {
							$("#dataid").attr("placeholder", "暂时没用");
							$("#decid").attr("placeholder", "暂时没用");
							$("#dataid").attr("disabled", true); // 设置为可编辑
							$("#decid").attr("disabled", true); // 设置为可编辑
							$("#dataid").val("");
							$("#decid").val("");

						} else if ($("#datatypeid").val() == 401) {
							$("#dataid").attr("placeholder", "暂时没用");
							$("#decid").attr("placeholder", "暂时没用");
							$("#dataid").attr("disabled", true); // 设置为可编辑
							$("#decid").attr("disabled", true); // 设置为可编辑
							$("#dataid").val("");
							$("#decid").val("");

						} else if ($("#datatypeid").val() == 402) {
							$("#dataid").attr("placeholder", "暂时没用");
							$("#decid").attr("placeholder", "暂时没用");
							$("#dataid").attr("disabled", true); // 设置为可编辑
							$("#decid").attr("disabled", true); // 设置为可编辑
							$("#dataid").val("");
							$("#decid").val("");

						} else if ($("#datatypeid").val() == 403) {
							$("#dataid").attr("placeholder", "暂时没用");
							$("#decid").attr("placeholder", "暂时没用");
							$("#dataid").attr("disabled", true); // 设置为可编辑
							$("#decid").attr("disabled", true); // 设置为可编辑
							$("#dataid").val("");
							$("#decid").val("");

						} else if ($("#datatypeid").val() == 404) {
							$("#dataid").attr("placeholder", "暂时没用");
							$("#decid").attr("placeholder", "暂时没用");
							$("#dataid").attr("disabled", true); // 设置为可编辑
							$("#decid").attr("disabled", true); // 设置为可编辑
							$("#dataid").val("");
							$("#decid").val("");

						} else if ($("#datatypeid").val() == 405) {
							$("#dataid").attr("placeholder", "暂时没用");
							$("#decid").attr("placeholder", "暂时没用");
							$("#dataid").attr("disabled", true); // 设置为可编辑
							$("#decid").attr("disabled", true); // 设置为可编辑
							$("#dataid").val("");
							$("#decid").val("");

						} else if ($("#datatypeid").val() == 406) {
							$("#dataid").attr("placeholder", "0~15");
							$("#dataid").attr("disabled", false); // 设置为可编辑
							$("#decid").attr("disabled", false); // 设置为可编辑
							$("#decid").attr("placeholder", "0~15");
							$("#dataid").val("");
							$("#decid").val("");

						} else if ($("#datatypeid").val() == 1000) {
							$("#dataid").attr("placeholder", "无整数");
							$("#decid").attr("placeholder", "无小数");
							$("#dataid").attr("disabled", true); // 设置为可编辑
							$("#decid").attr("disabled", true); // 设置为可编辑
							$("#dataid").val("");
							$("#decid").val("");
						}

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
						$("#dataid").val("");
						$("#decid").val("");
						$scope.showAllPlcConf(0);

					}
					// 保存添加/修改监控点
					$scope.saveupmonitor = function() {
						var num;
						var dec;
						var digit_counts = [];
						var rangdata, child_rangdata;
						var rangs = [];
						var scaliedata, child_scaliedata;
						var scalies = [];
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
						var display = $('#registeraddr').css('display');
						if (display == 'block') {
							var rang, reg;
							if ($("#scaleid").text() == "八进制") {
								rang = $("#rangid").text().split(" ");
								reg = /^[0-7]*$/;
								if (!reg.test($("#addrid").val())) {
									alert("寄存器地址主编号格式错误");
									return;
								}
								if (parseInt($("#addrid").val(), 8) < parseInt(rang[0])
										|| parseInt($("#addrid").val(), 8) > parseInt(rang[1])) {
									alert("寄存器地址主编号范围有误");
									return;
								}

							} else if ($("#scaleid").text() == "十进制") {
								rang = $("#rangid").text().split(" ");
								reg = /^0|[1-9]\d*$/;
								if (!reg.test($("#addrid").val())) {
									alert("寄存器地址主编号格式错误");
									return;
								}
								if ($("#addrid").val() < parseInt(rang[0])
										|| $("#addrid").val() > parseInt(rang[1])) {
									alert("寄存器地址主编号范围有误");
									return;
								}

							} else if ($("#scaleid").text() == "十六进制") {
								rang = $("#rangid").text().split(" ");
								reg = /^[0-9a-fA-F]*$/;
								if (!reg.test($("#addrid").val())) {
									alert("寄存器地址主编号格式错误");
									return;
								}
								if (parseInt($("#addrid").val(), 16) < parseInt(rang[0])
										|| parseInt($("#addrid").val(), 16) > parseInt(rang[1])) {
									alert("寄存器地址主编号范围有误");
									return;

								}
							}
							scaliedata = $("#scaleid").text();
							scalies.push(scaliedata);
							rangdata = $("#addrid").val();
							rangs.push(rangdata);

						} else {
							$("#rangid").html("");
							$("#scaleid").html("");
						}
						var display = $('#child_registeraddr').css('display');
						if (display == 'block') {
							var child_rang, child_reg;
							if ($("#child_scaleid").text() == "八进制") {

								child_rang = $("#child_rangid").text().split(
										" ");
								child_reg = /^[0-7]*$/;
								if (!child_reg.test($("#child_addrid").val())) {
									alert("寄存器地址子编号格式错误");
									return;
								}
								if (parseInt($("#child_addrid").val(), 8) < parseInt(child_rang[0])
										|| parseInt($("#child_addrid").val(), 8) > parseInt(child_rang[1])) {
									alert("寄存器地址子编号范围有误");
									return;
								}
							} else if ($("#child_scaleid").text() == "十进制") {
								child_rang = $("#child_rangid").text().split(
										" ");
								child_reg = /^0|[1-9]\d*$/;
								if (!child_reg.test($("#child_addrid").val())) {
									alert("寄存器地址子编号格式错误");
									return;
								}
								if ($("#child_addrid").val() < parseInt(child_rang[0])
										|| $("#child_addrid").val() > parseInt(child_rang[1])) {
									alert("寄存器地址子编号范围有误");
									return;
								}
							} else if ($("#child_scaleid").text() == "十六进制") {
								child_rang = $("#child_rangid").text().split(
										" ");
								child_reg = /^[0-9a-fA-F]*$/;
								if (!child_reg.test($("#child_addrid").val())) {
									alert("寄存器地址子编号格式错误");
									return;
								}
								if (parseInt($("#child_addrid").val(), 16) < parseInt(child_rang[0])
										|| parseInt($("#child_addrid").val(),
												16) > parseInt(child_rang[1])) {
									alert("寄存器地址子编号范围有误");
									return;
								}
							}
							child_scaliedata = $("#child_scaleid").text();
							scalies.push(child_scaliedata);
							child_rangdata = $("#child_addrid").val();
							rangs.push(child_rangdata);
						} else {

							$("#child_rangid").html("");
							$("#child_scaleid").html("");

						}
						if ($("#addrtypeid").val() != 0) {
							var regnum = /^0|[1-9]\d*$/;
							var datadisabled = $("#dataid").prop("disabled");
							alert(datadisabled);
							console.log(datadisabled);
							if (datadisabled == 'false') {
								alert(datadisabled);
								if (!regnum.test($("#dataid").val())) {
									alert("整数位数格式错误");
									return;
								}

							}
							console.log(datadisabled);
							var decdisabled = $("#decid").prop("disabled");
							if (decdisabled == 'false') {
								alert(decdisabled);
								if (!regnum.test($("#dataid").val())) {
									alert("小数位数格式错误");
									return;
								}
							}
							if ($("#datatypeid").val() == 100) {
								if ($("#dataid").val().length < 1
										|| $("#dataid").val().length > 16) {
									alert("整数范围有误");
									return;
								}

							} else if ($("#datatypeid").val() == 101) {
								if ($("#dataid").val().length < 1
										|| $("#dataid").val().length > 6) {
									alert("整数范围有误");
									return;
								}

							} else if ($("#datatypeid").val() == 102) {
								if ($("#dataid").val().length < 1
										|| $("#dataid").val().length > 4) {
									alert("整数范围有误");
									return;
								}

							} else if ($("#datatypeid").val() == 103) {
								if ($("#dataid").val().length < 0
										|| $("#dataid").val().length > 4) {
									alert("整数范围有误");
									return;
								}
								if ($("#decid").val().length < 0
										|| $("#decid").val().length > 4) {
									alert("小数数范围有误");
									return;
								}
								var totle = parseInt($("#dataid").val().length)
										+ parseInt($("#decid").val().length);
								if (totle < 1 || totle > 4) {
									alert("整数位数+小数位数必须大于1小于4");
									return;

								}
							} else if ($("#datatypeid").val() == 104
									|| $("#datatypeid").val() == 105) {
								if ($("#dataid").val().length < 0
										|| $("#dataid").val().length > 5) {
									alert("整数范围有误");
									return;
								}
								if ($("#decid").val().length < 0
										|| $("#decid").val().length > 5) {
									alert("小数数范围有误");
									return;
								}
								var totle = parseInt($("#dataid").val().length)
										+ parseInt($("#decid").val().length);
								if (totle < 1 || totle > 5) {
									alert("整数位数+小数位数必须大于1小于5");
									return;

								}
							} else if ($("#datatypeid").val() == 200) {
								if ($("#dataid").val().length < 1
										|| $("#dataid").val().length > 32) {
									alert("整数范围有误");
									return;
								}

							} else if ($("#datatypeid").val() == 201) {
								if ($("#dataid").val().length < 1
										|| $("#dataid").val().length > 11) {
									alert("整数范围有误");
									return;
								}

							} else if ($("#datatypeid").val() == 202) {
								if ($("#dataid").val().length < 1
										|| $("#dataid").val().length > 8) {
									alert("整数范围有误");
									return;
								}

							} else if ($("#datatypeid").val() == 203) {
								if ($("#dataid").val().length < 0
										|| $("#dataid").val().length > 8) {
									alert("整数范围有误");
									return;
								}
								if ($("#decid").val().length < 0
										|| $("#decid").val().length > 8) {
									alert("小数数范围有误");
									return;
								}
								var totle = parseInt($("#dataid").val().length)
										+ parseInt($("#decid").val().length);
								if (totle < 1 || totle > 8) {
									alert("整数位数+小数位数必须大于1小于8");
									return;

								}

							} else if ($("#datatypeid").val() == 204
									|| $("#datatypeid").val() == 205) {
								if ($("#dataid").val().length < 0
										|| $("#dataid").val().length > 10) {
									alert("整数范围有误");
									return;
								}
								if ($("#decid").val().length < 0
										|| $("#decid").val().length > 10) {
									alert("小数数范围有误");
									return;
								}
								var totle = parseInt($("#dataid").val().length)
										+ parseInt($("#decid").val().length);
								if (totle < 1 || totle > 10) {
									alert("整数位数+小数位数必须大于1小于10");
									return;

								} else if ($("#datatypeid").val() == 206) {
									if ($("#dataid").val().length < 0
											|| $("#dataid").val().length > 7) {
										alert("整数范围有误");
										return;
									}
									if ($("#decid").val().length < 0
											|| $("#decid").val().length > 7) {
										alert("小数数范围有误");
										return;
									}
									var totle = parseInt($("#dataid").val().length)
											+ parseInt($("#decid").val().length);
									if (totle < 1 || totle > 7) {
										alert("整数位数+小数位数必须大于1小于7");
										return;

									}

								} else if ($("#datatypeid").val() == 400
										|| $("#datatypeid").val() == 401
										|| $("#datatypeid").val() == 402
										|| $("#datatypeid").val() == 403
										|| $("#datatypeid").val() == 404
										|| $("#datatypeid").val() == 405
										|| $("#datatypeid").val() == 1000) {
									$("#dataid").val("");
									$("#decid").val("");

								} else if ($("#datatypeid").val() == 406) {
									if ($("#dataid").val().length < 0
											|| $("#dataid").val().length > 15) {
										alert("整数范围有误");
										return;
									}
									if ($("#decid").val().length < 0
											|| $("#decid").val().length > 15) {
										alert("小数数范围有误");
										return;
									}
									var totle = parseInt($("#dataid").val().length)
											+ parseInt($("#decid").val().length);
									if (totle < 1 || totle > 15) {
										alert("整数位数+小数位数必须大于1小于15");
										return;

									}

								}

							}
						} else {
							$('#divdatatypeid').css('display', 'none');
							$('#datadigitid').css('display', 'none');

							$("#dataid").val("");
							$("#decid").val("");

						}
						if ($("#dataid").val() != "" || $("#decid").val() != "") {
							num = $("#dataid").val();
							dec = $("#decid").val();
							digit_counts.push(num);
							digit_counts.push(dec);
							var digs = digit_counts.join(",");
						}
						var rang_datas = rangs.join(",");
						var scalie_datas = scalies.join(",");
						var params = {
							id : mid,
							plc_id : plcId,
							device_id : $scope.deviceid,
							name : $("#nameid").val(),
							data_id : $("#datatypeid").val(),
							addr_type : $("#addrtypeid").val(),
							addr : rang_datas,
							digit_binary : scalie_datas,
							rid : $("#registerid").val(),
							rang : $("#rangid").val(),
							describe : $("#describeid").val(),
							digit_count : digs,
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