package com.epic.localmusic.util;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatDelegate;

import com.epic.localmusic.WebSocket.WebSocketHandler;
import com.epic.localmusic.database.DBManager;
import com.epic.localmusic.receiver.PlayerManagerReceiver;
import com.epic.localmusic.service.MusicPlayerService;


public class MyApplication extends Application {
    private static final String TAG = "MyApplication";

    private static Context       context;
    private static DBManager     dbManager;
    private static MyApplication Instance;


    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        Instance = this;
        Intent startIntent = new Intent(MyApplication.this, MusicPlayerService.class);
        startService(startIntent);
        initNightMode();
        dbManager = DBManager.getInstance(this);
        // TODO 连接服务器
        WebSocketHandler.getInstance().connect();
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

    public static MyApplication getInstance() {
        return Instance;
    }

    public static Context getContext() {
        return context;
    }
}
