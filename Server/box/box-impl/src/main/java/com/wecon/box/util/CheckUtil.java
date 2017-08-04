package com.wecon.box.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zengzhipeng on 2017/8/2.
 */
public class CheckUtil {
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
}
