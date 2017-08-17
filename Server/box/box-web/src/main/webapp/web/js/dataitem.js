var appModule = angular.module('weconweb', []);
appModule
		.controller(
				"infoController",
				function($scope, $http, $compile) {
					$scope.onInit = function() {
						// $scope.act_submit();
						/*
						 * $scope.deviceid = T.common.util
						 * .getParameter("device_id"); $scope.dirid =
						 * T.common.util.getParameter("acc_dir_id");
						 */
						 $scope.deviceid=1;
						 $scope.dirid=1;
						$scope.paginationConf = {
							currentPage : 1,
							itemsPerPage : 10,
							totalItems : $scope.count,
							pagesLength : 15,
							perPageOptions : [ 5, 10, 20, 50, 100 ],
							rememberPerPage : 'perPageItems',
							onChange : function() {
								if (this.currentPage != 0) {
									$scope.act_submit(this.currentPage,
											this.itemsPerPage,$scope.deviceid, $scope.dirid);
								}
							}
						}

					}
					$scope.paginationConf = {
						totalItems : $scope.count,
					}

					var t;

					/**
					 * 提交接口请求
					 */
					$scope.act_submit = function(pageIndex, pageSize,deviceid,dirid) {
						if (pageIndex == 0)
							pageIndex = 1;
						var params = {
							device_id :deviceid,
							acc_dir_id : dirid,
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
											t = setTimeout(function(){$scope.act_submit($scope.paginationConf.currentPage,$scope.paginationConf.itemsPerPage,$scope.deviceid, $scope.dirid)}, 3000);
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

				})