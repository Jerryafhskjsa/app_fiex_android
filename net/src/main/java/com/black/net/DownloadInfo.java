package com.black.net;

import java.io.File;

public class DownloadInfo {
    static final long TOTAL_ERROR = 0;
    public String url;
    public File file;
    long currentLength;
    long totalLength;

    DownloadInfo(String url, File file) {
        this.url = url;
        this.file = file;
    }

    public void setProgress(long progress) {
        currentLength = progress;
    }

    public File getFile() {
        return file;
    }
}
