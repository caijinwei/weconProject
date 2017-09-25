/**
 * Created by zengzhipeng on 2017/8/7.
 */
var appModule = angular.module('weconweb', []);
appModule.controller("listController", function ($scope, $http, $compile) {
    $scope.onInit = function () {
        $scope.paginationConf = {
            currentPage: 1,
            itemsPerPage: 10,
            totalItems: $scope.count,
            pagesLength: 15,
            perPageOptions: [5, 10, 20, 50, 100],
            rememberPerPage: 'perPageItems',
            onChange: function () {
                if (this.currentPage != 0) {
                    $scope.getList(this.currentPage, this.itemsPerPage);
                }
            }
        }
    }

    $scope.paginationConf = {
        totalItems: $scope.count,
    }
    /**
     * 查询数据
     * @param pageIndex
     * @param pageSize
     */
    $scope.getList = function (pageIndex, pageSize) {
        if (pageIndex == 0)
            pageIndex = 1;
        $("#loadingModal").modal("show");
        var params = {
            pageIndex: pageIndex,
            pageSize: pageSize
        };
        T.common.ajax.request("WeconBox", "user/getviewusers", params, function (data, code, msg) {
            if (code == 200) {
                $scope.paginationConf.totalItems = data.page.totalRecord;
                $scope.pushlist = data.page.list;
                $scope.$apply();
                $("#loadingModal").modal("hide");
            }
            else {
                alert(code + " " + msg);
                $("#loadingModal").modal("hide");
            }
        }, function () {
            alert("ajax error");
        });
    }

    /**
     * 打开新增窗口
     */
    $scope.showaddmodal = function () {
        $("#addViewAccount").modal('show');
        $("#username").val("");
        $("#password").val("");
    }
    /**
     * 新增视图帐号
     */
    $scope.addviewuser = function () {
        if ($("#username").val().trim() == "" || $("#password").val().trim() == "") {
            alert("请输入帐号和密码");
            return;
        }
        $("#loadingModal").modal("show");
        var params = {
            username: $("#username").val().trim(),
            password: T.common.util.md5($("#password").val().trim())
        };
        if ($('#state').prop("checked")) {
            params['state'] = "1";
        } else {
            params['state'] = "0";
        }
        T.common.ajax.request("WeconBox", "user/addviewuser", params, function (data, code, msg) {
            if (code == 200) {
                $scope.getList($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
            }
            else {
                alert(msg);
            }
            $("#loadingModal").modal("hide");
            $("#addViewAccount").modal("hide");
        }, function () {
            alert("ajax error");
        });
    }

    $scope.methods = {
        updstate: function (model) {
            var confirm_msg = "确认要做此操作吗?";
            if (model.state == 1) {
                confirm_msg = "确认要禁用此用户吗?";
            } else {
                confirm_msg = "确认要启用此用户吗?";
            }
            if (confirm(confirm_msg)) {
                var params = {
                    user_id: model.account_id
                };
                if (model.state == 1) {
                    params["state"] = "0";
                } else if (model.state == 0) {
                    params["state"] = "1";
                }
                T.common.ajax.request("WeconBox", "user/chgviewuserstate", params, function (data, code, msg) {
                    if (code == 200) {
                        $scope.getList($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
                        $("#loadingModal").modal("hide");
                    }
                    else {
                        alert(msg);
                        $("#loadingModal").modal("hide");
                    }
                }, function () {
                    alert("ajax error");
                });
            }
        },
        viewpoint0: function (model) {
            location = "viewpoint.html?type=0&viewid=" + model.account_id + "&name=" + model.username;
        },
        viewpoint1: function (model) {
            location = "viewpoint.html?type=1&viewid=" + model.account_id + "&name=" + model.username;
        },
        viewpointalarm: function (model) {
            location = "viewpointalarm.html?type=2&viewid=" + model.account_id + "&name=" + model.username;
        }
    }
})