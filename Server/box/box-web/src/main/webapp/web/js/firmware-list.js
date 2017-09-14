/**
 * Created by zengzhipeng on 2017/9/14.
 */
var appModule = angular.module('weconweb', []);
appModule.controller("listController", function ($scope, $http, $compile) {
    $scope.onInit = function () {

    }

    $scope.addNew = function () {
        location.href = "firmware-info.html";
    }
})