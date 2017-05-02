package com.getyourlocation.app.gylclient;

import android.app.Application;

import com.getyourlocation.app.gylclient.util.FileUtil;
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
