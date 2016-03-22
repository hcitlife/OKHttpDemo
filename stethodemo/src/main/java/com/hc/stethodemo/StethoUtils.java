package com.hc.stethodemo;

import android.util.Log;

import com.facebook.stetho.okhttp.StethoInterceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

public class StethoUtils {
    private static final boolean debug = true;
    private static OkHttpClient okHttpClient = new OkHttpClient();

    static {
        if (debug) {
            //下面这句话显得尤为重要，加入后才能拦截到http请求。
            okHttpClient.networkInterceptors().add(new StethoInterceptor());
        }
    }

    public static final void networkRequest() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Request request = new Request.Builder().url("http://www.baidu.com")
                        .build();
                try {
                    Response response = okHttpClient.newCall(request).execute();
                    String reslut = response.body().string();
                    Log.i("TAG", "★  " + reslut);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

}