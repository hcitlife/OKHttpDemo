package com.hc.okhttpdemo2;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.squareup.okhttp.FormEncodingBuilder;

public class MainActivity extends AppCompatActivity {

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.textView);
//        int hasWriteExternalStorage = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    }

    public void getFun(View view) {
        String url = "http://www.baidu.com/";
        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == OkHttpUtil.SUCCESS)
                    textView.setText(msg.obj.toString());
                else
                    textView.setText("error");
            }
        };
        OkHttpUtil.doGet(url, handler);
    }

    public void postFun(View view) {
        String url = "http://192.168.1.51:8080/AndroidServer/login";
        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == OkHttpUtil.SUCCESS)
                    textView.setText(msg.obj.toString());
                else
                    textView.setText("error");
            }
        };
        FormEncodingBuilder builder = new FormEncodingBuilder();
        builder.add("name", "hc");
        builder.add("pass", "hc");
        OkHttpUtil.doPost(url, builder, handler);
    }

    public void getFun2(View view) {
        String url = "http://192.168.1.51:8080/AndroidServer/login.html";
        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == OkHttpUtil.SUCCESS)
                    textView.setText(msg.obj.toString());
                else
                    textView.setText("error");
            }
        };
        OkHttpUtil.doRequest(url,null,handler);
    }

    ////////////////////////////////////////////////////////////////////
    public void postFun2(View view) {
        String url = "http://192.168.1.51:8080/AndroidServer/login.jsp";
        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == OkHttpUtil.SUCCESS)
                    textView.setText(msg.obj.toString());
                else
                    textView.setText("error");
            }
        };
        FormEncodingBuilder builder = new FormEncodingBuilder();
        builder.add("name", "hc");
        builder.add("pass", "hc");
        OkHttpUtil.doRequest(url, builder, handler);
    }

}
