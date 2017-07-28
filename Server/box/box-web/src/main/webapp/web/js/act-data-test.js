var appModule = angular.module('weconweb', []);
appModule.controller("infoController", function ($scope, $http, $compile) {
    $scope.onInit = function () {
        $scope.act_submit();
    }

    /**
     * 提交接口请求
     */
    $scope.act_submit = function act_submit() {

        var params = {
            machine_code: $("#machine_code_search").val()
        };

        T.common.ajax.request("WeconBox", "testact/getActDate", params, function (data, code, msg) {
            if (code == 200) {
                $scope.actDatas = data.piBoxActDateMode;
                $scope.act_time_data_list = data.piBoxActDateMode.act_time_data_list;
                $scope.addr_list = data.piBoxActDateMode.act_time_data_list.addr_list;
                $scope.$apply();
                setTimeout(act_submit, 1000);
            }
            else {

                alert(code + "-" + msg);
            }
        }, function () {
            alert("ajax error");
        });
    }

    $scope.putMess = function () {
        var params = {
            machine_code: $("#machine_code").val(),
            com: $("#com").val(),
            addr: $("#addr").val(),
            value: $("#value").val(),
            addr_id: $("#addr_id").val()

        };
        T.common.ajax.request("WeconBox", "testact/putMess", params, function (data, code, msg) {
            if (code == 200) {
                alert("消息发送成功");
            }
            else {

                alert(code + "-" + msg);
            }
        }, function () {
            alert("ajax error");
        });
    }


})