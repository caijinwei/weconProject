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
        var mapStr = T.common.util.getParameter("map");
        //初始化地图
        var geolocation = new BMap.Geolocation();
        geolocation.getCurrentPosition(function(r) {
            if(this.getStatus() == BMAP_STATUS_SUCCESS) {
                var mk = new BMap.Marker(r.point);
                if(mapStr != null && mapStr !="") {
                    $scope.mapArray = mapStr.split(",");
                    var mPoint = new BMap.Point($scope.mapArray[0],$scope.mapArray[1]);
                    map.centerAndZoom(mPoint, 12) //标注当前位置
                }else{
                    map.centerAndZoom(r.point, 12) //标注当前位置
                }
            } else {
                alert('failed' + this.getStatus());
            }
        }, {
            enableHighAccuracy: true
        })
        //产生搜索框
        $scope.createSearch();
        var marker;
        console.log("将要产生标注点");
        angular.forEach($scope.boxsGroup, function(value, key) {
            angular.forEach(value.boxList, function(box, boxkey) {
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
                            offset: new BMap.Size(20, -10)
                        });
                        label.setStyle({
                            fontSize: "14px",
                            backgroundColor: "white",
                            position:"relative",
                            border:"0",
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
                            "<h4 style='margin:0 0 5px 0;padding:0.2em 0'>富昌维控</h4>" +
                            "<img style='float:right;margin:4px' id='imgDemo' src='../img/wecon.png' width='139' height='104' title='天安门'/>" +
                            "<p style='margin:0;line-height:1.5;font-size:13px;text-indent:2em'>维控坐落在中国福州市中心,故宫的南侧,与天安门广场隔长安街相望,是清朝皇城的大门...</p>" +
                            "<button onclick='javascript:window.top.redirect(\"http://localhost:8080/box-web/webref/html/boxtitle.html?device_id="+boxId+"&device_name="+boxName+"\")'>跳转</button>"+"</div>";
                        var infoWindow = new BMap.InfoWindow(sContent);  // 创建信息窗口对象
                        // marker.addEventListener('click', function() {
                        //     $state.go("box", {
                        //         box: angular.toJson(box)
                        //     });
                        // });

                        marker.addEventListener('click', function() {
                            this.openInfoWindow(infoWindow);
                            //图片加载完毕重绘infowindow
                            document.getElementById('imgDemo').onload = function () {
                                infoWindow.redraw();   //防止在网速较慢，图片未加载时，生成的信息框高度比图片的总高度小，导致图片部分被隐藏
                            }
                        });
                    }
                }
            })
        })
        console.log("已经产生标注点");
    }

    $scope.createSearch = function(){
        //return;
        function ZoomControl(){
            // 默认停靠位置和偏移量
            this.defaultAnchor = BMAP_ANCHOR_TOP_LEFT;
            this.defaultOffset = new BMap.Size(10, 52);
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
        searchbox.innerHTML = "<div class='input-group'><input type='text' id='boxid' class='form-control' placeholder='请输入搜索内容'><span class='input-group-btn'><button type='button' class='btn btn-default' onclick='search()'><i class='glyphicon glyphicon-search'></i></button></span></div><div id='searchResultPanel' style='width:142px;height:auto;display:none;'></div>";

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
                    if(that.val() != "" && boxName.indexOf(that.val())>-1){
                        strHTML += "<li class='searchitem'><i class='glyphicon glyphicon-search'></i><span id='name'>"+boxName+"</span><span id='position'>"+positionStr+"</span></li>";
                    }
                })
            })

            searchResultPanel.innerHTML = "<ul>"+strHTML+"</ul>";
        });
        //
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
        // search = function(){
        //     var name = $('#boxid').val();
        //     locationonfocus(name,"btn");
        // }
        //
        // //搜索点
        // function locationonfocus(name,type){
        //     for(i = 0; i < boxdata.length; i++){
        //         if(type == "btn"){
        //             if(boxdata[i].name == name){
        //                 $("#searchResultPanel").css("display","none");
        //                 $('#boxid').val("");
        //                 moveCenter(boxdata[i].lng,boxdata[i].lat);
        //                 break;
        //             }
        //         }
        //         else{
        //             if(boxdata[i].name.indexOf(name)>-1){
        //                 moveCenter(boxdata[i].lng,boxdata[i].lat);
        //                 break;
        //             }
        //         }
        //     }
        //     if(i == boxdata.length){
        //         alert("未找到您搜索的盒子");
        //     }
        // }
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