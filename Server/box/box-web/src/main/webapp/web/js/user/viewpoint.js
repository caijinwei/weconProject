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
        $scope.showRealPoint(type, viewid);
    }


    $scope.showRealPoint = function (type, view_id) {
        var params =
        {
            view_id: view_id,
            type: type
        }
        T.common.ajax.request("WeconBox", "Viewpoint/showReal", params, function (data, code, msg) {
            if (code == 200) {
                $scope.realpointDatas = data.list;
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
     * 分页
     * 分配实时历史监控点
     *       展示管理员用户余下监控点
     *
     * */
    $scope.showRestPoint = function () {
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
    $scope.paginationConf = {
        totalItems: $scope.count,
    }
    $scope.getRestList = function (pageIndex, pageSize) {
        var view_id = T.common.util.getParameter("viewid");
        var type = T.common.util.getParameter("type");
        if (pageIndex == 0)
            pageIndex = 1;
        $("#loadingModal").modal("show");
        var params =
        {
            view_id: view_id,
            type: type,
            pageIndex: pageIndex,
            pageSize: pageSize
        }
        T.common.ajax.request("WeconBox", "Viewpoint/showRealHiss", params, function (data, code, msg) {
            if (code == 200) {
                $scope.paginationConf.totalItems = data.page.totalRecord;
                $scope.resrPointDatas = data.page.list;
                $scope.$apply();
                $("#loadingModal").modal("hide");
            }
            else {
                alert(code + "-" + msg);
            }
        }, function () {
            alert("ajax error");
        });
    }


    $scope.selected = [];
    $scope.isChecked = function (id) {
        return $scope.selected.indexOf(id) >= 0;
    };
    //更新某一列数据的选择
    $scope.updateSelection = function ($event, id) {
        var checkbox = $event.target;
        var action = (checkbox.checked ? 'add' : 'remove');
        updateSelected(action, id);
    };
    var updateSelected = function (action, id) {
        if (action == 'add' && $scope.selected.indexOf(id) == -1)
            $scope.selected.push(id);
        if (action == 'remove' && $scope.selected.indexOf(id) != -1)
            $scope.selected.splice($scope.selected.indexOf(id), 1);
    };
    /*
     * 提交选中的监控点
     * */
    $scope.setViewOpint = function () {
        var viewid = T.common.util.getParameter("viewid");
        var rightOption = [];
        var chk_value = [];
        $('input[name="cbid"]:checked').each(function () {
            chk_value.push($(this).val());

            var tem = "right_" + $(this).val();
            rightOption.push($("input[name=" + tem + "]:checked").val());
        });
        var ids = chk_value.join(",");
        var rights = rightOption.join(",");
        var params = {viewId: viewid, selectedId: ids, rights: rights};
        T.common.ajax.request("WeconBox", "Viewpoint/setViewPoint", params, function (data, code, msg) {
            if (code == 200) {
                $("#showRestOpint").modal("hide");
                alert("添加成功");
                var type = T.common.util.getParameter("type");
                $scope.showRealPoint(type,viewid);
                //$scope.getRestList( $scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
            }
            else {
                alert(code + "-" + msg);
            }
        }, function () {
            alert("ajax error");
        });
    }
});