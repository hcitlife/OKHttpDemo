package com.hc.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.squareup.okhttp.Cache;
import com.squareup.okhttp.CacheControl;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    private String baseUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i("TAG", "onCreate" + Thread.currentThread().getId());
        baseUrl = "http://192.168.0.105:8080/AndroidServer/";
    }

    public void getFun1(View view) {//请求字符(同步Get)
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = "http://wthrcdn.etouch.cn/weather_mini?citykey=101010100";
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(url)
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        //    响应体的 string() 方法对于小文档来说十分方便、高效。但是如果响应体太大（超过1MB），应避免适应 string()方法 ，
                        //    因为他会将把整个文档加载到内存中。 对于超过1MB的响应body，应使用流的方式来处理body。
                        final String res = response.body().string();
                        Log.i("TAG", "success " + res);
                        showTxt(res);//将获取到的信息显示到屏幕中
                        //将获取到的信息保存到文件中
                        FileWriter out = new FileWriter(new File(getFilesDir(), "abc.txt"));
                        out.write(res.toCharArray());
                        out.flush();
                    } else {
                        Log.i("TAG", "failed");
                        throw new IOException("Unexpected code " + response);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void showTxt(final String res) {//在屏幕上显示文本信息
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView textView = (TextView) findViewById(R.id.textView);
                textView.setText(res + "");
            }
        });
    }

    //下载图片
    public void getFun2(View view) { //请求下载（异步Get）
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://y3.ifengimg.com/a/2016_09/f9d10bbdd039474.jpg")
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {// 开启异步线程访问网络
            @Override
            public void onFailure(Request request, IOException e) {
                Log.i("TAG", "failed");
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.i("TAG", "onResponse" + Thread.currentThread().getId());
                    InputStream inputStream = response.body().byteStream();//获取到流的形式
                    saveImg(inputStream);
                    //下面两行是将下载下来的图片转换成Bitmap，不过由于图片没有压缩不能正常显示到ImageView中
                    // byte[] bytes = response.body().bytes();
                    // Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                } else {
                    throw new IOException("Unexpected code " + response);
                }
            }
        });
    }

    private void saveImg(InputStream inputStream) {
        try {
            FileOutputStream fos = new FileOutputStream(new File(getFilesDir(), "aa.png"));
            byte[] temp = new byte[1024];
            int length = -1;
            while ((length = inputStream.read(temp)) != -1) {
                fos.write(temp, 0, length);
            }
            inputStream.close();
            fos.close();
        } catch (Exception e) {
        }
    }

    //    典型的HTTP头 像是一个 Map<String, String>:每个字段都有一个或没有值。但是一些头允许多个值，像Guava的Multimap。例如：HTTP响应里面提供的Vary响应头，就是多值的。OkHttp的api试图让这些情况都适用。
    //    当写请求头的时候，使用header(name, value)可以设置唯一的name、value。如果已经有值，旧的将被移除，然后添加新的。使用addHeader(name, value)可以添加多值（添加，不移除已有的）。
    //    当读取响应头时，使用header(name)返回最后出现的name、value。通常情况这也是唯一的name、value。如果没有值，那么header(name)将返回null。如果想读取字段对应的所有值，使用headers(name)会返回一个list。
    //    为了获取所有的Header，Headers类支持按index访问。
    public void getFun3(View view) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://www.baidu.com/")
                .header("User-Agent", "OkHttp Headers.java")
                .addHeader("Accept", "application/json; q=0.5")
                .addHeader("Accept", "application/vnd.github.v3+json")
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.i("TAG", "failed");
            }

            @Override
            public void onResponse(Response response) throws IOException {
                Headers headers = response.headers();
                for (int i = 0; i < headers.size(); i++)
                    Log.i("TAG", headers.name(i) + " ★ " + headers.value(i));
                Log.i("TAG", "Server : " + response.header("Server"));
                Log.i("TAG", "Date : " + response.header("Date"));
                Log.i("TAG", "Vary : " + response.header("Vary"));
            }
        });
    }

    //POST方式提交String
    public void postFun1(View view) {   //提交JSON形式的数据
        MediaType type = MediaType.parse("application/json; charset=utf-8");
        String json = "{\"name\"=\"zhangsan\",\"age\"=22}";
        RequestBody body = RequestBody.create(type, json);
        Request request = new Request.Builder()
                //                .url(baseUrl + "json.jsp")
                .url(baseUrl + "jsonServlet")
                .post(body)
                .build();
        OkHttpClient client = new OkHttpClient();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {// 开启异步线程访问网络
            @Override
            public void onFailure(Request request, IOException e) {
                Log.i("TAG", "failed");
            }

            @Override
            public void onResponse(Response response) throws IOException {
                String res = response.body().string();
                Log.i("TAG", "JSON " + res);
                showTxt(res);
            }
        });
    }

    //Post方式提交文件
    public void postFun3(View view) {//以文件作为请求体
        MediaType type = MediaType.parse("text/plain; charset=utf-8");
        File file = new File(getFilesDir(), "abc.txt");
        RequestBody body = RequestBody.create(type, file);
        Request request = new Request.Builder()
                .url(baseUrl + "fileServlet")
                .post(body)
                .build();
        OkHttpClient client = new OkHttpClient();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {// 开启异步线程访问网络
            @Override
            public void onFailure(Request request, IOException e) {
                Log.i("TAG", "failed");
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.isSuccessful()) {
                    String res = response.body().string();
                    Log.i("TAG", "JSON " + res);
                    showTxt(res);
                } else {
                    throw new IOException("Unexpected code " + response);
                }
            }
        });
    }

    //POST方式提交表单
    //使用FormEncodingBuilder来构建和HTML<form>标签相同效果的请求体。键值对将使用一种HTML兼容形式的URL编码来进行编码。
    public void postFun2(View view) { //提交键值对
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    RequestBody body = new FormEncodingBuilder()
                            .add("name", "hc")
                            .add("pass", "hc")
                            .build();
                    Request request = new Request.Builder()
                            .url(baseUrl + "login")
                            .post(body)
                            .build();
                    OkHttpClient client = new OkHttpClient();
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        String res = response.body().string();
                        Log.i("TAG", "** " + res);
                        showTxt(res);
                    } else {
                        throw new IOException("Unexpected code " + response);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    //Post方式提交分块请求
    //MultipartBuilder可以构建复杂的请求体，与HTML文件上传形式兼容。多块请求体中每块请求都是一个请求体，可以定义自己的请求头。
    //这些请求头可以用来描述这块请求，例如他的Content-Disposition。如果Content-Length和Content-Type可用的话，他们会被自动添加到请求头中。
    public void uploadFun(View view) {//文件上传
        Log.i("TAG", getFilesDir().getPath().toString()); //data/user/0/com.hc.activity/files
        File file = new File(getFilesDir(), "aa.png");

        MediaType mediaType = MediaType.parse("image/png");
        RequestBody requestBody = new MultipartBuilder().type(MultipartBuilder.FORM)
                .addPart(
                        Headers.of("Content-Disposition", "form-data; name=\"fileName\""),
                        RequestBody.create(null, "headImg.png"))
                .addPart(
                        Headers.of("Content-Disposition", "form-data; name=\"mFile\"; filename =\"aa.mp4\"")
                        , RequestBody.create(mediaType, file))
                .build();

        Request request = new Request.Builder()
                .url(baseUrl + "fileUpload")
                .post(requestBody)
                .build();
        OkHttpClient client = new OkHttpClient();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {// 开启异步线程访问网络
            @Override
            public void onFailure(Request request, IOException e) {
                Log.i("TAG", "failed");
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.isSuccessful()) {
                    String res = response.body().string();
                    Log.i("TAG", "upload result " + res);
                    showTxt(res);
                } else {
                    throw new IOException("Unexpected code " + response);
                }
            }
        });
    }

    public void uploadFun2(View view) {//文件上传
        Log.i("TAG", getFilesDir().getPath().toString()); //data/user/0/com.hc.activity/files
        File file = new File(getFilesDir(), "aa.png");

        RequestBody requestBody = new MultipartBuilder()
                .type(MultipartBuilder.FORM)
                .addFormDataPart("fileName", file.getName())
                .addFormDataPart("mFile", file.getName(), RequestBody.create(null, file))
                .build();

        Request request = new Request.Builder()
                .url(baseUrl + "fileUpload")
                .post(requestBody)
                .build();
        OkHttpClient client = new OkHttpClient();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {// 开启异步线程访问网络
            @Override
            public void onFailure(Request request, IOException e) {
                Log.i("TAG", "failed");
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.isSuccessful()) {
                    String res = response.body().string();
                    Log.i("TAG", "upload result " + res);
                    showTxt(res);
                } else {
                    throw new IOException("Unexpected code " + response);
                }
            }
        });
    }

    //请求缓存
    public void cacheFun(View view) {
        final OkHttpClient client = new OkHttpClient();
        File sdcache = getExternalCacheDir();
        int cacheSize = 10 * 1024 * 1024; // 10 MiB
        client.setCache(new Cache(sdcache.getAbsoluteFile(), cacheSize));


        new Thread(new Runnable() {
            @Override
            public void run() {
                Request request = new Request.Builder()
                        .url("http://www.qq.com")
                        .build();
                try {
                    Response response1 = client.newCall(request).execute();
                    if (!response1.isSuccessful())
                        throw new IOException("Unexpected code " + response1);
                    String response1Body = response1.body().string();
                    Log.i("TAG", "Response1 response:           " + response1);
                    Log.i("TAG", "Response1 cacheResponse:      " + response1.cacheResponse());
                    Log.i("TAG", "Response1 networkResponse:    " + response1.networkResponse());

                    Response response2 = client.newCall(request).execute();
                    if (!response2.isSuccessful())
                        throw new IOException("Unexpected code " + response2);
                    String response2Body = response2.body().string();
                    Log.i("TAG", "Response2 response:           " + response2);
                    Log.i("TAG", "Response2 cacheResponse:      " + response2.cacheResponse());
                    Log.i("TAG", "Response2 networkResponse:    " + response2.networkResponse());
                    Log.i("TAG", "Response2 equals Response1?   " + response1Body.equals(response2Body));

                    // response1的结果networkResponse不为null，代表是从网络请求加载过来的；因为设置了缓存，因此第二次请求时发现cacheResponse有数据，
                    // 因此不再去进行网络请求，response2的networkResponse为null。
                    //有时候即使在有缓存的情况下我们依然需要去后台请求最新的资源（比如资源更新了）,这个时候可以使用强制走网络来要求必须请求网络数据
                    request = request.newBuilder().cacheControl(CacheControl.FORCE_NETWORK).build();
                    Response response3 = client.newCall(request).execute();
                    if (!response3.isSuccessful())
                        throw new IOException("Unexpected code  " + response3);
                    String response3Body = response3.body().string();
                    Log.i("TAG", "Response3 response:           " + response3);
                    Log.i("TAG", "Response3 cacheResponse:      " + response3.cacheResponse());
                    Log.i("TAG", "Response3 networkResponse:    " + response3.networkResponse());
                    Log.i("TAG", "Response3 equals Response1?   " + response1Body.equals(response3Body));
                    //从结果可以发现：response3的cacheResponse为null，networkResponse依然有数据。

                    //使用FORCE_CACHE强制只要使用缓存的数据，但如果请求必须从网络获取才有数据，但又使用了FORCE_CACHE策略就会返回504错误，
                    // 代码如下，我们去okhttpclient的缓存，并设置request为FORCE_CACHE
                    request = request.newBuilder().cacheControl(CacheControl.FORCE_CACHE).build();
                    Response response4 = client.newCall(request).execute();
                    if (!response4.isSuccessful())
                        throw new IOException("Unexpected code " + response4);

                    String response4Body = response4.body().string();
                    Log.i("TAG", "Response 4 response:          " + response4);
                    Log.i("TAG", "Response 4 cache response:    " + response2.cacheResponse());
                    Log.i("TAG", "Response 4 network response:  " + response4.networkResponse());
                    Log.i("TAG", "Response 4 equals Response 1? " + response1Body.equals(response4Body));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void cancelFun(View view) { //取消请求
        Request request = new Request.Builder()
                .url(baseUrl + "cancelServlet")//服务耗时3秒
                .build();
        OkHttpClient client = new OkHttpClient();
        final Call call = client.newCall(request);

//        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
//        executor.schedule(new Runnable() {
//            @Override
//            public void run() {
//                call.cancel();
//            }
//        },1, TimeUnit.SECONDS);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                showTxt("cancel");
            }

            @Override
            public void onResponse(Response response) throws IOException {
                showTxt(response.body().string());
            }
        });
    }

}