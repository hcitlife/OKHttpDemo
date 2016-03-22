package com.hc.stethodemo;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    public void fun1(View view){
        StethoUtils.networkRequest();
    }
    public void fun2(View view){
        DBOpenHelper dbHelper =new DBOpenHelper(getApplicationContext(), "demo.db", 1);
        dbHelper.getWritableDatabase();
    }
    public void fun3(View view){
        SharedPreferences preferences = getSharedPreferences("info", 0);
        preferences.edit().putString("key","hc").commit();
    }
}
