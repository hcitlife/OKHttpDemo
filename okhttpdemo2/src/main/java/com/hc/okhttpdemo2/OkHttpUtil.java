package com.hc.okhttpdemo2;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.squareup.okhttp.Cache;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

//使用 OkHttpClient ，所有的HTTP Client配置包括代理设置、超时设置、缓存设置。当你需要为单个call改变配置的时候，
// clone 一个 OkHttpClient 。这个api将会返回一个浅拷贝（shallow copy），你可以用来单独自定义。
public class OkHttpUtil {

    public static Logger logger = Logger.getLogger("logger");

    public static final int SUCCESS = 1;
    public static final int ERROR = -1;

    private static final OkHttpClient client = new OkHttpClient();

    static {
        client.setCookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ORIGINAL_SERVER));
        client.setConnectTimeout(30, TimeUnit.SECONDS);//设置连接超时
        client.setWriteTimeout(10, TimeUnit.SECONDS);
        client.setReadTimeout(10, TimeUnit.SECONDS);
        client.setRetryOnConnectionFailure(true);

        //有些响应消息通过包含 Cache-Control HTTP 首部字段允许缓存，但是默认情况下，OkHttp 并不会缓存这些响应消息。
        //因此客户端可能会因为不断请求相同的资源而浪费时间和带宽，而不是简单地读取一下首次响应消息的缓存副本。
        //为了在文件系统中开启响应缓存，需要配置一个com.squareup.okhttp.Cache实例，然后把它传递给
        //OkHttpClient实例的setCache方法。你必须用一个表示目录的File对象和最大字节数来实例化Cache对象。那些能够缓存的响应消息会
        //被写在指定的目录中。如果已缓存的响应消息导致目录内容超过了指定的大小，响应消息会按照最近最少使用（LRU Policy）的策略被移除。
        //设置HTTP缓存，提升用户体验
        Log.i("TAG",BasicApplication.getAppContext().getExternalCacheDir().toString() );
        File okHttpCache = new File(BasicApplication.getAppContext().getExternalCacheDir() + "okHttpCache");
        if (!okHttpCache.exists()) {
            okHttpCache.mkdirs();
        }
        //OkHttp本身有缓存，如果不设置，它是不起作用的
        client.setCache(new Cache(okHttpCache, 10 * 1024 * 1024)); //设置缓存目录和缓存大小，OkHttp内部是使用LRU来管理缓存的,大小为10M
        client.networkInterceptors().add(new Interceptor() {//拦截器
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                Log.i("sending request ",request.url()+"");
                Log.i("sending request ",chain.connection()+"");
                Log.i("sending request ",request.headers()+"");

                Response response = chain.proceed(request);

                Log.i("recived response ",response.request().url()+"");
                Log.i("recived response ",response.headers()+"");

                return response.newBuilder()
                        .removeHeader("Pragma")
                        .header("Cache-Control", String.format("max-age=%d", 60))
                        .build();
            }
        });
    }

    //封装GET请求
    public static void doGet(String url, final Handler handler) {
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                handler.sendEmptyMessage(ERROR);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                Message msg = handler.obtainMessage();
                msg.what = SUCCESS;
                msg.obj = response.body().string();
                handler.sendMessage(msg);
            }
        });
    }

    //封装POST请求
    public static void doPost(String url, FormEncodingBuilder builder, final Handler handler) {
        Request request = new Request.Builder().url(url).post(builder.build()).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                handler.sendEmptyMessage(ERROR);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                Message msg = handler.obtainMessage();
                msg.what = SUCCESS;
                msg.obj = response.body().string();
                handler.sendMessage(msg);
            }
        });
    }

    //封装GET/POST请求
    public static void doRequest(String url, FormEncodingBuilder builder, final Handler handler) {
        Request request = null;
        if (builder == null) {//GET请求
            request = new Request.Builder().url(url).get().build();
        } else {//POST请求
            request = new Request.Builder().url(url).post(builder.build()).build();
        }
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                handler.sendEmptyMessage(ERROR);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                Message msg = handler.obtainMessage();
                msg.what = SUCCESS;
                msg.obj = response.body().string();
                handler.sendMessage(msg);
            }
        });
    }

}