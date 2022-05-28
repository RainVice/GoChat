package com.rainvice.sockettest_1.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class StringUtil {

    /**
     * 去掉byte[]中填充的0 转为String
     * @param buffer
     * @return
     */
    public static String byteToStr(byte[] buffer) {
        int length = 0;
        for (int i = 0; i < buffer.length; i++) {
            if (buffer[i] == 0) {
                length = i;
                break;
            }
        }
        return new String(buffer, 0, length, StandardCharsets.UTF_8);
    }
}
