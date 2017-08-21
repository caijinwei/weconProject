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
						$scope.act_group($scope.deviceid);

						$scope.paginationConf = {
							currentPage : 1,
							itemsPerPage : 10,
							totalItems : $scope.count,
							pagesLength : 15,
							perPageOptions : [ 5, 10, 20, 50, 100 ],
							rememberPerPage : 'perPageItems',
							onChange : function() {
								if (this.currentPage != 0) {
									// $scope.act_submit(
									// this.currentPage,
									// this.itemsPerPage);
								}
							}
						}

						// 打开模态框
						function showAddGroup() {
							$('#identifier').modal('show');
						}
						// 添加导航项，关闭模态框
						function addGroup() {
							var name = $('#newGroupName').val();
							var length = $('#monitorTab').children().length;
							var lastPosition = length - 3;
							($("#monitorTab li:eq(" + lastPosition + ")"))
									.after("<li><a href=\"#data-item-1\" data-toggle=\"tab\">"
											+ name + "</a></li>");
							$("#addDataGroup").modal("hide");
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
												if (data.ActGroup != null) {
													var fristGroupId = data.ActGroup[0].id;
													$scope
															.act_submit(
																	$scope.paginationConf.currentPage,
																	$scope.paginationConf.itemsPerPage,
																	fristGroupId);
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
					$scope.act_submit = function(pageIndex, pageSize, groupId) {
						actgroupId = groupId;
						if (pageIndex == 0)
							pageIndex = 1;
						var params = {
							device_id : $scope.deviceid,
							acc_dir_id : groupId,
							pageIndex : pageIndex,
							pageSize : pageSize

						};
						T.common.ajax
								.request(
										"WeconBox",
										"actDataAction/getActData",
										params,
										function(data, code, msg) {
											if (code == 200) {
												$scope.paginationConf.totalItems = data.piBoxActDateMode.totalRecord;
												$scope.actDatas = data.piBoxActDateMode.list;
												$scope.$apply();
												angular
														.forEach(
																$scope.actDatas,
																function(data,
																		index,
																		array) {
																	console
																			.log("初始化=="
																					+ data.id);
																	$scope
																			.editable_name(data);
																	$scope
																			.editable_value(data);
																});

												t = setTimeout(
														function() {
															$scope
																	.act_submit(
																			$scope.paginationConf.currentPage,
																			$scope.paginationConf.itemsPerPage,
																			groupId)
														}, 3000);
											} else {

												alert(code + "-" + msg);
											}
										}, function() {
											alert("ajax error");
										});
					}

					$scope.mc_change = function() {
						clearTimeout(t);
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
												var lastPosition = length - 3;
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
							emptytext : "默认值0",
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
							id : model.id

						};
						T.common.ajax
								.request(
										"WeconBox",
										"actDataAction/upActcfgName",
										params,
										function(data, code, msg) {
											if (code == 200) {
												$scope.mc_change();
												$scope
														.act_submit(
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
							machine_code : model.machine_code,
							com : model.plc_id,
							value : value,
							addr_id : model.id

						};
						T.common.ajax.request("WeconBox",
								"actDataAction/putMess", params, function(data,
										code, msg) {
									if (code == 200) {
										alert("数据下发盒子成功！");
									} else {

										alert(code + "-" + msg);
									}
								}, function() {
									alert("ajax error");
								});
					}

				})