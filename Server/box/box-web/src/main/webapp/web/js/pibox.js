var appModule = angular.module('weconweb', []);
appModule.controller("infoController", function($scope, $http, $compile) {
	$scope.onInit = function() {
		$scope.devicename = T.common.util.getParameter("device_name");
		$scope.deviceid = T.common.util.getParameter("device_id");
	}
})