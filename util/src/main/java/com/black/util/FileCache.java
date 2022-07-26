package com.black.util;

import android.content.Context;

import java.io.File;

public class FileCache {
    private File cacheDir;

    public FileCache(Context context) {
        // 找一个用来缓存图片的路径
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
            cacheDir = new File(android.os.Environment.getExternalStorageDirectory(), "fbsex");
        else
            cacheDir = context.getCacheDir();
        if (!cacheDir.exists())
            cacheDir.mkdirs();
    }

    public File getFile(String url) {
        String filename = String.valueOf(url.hashCode());
        File f = new File(cacheDir, filename);
        return f;
    }

    public String getFilePath(String url) {
        if (url == null || url.equals("")) {
            return "";
        }
        String filename = String.valueOf(url.hashCode());
        return cacheDir + "/" + filename;
    }

    public void clear() {
        File[] files = cacheDir.listFiles();
        if (files == null)
            return;
        for (File f : files)
            f.delete();
    }

}
