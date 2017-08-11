/**
 * Created by zengzhipeng on 2017/8/9.
 */
var appModule = angular.module('weconweb', []);
appModule.controller("listController", function ($scope, $http, $compile) {
    $scope.onInit = function () {
        var viewid = T.common.util.getParameter("viewid");
        var type = T.common.util.getParameter("type");
        $scope.name = T.common.util.getParameter("name");
        if (type == 0) {
            $scope.title = "实时监控点权限列表";
        } else if (type == 1) {
            $scope.title = "历史监控点权限列表";
        }
        $scope.showRealPoint(type,viewid);
    }


    $scope.showRealPoint=function (type,view_id)
    {
        var params=
        {
            view_id:view_id,
            type:type
        }
            T.common.ajax.request("WeconBox", "Viewpoint/showReal", params, function (data, code, msg) {
                if (code == 200) {
                    $scope.realpointDatas=data.list;
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
    * 分配实时历史监控点
    *       展示管理员用户余下监控点
    * */
    $scope.shiwRestPoint=function()
    {
        $scope.paginationConf = {
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
    }

    $scope.getRestList= function(pageIndex,pageSize)
    {
        var view_id = T.common.util.getParameter("viewid");
        var type = T.common.util.getParameter("type");
        //@RequestParam("view_id") Integer viewId,@RequestParam("type") Integer type,@RequestParam("pageIndex") Integer pageIndex,@RequestParam("pageSize") Integer pageSize)
        // pageIndex:$("#pageIndex").val(),
        //pageSize:$("#pageSize").val()

        var params=
        {
            view_id:view_id,
            type:type,
            pageIndex:pageIndex,
            pageSize:pageSize
        }
        T.common.ajax.request("WeconBox", "Viewpoint/showRealHiss", params, function (data, code, msg)
        {
            if (code == 200) {
                $scope.resrPointDatas=data.list;
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