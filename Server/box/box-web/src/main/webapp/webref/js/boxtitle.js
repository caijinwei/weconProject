/**
 * Created by lanph on 2018/01/24.
 */
var appModule = angular.module('weconweb', []);
appModule
		.controller(
				"listController",
				function($scope, $http, $compile) {
					$scope.onInit = function() {
						$scope.deviceid = T.common.util
						.getParameter("device_id");
						$scope.deviceName = T.common.util.getParameter("device_name");

						$("#myiframe").attr("src","baseinfo.html?device_id="+$scope.deviceid+"&device_name="+$scope.deviceName);
					}

					
					
			
				});