/**
 * Created by zengzhipeng on 2017/9/16.
 */
var appModule = angular.module('weconweb', []);
appModule.controller("listController", function ($scope, $http, $compile) {
    $scope.onInit = function () {
        var params = {};
        //加载基本信息
        T.common.ajax.request("WeconBox", "appveraction/getversion", params, function (data, code, msg) {
            if (code == 200) {
                if (data.versions != null) {
                    $('#androidVersion').val(data.versions[0]);
                    $('#iosVersion').val(data.versions[1]);
                    $('#updateContent').val(data.versions[2]);
                    $('#isforce').val(data.versions[3]);
                } else {
                    return;
                }
            }
        }, function () {
            console.log("ajax error");
        });
    }

    $scope.updateVersion = function () {
        var params = {};
        var fields = $('#search-div .form-control');
        for (var i = 0; i < fields.length; i++) {
            var f = $(fields[i]);
            params[f.attr('id')] = f.val();
        }
        T.common.ajax.request("WeconBox", "appveraction/updateversion", params, function (data, code, msg) {
            if (code == 200) {
                alert('保存成功');
                $scope.$apply();
                $("#loadingModal").modal("hide");
            }
        }, function () {
        });
    }

})