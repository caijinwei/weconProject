/**
 * Created by zengzhipeng on 2017/8/4.
 */
var appModule = angular.module('weconweb', []);
appModule.controller("infoController", function ($scope, $http, $compile) {
    $scope.onInit = function () {

        T.common.ajax.request('WeconBox', "user/userinfod", new Object(), function (data, code, msg) {
            if (code == 200) {
                $scope.userInfo = data.userInfo;
                $scope.$apply();
            }
            else {
                alert(code + " " + msg);
            }
        });
    }

    $scope.chgusername = function () {
    }
})