/**
 * Created by lanph on 2018/01/24.
 */
var appModule = angular.module('weconweb', []);
appModule
		.controller(
				"listController",
				function($scope, $http, $compile) {
					$scope.onInit = function() {
						$scope.deviceid = T.common.util
						.getParameter("device_id");
						$scope.deviceName = T.common.util.getParameter("device_name");
						$scope.url = T.common.util.getParameter("type");
						switch($scope.url){
							case '1':
								$("#myiframe").attr("src","datamonitor.html?device_id="+$scope.deviceid);
								$("div.menu-item ul li").eq($scope.url).children("a").addClass("active");
								break;
                            case '2':
                            	$("#myiframe").attr("src","alarmlog.html?device_id="+$scope.deviceid);
                                $("div.menu-item ul li").eq($scope.url).children("a").addClass("active");
                                break;
                            case '3':
                            	$("#myiframe").attr("src","historydata.html?device_id="+$scope.deviceid);
                                $("div.menu-item ul li").eq($scope.url).children("a").addClass("active");
                                break;
                            case '4':
                            	$("#myiframe").attr("src","baseconfig.html?device_id="+$scope.deviceid);
                                $("div.menu-item ul li").eq($scope.url).children("a").addClass("active");
                                break;
							default:
								$("#myiframe").attr("src","datamonitor.html?device_id="+$scope.deviceid);
                                $("div.menu-item ul li").eq(0).children("a").addClass("active");
                                break;
						}

					}

					
					
			
				});