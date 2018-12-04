package com.wuwenze.wechatsmallapptmplmsg.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author wwz
 * @version 1 (2018/8/16)
 * @since Java7
 */
public class SecurityUtil {
    /**
     * sha1加密
     *
     * @param str toke+timestamp+nonce
     * @return
     */
    public static String getSha1(String str) {
        try {
            StringBuffer sb = new StringBuffer();
            MessageDigest md = MessageDigest.getInstance("sha1");
            md.update(str.getBytes());

            byte[] msg = md.digest();
            for (byte b : msg) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}
