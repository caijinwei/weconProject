/**
 * Created by zengzhipeng on 2017/8/9.
 */
var appModule = angular.module('weconweb', []);
appModule.controller("listController", function ($scope, $http, $compile) {
    $scope.onInit = function () {
        var viewid = T.common.util.getParameter("viewid");
        $scope.name = T.common.util.getParameter("name");
        $scope.$apply();
        console.log(T.common.util.getParameter("name"));
    }
})