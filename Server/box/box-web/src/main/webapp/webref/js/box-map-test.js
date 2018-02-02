var appModule = angular.module('weconweb', []);
appModule.controller("infoController", function ($scope, $http, $compile) {
    $scope.ifIndex = false;
    $scope.boxsGroup = [];
    $scope.deviceDatas = [];
    //查询盒子列表
    $scope.findBoxsList = function() {
        var selAlarm = T.common.util.getParameter("selAlarm");
        var params = {};
        if(null != selAlarm) {
            params = {selAlarm: selAlarm};
        }

        // var data = "{\"code\":200,\"msg\":\"ok\",\"reqId\":\"1517541491260\",\"result\":{\"list\":[{\"groupName\":\"默认组\",\"boxList\":[{\"machineCode\":\"137001170928113b5d817101102\",\"devModel\":\"VBOX\",\"isAlarm\":0,\"boxName\":\"1102\",\"remark\":\"\",\"state\":\"0\",\"map\":\"118.464668,26.061996\",\"boxId\":\"2\"},{\"machineCode\":\"V02001171018005027a249d4b9f\",\"devModel\":\"V-BOX S-4G\",\"isAlarm\":0,\"boxName\":\"4b9f\",\"remark\":\"\",\"state\":\"0\",\"map\":\"119.328766,25.949253\",\"boxId\":\"3\"},{\"machineCode\":\"V02001171023997d2db2a945f29\",\"devModel\":\"V-BOX S-4G\",\"isAlarm\":1,\"boxName\":\"1111\",\"remark\":\"\",\"state\":\"0\",\"map\":\"119.23448,26.053167\",\"boxId\":\"8\"},{\"machineCode\":\"V02001171119999d2db2a945cbf\",\"devModel\":\"V-BOX S-4G\",\"isAlarm\":0,\"boxName\":\"1119\",\"remark\":null,\"state\":\"0\",\"map\":null,\"boxId\":\"20\"},{\"machineCode\":\"V02001171130990d2db2a94421f\",\"devModel\":\"V-BOX S-4G\",\"isAlarm\":0,\"boxName\":\"4421f\",\"remark\":null,\"state\":\"0\",\"map\":null,\"boxId\":\"35\"},{\"machineCode\":\"1870011603240285dd4dc845X0013\",\"devModel\":\"VBOX\",\"isAlarm\":1,\"boxName\":\"模拟1\",\"remark\":null,\"state\":\"0\",\"map\":null,\"boxId\":\"36\"},{\"machineCode\":\"1870011603240285dd4dc845X0014\",\"devModel\":\"VBOX\",\"isAlarm\":0,\"boxName\":\"模拟2\",\"remark\":null,\"state\":\"0\",\"map\":null,\"boxId\":\"37\"},{\"machineCode\":\"3870011603240285dd4dc845X0013\",\"devModel\":\"VBOX\",\"isAlarm\":0,\"boxName\":\"123\",\"remark\":\"\",\"state\":\"1\",\"map\":\"119.307396,26.079561\",\"boxId\":\"40\"},{\"machineCode\":\"3870011603240285dd4dc845X0099\",\"devModel\":\"VBOX\",\"isAlarm\":0,\"boxName\":\"test1\",\"remark\":null,\"state\":\"0\",\"map\":null,\"boxId\":\"41\"},{\"machineCode\":\"3870011603240285dd4dc845X0115\",\"devModel\":\"VBOX\",\"isAlarm\":0,\"boxName\":\"x0115\",\"remark\":null,\"state\":\"0\",\"map\":null,\"boxId\":\"49\"},{\"machineCode\":\"3870011603240285dd4dc845X0116\",\"devModel\":\"VBOX\",\"isAlarm\":0,\"boxName\":\"X0116\",\"remark\":null,\"state\":\"0\",\"map\":null,\"boxId\":\"50\"},{\"machineCode\":\"3870011603240285dd4dc845X0117\",\"devModel\":\"VBOX\",\"isAlarm\":0,\"boxName\":\"X0117\",\"remark\":null,\"state\":\"0\",\"map\":null,\"boxId\":\"51\"},{\"machineCode\":\"3870011603240285dd4dc845X0118\",\"devModel\":\"VBOX\",\"isAlarm\":0,\"boxName\":\"X0118\",\"remark\":null,\"state\":\"0\",\"map\":null,\"boxId\":\"52\"},{\"machineCode\":\"3870011603240285dd4dc845X0119\",\"devModel\":\"VBOX\",\"isAlarm\":0,\"boxName\":\"X0119\",\"remark\":null,\"state\":\"0\",\"map\":null,\"boxId\":\"53\"},{\"machineCode\":\"3870011603240285dd4dc845X0120\",\"devModel\":\"VBOX\",\"isAlarm\":0,\"boxName\":\"X0120\",\"remark\":null,\"state\":\"0\",\"map\":null,\"boxId\":\"54\"},{\"machineCode\":\"3870011603240285dd4dc845X0121\",\"devModel\":\"VBOX\",\"isAlarm\":0,\"boxName\":\"X0121\",\"remark\":null,\"state\":\"0\",\"map\":null,\"boxId\":\"55\"},{\"machineCode\":\"3870011603240285dd4dc845X0122\",\"devModel\":\"VBOX\",\"isAlarm\":0,\"boxName\":\"X0122\",\"remark\":null,\"state\":\"0\",\"map\":null,\"boxId\":\"56\"},{\"machineCode\":\"3870011603240285dd4dc845X0123\",\"devModel\":\"VBOX\",\"isAlarm\":0,\"boxName\":\"X0123\",\"remark\":null,\"state\":\"0\",\"map\":null,\"boxId\":\"57\"},{\"machineCode\":\"3870011603240285dd4dc845X0124\",\"devModel\":\"VBOX\",\"isAlarm\":0,\"boxName\":\"X0124\",\"remark\":null,\"state\":\"0\",\"map\":null,\"boxId\":\"58\"},{\"machineCode\":\"3870011603240285dd4dc845X0299\",\"devModel\":\"VBOX\",\"isAlarm\":0,\"boxName\":\"X0299\",\"remark\":null,\"state\":\"0\",\"map\":null,\"boxId\":\"59\"}]},{\"groupName\":\"新分组1\",\"boxList\":[]}]}}";
        // var result = JSON.parse(data);
        // $scope.boxsGroup = result.result.list;
        // $scope.$apply();
        // $scope.initMap();
        T.common.ajax.request("WeconBox",
            "data/boxs", params, function(
                data, code, msg) {
                if (code == 200) {
                    $scope.boxsGroup = data.list;
                    $scope.$apply();
                    $scope.initMap();
                } else {
                    alert(code + "-" + msg);
                }
            }, function() {
                alert("ajax error");
            });
    }

    //初始化地图
    $scope.initMap = function() {
        function myFun(result){
            var cityName = result.name;
            map.setCenter(cityName);
        }
        var mapStr = T.common.util.getParameter("map");
        // alert(mapStr);
        //初始化地图
        if(mapStr != null && mapStr !="") {
            $scope.mapArray = mapStr.split(",");
            var mPoint = new BMap.Point($scope.mapArray[0],$scope.mapArray[1]);
            map.centerAndZoom(mPoint, 12) //标注当前位置
        }else{
            var point = new BMap.Point(116.331398,39.897445);
            map.centerAndZoom(point,12);
            var myCity = new BMap.LocalCity();
            myCity.get(myFun);
        }
        // var geolocation = new BMap.Geolocation();
        // geolocation.getCurrentPosition(function(r) {
        //     if(this.getStatus() == BMAP_STATUS_SUCCESS) {
        //         var mk = new BMap.Marker(r.point);
        //         if(mapStr != null && mapStr !="") {
        //             $scope.mapArray = mapStr.split(",");
        //             var mPoint = new BMap.Point($scope.mapArray[0],$scope.mapArray[1]);
        //             map.centerAndZoom(mPoint, 12) //标注当前位置
        //         }else{
        //             map.centerAndZoom(r.point, 12) //标注当前位置
        //         }
        //     } else {
        //         alert('failed' + this.getStatus());
        //     }
        // }, {
        //     enableHighAccuracy: true
        // })
        //产生搜索框选择框
        $scope.createSearch();
        var marker;
        console.log("将要产生标注点");
        $scope.showalarm = T.common.util.getParameter("showalarm");
        if($scope.showalarm == 1){
            $(".devicetype").val("0");
        }angular.forEach($scope.boxsGroup, function(value, key) {
            angular.forEach(value.boxList, function(box, boxkey) {
                var isAlarm = box.isAlarm;
                if($scope.showalarm == 1){
                    if(isAlarm == 0){
                        return true;
                    }
                }
                var positionStr = box.map;
                var boxName = box.boxName;
                var boxId = box.boxId;
                var state = box.state;
                console.log("盒子的位置信息：" + positionStr);
                if(positionStr != null) {
                    $scope.positions = positionStr.split(",");
                    if($scope.positions.length == 2) {
                        console.log("经度：" + $scope.positions[0] + "纬度：" + $scope.positions[1]);
                        var boxTag = new BMap.Point($scope.positions[0], $scope.positions[1]);
                        var label = new BMap.Label(boxName, {
                            offset: new BMap.Size(23, -10)
                        });
                        label.setStyle({
                            fontSize: "14px",
                            backgroundColor: "white",
                            position:"relative",
                            border:"1px solid red",
                            padding:"5px",
                        });
                        var myIcon = new BMap.Icon("../img/"+(1==state?"biaozhu3Dgreen.png":"biaozhu3Dgrey.png"), new BMap.Size(23, 33),{
                            anchor: new BMap.Size(12,23)
                        });
                        marker = new BMap.Marker(boxTag, {
                            icon: myIcon
                        });
                        map.addOverlay(marker);
                        marker.setLabel(label);
                        //文字描述信息
                        var sContent =
                            "<table class='table boxInfo'><thead><tr><th>" +value.groupName+
                            "</th><th></th></tr></thead>"+
                            "<tbody><tr><td>盒子名称</td><td>"+box.boxName+"</td></tr></tbody>"+
                            "<tbody><tr><td>机器码</td><td>"+box.machineCode+"</td></tr></tbody>"+
                            "<tbody><tr><td>设备型号</td><td>"+box.devModel+"</td></tr></tbody>"+"</table>"+
                            "<button class='btn btn-primary dim' onclick='javascript:window.top.redirect(\"" +
                            "webref/html/boxtitle.html?type=0&device_id="+boxId+"&device_name="+boxName+"\")'>数据监控</button>"+
                            "<button class='btn btn-warning dim' onclick='javascript:window.top.redirect(\"" +
                            "webref/html/boxtitle.html?type=1&device_id="+boxId+"&device_name="+boxName+"\")'>报警记录</button>"+
                            "<button class='btn btn-success dim' onclick='javascript:window.top.redirect(\"" +
                            "webref/html/boxtitle.html?type=2&device_id="+boxId+"&device_name="+boxName+"\")'>历史数据</button>"+
                            "<button class='btn btn-info dim' onclick='javascript:window.top.redirect(\"" +
                            "webref/html/boxtitle.html?type=3&device_id="+boxId+"&device_name="+boxName+"\")'>基本配置</button>";
                        var infoWindow = new BMap.InfoWindow(sContent);  // 创建信息窗口对象
                        marker.addEventListener('click', function() {
                            this.openInfoWindow(infoWindow);
                            //图片加载完毕重绘infowindow
                        });

                        //报警跳动
                        if(isAlarm){
                            marker.setAnimation(BMAP_ANIMATION_BOUNCE);
                        }
                    }
                }
            })
        })

        console.log("已经产生标注点");
        map.addEventListener("tilesloaded",function(){
            // $("#loader-wrapper").css("display","none");
        });
    }

    $scope.createSearch = function(){
        //return;
        function ZoomControl(){
            // 默认停靠位置和偏移量
            this.defaultAnchor = BMAP_ANCHOR_TOP_LEFT;
            this.defaultOffset = new BMap.Size(10, 22);
        }
        // 通过JavaScript的prototype属性继承于BMap.Control
        ZoomControl.prototype = new BMap.Control();
        // 自定义控件必须实现自己的initialize方法,并且将控件的DOM元素返回
        // 在本方法中创建个div元素作为控件的容器,并将其添加到地图容器中
        ZoomControl.prototype.initialize = function(map){
            // 创建一个DOM元素
            var div = document.createElement("div");
            div.id="searchbox";
            // 添加DOM元素到地图中
            map.getContainer().appendChild(div);
            // 将DOM元素返回
            return div;
        }
        // 创建控件
        var myZoomCtrl = new ZoomControl();
        // 添加到地图当中
        map.addControl(myZoomCtrl);
        var searchbox = document.getElementById("searchbox");
        searchbox.innerHTML = "<div class='input-group'><input type='text' id='boxid' class='form-control' placeholder='搜索：V-BOX名称'><span class='input-group-btn'><button type='button' class='btn btn-primary' onclick='search()'><i class='glyphicon glyphicon-search'></i></button></span></div><select class='form-control devicetype'><option value='1'>所有设备</option><option value='0'>报警设备</option></select><div id='searchResultPanel' style='width:160px;height:auto;max-height:400px;overflow-y: scroll;display:none;'></div>";

        //选择框重新加载页面事件
        $(".devicetype").bind("change",function(){
            var dataname = $(this).val();
            if(dataname == 1){
                window.location.href="box-map-test.html?showalarm=0";
            }else{
                window.location.href="box-map-test.html?showalarm=1";
            }
        });


        // //搜索框的数据改变事件
        $('#boxid').bind('input propertychange', function() {
            $("#searchResultPanel").css("display","block");
            var searchResultPanel = document.getElementById("searchResultPanel");
            strHTML = "";
            var that = $(this);
            angular.forEach($scope.boxsGroup, function(value, key) {
                angular.forEach(value.boxList, function(box, boxkey) {
                    var positionStr = box.map;
                    var boxName = box.boxName;
                    var isAlarm = box.isAlarm;
                    if($scope.showalarm == 1){
                        if(isAlarm == 0){
                            return true;
                        }
                    }
                    if(that.val() != "" && boxName.indexOf(that.val())>-1){
                        strHTML += "<li class='searchitem'><i class='glyphicon glyphicon-search'></i><span id='name'>"+boxName+"</span><span id='position'>"+positionStr+"</span></li>";
                    }
                })
            })

            searchResultPanel.innerHTML = "<ul>"+strHTML+"</ul>";
        });

        // //点击搜索框列表的事件
        $(document).on("click","#searchResultPanel ul li.searchitem", function() {
            var searchtext =  $(this).children("#name").text();
            var positionStr =  $(this).children("#position").text();
            var positions = positionStr.split(",");
            moveCenter(positions[0],positions[1]);
            $("#searchResultPanel").css("display","none");
            $('#boxid').val("");
        })
        //
        // //点击搜索按钮触发的事件
        search = function(){
            var name = $('#boxid').val();
            locationonfocus(name);
        }
        //
        // //搜索点
        function locationonfocus(name){
            var ifExist = false;
            angular.forEach($scope.boxsGroup, function(value, key) {
                angular.forEach(value.boxList, function(box, boxkey) {
                    var positionStr = box.map;
                    var boxName = box.boxName;
                    var isAlarm = box.isAlarm;
                    if($scope.showalarm == 1){
                        if(ifAlarm == 0){
                            return true;
                        }
                    }
                    if(boxName == name){
                        $("#searchResultPanel").css("display","none");
                        $('#boxid').val("");
                        var positions = positionStr.split(",");
                        moveCenter(positions[0],positions[1]);
                        ifExist = true;
                        return true;
                    }
                })

            })
            if(!ifExist) {
                alert("未找到您搜索的盒子");
            }
        }
        //
        // //移动中心点
        function moveCenter(lng,lat){
            var new_point = new BMap.Point(lng,lat);  // 创建点坐标
            map.panTo(new_point);
        }
    }
    var map = new BMap.Map("allmap");
    map.enableScrollWheelZoom(); //激活滚轮调整大小功能
    $scope.findBoxsList(); //查询盒子列表

})