/**
 * Created by caijinw on 2017/8/8.
 */
var appModule = angular.module('weconweb', []);
appModule.controller("infoController", function ($scope,$window,$http,$location,$compile) {

   /*
   * 盒子基本信息展示
   * */
    $scope.onInit = function ()
    {
        $scope.device_id=T.common.util.getParameter("device_id");
     //   $("#device_id").val($scope.device_id);
        var params={device_id : $scope.device_id};
        T.common.ajax.request("WeconBox", "baseInfoAction/showBaseInfo", params,function (data, code, msg) {

            if (code == 200) {

                /*
                * 0表示在离线 1表示在线，
                * */
                if(data.device.state=="0")
                {
                    data.device.state="离线";
                }else
                {
                    data.device.state="在线";
                }

                $scope.infoData = data.device;
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

