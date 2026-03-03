package com.artdesign.backend.util;

import org.springframework.util.DigestUtils;

/**
 * MD5 加密工具类
 */
public class MD5Util {

    /**
     * 对字符串进行 MD5 加密
     * 
     * @param str 原始字符串
     * @return 加密后的 32 位十六进制字符串
     */
    public static String encrypt(String str) {
        if (str == null) {
            return null;
        }
        return DigestUtils.md5DigestAsHex(str.getBytes());
    }
}
