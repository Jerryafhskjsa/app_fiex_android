package com.black.net;

import android.os.Handler;
import android.os.SystemClock;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadManager {
    private static final int DEFAULT_TIME_OUT = 15;//超时时间5s
    private static final int DEFAULT_READ_TIME_OUT = 200;//读取时间
    private static final int DEFAULT_WRITE_TIME_OUT = 15;//读取时间
    private static final long CALLBACK_MIN_TIME = 50;//进度回调最小时间 ms，防止主线程阻塞
    private static final AtomicReference<DownloadManager> INSTANCE = new AtomicReference<>();
    private HashMap<String, Call> downCalls;//用来存放各个下载的请求
    private OkHttpClient mClient;//OKHttpClient;
    private Handler handler = new Handler();

    //获得一个单例类
    public static DownloadManager getInstance(String cachePath) {
        for (; ; ) {
            DownloadManager current = INSTANCE.get();
            if (current != null) {
                return current;
            }
            current = new DownloadManager(cachePath);
            if (INSTANCE.compareAndSet(null, current)) {
                return current;
            }
        }
    }

    private DownloadManager(String cachePath) {
        downCalls = new HashMap<>();
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(DEFAULT_TIME_OUT, TimeUnit.SECONDS);
        builder.readTimeout(DEFAULT_READ_TIME_OUT, TimeUnit.SECONDS);
        builder.writeTimeout(DEFAULT_WRITE_TIME_OUT, TimeUnit.SECONDS);
        builder.cache(new Cache(new File(cachePath), 1024 * 1024 * 10));
        mClient = builder.build();
    }

    /**
     * 开始下载
     *
     * @param url 下载请求的网址
     */
    public void download(String url, File file, DownloadListener downloadListener) {
        Observable.just(url)
                .filter(s -> !downCalls.containsKey(s))//call的map已经有了,就证明正在下载,则这次不下载
                .flatMap(s -> Observable.just(createDownInfo(s, file)))
                .flatMap(downloadInfo -> Observable.create(new DownloadSubscribe(downloadInfo)))//下载
                .observeOn(AndroidSchedulers.mainThread())//在主线程回调
                .subscribeOn(Schedulers.io())//在子线程执行
                .subscribe(new DownLoadObserver(downloadListener));//添加观察者

    }

    public void clear() {
        for (Map.Entry<String, Call> entry : downCalls.entrySet()) {
            Call call = downCalls.get(entry.getKey());
            if (call != null) {
                call.cancel();//取消
            }
        }
        downCalls.clear();
        handler.removeMessages(0);
    }

    public void cancel(String url) {
        Call call = downCalls.get(url);
        if (call != null) {
            call.cancel();//取消
        }
        downCalls.remove(url);
        handler.removeMessages(0);
    }

    /**
     * 创建DownInfo
     *
     * @param url 请求网址
     * @return DownInfo
     */
    private DownloadInfo createDownInfo(String url, File file) {
        DownloadInfo downloadInfo = new DownloadInfo(url, file);
        downloadInfo.totalLength = getContentLength(url);
        return downloadInfo;
    }

    private class DownloadSubscribe implements ObservableOnSubscribe<DownloadInfo> {
        private DownloadInfo downloadInfo;
        private long lastDownloadTime;

        public DownloadSubscribe(DownloadInfo downloadInfo) {
            this.downloadInfo = downloadInfo;
        }

        @Override
        public void subscribe(ObservableEmitter<DownloadInfo> e) throws Exception {
            String url = downloadInfo.url;
            //初始进度信息
            e.onNext(downloadInfo);

            Request request = new Request.Builder().url(url).build();
            Call call = mClient.newCall(request);
            downCalls.put(url, call);//把这个添加到call里,方便取消
            Response response = call.execute();

            File file = downloadInfo.file;
            if (file == null) {
                e.onError(new RuntimeException("Download file is null"));
                return;
            }
            if (file.exists()) {
                file.delete();
            }
            File directory = file.getParentFile();
            if (directory != null && !directory.exists()) {
                directory.mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            InputStream is = null;
            FileOutputStream fileOutputStream = null;
            try {
                is = response.body().byteStream();
                fileOutputStream = new FileOutputStream(file, true);
                byte[] buffer = new byte[2048];//缓冲数组2kB
                int len;
                long currentLength = 0;
                lastDownloadTime = SystemClock.elapsedRealtime();
                while ((len = is.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, len);
                    currentLength += len;
                    downloadInfo.setProgress(currentLength);
                    long thisTime = SystemClock.elapsedRealtime();
                    long timeSpe = thisTime - lastDownloadTime;
                    if (timeSpe >= CALLBACK_MIN_TIME) {
                        lastDownloadTime = thisTime;
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                e.onNext(downloadInfo);
                            }
                        });
                    }
                }
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        e.onNext(downloadInfo);
                    }
                }, CALLBACK_MIN_TIME);
                fileOutputStream.flush();
                downCalls.remove(url);
            } finally {
                //关闭IO流
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close(); //关闭输出流
                    } catch (IOException ee) {
                    }
                }
                if (is != null) {
                    try {
                        is.close(); //关闭输入流
                    } catch (IOException ee) {
                    }
                }
            }
            e.onComplete();//完成
        }
    }

    /**
     * 获取下载长度
     *
     * @param downloadUrl
     * @return
     */
    private long getContentLength(String downloadUrl) {
        Request request = new Request.Builder()
                .url(downloadUrl)
                .build();
        try {
            Response response = mClient.newCall(request).execute();
            if (response != null && response.isSuccessful()) {
                long contentLength = response.body().contentLength();
                response.close();
                return contentLength == 0 ? DownloadInfo.TOTAL_ERROR : contentLength;
            }
        } catch (IOException e) {
        }
        return DownloadInfo.TOTAL_ERROR;
    }

}
