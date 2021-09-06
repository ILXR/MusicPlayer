package com.epic.localmusicnoserver.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.util.Log;
import android.widget.Toast;

import com.epic.localmusicnoserver.database.DBManager;
import com.epic.localmusicnoserver.fragment.PlayBarFragment;
import com.epic.localmusicnoserver.util.MusicConstant;
import com.epic.localmusicnoserver.util.MyMusicUtil;
import com.epic.localmusicnoserver.util.UpdateUIThread;

import java.io.File;


public class PlayerManagerReceiver extends BroadcastReceiver {

    public static final String ACTION_UPDATE_UI_ADAPTER = "com.epic.localmusic.receiver.PlayerManagerReceiver:action_update_ui_adapter_broad_cast";

    private MediaPlayer mediaPlayer;

    private DBManager dbManager;

    /**
     * 全局变量，播放器的状态
     */
    public static int status = MusicConstant.STATUS_STOP;

    private int threadNumber;

    private Context context;


    public PlayerManagerReceiver(Context context) {
        super();
        this.context = context;
        dbManager = DBManager.getInstance(context);
        mediaPlayer = new MediaPlayer();
        initMediaPlayer();
    }

    public PlayerManagerReceiver(){}


    /**
     * 接收广播：PLAYER_MANAGER_ACTION
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        int cmd = intent.getIntExtra(MusicConstant.COMMAND, MusicConstant.COMMAND_INIT);
        switch (cmd) {
            case MusicConstant.COMMAND_INIT:	//已经在创建的时候初始化了，可以撤销了
                break;
            case MusicConstant.COMMAND_PLAY:  //接收播放指令
                status = MusicConstant.STATUS_PLAY;
                String musicPath = intent.getStringExtra(MusicConstant.KEY_PATH);
                if (musicPath != null) {
                    playMusic(musicPath);
                }else {
                    mediaPlayer.start();
                }
                break;
            case MusicConstant.COMMAND_PAUSE:  //接收暂停指令
                mediaPlayer.pause();
                status = MusicConstant.STATUS_PAUSE;
                break;
            case MusicConstant.COMMAND_STOP: //接收停止指令、本程序停止状态都是删除当前播放音乐触发
                NumberRandom();
                status = MusicConstant.STATUS_STOP;
                if(mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                initStopOperate();
                break;
            case MusicConstant.COMMAND_PROGRESS:  //拖动进度
                int currentProgress = intent.getIntExtra(MusicConstant.KEY_CURRENT, 0);
                mediaPlayer.seekTo(currentProgress);
                break;
            case MusicConstant.COMMAND_RELEASE:  //结束播放
                NumberRandom();
                status = MusicConstant.STATUS_STOP;  //停止
                if(mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                }
                break;
        }

        //歌曲状态设置完之后
        UpdateUI();
    }


    /**
     * 切换到歌单的第一首歌曲
     */
    private void initStopOperate(){
        MyMusicUtil.setIntSharedPreference(MusicConstant.KEY_ID,dbManager.getFirstId(MusicConstant.LIST_ALLMUSIC));
    }


    /**
     * 播放音乐
     */
    private void playMusic(String musicPath) {
        NumberRandom();
        int oldCurrent = 0;
        if (mediaPlayer != null) {
            if(mediaPlayer.isPlaying()){
                oldCurrent = mediaPlayer.getCurrentPosition();
                Log.d("old!!!!!", String.valueOf(oldCurrent));
            }
            mediaPlayer.release();  //进行回收
        }
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {  //歌曲播放完毕后进行回调
            @Override
            public void onCompletion(MediaPlayer mp) {
                UpdateUI();  //更新界面
                NumberRandom();  //切换线程号
                onComplete();  //调用音乐切换模块，进行相应操作
            }
        });

        try {
            File file = new File(musicPath);
            if(!file.exists()){
                Toast.makeText(context,"歌曲文件不存在，请重新扫描",Toast.LENGTH_SHORT).show();
                MyMusicUtil.playNextMusic(context);  //播放下一首音乐
                return;
            }
            mediaPlayer.reset();
            mediaPlayer.setDataSource(musicPath);  //设置MediaPlayer数据源
            mediaPlayer.prepare();
            mediaPlayer.start();
            new UpdateUIThread(this, context, threadNumber).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 给出随机线程号
     */
    private void NumberRandom() {
        int count;
        do {
            count =(int)(Math.random()*100);
        } while (count == threadNumber);
        threadNumber = count;
    }


    private void onComplete() {
        MyMusicUtil.playNextMusic(context);
    }


    /**
     * 更新UI
     */
    private void UpdateUI() {
        Intent playBarIntent = new Intent(PlayBarFragment.ACTION_UPDATE_UI_PlAYBAR);  //playBar的广播接收器进行接收
        playBarIntent.putExtra(MusicConstant.STATUS, status);  //来通知状态
        context.sendBroadcast(playBarIntent);

        Intent intent = new Intent(ACTION_UPDATE_UI_ADAPTER);  //接收广播为所有歌曲列表的adapter(不用发送该广播也可以)
        context.sendBroadcast(intent);
    }


    /**
     * 初始化音乐播放器
     */
    private void initMediaPlayer() {
        NumberRandom(); // 改变线程号,使旧的播放线程停止

        int musicId = MyMusicUtil.getIntSharedPreference(MusicConstant.KEY_ID);  //当前正在播放音乐的id
        int current = MyMusicUtil.getIntSharedPreference(MusicConstant.KEY_CURRENT);  //当前播放的进度

        //如果是没取到当前正在播放的音乐ID，则从数据库中获取第一首音乐的播放信息初始化
        if (musicId == -1) {
            return;
        }

        String path = dbManager.getMusicPath(musicId);
        if (path == null) {
            return;
        }

        if (current == 0) {
            status = MusicConstant.STATUS_STOP; //设置播放状态为停止
        }else {
            status = MusicConstant.STATUS_PAUSE; //设置播放状态为暂停
        }

        MyMusicUtil.setIntSharedPreference(MusicConstant.KEY_ID,musicId);
        MyMusicUtil.setStringSharedPreference(MusicConstant.KEY_PATH,path);
        UpdateUI();
    }


    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }


    public int getThreadNumber() {
        return threadNumber;
    }
}
