package com.getyourlocation.app.gyl_client;

import android.app.Application;
import android.util.Log;

import com.getyourlocation.app.gyl_client.util.FileUtil;
import com.palmaplus.nagrand.core.Engine;


public class GYLApp extends Application {
    private static final String TAG = "GYLApp";

    @Override
    public void onCreate() {
        super.onCreate();
        Engine.getInstance().startWithLicense(Constant.APP_KEY, this);
        FileUtil.copyLuaToStorage(this);
    }
}
