package com.rainvice.gochat.utils;

import androidx.annotation.NonNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class StrZipUtil {

    /**
     * @param input 须要压缩的字符串
     * @return 压缩后的字符串
     * @throws IOException IO
     */
    public static byte[] compress(@NonNull String input) throws IOException {
        if (input.length() == 0) {
            return input.getBytes();
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzipOs = new GZIPOutputStream(out);
        gzipOs.write(input.getBytes());
        gzipOs.close();
        return out.toByteArray();
    }

    /**
     * @param input 须要压缩的字符串
     * @return 压缩后的字符串
     * @throws IOException IO
     */
    public static byte[] compress(@NonNull byte[] input) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzipOs = new GZIPOutputStream(out);
        gzipOs.write(input);
        gzipOs.close();
        return out.toByteArray();
    }

    /**
     * @param zippedStr 压缩后的字符串
     * @return 解压缩后的
     * @throws IOException IO
     */
    public static byte[] uncompressA(@NonNull byte[] zippedStr) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(zippedStr);
        GZIPInputStream gzipIs = new GZIPInputStream(in);
        byte[] buffer = new byte[256];
        int n;
        while ((n = gzipIs.read(buffer)) >= 0) {
            out.write(buffer, 0, n);
        }
        // toString()应用平台默认编码，也能够显式的指定如toString("GBK")
        return out.toByteArray();
    }


    /**
     * @param zippedStr 压缩后的字符串
     * @return 解压缩后的
     * @throws IOException IO
     */
    public static String uncompress(@NonNull byte[] zippedStr) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(zippedStr);
        GZIPInputStream gzipIs = new GZIPInputStream(in);
        byte[] buffer = new byte[256];
        int n;
        while ((n = gzipIs.read(buffer)) >= 0) {
            out.write(buffer, 0, n);
        }
        // toString()应用平台默认编码，也能够显式的指定如toString("GBK")
        return out.toString();
    }
}