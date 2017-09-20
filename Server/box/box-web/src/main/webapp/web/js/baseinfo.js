/**
 * Created by caijinw on 2017/8/8.
 */
var appModule = angular.module('weconweb', []);
appModule.controller("infoController", function ($scope, $http, $compile) {
    /*
     * --------------------------------------------------------基本信息------------------------------------------------------------------------------
     * */
    /*
     * 盒子基本信息展示
     * */
    $scope.onInit = function () {
        $("#loadingModal").modal("hide");
        $scope.device_id = T.common.util.getParameter("device_id");
        $scope.device_name = T.common.util.getParameter("device_name");
        $("#loadingModal").modal("show");
        $scope.showBaseInfo();
        $scope.showPlcSetDefault();
        $("#loadingModal").modal("hide");
    }
    $scope.showBaseInfo = function () {
        $scope.device_id = T.common.util.getParameter("device_id");
        var params = {device_id: $scope.device_id};
        T.common.ajax.request("WeconBox", "baseInfoAction/showBaseInfo", params, function (data, code, msg) {
            if (code == 200) {
                $scope.infoData = data.device;

                //固件更新时候要用到
                $scope.dev_model = data.device.dev_model;
                $scope.accounttype = data.userType;
                var map = data.device.map;
                if (map != "" && map != null) {
                    var maps = map.split(",");
                    $scope.map_a = maps[0];
                    $scope.map_o = maps[1];
                }
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
     * 点击刷新  更新用户的state状态
     * */
    $scope.flushState = function () {
        $scope.infoData.state = "";
        $scope.device_id = T.common.util.getParameter("device_id");
        var params = {device_id: $scope.device_id};
        T.common.ajax.request("WeconBox", "baseInfoAction/showBaseInfo", params, function (data, code, msg) {
            if (code == 200) {
                $scope.infoData.state = data.device.state;
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
     * 盒子与账户解除关联
     * */
    $scope.deletePIBoxBtn = function (device_id) {
        var parmas =
        {
            device_id: device_id
        }
        T.common.ajax.request("WeconBox", "baseInfoAction/deletePIBox", parmas, function (data, code, msg) {
            if (code == 200) {
                $("#deletePIBox").modal('hide');
                alert("解除绑定成功！");
                if (self != top) {
                    window.parent.reloadBoxList();
                }
                window.location.href = "overview.html";
            }
            else {
                alert(code + "-" + msg);
            }
        }, function () {
            alert("ajax error");
        });
    }
    /*
     * --------------------------------------------------------通讯口配置------------------------------------------------------------------------------
     * */
    /*
     * 盒子下plc配置展示
     * */
    $scope.initPlcSetting = function () {
        $scope.showPlcList();
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
     * 修改时候 单个plc展示
     * 根据plc_id展示plc
     * */
    $scope.showPlcInfoById = function (plc_id) {
        var params = {plc_id: plc_id};
        T.common.ajax.request("WeconBox", "plcInfoAction/findPlcInfoById", params, function (data, code, msg) {
            if (code == 200) {
                $scope.plcInfoById = data.plcInfo;
                $scope.selectedPort = $scope.plcInfoById.port;
                $("#port").val($scope.plcInfoById.port);
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
                var temType = $scope.plcInfoById.type;
                $scope.$apply();
                $scope.selectedPtype = $scope.plcInfoById.ptype;
                /*
                 * 修改Ptype
                 * */
                $scope.selectedPtype = $scope.temType[temType][0].ptype;
                var TemPtype = $scope.selectedPtype;
                $scope.pType = $scope.temPtype[TemPtype];
                $scope.$apply();

                if ($scope.plcInfoById.port == 'Ethernet') {
                    $scope.ethernetShow = 1;
                    $scope.portIfShow = 0;
                    $('#net_ipaddr').val($scope.plcInfoById.net_ipaddr);
                    $('#net_type').val($scope.plcInfoById.net_type);
                    $('#net_port').val($scope.plcInfoById.net_port);
                    $('#net_isbroadcast').val($scope.plcInfoById.net_isbroadcast);
                    $('#net_broadcastaddr').val($scope.plcInfoById.net_broadcastaddr);
                    $("#state").val($scope.plcInfoById.state);
                } else if ($scope.plcInfoById.port == 'COM1' || $scope.plcInfoById.port == 'COM2') {
                    $scope.ethernetShow = 0;
                    $scope.portIfShow = 1;
                    $scope.$apply();
                    $("#comtype").val($scope.plcInfoById.comtype);
                    $("#baudrate").val($scope.plcInfoById.baudrate);
                    $("#stop_bit").val($scope.plcInfoById.stop_bit);
                    $("#data_length").val($scope.plcInfoById.data_length);
                    $("#check_bit").val($scope.plcInfoById.check_bit);
                    $scope.$apply();
                } else {
                    $scope.ethernetShow = 0;
                    $scope.portIfShow = 0;
                }
                $("#driver").val($scope.plcInfoById.driver);
                $("#retry_times").val($scope.plcInfoById.retry_times);
                $("#wait_timeout").val($scope.plcInfoById.wait_timeout);
                $("#rev_timeout").val($scope.plcInfoById.rev_timeout);
                $("#com_stepinterval").val($scope.plcInfoById.com_stepinterval);
                $("#com_iodelaytime").val($scope.plcInfoById.com_iodelaytime);
                $("#retry_timeout").val($scope.plcInfoById.retry_timeout);
                $('#box_stat_no').val($scope.plcInfoById.box_stat_no);
                $('#plc_stat_no').val($scope.plcInfoById.plc_stat_no);
                //$('#retry_times').val($scope.plcInfoById.retry_times);
                $scope.selectedType = temType;
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
            state: "1",
            driver: $('#driver').val()
        };
        var selectport = $("#port").val();
        if (params.device_id == "" || params.type == "" || params.driver == "" || params.box_stat_no == "" || params.plc_stat_no == "" || params.port == "" || params.retry_times == ""
            || params.wait_timeout == "" || params.rev_timeout == "" || params.com_stepinterval == "" || params.com_iodelaytime == "" || params.retry_timeout == "") {
            alert("配置参数填未填写");
            return;
        }
        if (selectport == 'Ethernet') {
            if (params.net_port == "" || params.net_type == "" || params.net_isbroadcast == "" || params.net_broadcastaddr == "" || params.net_ipaddr == "") {
                alert("配置参数填未填写");
                return;
                if (isValidIP(params.net_ipaddr) != true) {
                    alert("情输入正确的IP地址");
                    return;
                }
            }
            params.comtype = "0";
            params.baudrate = "0";
            params.stop_bit = "0";
            params.data_length = "0";
            params.check_bit = "0";
        } else if (selectport == 'COM1' || selectport == 'COM2') {
            params.net_port = "0";
            params.net_type = "0";
            params.net_isbroadcast = "0";
            params.net_broadcastaddr = "0";
            params.net_ipaddr = "0";
            //params.state = "0";
            if (params.baudrate == "" || params.stop_bit == "" || params.data_length == "" || params.check_bit == "" || params.comtype == "") {
                alert("配置参数填未填写");
                return;
            }
        } else {
            params.net_port = "0";
            params.net_type = "0";
            params.net_isbroadcast = "0";
            params.net_broadcastaddr = "0";
            params.net_ipaddr = "0";
            params.state = "1";
            params.comtype = "0";
            params.baudrate = "0";
            params.stop_bit = "0";
            params.data_length = "0";
            params.check_bit = "0";
        }
        /*
         * 当plc_id 不等于0
         * 对plc_id的通讯口就行修改编辑
         * */
        T.common.ajax.request("WeconBox", "plcInfoAction/savePlcInfo", params, function (data, code, msg) {
            if (code == 200) {
                alert("操作成功");
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
    }
    /*
     * 设置 点击的plc_id
     * 用于区别是添加通讯口配置还是修改通讯口配置
     * */
    $scope.setplc_id = function (plc_id) {
        $("#plc_id").val(plc_id);
        $scope.showPlcInfoById(plc_id);
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
     * 修改 port通讯协议
     * 以后改 switch
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
        $scope.selectedPtype = "";
        $scope.selectedType = "";
    }
    /*
     * 修改设备类型
     * */
    $scope.chgPtype = function () {
        $scope.selectedType = "";
        var temPtype = $scope.temPtype;
        $scope.pType = temPtype[$scope.selectedPtype];
    }
    /*
     * 修改驱动名称
     * */
    $scope.chgType = function () {
        //if ($scope.selectedPort == "USB") {
        //    $scope.temPtype = $scope.usbDeviceMapListByPtype;
        //    $scope.pTypeSet = $scope.usbDeviceMapListKeyByPtype;
        //    $scope.temType = $scope.usbDeviceMapListByType;
        //
        //} else if ($scope.selectedPort == "Ethernet") {
        //    $scope.temPtype = $scope.ethernetMapListByPtype;
        //    $scope.pTypeSet = $scope.ethernetMapListKeyByPtype;
        //    $scope.temType = $scope.ethernetMapListByType;
        //} else {
        //    $scope.temPtype = $scope.comMapListByPtype;
        //    $scope.pTypeSet = $scope.comMapListKeyByPtype;
        //    $scope.temType = $scope.comMapListByType;
        //}
        var temType = $scope.temType;
        $scope.type = temType[$scope.selectedType];
        var type = temType[$scope.selectedType];
        $('#comtype').val(type[0].comtype);
        $('#box_stat_no').val(type[0].box_stat_no);
        $('#plc_stat_no').val(type[0].plc_stat_no);
        console.log("获取到默认的对象", type[0]);
        $('#retry_times').val(type[0].retry_times);
        $('#wait_timeout').val(type[0].wait_timeout);
        $('#rev_timeout').val(type[0].rev_timeout);
        $('#driver').val(type[0].driver);
        $('#retry_timeout').val(type[0].retry_timeout);
        $('#com_iodelaytime').val("0");
        $('#com_stepinterval').val("0");
        if ($('#port').val() == 'Ethernet') {
            $scope.portIfShow = 0;
            $scope.ethernetShow = 1;
            $('#net_ipaddr').val(type[0].net_ipaddr);
            $('#net_type').val(type[0].net_type);
            $('#net_port').val(type[0].net_port);
            $('#net_isbroadcast').val(type[0].net_isbroadcast);
            $('#net_broadcastaddr').val(type[0].net_broadcastaddr);
        } else if ($('#port').val() == 'COM1' || $('#port').val() == "COM2") {
            $scope.portIfShow = 1;
            $scope.ethernetShow = 0;
            $('#baudrate').val(type[0].baudrate);
            $('#stop_bit').val(type[0].stop_bit);
            $('#data_length').val(type[0].data_length);
            $('#check_bit').val(type[0].check_bit);
        } else {
            $scope.portIfShow = 0;
            $scope.ethernetShow = 0;
        }

    }
    $scope.chgPiboxInFoName = function (device_id) {
        var piBoxName = $('#PIBoxName').val();
        var remark = $("#remark").val();
        var map_a = $("#map_a").val();
        var map_o = $("#map_o").val();
        if (isNaN(map_a) || isNaN(map_o)) {
            alert("地图坐标格式错误");
            return;
        }
        var map = $("#map_a").val() + "," + $("#map_a").val();
        var params = {
            deviceId: device_id,
            piBoxName: piBoxName,
            remark: remark,
            map: map
        }
        T.common.ajax.request("WeconBox", "baseInfoAction/chgPiboxInFoName", params, function (data, code, msg) {
            if (code == 200) {
                alert("保存成功！");
                //$scope.$apply();
            }
            else {
                alert(code + "-" + msg);
            }
        }, function () {
            alert("ajax error");
        });
    };
    /*
     * 解绑plc
     * */
    $scope.deletePlc = function (plc_id) {
        $scope.deletePlc_id = "";
        $scope.deletePlc_id = plc_id;
    }
    $scope.unbundledPlc = function (plc_id) {
        var params = {plc_id: plc_id}
        T.common.ajax.request("WeconBox", "plcInfoAction/unbundledPlc", params, function (data, code, msg) {
            if (code == 200) {
                $("#deletePlc").modal("hide");
                $scope.showPlcList();
                $scope.$apply();
            }
            else {
                $("#deletePlc").modal("hide");
                alert(code + "-" + msg);
            }
        }, function () {
            alert("ajax error");
        });

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
    $scope.cleanInput = function () {
        $("#addConfig").modal("show");
        clearForm($('#addConfig'));
        $("#port").val("COM1");
        $scope.chgPort();
    };
    function clearForm(form) {
        $("input[type!='hidden'][name!='device_id']", form).each(function () {
            var type = this.type;
            //var tag = this.tagName.toLowerCase();
            if (type == 'text' || type == 'number') {
                this.value = "";
                $('#plc_id').val("0");
            }
        });
        $("select", form).each(function () {
            this.selectedIndex = -1;
        });
    };

    //--------------------------------------------------------------------debugInfo上报调试信息-------------------------------------------------------------------------------------------------------------------------------
    var ws;

    $scope.ws_send = function (msg) {
        ws.send(msg);
    }
    $scope.ws_close = function (state) {
        if (state != 1) {
            alert("盒子已经离线");
            return;
        }
        ws.close();
        ws = "";
    }
    $scope.ws_log = function (data) {
        $('#wsLog').prepend('<p>' + data + '</p>');
    }
    $scope.ws_clear = function () {
        $("#wsLog").empty();
        $scope.ws_log("Clear :)");
    }
    $scope.ws_connect = function (machine_code) {
        if ("WebSocket" in window) {
            ws = new WebSocket(T.common.requestUrl['WeconBoxWs'] + 'debugInfo-websocket/websocket?' + T.common.websocket.getParams());
            ws.onopen = function () {
                $scope.ws_send(machine_code);
            };
            ws.onmessage = function (evt) {
                $scope.ws_log(evt.data);
            };
            ws.onclose = function (evt) {
                $scope.ws_log('>>>关闭获取调试' + " ");
                console.log(evt);
            };
            ws.onerror = function (evt) {
                $scope.ws_log('>>>error' + " " + evt.data);
                console.log(evt);
            };
        } else {
            alert("WebSocket isn't supported by your Browser!");
        }
    }
    $scope.openGetDeviceDebugInfo = function (machine_code, state) {
        if (state != 1) {
            alert("盒子已经离线");
            return;
        }
        if (ws == "" || ws == undefined) {
            $scope.ws_connect(machine_code);
        }
    };

    //--------------------------------------------------------------------------固件信息------------------------------------------------------------------------------------------------------------

    $scope.dev_firmShow = function (state) {
        var device_id = T.common.util.getParameter("device_id");
        var params = {
            device_id: device_id
        }
        T.common.ajax.request("WeconBox", "dirFirmAction/getDirFirmInfoByDevId", params, function (data, code, msg) {
            if (code == 200) {
                if(data.devFirmInfo!=null) {
                    $scope.devFirmInfo = data.devFirmInfo;
                    console.log("固件对象", data.devFirmInfo);
                    //固件更新要用到
                    $scope.localVersionCode = data.devFirmInfo.f_ver;
                    $scope.$apply();
                }
            }
            else {
                $("#deletePlc").modal("hide");
                alert(code + "-" + msg);
            }
        }, function () {
            alert("ajax error");
        });
    }

    //检查更新
    $scope.checkUpdate= function () {
        var localVersionCode = $scope.localVersionCode;
        var dev_model = $scope.dev_model;
        if (dev_model == "" || dev_model == undefined || localVersionCode == "" || localVersionCode == undefined) {
            alert("系统错误");
            return;
        }
        var device_id = $scope.device_id;
        if (device_id == "" || device_id == undefined) {
            alert("系统忙，稍后操作");
            return;
        }
        var params = {
            device_id: device_id,
            dev_model: dev_model,
            localVersionCode: localVersionCode
        }
        T.common.ajax.request("WeconBox", "driveraction/checkUpdate", params, function (data, code, msg) {
            if (code == 200) {
                $("#checkUpdateFir").modal("show");
                $scope.driverIsUpdate=data.driverData.isUpdate;
                $scope.firmIsUpdate=data.firmData.isNewVersion;

                //提示信息
                if ($scope.driverIsUpdate||$scope.firmIsUpdate) {
                    //用于展示提示内容
                    $("#noticeDiv").empty();
                    $("#noticeDiv").prepend('<div>确定更新</div>');
                }else{
                    $("#noticeDiv").empty();
                    $("#noticeDiv").prepend('<div>已经是最新版了，不需要更新</div>');
                }
                if($scope.firmIsUpdate){
                    $scope.file_id = data.firmData.file_id;
                    $scope.fileData = data.firmData.fileData;
                }
                console.log("结果是：   ",data);
            }
            else {
                alert(code + "-" + msg);
            }
        }, function () {
            alert("ajax error");
        });
    }
    //更新操作
    $scope.update= function ()
    {
        var device_id = $scope.device_id;
        var updateType=0;
        if($scope.driverIsUpdate&&$scope.firmIsUpdate){
            updateType=3;
        }else if($scope.driverIsUpdate){
            updateType=1;
        }else if($scope.firmIsUpdate){
            updateType=2
        }else{
            $("#checkUpdateFir").modal("hide");
            return;
        }
        var fileData = $scope.fileData;
        var versionName = fileData.version_name;
        var version_code = fileData.version_code;
        var file_id = fileData.file_id;
        var params = {
            updateType:updateType,
            device_id: device_id,
            versionName: versionName,
            version_code: version_code,
            file_id: file_id
        }
        console.log("参数是：",params);
        T.common.ajax.request("WeconBox", "driveraction/update", params, function (data, code, msg) {
            if (code == 200) {
                alert("指令已下发盒子成功！");
                $("#checkUpdateFir").modal("hide");
            }
            else {
                alert(code + "-" + msg);
            }
        }, function () {
            alert("ajax error");
        });
    }

    //检查更新所有驱动文件
    $scope.chkUpdateDriver = function () {
        var device_id = $scope.device_id;
        if (device_id == "" || device_id == undefined) {
            alert("系统忙，稍后操作");
            return;
        }
        var params = {
            device_id: device_id
        }
        T.common.ajax.request("WeconBox", "driveraction/checkUpdatePlcDriver", params, function (data, code, msg) {
            if (code == 200) {
                $("#checkUpdateFir").modal("show");
                if (data.isUpdate) {
                    //用于展示提示内容
                    $("#noticeDiv").empty();
                    $("#noticeDiv").prepend('<div>确定更新</div>');
                }else{
                    $("#noticeDiv").empty();
                    $("#noticeDiv").prepend('<div>已经是最新版了，不需要更新</div>');
                }
            }
            else {
                alert(code + "-" + msg);
            }
        }, function () {
            alert("ajax error");
        });
    }
    //更新 plc全部驱动
    $scope.updateAllDriver= function (machine_code) {
        var device_id = $scope.device_id;
        if (device_id == "" || device_id == undefined) {
            alert("系统忙，稍后操作");
            return;
        }
        var params = {
            device_id: device_id,
            machine_code:machine_code
        }
        T.common.ajax.request("WeconBox", "driveraction/updateAllDriver", params, function (data, code, msg) {
            if (code == 200) {
                alert("指令已下发盒子成功！");
            }
            else {
                alert(code + "-" + msg);
            }
        }, function () {
            alert("ajax error");
        });
    }

    //检查更新驱动文件
    $scope.checkUpdateFir = function () {
        var localVersionCode = $scope.localVersionCode;
        var dev_model = $scope.dev_model;
        if (dev_model == "" || dev_model == undefined || localVersionCode == "" || localVersionCode == undefined) {
            alert("系统错误");
            return;
        }
        var params = {
            dev_model: dev_model,
            localVersionCode: localVersionCode
        }
        T.common.ajax.request("WeconBox", "dirFirmAction/getLatestFirmVersion", params, function (data, code, msg) {
            if (code == 200) {
                $("#checkUpdateFir").modal("show");
                if (data.isNewVersion) {
                    //用于展示提示内容
                    $("#noticeDiv").empty();
                    $("#noticeDiv").prepend('<div>最新版本:' + data.versionCode + '</div>');


                    $scope.file_id = data.file_id;
                    $scope.fileData = data.fileData;
                } else {
                    //用于展示提示内容
                    $("#noticeDiv").empty();
                    $("#noticeDiv").prepend('<div>已经是最新版了，不需要更新</div>');
                }
            }
            else {
                $("#deletePlc").modal("hide");
                alert(code + "-" + msg);
            }
        }, function () {
            alert("ajax error");
        });
    }
    $scope.updateFirFile = function (device_id) {
        var fileData = $scope.fileData;
        var versionName = fileData.version_name;
        var version_code = fileData.version_code;
        var file_id = fileData.file_id;
        var params = {
            versionName: versionName,
            version_code: version_code,
            file_id: file_id,
            device_id: device_id
        }
        if (machine_code == "" || machine_code == undefined || versionName == "" || versionName == undefined || version_code == "" || version_code == undefined || file_id == "" || file_id == undefined) {
            $("#checkUpdateFir").modal("hide");
            alert("系统忙，稍后操作！");
        }
        T.common.ajax.request("WeconBox", "dirFirmAction/updateFirmFile", params, function (data, code, msg) {
            $("#checkUpdateFir").modal("hide");
            alert("指令已下发盒子成功！");
        }, function () {
            alert("ajax error");
        });
    }

});

