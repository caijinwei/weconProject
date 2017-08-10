var appModule = angular.module('weconweb', []);
appModule.controller("infoController", function($scope, $http, $compile) {
	$scope.onInit = function() {
		$scope.act_submit();
	}

	/**
	 * 提交接口请求
	 */
	$scope.act_submit = function act_submit() {
		T.common.ajax.request("WeconBox", "alarmDataAction/getNowAlarmData",
				new Object(), function(data, code, msg) {
					if (code == 200) {
						$scope.alarmDatas = data.alarmData;
						$scope.$apply();
					} else {

						alert(code + "-" + msg);
					}
				}, function() {
					alert("ajax error");
				});
	}

})