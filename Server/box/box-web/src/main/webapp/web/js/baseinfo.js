/**
 * Created by caijinw on 2017/8/8.
 */
var appModule = angular.module('weconweb', []);
appModule.controller("infoController", function ($scope, $window, $http, $location, $compile) {

    /*
     * 盒子基本信息展示
     * */
    $scope.onInit = function () {
        $scope.showBaseInfo();
    }
    $scope.showBaseInfo = function () {
        $scope.device_id = T.common.util.getParameter("device_id");
        //   $("#device_id").val($scope.device_id);
        var params = {device_id: $scope.device_id};
        T.common.ajax.request("WeconBox", "baseInfoAction/showBaseInfo", params, function (data, code, msg) {

            if (code == 200) {
                $scope.infoData = data.device;
                $scope.$apply();
            }
            else {
                alert(code + "-" + msg);
            }
        }, function () {
            alert("ajax error");
        });
    }
    /*
     * 盒子与账户解除关联
     * */
    $scope.deletePIBoxBtn = function (device_id) {
        var parmas =
        {
            device_id: device_id
        }
        T.common.ajax.request("WeconBox", "baseInfoAction/deletePIBox", parmas, function (data, code, msg) {
            if (code == 200) {
                $("#deletePIBox").modal('hide');
                alert("解除绑定成功！")
                $scope.showBaseInfo();
                $scope.$apply();
            }
            else {
                alert(code + "-" + msg);
            }
        }, function () {
            alert("ajax error");
        });
    }


});

