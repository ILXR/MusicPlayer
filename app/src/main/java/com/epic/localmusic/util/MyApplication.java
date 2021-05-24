package com.epic.localmusic.util;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatDelegate;

import com.epic.localmusic.WebSocket.WebSocketHandler;
import com.epic.localmusic.service.MusicPlayerService;


public class MyApplication extends Application {
    private static final String TAG = "MyApplication";

    private static Context context;


    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        Intent startIntent = new Intent(MyApplication.this, MusicPlayerService.class);
        startService(startIntent);
        initNightMode();
        // TODO WebSocket 测试代码需要修改
        WebSocketHandler.getInstance().testConnect();
    }


    protected void initNightMode() {
        boolean isNight = MyMusicUtil.getNightMode(context);
        if (isNight) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }


    public static Context getContext() {
        return context;
    }
}
