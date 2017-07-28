
var appModule = angular.module('weconweb', []);
appModule.controller("infoController", function ($scope, $http, $compile) {
    $scope.onInit = function () {
    }

    /**
     * 提交接口请求
     */
    $scope.act_submit = function () {

        var params ={
            machine_code:$("#machine_code").val()
        };

        T.common.ajax.request("WeconBox","testact/getActDate",params, function (data, code, msg)
        {
            if (code == 200)
            {
                $scope.actDatas=data.piBoxActDateMode;
                $scope.act_time_data_list=data.piBoxActDateMode.act_time_data_list;

                tem=data.piBoxActDateMode.act_time_data_list.addr_list;
                $scope.addr_list=tem;
                $scope.$apply();
            }
            else {

                alert(code + "-" + msg);
            }
        }, function () {
            alert("ajax error");
        });
    }

    $scope.putMess=function()
    {
        var params={
            machine_code:$("#machine_code").val(),
            com:$("#com").val(),
            addr:$("#addr").val(),
            value:$("#value").val()
        };
        T.common.ajax.request("WeconBox","testact/putMess",params, function (data, code, msg)
        {
            if (code == 200)
            {
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