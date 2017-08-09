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


    /*
     * 添加盒子
     *  1）判定条件：
     *           1。盒子序列号 密码 别名 是否存在
     *           2.是否是管理员
     *           3.改盒子是否已经绑定了盒子
     *           4.如果有选择盒子就添加到表acoount_dir_rel中
     *               -如果没有就放在默认分组中
     *
     * */
    $scope.boundBox = function () {
        if ($("#acc_dir_id").val() == "" || $("#machine_code").val() == "" || $("#dev_password").val() == "") {
            alert("必填参数没有填写完整");
            return;
        }
        var params =
        {
            acc_dir_id: $("#acc_dir_id").val(),
            machine_code: $("#machine_code").val(),
            name: $("#dev_name").val(),
            password: $("#dev_password").val()
        }
        T.common.ajax.request('WeconBox', "baseInfoAction/boundBox", params, function (data, code, msg) {
            if (code == 200) {
                alert("PIBox绑定成功");
                $("#addPIBox").modal("hide");
                clearInput();
            }
            else {
                alert(code + "-" + msg);
            }
        }, function () {
            alert("ajax error");
        });
    }

    /*
    * 绑定PIBox表单中展示几个分组
    * */
    $scope.getRefList = function () {
        var params = {
            type: "0"
        }
        T.common.ajax.request("WeconBox", "userdiract/getuserdirs", params, function (data, code, msg) {
            if (code == 200) {
                $scope.refList = data.list;
                $scope.$apply();
            }
            else {
                alert(code + " " + msg);
            }
        }, function () {
            alert("ajax error");
        });
    }

    $scope.clearInput=function() {
        $("#machine_code").val("") ;
        $("#dev_password").val("");
        $("#dev_name").val("");
    }


    })

