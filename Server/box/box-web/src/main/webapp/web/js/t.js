/**
 * Created by Administrator on 2017/7/19.
 */
var appModule = angular.module('weconweb', []);
appModule.controller("infoController", function ($scope, $http, $compile) {
    $scope.onInit = function () {
        $scope.paginationConf = {
            currentPage: 1,
            itemsPerPage: 2,
            totalItems: $scope.count,
            pagesLength: 15,
            perPageOptions: [5, 10, 20, 50, 100],
            //rememberPerPage: 'perPageItems',
            onChange: function () {
                if (this.currentPage != 0) {
                    $scope.getList(this.currentPage, this.itemsPerPage);
                }
            }
        }

        $scope.paginationConf2 = {
            currentPage: 1,
            itemsPerPage: 10,
            totalItems: $scope.count,
            pagesLength: 15,
            perPageOptions: [5, 10, 20, 50, 100],
            //rememberPerPage: 'perPageItems',
            onChange: function () {
                if (this.currentPage != 0) {
                    $scope.getList2(this.currentPage, this.itemsPerPage);
                }
            }
        }
    }

    $scope.paginationConf = {
        totalItems: $scope.count,
    }

    $scope.getList = function (pageIndex, pageSize) {
        if (pageIndex == 0)
            pageIndex = 1;
        $scope.paginationConf.totalItems = 15;
        var list = new Array();
        ;
        for (var i = 0; i < pageSize; i++) {
            var item = {id: i, name: "11111-name-" + i + ",pageIndex=" + pageIndex};
            list.push(item);
        }
        $scope.pushlist = list;
    }

    $scope.paginationConf2 = {
        totalItems: $scope.count,
    }

    $scope.getList2 = function (pageIndex, pageSize) {
        if (pageIndex == 0)
            pageIndex = 1;
        $scope.paginationConf2.totalItems = 15;
        var list = new Array();
        ;
        for (var i = 0; i < pageSize; i++) {
            var item = {id: i, name: "22222-name-" + i + ",pageIndex=" + pageIndex};
            list.push(item);
        }
        $scope.pushlist2 = list;
    }

    /**
     * 提交接口请求
     */
    $scope.act_submit = function () {
        var params = $.parseJSON($("#act_params").val());
        T.common.ajax.request("WeconBox", $("#act_url").val(), params, function (data, code, msg) {
            if (code == 200) {
//                    console.log(data);
                $("#act_ret").val(JSON.stringify(data, null, '\t'));
            }
            else {
                alert(code + "-" + msg);
            }
        }, function () {
            alert("ajax error");
        });
    }

    var sock;
    $scope.ws_connect = function () {
        console.log(1);
        sock = new SockJS(T.common.requestUrl['WeconBox'] + 'actdata-websocket/');
        sock.onopen = function () {
            $scope.ws_log('>>>open');
        };

        sock.onmessage = function (e) {
            $scope.ws_log('server message ->' + e.data);
        };

        sock.onclose = function () {
            $scope.ws_log('>>>close');
        };
        console.log(2);
    }
    $scope.ws_send = function () {
        sock.send($("#wsMsg").val());
        $scope.ws_log('client message ->' + $("#wsMsg").val());
    }
    $scope.ws_close = function () {
        sock.close();
    }
    $scope.ws_clear = function () {
        $("#wsLog").empty();
        $scope.ws_log("Clear :)");
    }
    $scope.ws_log = function (data) {
        $('#wsLog').prepend('<p>' + data + '</p>');
    }

    var ws;
    $scope.ws_connect2 = function () {
        if ("WebSocket" in window) {
            alert("WebSocket is supported by your Browser!");
        }
        ws = new WebSocket("ws://localhost:8080/wecon-box/api/" + 'actdata-websocket/');
        ws.onopen = function () {
            $scope.ws_log('>>>open');
        };
        ws.onmessage = function (evt) {
            $scope.ws_log('server message ->' + evt.data);
        };
        ws.onclose = function (evt) {
            $scope.ws_log('>>>close' + " " + evt.data);
            console.log(evt);
        };
        ws.onerror = function (evt) {
            $scope.ws_log('>>>error' + " " + evt.data);
            console.log(evt);
        };
    }
    $scope.ws_send2 = function () {
        ws.send($("#wsMsg").val());
        $scope.ws_log('client message ->' + $("#wsMsg").val());
    }
    $scope.ws_close2 = function () {
        s
        ws.close();
    }
})