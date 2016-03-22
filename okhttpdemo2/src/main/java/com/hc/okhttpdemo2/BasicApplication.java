package com.hc.okhttpdemo2;

import android.app.Application;
import android.content.Context;

/**
 * Created by hcitl on 2016-3-21-0021.
 */
public class BasicApplication extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    public static Context getAppContext() {
        return context;
    }
}
