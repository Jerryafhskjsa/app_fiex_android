package com.black.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtil {
    public static void copyFile(String dest, String src) throws IOException {
        if (dest == null || src == null) {
            return;
        }
        FileOutputStream fos = new FileOutputStream(dest);
        FileInputStream fis = new FileInputStream(src);
        byte[] buf = new byte[1024];
        int i = 0;
        while ((i = fis.read(buf)) != -1) {
            fos.write(buf, 0, i);
        }
        fis.close();
        fos.close();
    }

    public static void copyFile(File dest, File src) throws IOException {
        if (dest == null || src == null) {
            return;
        }
        FileOutputStream fos = new FileOutputStream(dest);
        FileInputStream fis = new FileInputStream(src);
        byte[] buf = new byte[1024];
        int i = 0;
        while ((i = fis.read(buf)) != -1) {
            fos.write(buf, 0, i);
        }
        fis.close();
        fos.close();
    }
}
