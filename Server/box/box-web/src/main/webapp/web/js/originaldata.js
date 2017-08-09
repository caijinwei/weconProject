var appModule = angular.module('weconweb', []);
appModule.controller("infoController", function($scope, $http, $compile) {
	$scope.onInit = function() {
		$scope.commointor_submit();

	}

	/**
	 * 提交接口请求
	 */
	$scope.commointor_submit = function() {
		T.common.ajax.request("WeconBox", "testact/getComMonitor",
				new Object(), function(data, code, msg) {
					if (code == 200) {
						$scope.commonitors = data.comMonitor;
						$scope.$apply();
						$scope.change();
						$("#comid").change(function() {
							$scope.change();
						})
					} else {

						alert(code + "-" + msg);
					}
				}, function() {
					alert("ajax error");
				});
	}
	$scope.change = function() {
		angular.forEach($scope.commonitors, function(data, index, array) {
			console.log($("#comid").val());
			if ($("#comid").val() == data.plc_id) {
				console.log(data.com + '=' + array[index].com);
				$scope.commonitors_item = data.arrmonitors;
				$scope.$apply();
			}
		});

	}
	$scope.searchHisData = function() {
		var params = {
			real_his_cfg_id : $("#monitorid").val(),
			start_date : $("#startdateid").val(),
			end_date : $("#enddateid").val(),

		};
		T.common.ajax.request("WeconBox", "testact/getHisData", params,
				function(data, code, msg) {
					if (code == 200) {
						$scope.Hisdatas = data.realHisCfgDataList;
						$scope.$apply();
					} else {

						alert(code + "-" + msg);
					}
				}, function() {
					alert("ajax error");
				});
	}

})