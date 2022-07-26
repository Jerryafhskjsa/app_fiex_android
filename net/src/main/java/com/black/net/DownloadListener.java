package com.black.net;

import java.io.File;

public interface DownloadListener {
    void onStart();

    void onProgress(long current, long total);

    void onFinish(File file);

    void onFailure();
}
