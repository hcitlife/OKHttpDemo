package com.hc.stethodemo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBOpenHelper extends SQLiteOpenHelper {
    public DBOpenHelper(Context context, String dbName, int version) {
        super(context, dbName, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) { //创建表结构
        String sql = "CREATE TABLE IF NOT EXISTS stu(" +
                "_id integer primary key autoincrement , " +
                "name text(20) , " +
                "age integer , " +
                "sex varchar(5)" +
                ")";
        db.execSQL(sql);
        db.execSQL("insert into stu (name,age,sex) values('zhangsan',22,'male')");
        db.execSQL("insert into stu (name,age,sex) values('lisi',23,'female')");
        db.execSQL("insert into stu (name,age,sex) values('wanger',21,'male')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
