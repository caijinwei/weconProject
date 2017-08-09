var appModule = angular.module('weconweb', []);
appModule.controller("infoController", function($scope, $http, $compile) {
	$scope.onInit = function() {
		$scope.act_submit();
	}

	var t;

	/**
	 * 提交接口请求
	 */
	$scope.act_submit = function act_submit() {
		T.common.ajax.request("WeconBox", "testact/getActData", new Object(),
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