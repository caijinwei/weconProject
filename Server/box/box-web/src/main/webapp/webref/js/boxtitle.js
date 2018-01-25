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
						  console.log($scope.deviceid);
					}

					
					
			
				});