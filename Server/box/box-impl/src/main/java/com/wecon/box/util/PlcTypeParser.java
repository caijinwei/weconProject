package com.wecon.box.util;

import com.wecon.box.entity.plcdom.AddrDom;
import com.wecon.box.entity.plcdom.Plc;
import com.wecon.box.entity.plcdom.Res;
import org.dom4j.Document;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by whp on 2017/8/19.
 */

public class PlcTypeParser {
    //private static Logger logger = LoggerFactory.getLogger(PlcTypeParser.class);

    /**
     * 解析plcType文件中的数据填充到po
     */
    public static void doParse() {
        if (PlcTypeCache.localPlcTypeData != null && PlcTypeCache.localPlcTypeData.size() > 0) {
            return;
        }
        try {
            //logger.debug("parse begin");
            Document doc = DOMUtil.getXMLByFilePath("/PLCType.plc");
            Element root = doc.getRootElement();
            /** 解析plc节点*/
            List<Element> plcElementList = root.elements("plc");
            if (null == plcElementList) {
                return;
            }
            List<Plc> plcList = new ArrayList<Plc>();
            for (Element plcElement : plcElementList) {
                Plc plc = new Plc();
                //设置plc标签属性
                plc.setAttributes(plcElement.attributes());
                /** 解析plc下一级addr节点*/
                List<Element> addrElementList = plcElement.elements();
                if (null == addrElementList) {
                    continue;
                }
                for (Element addrElement : addrElementList) {
                    AddrDom addrDom = new AddrDom();
                    //设置addr标签属性
                    addrDom.setAttributes(addrElement.attributes());
                    /** 解析res节点*/
                    List<Element> resElementList = addrElement.elements("res");
                    if (null != resElementList) {
                        for (Element resElement : resElementList) {
                            Res res = new Res();
                            //设置res标签属性
                            res.setAttributes(resElement.attributes());
                            addrDom.getResList().add(res);
                        }
                    }
                    plc.getAddrs().put(addrElement.getName(), addrDom);
                }
                plcList.add(plc);
            }
            //logger.debug("parse finish", "total："+plcList.size());
            PlcTypeCache.localPlcTypeData = plcList;
        } catch (Exception e) {
            //logger.error("parse fail", e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] arg) {
//        doParse();
//       // List<String> result = PlcTypeQuerier.getInstance().queryValuesByKey("plctype");
//
//
//           List<Plc> result1= PlcTypeQuerier.getInstance().query("plctype", "");
//        System.out.print(result1);
////
////            for(Plc plc:result){
////                Map<String, AddrDom> a=plc.getAddrs();
////
////                AddrDom b=a.get("bitaddr");
////                System.out.println(b);
//
////        	List<Res> sa=b.getResList();
////        	Map<String, String> ss=sa.get(0).getAttributes();
////        	String gg=ss.get("Rid");
////        	System.out.println(gg);
//
//
////        }
////        System.out.print(result);

        List<Integer> points=new ArrayList<Integer>();
        points.add(1);
        points.add(2);
        StringBuffer pointIds=new StringBuffer("");

        for(Integer i:points)
        {
            pointIds.append(i+",");
        }
        System.out.print(pointIds.substring(0,pointIds.length()-1));

    }
}
