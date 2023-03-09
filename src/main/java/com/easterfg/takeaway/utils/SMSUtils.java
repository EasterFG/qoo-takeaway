package com.easterfg.takeaway.utils;

import com.google.common.base.Strings;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

/**
 * @author EasterFG on 2022/11/23
 */
public class SMSUtils {
    private static Random random;

    static {
        try {
            random = SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private SMSUtils() {
    }

    /**
     * 生成6位验证码
     */
    public static String generateValidateCode() {
        int code = random.nextInt(999999);
        String str = String.valueOf(code);
        if (str.length() < 6) {
            return Strings.repeat("0", (6 - str.length()));
        }
        return str;
    }

}
