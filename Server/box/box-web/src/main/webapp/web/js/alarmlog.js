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
					$scope.displaySecond = function() {
						var value = $('#selectWith').val();
						if (value == "none") {
							$('#secondCondition').css('display', 'none');
						} else {
							$('#secondCondition').css('display', 'block');
						}
					}
					/**
					 * 获取报警配置列表
					 */
					$scope.alarm_group = function() {

						var params = {
							device_id : $scope.deviceid
						};
						T.common.ajax.request("WeconBox",
								"alarmDataAction/getAlarmGroup", params,
								function(data, code, msg) {
									if (code == 200) {
										$scope.dir_list = data.alarmGroup;
										$scope.accounttype = data.type;
										$scope.$apply();

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
													$("#rangid")
															.val(
																	data.allAddr[0].addrRid[0].range);
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

															$("#registerid")
																	.val(
																			$scope.addrvalues[0].addrvalue);

															$("#rangid")
																	.val(
																			$scope.addrvalues[0].range);

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

				})