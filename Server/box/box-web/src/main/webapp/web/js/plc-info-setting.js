var appModule = angular.module('weconweb', []);



appModule.controller("infoController", function ($scope,$window,$http,$location,$compile) {
    $scope.onInit = function () {
        /*
         * plc配置展示
         * */

        $scope.device_id=T.common.util.getParameter("device_id");
        $("#device_id").val($scope.device_id);
        var params={device_id:$("#device_id").val()};
        T.common.ajax.request("WeconBox", "plcInfoAction/showAllPlcConf", params,function (data, code, msg) {
            var test = 1;
            if (code == 200) {
                $scope.infoDatas = data.infoDatas;
                $scope.$apply();
            }
            else {
                alert(code + "-" + msg);
            }
        }, function () {
            alert("ajax error");
        });
    }

    /**
     * 提交接口请求
     */
    $scope.addPlcInfosetting_submit = function () {
        var params = {
            plc_id: $("#plc_id").val(),
            device_id: $("#device_id").val(),
            type: $("#type").val(),
            driver: $("#driver").val(),
            box_stat_no: $("#box_stat_no").val(),
            plc_stat_no: $("#plc_stat_no").val(),
            port: $("#port").val(),
            comtype: $("#comtype").val(),
            baudrate: $("#baudrate").val(),
            stop_bit: $("#stop_bit").val(),

            data_length: $("#data_length").val(),
            check_bit: $("#check_bit").val(),
            retry_times: $("#retry_times").val(),
            wait_timeout: $("#wait_timeout").val(),
            rev_timeout: $("#rev_timeout").val(),
            com_stepinterval: $("#com_stepinterval").val(),
            com_iodelaytime: $("#com_iodelaytime").val(),
            retry_timeout: $("#retry_timeout").val(),
            net_port: $("#net_port").val(),
            net_type: $("#net_type").val(),
            net_isbroadcast: $("#net_isbroadcast").val(),
            net_broadcastaddr: $("#net_broadcastaddr").val(),
            net_ipaddr: $("#net_ipaddr").val(),
            state: $("#state").val()
        };

        if (params.device_id == "" || params.type == "" || params.driver == "" || params.box_stat_no == "" || params.plc_stat_no == "" || params.port == "" || params.comtype == "" || params.baudrate == "" || params.stop_bit == "" || params.data_length == "" ||
            params.check_bit == "" || params.retry_times == "" || params.wait_timeout == "" || params.rev_timeout == "" || params.com_stepinterval == "" || params.com_iodelaytime == "" || params.retry_timeout == "" || params.net_port == "" || params.net_type == "" ||
            params.net_isbroadcast == "" || params.net_broadcastaddr == "" || params.net_ipaddr == "" || params.state == "") {
            alert("配置参数填未填写");
            return;

            if(isValidIP(params.net_ipaddr)!=true)
            {
                alert("情输入正确的IP地址");
            }
            return;
        }

        /*
        * 当plc_id 不等于0
        * 对plc_id的通讯口就行修改编辑
        * */
        if($("#plc_id").val()!=0)
        {
            T.common.ajax.request("WeconBox", "plcInfoAction/updataPlcInfo", params, function (data, code, msg) {
                if (code == 200) {
//                    console.log(data);
                    alert("配置修改成功");
                    $("#addConfig").modal("hide");
                }
                else {
                    alert(code + "-" + msg);
                }
            }, function () {
                alert("ajax error");
            });

        }

        T.common.ajax.request("WeconBox", "plcInfoAction/addPlcInfo", params, function (data, code, msg) {
            if (code == 200) {
//                    console.log(data);
                alert("添加配置成功");

                $("#addConfig").modal("hide");
            }
            else {
                alert(code + "-" + msg);
            }
        }, function () {
            alert("ajax error");
        });
    };

    /*
    * 设置 点击的plc_id
    * 用于区别是添加通讯口配置还是修改通讯口配置
    * */
    $scope.setplc_id=function(plc_id)
    {
        $("#plc_id").value=plc_id;
        alert("plc_id设置成功");
    }

    /*
    * 进行输入框IP地址验证
    * */


    function isValidIP(ip) {
        var reg = /^(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])$/
        return reg.test(ip);
    }


});
