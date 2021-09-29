package com.epic.localmusicnoserver.util;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatDelegate;

import com.epic.localmusicnoserver.database.DBManager;
import com.epic.localmusicnoserver.receiver.PlayerManagerReceiver;
import com.epic.localmusicnoserver.service.MusicPlayerService;


public class MyApplication extends Application {
    private static final String TAG = "MyApplication";

    private static Context       context;
    private static DBManager     dbManager;
    private static MyApplication Instance;

    public static MyApplication getInstance() {
        return Instance;
    }

    public static Context getContext() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Instance = this;
        context = getApplicationContext();
        Intent startIntent = new Intent(MyApplication.this, MusicPlayerService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(startIntent);
        } else {
            startService(startIntent);
        }
        initNightMode();
        dbManager = DBManager.getInstance(this);
    }

    public void sendBroadCast(String action) {
        Intent intent = new Intent();
        intent.setAction(action);
        sendBroadcast(intent);
    }

    protected void initNightMode() {
        boolean isNight = MyMusicUtil.getNightMode(context);
        if (isNight) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    public void play() {
        int musicId;
        musicId = MyMusicUtil.getIntSharedPreference(MusicConstant.KEY_ID);
        if (musicId == -1 || musicId == 0) {
            musicId = dbManager.getFirstId(MusicConstant.LIST_ALLMUSIC);
            Intent intent = new Intent(MusicConstant.MP_FILTER);
            intent.putExtra(MusicConstant.COMMAND, MusicConstant.COMMAND_STOP);
            sendBroadcast(intent);
            return;
        }
        if (PlayerManagerReceiver.status == MusicConstant.STATUS_PAUSE) {  //暂停-播放
            Intent intent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
            intent.putExtra(MusicConstant.COMMAND, MusicConstant.COMMAND_PLAY);
            sendBroadcast(intent);
        } else if (PlayerManagerReceiver.status == MusicConstant.STATUS_PLAY) {  //播放-暂停
            Intent intent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
            intent.putExtra(MusicConstant.COMMAND, MusicConstant.COMMAND_PAUSE);
            sendBroadcast(intent);
        } else {  //停止-播放
            String path = dbManager.getMusicPath(musicId);
            Intent intent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
            intent.putExtra(MusicConstant.COMMAND, MusicConstant.COMMAND_PLAY);
            intent.putExtra(MusicConstant.KEY_PATH, path);
            sendBroadcast(intent);
        }
    }
}
