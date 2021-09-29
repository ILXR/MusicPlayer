package com.epic.localmusicnoserver.BlueTooth;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

import com.epic.localmusicnoserver.util.MusicConstant;
import com.epic.localmusicnoserver.util.MyApplication;
import com.epic.localmusicnoserver.util.MyMusicUtil;

import java.util.ArrayList;
import java.util.Collections;


public class CommandParsing {
    private static final String         TAG           = "CommandParsing";
    private static final Double         crabThreshold = 0.45d;
    private static       CommandParsing INSTANCE;
    private final        AudioManager   mAudioManager;

    public CommandParsing() {
        mAudioManager = (AudioManager) MyApplication.getContext().getSystemService(Context.AUDIO_SERVICE);
    }

    public static synchronized CommandParsing getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CommandParsing();
        }
        return INSTANCE;
    }

    public ActionType commandParseOld(ArrayList<Double> maxValues) {
        if (maxValues.size() != 4)
            return null;
        Double max = Collections.max(maxValues);
        if (max >= crabThreshold)
            return ActionType.Crab;
        Double v1 = maxValues.get(0), v2 = maxValues.get(1), v3 = maxValues.get(2), v4 = maxValues.get(3);
        if (max <= v3)
            return ActionType.Touch1;
        if (max <= v2) {
            if (Math.abs(v2 - v4) > Math.max(Math.abs(v4 - v1), Math.abs(v4 - v3)) && v4 > Math.max(v1, v3))
                return ActionType.Touch3;
            else
                return ActionType.Touch2;
        }
        return null;
    }

    public ActionType commandParseNew(ArrayList<Double> maxValues) {
        if (maxValues.size() != 4)
            return null;
        Double max = Collections.max(maxValues);
        if (max >= crabThreshold)
            return ActionType.Crab;
        Double v1 = maxValues.get(0), v2 = maxValues.get(1), v3 = maxValues.get(2), v4 = maxValues.get(3);
        if (v2 >= max) {
            return ActionType.Touch1;
        } else if (v4 >= max) {
            return ActionType.Touch2;
        } else if (v1 >= max) {
            return ActionType.Touch3;
        }
        return null;
    }

    public void act(ActionType type) {
        switch (type) {
            case Touch1:
                actionVolumePlus();
                break;
            case Touch2:
                actionVolumeSub();
                break;
            case Touch3:
                actionMusicPlay();
                break;
            case Crab:
                actionNextMusic();
                break;
        }
    }

    private void actionVolumePlus() {
        Log.i(TAG, "onMessage: 音量加");
        MyMusicUtil.showToast("音量增大");
        mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
    }

    private void actionVolumeSub() {
        Log.i(TAG, "onMessage: 音量减");
        MyMusicUtil.showToast("音量减小");
        mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
    }

    private void actionMusicPlay() {
        Log.i(TAG, "onMessage: 播放暂停");
        MyMusicUtil.showToast("播放/暂停");
        MyApplication.getInstance().play();
    }

    private void actionNextMusic() {
        Log.i(TAG, "onMessage: 切歌");
        MyMusicUtil.showToast("下一首");
        new Runnable() {
            @Override
            public void run() {
                MyMusicUtil.playNextMusic(MyApplication.getContext());
            }
        }.run();

    }

    private void actionChangeMode() {
        Log.i(TAG, "onMessage: 播放列表模式");
        MyMusicUtil.showToast("切换播放模式");
        int playMode = MyMusicUtil.getIntSharedPreference(MusicConstant.KEY_MODE);
        if (playMode == MusicConstant.PLAYMODE_RANDOM)
            MyMusicUtil.setIntSharedPreference(MusicConstant.KEY_MODE, MusicConstant.PLAYMODE_SINGLE_REPEAT);
        else if (playMode == MusicConstant.PLAYMODE_SINGLE_REPEAT)
            MyMusicUtil.setIntSharedPreference(MusicConstant.KEY_MODE, MusicConstant.PLAYMODE_SEQUENCE);
        else
            MyMusicUtil.setIntSharedPreference(MusicConstant.KEY_MODE, MusicConstant.PLAYMODE_RANDOM);
        MyApplication.getInstance().sendBroadCast(MusicConstant.UPDATE_PLAY_MODE);
    }

    public enum ActionType {
        Touch1,
        Touch2,
        Touch3,
        Crab
    }
}
