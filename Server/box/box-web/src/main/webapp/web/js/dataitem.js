var appModule = angular.module('weconweb', []);
appModule.controller("infoController", function($scope, $http, $compile) {
	$scope.onInit = function() {
//		$scope.deviceid = T.common.util.getParameter("device_id");
//		alert($scope.deviceid);
		$scope.act_submit();

	}

	/**
	 * 提交接口请求
	 */
	/*
	 * $scope.search_submit = function() { T.common.ajax.request("WeconBox",
	 * "actDataAction/getBoxGroup", new Object(), function(data, code, msg) { if
	 * (code == 200) { $scope.allDatas = data.allData; $scope.$apply();
	 * $scope.change(); $("#currGroupSelect").change(function() {
	 * $scope.change(); }) //初始化下拉框 $('.selectpicker').selectpicker({
	 * 'selectedText': 'cat' });
	 *  } else {
	 * 
	 * alert(code + "-" + msg); } }, function() { alert("ajax error"); }); }
	 * 
	 * $scope.change = function() { angular.forEach($scope.allDatas,
	 * function(data, index, array) { if ($("#currGroupSelect").val() ==
	 * data.accountdirId) {
	 * 
	 * angular.forEach(data.deviceList, function(data, index, array) {
	 * 
	 * 
	 * console.log( data.deviceName); console.log( data.deviceId); console.log(
	 * $scope.boxid); $scope.boxid = data.deviceId; $scope.$apply();
	 * 
	 * });
	 * 
	 *  } });
	 *  }
	 */

	var t;

	/**
	 * 提交接口请求
	 */
	$scope.act_submit = function act_submit() {
		var params = {
			device_id :1

		};
		T.common.ajax.request("WeconBox", "actDataAction/getActGroup", params,
				function(data, code, msg) {
					if (code == 200) {
						$scope.actDatas = data.piBoxActDateMode;
						$scope.$apply();
						t = setTimeout(act_submit, 3000);
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