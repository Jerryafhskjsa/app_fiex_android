package com.black.net;

import android.util.Log;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class DownLoadObserver implements Observer<DownloadInfo> {
    protected Disposable d;//可以用于取消注册的监听者
    protected DownloadInfo downloadInfo;
    private DownloadListener downloadListener;

    public DownLoadObserver(DownloadListener downloadListener) {
        this.downloadListener = downloadListener;
    }

    @Override
    public void onSubscribe(Disposable d) {
        this.d = d;
        if (downloadListener != null) {
            downloadListener.onStart();
        }
    }

    @Override
    public void onNext(DownloadInfo downloadInfo) {
        this.downloadInfo = downloadInfo;
        if (downloadListener != null && downloadInfo != null) {
            downloadListener.onProgress(downloadInfo.currentLength, downloadInfo.totalLength);
        }
    }

    @Override
    public void onError(Throwable e) {
        if (downloadListener != null) {
            downloadListener.onFailure();
        }
    }

    @Override
    public void onComplete() {
        if (downloadListener != null && downloadInfo != null) {
            downloadListener.onFinish(downloadInfo.file);
        }
    }
}