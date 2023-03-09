package com.easterfg.takeaway.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author EasterFG on 2022/9/28
 */
public class ImageUtils {

    private ImageUtils() {
    }

    /**
     * 允许的文件类型
     */
    private static final String JPG_FILE_HEAD = "ffd8ff";
    private static final String PNG_FILE_HEAD = "89504e";

    public static boolean isImage(MultipartFile file) {
        byte[] data;
        try {
            data = file.getBytes();
        } catch (IOException e) {
            return false;
        }
        if (data.length < 6) {
            return false;
        }
        // 读取前3个字节
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            int v = data[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                builder.append(0);
            }
            builder.append(hv);
        }
        String head = builder.toString();
        return JPG_FILE_HEAD.equals(head) || PNG_FILE_HEAD.contains(head);
    }


}
