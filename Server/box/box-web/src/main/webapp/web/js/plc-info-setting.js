var appModule = angular.module('weconweb', []);
appModule.controller("infoController", function ($scope, $window, $http, $location, $compile) {
    $scope.onInit = function () {
        $scope.showPlcList();
        $scope.showPlcSetDefault();
    }
    /*
     * plc配置展示
     * */
    $scope.showPlcList = function () {
        $scope.device_id = T.common.util.getParameter("device_id");
        $("#device_id").val($scope.device_id);
        var params = {device_id: $("#device_id").val()};
        T.common.ajax.request("WeconBox", "plcInfoAction/showAllPlcConf", params, function (data, code, msg) {
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
    /*
     * 盒子下plc配置展示
     * */
    $scope.showAllPlcConf = function () {
        $scope.device_id = T.common.util.getParameter("device_id");
        $("#device_id").val($scope.device_id);
        var params = {device_id: $("#device_id").val()};
        T.common.ajax.request("WeconBox", "plcInfoAction/showAllPlcConf", params, function (data, code, msg) {
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
        alert(params);
        if (params.device_id == "" || params.type == "" || params.driver == "" || params.box_stat_no == "" || params.plc_stat_no == "" || params.port == "" || params.retry_times == ""
            || params.wait_timeout == "" || params.rev_timeout == "" || params.com_stepinterval == "" || params.com_iodelaytime == "" || params.retry_timeout == "") {
            alert("配置参数填未填写");
            return;
        }
        if ($scope.selectedPort == 'Ethernet') {
            if (params.net_port == "" || params.net_type == "" || params.net_isbroadcast == "" || params.net_broadcastaddr == "" || params.net_ipaddr == "" || params.state == "") {
                alert("配置参数填未填写");
                return;
                if (isValidIP(params.net_ipaddr) != true) {
                    alert("情输入正确的IP地址");
                    return;
                }
            }
        } else if ($scope.selectedPort == 'COM1' || $scope.selectedPort == 'COM2') {
            params.net_port = "0";
            params.net_type = "0";
            params.net_isbroadcast = "0";
            params.net_broadcastaddr = "0";
            params.net_ipaddr = "0";
            params.state = "0";
            if (params.baudrate == "" || params.stop_bit == "" || params.data_length == "" || params.check_bit == "" || params.comtype == "") {
                alert("配置参数填未填写");
                return;
            }
        }
        /*
         * 当plc_id 不等于0
         * 对plc_id的通讯口就行修改编辑
         * */
        if ($("#plc_id").val() != 0) {
            T.common.ajax.request("WeconBox", "plcInfoAction/updataPlcInfo", params, function (data, code, msg) {
                if (code == 200) {
//                    console.log(data);
                    alert("配置修改成功");
                    $("#addConfig").modal("hide");
                    $scope.showPlcList();
                    $scope.$apply();
                }
                else {
                    alert(code + "-" + msg);
                }
            }, function () {
                alert("ajax error");
            });
        } else {
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
        }
        ;
    }
    /*
     * 设置 点击的plc_id
     * 用于区别是添加通讯口配置还是修改通讯口配置
     * */
    $scope.setplc_id = function (plc_id) {
        $("#plc_id").value = plc_id;
        alert("plc_id设置成功");
    }

    $scope.showPlcSetDefault = function () {
        var params = {};
        T.common.ajax.request("WeconBox", "plcInfoAction/showPlcSetDefault", params, function (data, code, msg) {
            if (code == 200) {
                //$scope.usbDeviceList = data.usbDeviceList;
                //$scope.ethernetList = data.ethernetList;
                $scope.usbDeviceMapListByType = data.usbDeviceMapListByType;
                $scope.ethernetMapListByType = data.ethernetMapListByType;

                $scope.usbDeviceMapListByPtype = data.usbDeviceMapListByPtype;
                $scope.ethernetMapListByPtype = data.ethernetMapListByPtype;
                $scope.usbDeviceMapListKeyByPtype = data.usbDeviceMapListKeyByPtype;
                $scope.ethernetMapListKeyByPtype = data.ethernetMapListKeyByPtype;

                $scope.comMapListKeyByPtype = data.comMapListKeyByPtype;
                $scope.comMapListByPtype = data.comMapListByPtype;
                $scope.comMapListByType = data.comMapListByType;

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
     * 修改 port通讯协议   以后改 switch
     * */
    $scope.chgPort = function () {
        if ($scope.selectedPort == "USB") {
            $scope.temPtype = $scope.usbDeviceMapListByPtype;
            $scope.pTypeSet = $scope.usbDeviceMapListKeyByPtype;
            $scope.temType = $scope.usbDeviceMapListByType;
        } else if ($scope.selectedPort == "Ethernet") {
            $scope.temPtype = $scope.ethernetMapListByPtype;
            $scope.pTypeSet = $scope.ethernetMapListKeyByPtype;
            $scope.temType = $scope.ethernetMapListByType;
        } else {
            $scope.temPtype = $scope.comMapListByPtype;
            $scope.pTypeSet = $scope.comMapListKeyByPtype;
            $scope.temType = $scope.comMapListByType;
        }
    }
    /*
     * 修改设备类型
     * */
    $scope.chgPtype = function () {
        var temPtype = $scope.temPtype;
        $scope.pType = temPtype[$scope.selectedPtype];
    }
    /*
     * 修改驱动名称
     * */
    $scope.chgType = function () {
        var temType = $scope.temType;
        $scope.type = temType[$scope.selectedType];
        var type = temType[$scope.selectedType];
        console.log("选取中的type:", $scope.type)

        $('#comtype').attr('value', $scope.comtype);

        $('#box_stat_no').attr('value', type[0].box_stat_no);
        $('#plc_stat_no').attr('value', type[0].plc_stat_no);
        $('#retry_times').attr('value', type[0].retry_times);
        $('#wait_timeout').attr('value', type[0].wait_timeout);
        $('#rev_timeout').attr('value', type[0].rev_timeout);


        if (type[0].port == 'Ethernet') {
            $scope.ethernetShow = 1;
            $scope.portIfShow = 0;
            //$scope.$apply();
            /*
             * 以太网配置
             * */
            /*
             * 	public int net_port;
             public int net_type;
             public int net_isbroadcast;
             public int net_broadcastaddr;
             public String net_ipaddr;
             public int state;
             * */
            $('#net_ipaddr').attr('value', type[0].net_ipaddr);
            $('#net_type').attr('value', type[0].net_port);
            $('#net_port').attr('value', type[0].net_port);
            $('#net_isbroadcast').attr('value', type[0].net_isbroadcast);
            $('#net_broadcastaddr').attr('value', type[0].net_broadcastaddr);

        } else if (type[0].port == 'USB') {
            $scope.portIfShow = 0;
            $scope.ethernetShow = 0;
        } else {
            $scope.portIfShow = 1;
            $scope.ethernetShow = 0;
        }
    }
    /*
     * 进行输入框IP地址验证
     * */
    function isValidIP(ip) {
        var reg = /^(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])$/
        return reg.test(ip);
    }

    /*
     * 是否使用广播地址
     * */
    $scope.isBroadcast = function () {
        if ($('#net_isbroadcast').is(':checked')) {
            $('#net_broadcastaddr').attr("disabled", "disabled");
            //$('#net_broadcastaddr').css('','');
        } else {
            $('#net_broadcastaddr').removeAttr('disabled');
        }
    }

});
