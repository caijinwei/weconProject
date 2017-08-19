var appModule = angular.module('weconweb', []);
appModule
		.controller(
				"infoController",
				function($scope, $http, $compile) {
					$scope.onInit = function() {

						$scope.deviceid = T.common.util
								.getParameter("device_id");
						$scope.type=0;
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

					/**
					 * 提交接口请求
					 */
					$scope.act_submit = function(pageIndex, pageSize, groupId) {
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
												$scope.type=1;

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

				})