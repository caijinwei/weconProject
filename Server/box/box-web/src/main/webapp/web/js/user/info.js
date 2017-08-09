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
                alert(msg);
            }
        });
    }

    /**
     * 修改密码
     */
    $scope.chgpwd = function () {
        if ($("#oldpwd").val().trim() == "" || $("#newpwd").val().trim() == "" || $("#newpwdconfirm").val().trim() == "") {
            alert("请输入完整信息");
            return;
        }
        if ($("#newpwd").val().trim() != $("#newpwdconfirm").val().trim()) {
            alert("两个输入的密码不一致");
            return;
        }
        var params =
        {
            oldpwd: T.common.util.md5($('#oldpwd').val().trim()),
            newpwd: T.common.util.md5($('#newpwd').val().trim())
        };
        T.common.ajax.request('WeconBox', "user/chgpwd", params, function (data, code, msg) {
            if (code == 200) {
                alert("修改成功");
                $("#updatePwd").hide();
            }
            else {
                alert(msg);
            }
        });
    }

    /**
     * 修改 email
     */
    $scope.chgemail = function () {
        if ($("#newemail").val().trim() == "") {
            alert("请邮箱地址");
            return;
        }
        var params =
        {
            email: $('#newemail').val().trim()
        };
        T.common.ajax.request('WeconBox', "user/chgemail", params, function (data, code, msg) {
            if (code == 200) {
                alert("修改成功,请到新邮箱激活");
                $("#updateEmail").hide();
            }
            else {
                alert(msg);
            }
        });
    }
})