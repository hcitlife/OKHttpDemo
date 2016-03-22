package com.hc.uploaddownwithprogress;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import hc.libs.okhttpwithprogress.helper.ProgressHelper;
import hc.libs.okhttpwithprogress.listener.impl.UIProgressListener;


public class MainActivity extends AppCompatActivity {
    private String baseUrl;
    private static final OkHttpClient client = new OkHttpClient();

    private ProgressBar uploadProgress, downloadProgeress;

    private void initClient() {    //设置超时，不设置可能会报异常
        client.setConnectTimeout(1000, TimeUnit.MINUTES);
        client.setReadTimeout(1000, TimeUnit.MINUTES);
        client.setWriteTimeout(1000, TimeUnit.MINUTES);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        uploadProgress = (ProgressBar) findViewById(R.id.upload_progress);
        downloadProgeress = (ProgressBar) findViewById(R.id.download_progress);

        baseUrl = "http://192.168.0.105:8080/AndroidServer/";

        initClient();
    }

    public void uploadFun(View view) {
        String path = Environment.getExternalStoragePublicDirectory("").getAbsolutePath();
        Log.i("TAG", path);

        File file = new File(path + "/aa.jpg");//abc.chm文件必须位于手机sd卡根目录中

        //这个是ui线程回调，可直接操作UI
        final UIProgressListener uiProgressRequestListener = new UIProgressListener() {
            @Override
            public void onUIProgress(long bytesWrite, long contentLength, boolean done) {
                Log.e("TAG", "bytesWrite:" + bytesWrite);
                Log.e("TAG", "contentLength" + contentLength);
                Log.e("TAG", (100 * bytesWrite) / contentLength + " % done ");
                Log.e("TAG", "done:" + done);
                Log.e("TAG", "================================");
                //ui层回调
                uploadProgress.setProgress((int) ((100 * bytesWrite) / contentLength));
                //Toast.makeText(getApplicationContext(), bytesWrite + " " + contentLength + " " + done, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onUIStart(long bytesWrite, long contentLength, boolean done) {
                super.onUIStart(bytesWrite, contentLength, done);
                Toast.makeText(getApplicationContext(), "start", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onUIFinish(long bytesWrite, long contentLength, boolean done) {
                super.onUIFinish(bytesWrite, contentLength, done);
                Toast.makeText(getApplicationContext(), "end", Toast.LENGTH_SHORT).show();
            }
        };

        //构造上传请求，类似web表单
        RequestBody requestBody = new MultipartBuilder()
                .type(MultipartBuilder.FORM)
                                .addFormDataPart("fileName", file.getName())
                                .addFormDataPart("mFile", file.getName(), RequestBody.create(null, file))
                                        //这两行代码和下面两行代码功能一样
//                .addPart(
//                        Headers.of("Content-Disposition", "form-data; name=\"fileName\""),
//                        RequestBody.create(null, "aa.jpg"))//设置上传成功后，服务器器保存的文件的名称
//                .addPart(
//                        Headers.of("Content-Disposition", "form-data; name=\"mFile\";filename=\"aa.mp4\""),
//                        RequestBody.create(MediaType.parse("image/png"), file))

                .build();

        //进行包装，使其支持进度回调
        final Request request = new Request.Builder()
                .url(baseUrl + "fileUpload")
                .post(ProgressHelper.addProgressRequestListener(requestBody, uiProgressRequestListener))
                .build();
        //开始请求
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.e("TAG", "error ", e);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                Log.e("TAG", response.body().string());
            }
        });

    }

    public void downloadFun(View view) {
//        //这个是非ui线程回调，不可直接操作UI
        //这个是ui线程回调，可直接操作UI
        final UIProgressListener uiProgressResponseListener = new UIProgressListener() {
            @Override
            public void onUIProgress(long bytesRead, long contentLength, boolean done) {
                Log.e("TAG", "bytesRead:" + bytesRead);
                Log.e("TAG", "contentLength:" + contentLength);
                Log.e("TAG", "done:" + done);
                if (contentLength != -1) {
                    //长度未知的情况下回返回-1
                    Log.e("TAG", (100 * bytesRead) / contentLength + "% done");
                }
                Log.e("TAG", "================================");
                //ui层回调
                downloadProgeress.setProgress((int) ((100 * bytesRead) / contentLength));
                //Toast.makeText(getApplicationContext(), bytesRead + " " + contentLength + " " + done, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onUIStart(long bytesRead, long contentLength, boolean done) {
                super.onUIStart(bytesRead, contentLength, done);
                Toast.makeText(getApplicationContext(), "start", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onUIFinish(long bytesRead, long contentLength, boolean done) {
                super.onUIFinish(bytesRead, contentLength, done);
                Toast.makeText(getApplicationContext(), "end", Toast.LENGTH_SHORT).show();
            }
        };

        //构造请求
        final Request request1 = new Request.Builder()
                .url(baseUrl + "res/male.png")
                .build();

        //包装Response使其支持进度回调
        ProgressHelper.addProgressResponseListener(client, uiProgressResponseListener)
                .newCall(request1)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(Request request, IOException e) {
                        Log.e("TAG", "error ", e);
                    }

                    @Override
                    public void onResponse(Response response) throws IOException {
                        //                Log.e("TAG", response.body().string());
                        if (response.isSuccessful()) {
                            Log.i("TAG", "onResponse" + Thread.currentThread().getId());
                            InputStream inputStream = response.body().byteStream();//获取到流的形式
                            saveImg(inputStream);
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

}
