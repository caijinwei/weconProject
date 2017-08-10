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

})