/**
 * Created by Administrator on 2017/8/29.
 */
/**
 * Created by caijinw on 2017/8/28.
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
            //rememberPerPage: 'perPageItems',
            onChange: function () {
                if (this.currentPage != 0) {
                    $scope.showAllDeviceDir("", this.currentPage, this.itemsPerPage);
                }
            }
        }
    }
    $scope.paginationConf = {
        totalItems: $scope.count,
    }
    /*
     * 根据用户id查询
     * */
    $scope.search = function () {
        alert(111);
        var accountId = $("#account_id").val();
        var bind_state=$("#bind_state").val();
        alert(bind_state);
        if(bind_state!="")
        {
            $scope.showDeviceByBoundState(bind_state,$scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
        }
        $scope.showAllDeviceDir(accountId, $scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
    }
    /*
     * 展示所有deviceDir
     * */
    $scope.showAllDeviceDir = function (accountId, pageNum, pageSize) {
        if (pageNum <= 0) {
            pageNum = 1;
        }
        var params =
        {
            accountId: accountId,
            pageNum: pageNum,
            pageSize: pageSize
        }
        T.common.ajax.request("WeconBox", "baseInfoAction/showAllDeviceDir", params, function (data, code, msg) {
            if (code == 200) {
                $scope.paginationConf.totalItems = data.page.totalRecord;
                $scope.pushlist = data.page.list;
                $scope.$apply();
            }
            else {
                alert(code + " " + msg);
                $("#loadingModal").modal("hide");
            }
        }, function () {
            alert("ajax error");
        });
    }
    /*
     * 设置绑定状态
     * */
    $scope.showDeviceByBoundState = function (bind_state, pageNum, pageSize) {
        if (pageNum <= 0) {
            pageNum = 1;
        }
        if(bind_state==-1)
        {
            $scope.showAllDeviceDir("",$scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
            return;
        }
        var params=
        {
            bind_state:bind_state,
            pageNum: pageNum,
            pageSize: pageSize
        }
        T.common.ajax.request("WeconBox", "baseInfoAction/showDeviceByBoundState", params, function (data, code, msg) {
            if (code == 200) {
                $scope.paginationConf.totalItems = data.page.totalRecord;
                $scope.pushlist = data.page.list;
                $scope.$apply();
            }
            else {
                alert(code + " " + msg);
                $("#loadingModal").modal("hide");
            }
        }, function () {
            alert("ajax error");
        });
    }


    ///**
    // * 查询数据
    // * @param pageIndex
    // * @param pageSize
    // */
    //$scope.getList = function (pageIndex, pageSize) {
    //    if (pageIndex == 0)
    //        pageIndex = 1;
    //    $("#loadingModal").modal("show");
    //    var params = {
    //        pageIndex: pageIndex,
    //        pageSize: pageSize
    //    };
    //    var fields = $('#search-div .form-control');
    //    for (var i = 0; i < fields.length; i++) {
    //        var f = $(fields[i]);
    //        params[f.attr('id')] = f.val();
    //    }
    //    if (params["account_id"] == "") {
    //        params["account_id"] = "-1";
    //    }
    //    T.common.ajax.request("WeconBox", "user/getallusers", params, function (data, code, msg) {
    //        if (code == 200) {
    //            $scope.paginationConf.totalItems = data.page.totalRecord;
    //            $scope.pushlist = data.page.list;
    //            $scope.$apply();
    //            $("#loadingModal").modal("hide");
    //        }
    //        else {
    //            alert(code + " " + msg);
    //            $("#loadingModal").modal("hide");
    //        }
    //    }, function () {
    //        alert("ajax error");
    //    });
    //}


});