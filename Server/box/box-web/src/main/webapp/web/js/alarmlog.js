var appModule = angular.module('weconweb', []);
appModule
		.controller(
				"infoController",
				function($scope, $http, $compile) {
					$scope.onInit = function() {
						$scope.deviceid = T.common.util
								.getParameter("device_id");
						$scope.devicename = T.common.util
								.getParameter("device_name");
						$scope.getDataType();
						$scope.paginationConf_current = {
							currentPage : 1,
							itemsPerPage : 10,
							totalItems : $scope.count,
							pagesLength : 15,
							perPageOptions : [ 5, 10, 20, 50, 100 ],
							rememberPerPage : 'perPageItems',
							onChange : function() {
								if (this.currentPage != 0) {

									$scope.alarm_submit(this.currentPage,
											this.itemsPerPage);

								}
							}
						}
						$scope.paginationConf_history = {
							currentPage : 1,
							itemsPerPage : 10,
							totalItems : $scope.count,
							pagesLength : 15,
							perPageOptions : [ 5, 10, 20, 50, 100 ],
							rememberPerPage : 'perPageItems',
							onChange : function() {
								if (this.currentPage != 0) {

									$scope.hisalarm_submit(this.currentPage,
											this.itemsPerPage);

								}
							}
						}

						$('.form_datetime').datetimepicker({
							weekStart : 1,
							todayBtn : 1,
							autoclose : 1,
							todayHighlight : 1,
							startView : 2,
							forceParse : 0,
							showMeridian : 1,
							pickerPosition : "bottom-left"
						}).on('changeDate', function(ev) { // 当日期被改变时触发
							// alert("当前日期是：" + ev.date.valueOf());
						});

					}

					$scope.paginationConf_current = {
						totalItems : $scope.count,
					}
					$scope.paginationConf_history = {
						totalItems : $scope.count,
					}
					$scope.paginationConf_alarmcfg = {
						totalItems : $scope.count,
					}

					/**
					 * 提交当前接口请求
					 */
					$scope.alarm_submit = function(pageIndex, pageSize) {
						$("#loadingModal").modal("show");
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
										"alarmDataAction/getNowAlarmData",
										params,
										function(data, code, msg) {
											if (code == 200) {
												$scope.alarmDatas = data.alarmData.list;
												$scope.accounttype = data.type;
												$scope.paginationConf_current.totalItems = data.alarmData.totalRecord;
												$scope.$apply();

												$("#loadingModal")
														.modal("hide");

											} else {
												$("#loadingModal")
														.modal("hide");
												alert(code + "-" + msg);
											}
										}, function() {
											$("#loadingModal").modal("hide");
											alert("ajax error");
										});
					}
					/**
					 * 提交历史报警接口请求
					 */
					$scope.hisalarm_submit = function(pageIndex, pageSize) {
						$("#loadingModal").modal("show");
						if (pageIndex == 0)
							pageIndex = 1;
						var params = {
							alarm_cfg_id : $("#alarmcfgid").val(),
							name : $("#alarmcfgname").val(),
							start_date : $("#startdateid").val(),
							end_date : $("#enddateid").val(),
							device_id : $scope.deviceid,
							pageIndex : pageIndex,
							pageSize : pageSize
						};

						T.common.ajax
								.request(
										"WeconBox",
										"alarmDataAction/getHisAlarmData",
										params,
										function(data, code, msg) {
											if (code == 200) {
												$scope.alarmHisDatas = data.alarmHisData.list;
												$scope.paginationConf_history.totalItems = data.alarmHisData.totalRecord;
												$scope.$apply();
												$("#loadingModal")
														.modal("hide");

											} else {
												$("#loadingModal")
														.modal("hide");
												alert(code + "-" + msg);
											}
										}, function() {
											$("#loadingModal").modal("hide");
											alert("ajax error");
										});
					}

					$("#selectWith").change(function() {

						var value = $('#selectWith').val();
						if (value == "0") {
							$('#secondCondition').css('display', 'none');
						} else {
							$('#secondCondition').css('display', 'block');
						}

					});

					/**
					 * 获取报警配置列表
					 */
					$scope.alarm_group = function() {

						var params = {
							device_id : $scope.deviceid
						};
						T.common.ajax
								.request(
										"WeconBox",
										"alarmDataAction/getAlarmGroup",
										params,
										function(data, code, msg) {
											if (code == 200) {
												$scope.dir_list = data.alarmGroup;
												$scope.accounttype = data.type;
												$scope.$apply();
												$scope.paginationConf_alarmcfg = {
													currentPage : 1,
													itemsPerPage : 10,
													totalItems : $scope.count,
													pagesLength : 15,
													perPageOptions : [ 5, 10,
															20, 50, 100 ],
													rememberPerPage : 'perPageItems',
													onChange : function() {
														if (this.currentPage != 0) {

															$scope
																	.showAlarmCfg(
																			this.currentPage,
																			this.itemsPerPage);

														}
													}
												}
												/*
												 * $("#dataGroupSelect").val(
												 * data.alarmGroup[0].id);
												 */
												$scope
														.showAlarmCfg(
																$scope.paginationConf_alarmcfg.currentPage,
																$scope.paginationConf_alarmcfg.itemsPerPage);

											} else {

												alert(code + "-" + msg);
											}
										}, function() {
											alert("ajax error");
										});
					}

					// 创建分组
					$scope.add_group = function() {
						if ($('#newGroupName').val() == "") {
							alert("名称不能为空");
							return;
						}
						var params = {
							id : -1,
							name : $('#newGroupName').val(),
							type : 3,
							device_id : $scope.deviceid
						};

						T.common.ajax.request("WeconBox",
								"userdiract/saveuserdir", params, function(
										data, code, msg) {
									if (code == 200) {
										$("#addGroup").modal("hide");
										$scope.alarm_group();
										alert("添加成功")

									} else {

										alert(code + "-" + msg);
									}
								}, function() {
									alert("ajax error");
								});
					}

					// 获取修改分组名称
					$scope.editGroup = function(model) {

						$("#editid").val(model.name);
						$scope.alarmgroupId = model.id;

						$("#editGroup").modal("show");

					}
					// 编辑分组
					$scope.edit_group = function() {
						if ($('#editid').val() == "") {
							alert("名称不能为空");
							return;
						}
						var params = {
							id : $scope.alarmgroupId,
							name : $('#editid').val(),
							type : 3,
							device_id : $scope.deviceid
						};

						T.common.ajax.request("WeconBox",
								"userdiract/saveuserdir", params, function(
										data, code, msg) {
									if (code == 200) {
										$("#editGroup").modal("hide");
										$scope.alarm_group();
										alert("修改成功")

									} else {

										alert(code + "-" + msg);
									}
								}, function() {
									alert("ajax error");
								});
					}
					// 获取删除分组名称
					$scope.delGroup = function(model) {
						var text = "确定删除【" + model.name + "】分组?"
						$("#delid").html(text);
						$scope.delAlarmGroupId = model.id;

						$("#deleteGroup").modal("show");

					}
					// 删除分组
					$scope.del_group = function() {
						var params = {
							id : $scope.delAlarmGroupId,
						};

						T.common.ajax.request("WeconBox",
								"userdiract/deluserdir", params, function(data,
										code, msg) {
									if (code == 200) {

										$("#deleteGroup").modal("hide");
										$scope.alarm_group();
										alert("修改成功")

									} else {

										alert(code + "-" + msg);
									}
								}, function() {
									alert("ajax error");
								});
					}

					/*
					 * 盒子下plc配置展示
					 */
					var mtype = 0;
					$scope.showAllPlcConf = function(dealtype) {
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

													if (data.infoDatas != "") {
														$scope
																.condevice(data.infoDatas[0].plcId);
														$("#nameid ").val("");
														$("#addrid").val("");
														$("#alarmtextid").val(
																"");

													}
												} else {
													$scope.showtype = 1;
													$("#nameid ").val(
															alarmfo.name);
													$("#conid ").val(
															alarmfo.plc_id);
													$scope
															.condevice(alarmfo.plc_id);

													$("#datatypeid").val(
															alarmfo.data_id);
													$("#addrid").val(
															alarmfo.addr);
													$("#alarmtextid").val(
															alarmfo.text);
													$('#selectgroup').val(
															alarmfo.dirId);

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
													if (data.allAddr[0].addrkey == 0) {
														$('#firstCondition')
																.css('display',
																		'none');
														$('#bitsetting').css(
																'display',
																'block');
														$('#secondCondition')
																.css('display',
																		'none');

														$('#selectWith').val(0);
													} else {

														$('#firstCondition')
																.css('display',
																		'block');
														$('#bitsetting').css(
																'display',
																'none');
														$('#secondCondition')
																.css('display',
																		'none');
														$('#selectWith').val(0);
													}

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
																		alarmfo.addr_type);
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
																alarmfo.rid);
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
															if ($("#addrtypeid")
																	.val() == 0) {

																$(
																		'#firstCondition')
																		.css(
																				'display',
																				'none');
																$('#bitsetting')
																		.css(
																				'display',
																				'block');
																$(
																		'#secondCondition')
																		.css(
																				'display',
																				'none');

																$('#selectWith')
																		.val(0);
																if (alarmfo.triggerValue == "ON") {
																	$(
																			"input[name='bitsetting'][value=6]")
																			.attr(
																					"checked",
																					true);
																	$(
																			"input[name='bitsetting'][value=7]")
																			.attr(
																					"checked",
																					false);
																} else {
																	$(
																			"input[name='bitsetting'][value=7]")
																			.attr(
																					"checked",
																					true);
																	$(
																			"input[name='bitsetting'][value=6]")
																			.attr(
																					"checked",
																					false);
																}

															} else {
																$(
																		'#firstCondition')
																		.css(
																				'display',
																				'block');
																if (alarmfo.condition_type == 0) {
																	$(
																			'#bitsetting')
																			.css(
																					'display',
																					'none');
																	$(
																			'#secondCondition')
																			.css(
																					'display',
																					'none');

																	$(
																			'#selectWith')
																			.val(
																					0);
																	$(
																			'#conditiononeid')
																			.val(
																					alarmfo.listAlarmTrigger[0].type);
																	$(
																			'#onenumid')
																			.val(
																					alarmfo.listAlarmTrigger[0].value);

																} else if (alarmfo.condition_type == 1) {

																	$(
																			'#bitsetting')
																			.css(
																					'display',
																					'none');
																	$(
																			'#secondCondition')
																			.css(
																					'display',
																					'block');

																	$(
																			'#selectWith')
																			.val(
																					1);
																	$(
																			'#conditiononeid')
																			.val(
																					alarmfo.listAlarmTrigger[0].type);
																	$(
																			'#onenumid')
																			.val(
																					alarmfo.listAlarmTrigger[0].value);
																	$(
																			'#conditiontwoid')
																			.val(
																					alarmfo.listAlarmTrigger[1].type);
																	$(
																			'#twonumid')
																			.val(
																					alarmfo.listAlarmTrigger[1].value);

																} else if (alarmfo.condition_type == 2) {
																	$(
																			'#bitsetting')
																			.css(
																					'display',
																					'none');
																	$(
																			'#secondCondition')
																			.css(
																					'display',
																					'block');

																	$(
																			'#selectWith')
																			.val(
																					2);
																	$(
																			'#conditiononeid')
																			.val(
																					alarmfo.listAlarmTrigger[0].type);
																	$(
																			'#onenumid')
																			.val(
																					alarmfo.listAlarmTrigger[0].value);
																	$(
																			'#conditiontwoid')
																			.val(
																					alarmfo.listAlarmTrigger[1].type);
																	$(
																			'#twonumid')
																			.val(
																					alarmfo.listAlarmTrigger[1].value);

																}

															}

															$("#rangid")
																	.val(
																			alarmfo.data_limit);

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
						if ($("#addrtypeid").val() == 0) {
							$('#firstCondition').css('display', 'none');
							$('#bitsetting').css('display', 'block');
							$('#secondCondition').css('display', 'none');
							$('#selectWith').val(0);
						} else {

							$('#firstCondition').css('display', 'block');
							$('#bitsetting').css('display', 'none');
							$('#secondCondition').css('display', 'none');
							$('#selectWith').val(0);
						}

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
					$("#dataGroupSelect")
							.change(
									function() {
										$scope
												.showAlarmCfg(
														$scope.paginationConf_alarmcfg.currentPage,
														$scope.paginationConf_alarmcfg.itemsPerPage);

									});

					/**
					 * 更加组ID获取报警配置
					 */
					$scope.showAlarmCfg = function(pageIndex, pageSize) {
						if (pageIndex == 0)
							pageIndex = 1;
						var params = {
							group_id : $("#dataGroupSelect").val(),
							pageIndex : pageIndex,
							pageSize : pageSize

						};

						T.common.ajax
								.request(
										"WeconBox",
										"alarmDataAction/getAlarmCfg",
										params,
										function(data, code, msg) {
											if (code == 200) {
												$scope.paginationConf_alarmcfg.totalItems = data.listalrmCfgTrigger.totalRecord;
												$scope.listalrmCfgTrigger = data.listalrmCfgTrigger.list;
												$scope.$apply();

											} else {
												alert(code + "-" + msg);
											}
										}, function() {
											alert("ajax error");
										});

					}

					var alarmid = -1;
					var alarmfo = null;
					// 获取修改报警配置信息
					$scope.editmonitor = function(model) {
						alarmfo = model;
						alarmid = model.alarmcfg_id;
						$scope.showAllPlcConf(1);

					}
					$scope.addmonitor = function() {

						alarmid = -1;
						$scope.showAllPlcConf(0);

					}
					// 获取删除监控点信息
					$scope.delmonitor = function(model) {
						alarmid = model.alarmcfg_id;
						$("#delid").html("确定要删除【" + model.name + "】报警条目吗？");
					}
					// 删除报警条目
					$scope.del_alarmcfg = function() {

						var params = {
							alarmcfg_id : alarmid
						};

						T.common.ajax
								.request(
										"WeconBox",
										"alarmDataAction/delAlrmCfg",
										params,
										function(data, code, msg) {
											if (code == 200) {
												$("#deleteAlarmid").modal(
														"hide");
												$scope
														.showAlarmCfg(
																$scope.paginationConf_alarmcfg.currentPage,
																$scope.paginationConf_alarmcfg.itemsPerPage);
												alert("删除成功！");

											} else {

												alert(code + "-" + msg);
											}
										}, function() {
											alert("ajax error");
										});
					}
					/**
					 * 添加或者更新报警配置
					 */
					$scope.addupAlarm = function() {

						if ($("#nameid").val() == ""
								|| $("#addrid").val() == ""
								|| $("#alarmtextid").val() == "") {
							alert("参数未配置完整！");
							return;
						}
						var types = [];
						var values = [];
						if ($("#addrtypeid").val() == "0") {
							types.push($("input[name='bitsetting']:checked")
									.val());
							if ($("input[name='bitsetting']:checked").val() == 6) {
								values.push("ON");
							} else {
								values.push("OFF");
							}
						} else {
							types.push($("#conditiononeid").val());
							if ($("#onenumid").val() == "") {
								alert("触发值不能为空");
								return;
							}
							values.push($("#onenumid").val());
							if ($("#selectWith").val() != 0) {
								if ($("#twonumid").val() == "") {
									alert("触发值不能为空");
									return;
								}
								types.push($("#conditiontwoid").val());
								values.push($("#twonumid").val());
							}

						}
						var alarmtypes = types.join(",");
						var alarmvalues = values.join(",");

						var params = {
							alarmcfg_id : alarmid,
							plc_id : plcId,
							device_id : $scope.deviceid,
							name : $("#nameid").val(),
							data_id : $("#datatypeid").val(),
							addr_type : $("#addrtypeid").val(),
							addr : $("#addrid").val(),
							rid : $("#registerid").val(),
							rang : $("#rangid").val(),
							group_id : $("#selectgroup").val(),
							text : $("#alarmtextid").val(),
							condition_type : $("#selectWith").val(),
							rang : $("#rangid").val(),
							type : alarmtypes,
							value : alarmvalues

						};

						T.common.ajax
								.request(
										"WeconBox",
										"alarmDataAction/addUpdataAlarmMonitor",
										params,
										function(data, code, msg) {
											if (code == 200) {
												$("#addAlarmRecord").modal(
														"hide");
												$("#dataGroupSelect")
														.val(
																$(
																		"#selectgroup")
																		.val());

												$scope
														.showAlarmCfg(
																$scope.paginationConf_alarmcfg.currentPage,
																$scope.paginationConf_alarmcfg.itemsPerPage);
												if (mtype == 0) {
													alert("添加报警配置成功");
												} else {
													alert("修改报警配置成功");
												}

											} else {
												alert(code + "-" + msg);
											}
										}, function() {
											alert("ajax error");
										});

					}

				})