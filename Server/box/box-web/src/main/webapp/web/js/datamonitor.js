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
						$scope.type = 0;
						$scope.getDataType();
						$scope.act_group($scope.deviceid);

						$scope.paginationConf = {
							currentPage : 1,
							itemsPerPage : 10,
							totalItems : $scope.count,
							pagesLength : 15,
							perPageOptions : [ 5, 10, 20, 50, 100 ],
							rememberPerPage : 'perPageItems'
							/*onChange : function() {
								if (this.currentPage != 0) {
									$scope.ws_send(this.currentPage,
											this.itemsPerPage, actgroupId);
								}
							}*/
						}

						// 打开模态框
						function showAddGroup() {
							$('#identifier').modal('show');
						}
					}

					$scope.act_group = function(deviceid) {

						var params = {
							device_id : deviceid
						};
						T.common.ajax
								.request(
										"WeconBox",
										"actDataAction/getActGroup",
										params,
										function(data, code, msg) {
											if (code == 200) {
												$scope.dir_list = data.ActGroup;
												$scope.accounttype = data.type;
												$scope.$apply();
												if (data.ActGroup != null
														&& $scope.type == 0) {
													var fristGroupId = data.ActGroup[0].id;
													$scope
															.ws_connect(fristGroupId);
													/*
													 * $scope .act_submit(
													 * $scope.paginationConf.currentPage,
													 * $scope.paginationConf.itemsPerPage,
													 * fristGroupId);
													 */
												}

											} else {

												alert(code + "-" + msg);
											}
										}, function() {
											alert("ajax error");
										});
					}

					$scope.paginationConf = {
						totalItems : $scope.count,
					}

					var t;
					var actgroupId;

					/**
					 * 提交接口请求
					 */
					/*
					 * $scope.act_submit = function(pageIndex, pageSize,
					 * groupId) { actgroupId = groupId; if (pageIndex == 0)
					 * pageIndex = 1; var params = { device_id :
					 * $scope.deviceid, acc_dir_id : groupId, pageIndex :
					 * pageIndex, pageSize : pageSize }; T.common.ajax .request(
					 * "WeconBox", "actDataAction/getActData", params,
					 * function(data, code, msg) { if (code == 200) {
					 * $scope.paginationConf.totalItems =
					 * data.piBoxActDateMode.totalRecord; $scope.actDatas =
					 * data.piBoxActDateMode.list; $scope.$apply(); angular
					 * .forEach( $scope.actDatas, function(data, index, array) {
					 * 
					 * console .log("初始化==" + data.id);
					 * 
					 * $scope .editable_name(data); $scope
					 * .editable_value(data); });
					 * 
					 * t = setTimeout( function() { $scope .act_submit(
					 * $scope.paginationConf.currentPage,
					 * $scope.paginationConf.itemsPerPage, actgroupId) }, 3000); }
					 * else {
					 * 
					 * alert(code + "-" + msg); } }, function() { alert("ajax
					 * error"); }); }
					 */

					/*
					 * $scope.mc_change = function() { clearTimeout(t); }
					 */
					/**
					 * webscoket发送数据
					 */
					var ws;
					$scope.ws_connect = function(fristGroupId) {
						if ("WebSocket" in window) {
							ws = new WebSocket(
									T.common.requestUrl['WeconBoxWs']
											+ '/actdataweb-websocket/websocket?'
											+ T.common.websocket.getParams());
							ws.onopen = function() {

								$scope.ws_send(
										$scope.paginationConf.currentPage,
										$scope.paginationConf.itemsPerPage,
										fristGroupId);
							};
							ws.onmessage = function(evt) {
								$scope.paginationConf.totalItems = JSON
										.parse(evt.data).piBoxActDateMode.totalRecord;
								$scope.actDatas = JSON.parse(evt.data).piBoxActDateMode.list;
								$scope.$apply();
								angular.forEach($scope.actDatas, function(data,
										index, array) {
									$scope.editable_name(data);
									$scope.editable_value(data);
								});
							};
							ws.onclose = function(evt) {
								console.log(evt);
							};
							ws.onerror = function(evt) {
								console.log(evt);
							};
						} else {
							alert("WebSocket isn't supported by your Browser!");
						}
					}
					$scope.ws_send = function(pageIndex, pageSize, groupId) {
						actgroupId = groupId;
						if (pageIndex == 0)
							pageIndex = 1;
						var params = {
							device_id : $scope.deviceid,
							acc_dir_id : groupId,
							pageIndex : pageIndex,
							pageSize : pageSize

						};

						ws.send(angular.toJson(params));
					}
					$scope.ws_close = function() {
						ws.close();
					}

					// 复制监控点
					$scope.copymonitor = function(model) {
						$scope.monitorid = model.id;// 监控点id
						$scope.alais = model.ref_alais;// 监控点别名

						angular.forEach($scope.dir_list, function(data, index,
								array) {
							if (actgroupId == data.id) {
								$("#nowgroupid").html(data.name);
								$scope.groupName = data.name;
							}
						});

					}
					// 复制监控点到其他组
					$scope.copy_monitor_group = function() {
						if ($('#copymonitorid').val() == actgroupId) {
							alert("【" + $scope.groupName + "】已经存在该监控点！");
							return;

						}
						var params = {
							monitorid : $scope.monitorid,
							alais : $scope.alais,
							acc_dir_id : $('#copymonitorid').val()
						};

						T.common.ajax
								.request(
										"WeconBox",
										"actDataAction/copyMonitor",
										params,
										function(data, code, msg) {
											if (code == 200) {
												$("#copyDataGroup").modal(
														"hide");
												$scope
														.ws_send(
																$scope.paginationConf.currentPage,
																$scope.paginationConf.itemsPerPage,
																actgroupId);
												alert("复制成功！");

											} else {

												alert(code + "-" + msg);
											}
										}, function() {
											alert("ajax error");
										});
					}
					// 移动监控点
					$scope.movemonitor = function(model) {
						$scope.moveMonitorid = model.id;// 监控点id
						$scope.moveAlais = model.ref_alais;// 监控点别名

						angular.forEach($scope.dir_list, function(data, index,
								array) {
							if (actgroupId == data.id) {
								$("#movenowgroupid").html(data.name);
								$scope.movegroupName = data.name;
							}
						});

					}
					// 移动监控点到其他组
					$scope.move_monitor_group = function() {
						if ($('#movemonitorid').val() == actgroupId) {
							alert("【" + $scope.movegroupName + "】已经存在该监控点！");
							return;

						}
						var params = {
							monitorid : $scope.moveMonitorid,
							alais : $scope.moveAlais,
							to_acc_dir_id : $('#movemonitorid').val(),
							from_acc_dir_id : actgroupId
						};

						T.common.ajax
								.request(
										"WeconBox",
										"actDataAction/moveMonitor",
										params,
										function(data, code, msg) {
											if (code == 200) {
												$("#moveDataGroup").modal(
														"hide");
												$scope
														.ws_send(
																$scope.paginationConf.currentPage,
																$scope.paginationConf.itemsPerPage,
																actgroupId);
												alert("移动成功！");

											} else {

												alert(code + "-" + msg);
											}
										}, function() {
											alert("ajax error");
										});
					}

					// 获取移除监控点信息
					$scope.remonitor = function(model) {
						$scope.delmonitorid = model.id;// 监控点id
						$scope.isdelmonitor = 1; // 1.移除监控点 2.删除监控点配置
						$("#delgroupid").html(
								"确定要从该分组移除【" + model.ref_alais + "】监控点吗？");
					}
					// 获取删除监控点信息
					$scope.delmonitor = function(model) {
						$scope.delmonitorid = model.id;// 监控点id
						$scope.isdelmonitor = 2; // 1.移除监控点 2.删除监控点配置
						$("#delgroupid").html(
								"确定要删除【" + model.ref_alais + "】监控点配置吗？");
					}
					// 移除监控点
					$scope.del_monitor_group = function() {

						var params = {
							monitorid : $scope.delmonitorid,
							acc_dir_id : actgroupId,
							isdel : $scope.isdelmonitor
						};

						T.common.ajax
								.request(
										"WeconBox",
										"actDataAction/delMonitor",
										params,
										function(data, code, msg) {
											if (code == 200) {
												$("#deletePoint").modal("hide");
												$scope
														.ws_send(
																$scope.paginationConf.currentPage,
																$scope.paginationConf.itemsPerPage,
																actgroupId);
												if ($scope.isdelmonitor == 1) {
													alert("移除成功！");

												} else {
													alert("删除成功！");
												}

											} else {

												alert(code + "-" + msg);
											}
										}, function() {
											alert("ajax error");
										});
					}
					// 创建分组
					$scope.add_group = function() {
						var params = {
							id : -1,
							name : $('#newGroupName').val(),
							type : 1,
							device_id : $scope.deviceid
						};

						T.common.ajax
								.request(
										"WeconBox",
										"userdiract/saveuserdir",
										params,
										function(data, code, msg) {
											if (code == 200) {

												var name = $('#newGroupName')
														.val();
												var length = $('#monitorTab')
														.children().length;
												var lastPosition = length - 2;
												($("#monitorTab li:eq("
														+ lastPosition + ")"))
														.after("<li><a href=\"#data-item-1\" data-toggle=\"tab\">"
																+ name
																+ "</a></li>");

												$("#addDataGroup")
														.modal("hide");
												$scope.type = 1;

												$scope
														.act_group($scope.deviceid);

											} else {

												alert(code + "-" + msg);
											}
										}, function() {
											alert("ajax error");
										});
					}
					$scope.showAddGroup = function() {
						$('#identifier').modal('show');
					}
					// 获取修改分组名称
					$scope.editGroup = function(model) {

						$("#editid").val(model.name);
						$scope.editGroupId = model.id;

						$("#editGroupName").modal("show");

					}
					// 修改分组
					$scope.edit_group = function() {
						var params = {
							id : $scope.editGroupId,
							name : $('#editid').val(),
							type : 1,
							device_id : $scope.deviceid
						};

						T.common.ajax.request("WeconBox",
								"userdiract/saveuserdir", params, function(
										data, code, msg) {
									if (code == 200) {

										$("#editGroupName").modal("hide");
										$scope.type = 1;

										$scope.act_group($scope.deviceid);

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
						$scope.delActGroupId = model.id;

						$("#deleteGroup").modal("show");

					}
					// 删除分组
					$scope.del_group = function() {
						var params = {
							id : $scope.delActGroupId,
						};

						T.common.ajax.request("WeconBox",
								"userdiract/deluserdir", params, function(data,
										code, msg) {
									if (code == 200) {

										$("#deleteGroup").modal("hide");
										$scope.type = 1;

										$scope.act_group($scope.deviceid);
										// $scope.mc_change();

									} else {

										alert(code + "-" + msg);
									}
								}, function() {
									alert("ajax error");
								});
					}

					$scope.editable_name = function(model) {

						$act_name = $('#act_name_' + model.id);
						$act_name.editable({
							type : "text", // 编辑框的类型。支持text|textarea|select|date|checklist等
							title : "监控点名称", // 编辑框的标题
							disabled : false, // 是否禁用编辑
							emptytext : "空文本", // 空值的默认文本
							mode : "inline", // 编辑框的模式：支持popup和inline两种模式，默认是popup
							validate : function(value) { // 字段验证
								if (!$.trim(value)) {
									return '不能为空';
								}
								$scope.upActcfgName(model, value);
							}
						});
					}
					$scope.editable_value = function(model) {
						$act_value = $('#act_value_' + model.id);
						$act_value.editable({
							type : "text",
							title : "监控点数值",
							pk : 1,
							url : '',
							disabled : false,
							emptytext : "0",
							mode : "inline",
							validate : function(value) {
								if (!$.trim(value)) {
									return '不能为空';
								}
								$scope.putMess(model, value);
							}
						});
					}

					// 修改监控点名称
					$scope.upActcfgName = function(model, name) {
						var params = {

							name : name,
							id : model.id,
							actgroupId : actgroupId

						};
						T.common.ajax
								.request(
										"WeconBox",
										"actDataAction/upActcfgName",
										params,
										function(data, code, msg) {
											if (code == 200) {
												// $scope.mc_change();
												$scope
														.ws_send(
																$scope.paginationConf.currentPage,
																$scope.paginationConf.itemsPerPage,
																actgroupId)
											} else {

												alert(code + "-" + msg);
											}
										}, function() {
											alert("ajax error");
										});
					}
					// 下发数据到盒子
					$scope.putMess = function(model, value) {
						var params = {
							value : value,
							addr_id : model.id

						};
						T.common.ajax.request("WeconBox",
								"actDataAction/putMess", params, function(data,
										code, msg) {
									if (code == 200) {
										$scope.resultData = data.resultData;
										if ($scope.resultData == 0) {
											alert("数据下发失败，请检查盒子是否在线！");
										} else {
											alert("数据下发盒子成功！");
										}

									} else {

										alert(code + "-" + msg);
									}
								}, function() {
									alert("ajax error");
								});
					}
					/*
					 * 展示所有监控点设置iframe的url属性
					 */
					$scope.showRestList = function() {
						console.log();
						var path = "viewmanagerpointTable.html?accounttype="
								+ $scope.accounttype + "&actgroupId="
								+ actgroupId;
						$("#myiframe").attr('src', path);
					}

					/*
					 * 提交选中的监控点
					 */
					$scope.setViewOpint = function() {
						var rightOption = [];
						var chk_value = [];
						$("#myiframe").contents().find(
								'input[name="cbid"]:checked').each(function() {
							chk_value.push($(this).val());
						});
						var ids = chk_value.join(",");
						if (chk_value.length == 0) {
							alert("请选择至少一条监控点");

							return;
						}
						var params = {
							acc_dir_id : actgroupId,
							selectedId : ids
						};
						T.common.ajax
								.request(
										"WeconBox",
										"actDataAction/allotMonitor",
										params,
										function(data, code, msg) {
											if (code == 200) {
												$("#dispatchpoint").modal(
														"hide");
												$scope
														.ws_send(
																$scope.paginationConf.currentPage,
																$scope.paginationConf.itemsPerPage,
																actgroupId);

												alert("分配监控点成功");

											} else {
												alert(code + "-" + msg);
											}
										}, function() {
											alert("ajax error");
										});
					};
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
													$("#addrid").val(
															minfo.main_addr);
													$("#describeid").val(
															minfo.describe);

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
													$scope.datatype();

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
										$scope.datatype();
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
								$("#rangid").html(data.range);
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
							if (!regnum.test($("#dataid").val())
									|| !regnum.test($("#decid").val())) {
								alert("整数位数或者小数位数格式错误");
								return;
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
						if($("#dataid").val()!=""||$("#decid").val()!=""){
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
							rang : $("#rangid").text(),
							describe : $("#describeid").val(),
							digit_count : digs,
							data_type : "0",
							group_id : actgroupId

						};
						T.common.ajax
								.request(
										"WeconBox",
										"actDataAction/addUpdataMonitor",
										params,
										function(data, code, msg) {
											if (code == 200) {
												$("#addpoint").modal("hide");
												$scope
														.ws_send(
																$scope.paginationConf.currentPage,
																$scope.paginationConf.itemsPerPage,
																actgroupId);
												if (mtype == 0) {
													alert("添加实时监控点成功");
												} else {
													alert("修改实时监控点成功");
												}

											} else {
												alert(code + "-" + msg);
											}
										}, function() {
											alert("ajax error");
										});

					}

				})