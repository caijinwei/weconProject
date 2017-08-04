/**
 * Created by zengzhipeng on 2017/8/3.
 */
var appModule = angular.module('weconweb', []);
appModule.controller("infoController", function ($scope, $http, $compile) {
    $scope.onInit = function () {
        T.common.user.checkAuth();

        T.common.ajax.request('WeconBox', "user/userinfo", new Object(), function (data, code, msg) {
            if (code == 200) {
                $scope.username = data.username;
                $scope.$apply();
            }
            else {
                alert(code + " " + msg);
            }
        });
    }

    $scope.logout = function () {
        T.common.ajax.request('WeconBox', "user/signout", new Object(), function (data, code, msg) {
            if (code == 200) {
                location = "user/login.html";
            }
            else {
                alert(code + " " + msg);
            }
        });
    }
})