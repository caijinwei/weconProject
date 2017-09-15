package com.wecon.box.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zengzhipeng on 2017/8/2.
 */
public class VerifyUtil {
    protected static Pattern regexValidEmail = Pattern.compile("^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$");

    /**
     * 验证邮箱地址地址是否为有效的
     *
     * @param email 邮箱地址
     * @return 有效返回true；无效地址返回false
     */
    public static boolean isValidEmail(String email) {
        Matcher matcher = regexValidEmail.matcher(email);
        return matcher.matches();
    }

    /**
     * 中国手机号码
     *
     * @param str
     * @return
     */
    public static boolean isChinaPhone(String str) {
        Pattern p = null;
        Matcher m = null;
        boolean b = false;
        p = Pattern.compile("^[1][3,4,5,7,8][0-9]{9}$"); // 验证手机号
        m = p.matcher(str);
        b = m.matches();
        return b;
    }

    /*
    *
    * */
    public static String getVersionNum(String version) {
//        version = "sdsd2V4.0.0-P:A8 ";

        String result = "";
        //分割 前面的“-”
        Integer index = version.indexOf("-");
        String s = version.substring(0, index);
        String tempV=version.substring(0, index);

        //提取版本号
        String REGEX = "[[0-9]+\\.]+[0-9]+";
        Pattern p = Pattern.compile(REGEX);
        Matcher m = p.matcher(tempV);
        while (m.find()) {
            result = (m.group());
        }
        System.out.println(result);
        return  result;
    }

    /*
    * localVsersion 本地版本
    * serverVsersion 服务器上面的版本
    *
    * return
    *        1)true
    *        serrverVersion > localVersion
    *        需要更新
    *
    *        2）flase
    *        serverVersion <= localVersion
    *        不需要更新
   * */
    public static boolean isNewVersion(String localVersion, String serverVersion) {
        if (localVersion.equals(serverVersion)) {
            return false;
        }
        String[] localArray = localVersion.split("\\.");
        String[] onlineArray = serverVersion.split("\\.");

        int length = localArray.length < onlineArray.length ? localArray.length : onlineArray.length;

        for (int i = 0; i < length; i++) {
            if (Integer.parseInt(onlineArray[i]) > Integer.parseInt(localArray[i])) {
                return true;
            } else if (Integer.parseInt(onlineArray[i]) < Integer.parseInt(localArray[i])) {
                return false;
            }
            // 相等 比较下一组值
        }

        return true;
    }
    public static void main(String[] args){
        System.out.println(isNewVersion(getVersionNum("sdsd2V4.0.0-P:A8"),getVersionNum("sdsd2V4.12.0-P:A8")));
    }
}
