/**
 * Created by caijinw on 2017/8/9.
 */
var appModule = angular.module('weconweb', []);
appModule.controller("listController", function ($scope, $http, $compile) {
    $scope.onInit = function () {
        var viewid = T.common.util.getParameter("viewid");
        var type = T.common.util.getParameter("type");
        $scope.conf = {
            currentPage: 1,
            itemsPerPage: 10,
            totalItems: $scope.count,
            pagesLength: 15,
            perPageOptions: [5, 10, 20, 50, 100],
            rememberPerPage: 'perPageItems',
            onChange: function () {
                if (this.currentPage != 0) {
                    $scope.getRestList(this.currentPage, this.itemsPerPage);
                }
            }
        }


        $scope.getRestList($scope.conf.currentPage, $scope.conf.itemsPerPage);
    }
    $scope.conf = {
        totalItems: $scope.count
    }
    $scope.getRestList = function (pageIndex, pageSize) {
        var view_id = T.common.util.getParameter("viewid");
        var type = T.common.util.getParameter("type");
        if (pageIndex == 0)
            pageIndex = 1;
        console.log("pageIndex：", pageIndex);
        console.log("pageSize:", pageSize);
        var params =
        {
            view_id: view_id,
            type: type,
            pageIndex: pageIndex,
            pageSize: pageSize
        }
        T.common.ajax.request("WeconBox", "Viewpoint/showRealHiss", params, function (data, code, msg) {
            if (code == 200) {
                $scope.conf.totalItems = data.page.totalRecord;
                $scope.resrPointDatas = data.page.list;
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
