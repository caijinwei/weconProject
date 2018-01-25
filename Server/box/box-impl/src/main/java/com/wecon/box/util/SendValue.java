package com.wecon.box.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import com.alibaba.fastjson.JSONObject;
import com.wecon.box.api.DeviceApi;
import com.wecon.box.api.RealHisCfgApi;
import com.wecon.box.api.RedisPiBoxApi;
import com.wecon.box.entity.Device;
import com.wecon.box.entity.PiBoxCom;
import com.wecon.box.entity.PiBoxComAddr;
import com.wecon.box.entity.RealHisCfg;
import com.wecon.box.entity.RedisPiBoxActData;
import com.wecon.box.enums.OpTypeOption;
import com.wecon.box.enums.ResTypeOption;
import com.wecon.common.util.CommonUtils;

/**
 * @author lanpenghui 2017年10月14日上午10:34:54
 */
@Component
public class SendValue {
    @Autowired
    private RedisPiBoxApi redisPiBoxApi;
    @Autowired
    private RealHisCfgApi realHisCfgApi;
    @Autowired
    private DeviceApi deviceApi;
    @Autowired
    protected DbLogUtil dbLogUtil;
    private static final Logger logger = LogManager.getLogger(SendValue.class.getName());

    public void putMQTTMess(String value, WebSocketSession session, String addr_id, OpTypeOption op_type, ClientMQTT reclient) {
        putMQTTMess(value, session, addr_id, op_type, reclient, false);
    }

    /**
     * @param value
     * @param session
     * @param addr_id
     * @param op_type
     * @param reclient
     * @param justSendMsg 如果只是通过mqtt发消息，不做长连接，务必传值true
     */
    public void putMQTTMess(String value, WebSocketSession session, String addr_id, OpTypeOption op_type, ClientMQTT reclient, boolean justSendMsg) {
        try {
            if (!CommonUtils.isNullOrEmpty(addr_id)) {
                RealHisCfg realHisCfg = realHisCfgApi.getRealHisCfg(Long.parseLong(addr_id));
                if (realHisCfg != null) {
                    Device device = deviceApi.getDevice(realHisCfg.device_id);
                    if (device != null) {

                        PiBoxComAddr addr1 = new PiBoxComAddr();
                        addr1.addr_id = addr_id;
                        addr1.value = value;
                        List<PiBoxCom> operate_data_list = new ArrayList<PiBoxCom>();
                        PiBoxCom piBoxCom = new PiBoxCom();
                        List<PiBoxComAddr> piBoxComAddrs = new ArrayList<PiBoxComAddr>();
                        piBoxCom.com = String.valueOf(realHisCfg.plc_id);
                        piBoxComAddrs.add(addr1);
                        piBoxCom.addr_list = piBoxComAddrs;
                        operate_data_list.add(piBoxCom);

                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("act", 2000);
                        jsonObject.put("machine_code", device.machine_code);
                        JSONObject oplistObject = new JSONObject();
                        oplistObject.put("operate_data_list", operate_data_list);
                        jsonObject.put("data", oplistObject);
                        jsonObject.put("feedback", 1);
                        String message = jsonObject.toJSONString();
                        logger.debug("put mqtt : " + message);
                        String topic = "pibox/stc/" + device.machine_code;

//						ClientMQTT reclient = new ClientMQTT(topic, "send" + session.getId());
//						reclient.start();
                        // <editor-fold desc="获取旧实时数据，操作日志">
                        try {
                            PiBoxComAddr addr1old = new PiBoxComAddr();
                            RedisPiBoxActData actData = redisPiBoxApi.getRedisPiBoxActData(device.machine_code);
                            if (actData != null && actData.act_time_data_list != null) {
                                for (PiBoxCom com : actData.act_time_data_list) {
                                    if (com.com != null && com.com.equals(piBoxCom.com) && com.addr_list != null) {
                                        for (PiBoxComAddr addr : com.addr_list) {
                                            if (addr.addr_id != null && addr.addr_id.equals(addr_id)) {
                                                addr1old.addr_id = addr_id;
                                                addr1old.value = addr.value;
                                                addr1old.state = addr.state;
                                                break;
                                            }
                                        }
                                        break;
                                    }
                                }
                            }
                            System.out.println(1);
                            System.out.println(addr1old == null);
                            System.out.println(addr1 == null);
                            dbLogUtil.updOperateLog(op_type, ResTypeOption.Act, Long.valueOf(addr_id),
                                    addr1old, addr1);
                        } catch (Exception exLog) {
                            logger.error(exLog);
                        }
                        // </editor-fold>

                        reclient.publish(topic, message);

                        if (justSendMsg) {
                            reclient.close();
                        }
                    }
                }

            }
        } catch (Exception e) {
            logger.error(e);
        } finally {
            if (justSendMsg && reclient != null) {
                try {
                    reclient.close();
                } catch (Exception ex) {
                    logger.error(ex);
                }

            }
        }

    }

}
