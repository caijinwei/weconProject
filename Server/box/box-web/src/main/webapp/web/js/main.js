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
                $scope.type = data.type;
                $scope.$apply();
            }
            else {
                alert(code + " " + msg);
            }
        });

        //获取分组信息
        var params = {
            type: "0"
        }
        T.common.ajax.request('WeconBox', "userdiract/getuserdirs", params, function (data, code, msg) {
            if (code == 200) {
                $scope.grouplist = data.list;
                $scope.$apply();
                $('.collapse').on('show.bs.collapse',function () {
                    $(this).prev('div').find('span').removeClass('glyphicon-folder-close');
                    $(this).prev('div').find('span').addClass('glyphicon-folder-open');
                }).on('hide.bs.collapse',function () {
                    $(this).prev('div').find('span').removeClass('glyphicon-folder-open');
                    $(this).prev('div').find('span').addClass('glyphicon-folder-close');
                })
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