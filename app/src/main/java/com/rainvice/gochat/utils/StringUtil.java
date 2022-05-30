package com.rainvice.gochat.utils;

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
    /**
     * 去掉byte[]中填充的0
     * @param buffer
     * @return
     */
    public static byte[] byteTobyte(byte[] buffer) {
        int length = 0;
        for (int i = 0; i < buffer.length; i++) {
            if (buffer[i] == 0) {
                length = i;
                break;
            }
        }
        byte[] bytes = new byte[buffer.length];
        System.arraycopy(buffer,0,bytes,0,length);
        return bytes;
    }
}
