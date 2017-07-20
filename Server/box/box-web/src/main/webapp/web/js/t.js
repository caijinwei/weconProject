/**
 * Created by Administrator on 2017/7/19.
 */
var appModule = angular.module('weconweb', []);
appModule.controller("infoController", function ($scope, $http, $compile) {
    $scope.onInit = function () {
    }

    /**
     * 提交接口请求
     */
    $scope.act_submit = function () {
        var params = $.parseJSON($("#act_params").val());
        T.common.ajax.request("WeconBox", $("#act_url").val(), params, function (data, code, msg)
        {
            if (code == 200)
            {
//                    console.log(data);
                $("#act_ret").val(JSON.stringify(data, null, '\t'));
            }
            else {
                alert(code + "-" + msg);
            }
        }, function () {
            alert("ajax error");
        });
    }
})