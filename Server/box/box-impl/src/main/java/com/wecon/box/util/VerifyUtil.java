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
}
