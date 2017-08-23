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

				})