package com.epic.localmusicnoserver.util;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.widget.Toast;

import com.epic.localmusicnoserver.activity.ThemeActivity;
import com.epic.localmusicnoserver.database.DBManager;
import com.epic.localmusicnoserver.entity.AlbumInfo;
import com.epic.localmusicnoserver.entity.FolderInfo;
import com.epic.localmusicnoserver.entity.MusicInfo;
import com.epic.localmusicnoserver.entity.SingerInfo;
import com.epic.localmusicnoserver.service.MusicPlayerService;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MyMusicUtil {

    private static final String  TAG          = MyMusicUtil.class.getName();
    private static final Handler handlerThree = new Handler(Looper.getMainLooper());

    public static Toast toast;

    // 全局显示Toast
    public static void showToast(final String msg) {
        handlerThree.post(new Runnable() {
            public void run() {
                if (toast != null) {
                    toast.cancel();
                }
                toast = Toast.makeText(MyApplication.getContext(), msg, Toast.LENGTH_LONG);
                toast.show();
            }
        });
    }

    /**
     * 获取当前播放的列表
     */
    public static List<MusicInfo> getCurrentPlayList(Context context) {
        DBManager dbManager = DBManager.getInstance(context);
        int playList = MyMusicUtil.getIntSharedPreference(MusicConstant.KEY_LIST);
        List<MusicInfo> musicInfoList = new ArrayList<>();
        switch (playList) {
            case MusicConstant.LIST_ALLMUSIC:  //所有音乐
                musicInfoList = dbManager.getAllMusicFromMusicTable();
                break;
            case MusicConstant.LIST_MYLOVE:  //我喜欢的音乐
                musicInfoList = dbManager.getAllMusicFromTable(MusicConstant.LIST_MYLOVE);
                break;
            case MusicConstant.LIST_LASTPLAY:  //最近播放
                musicInfoList = dbManager.getAllMusicFromTable(MusicConstant.LIST_LASTPLAY);
                break;
            case MusicConstant.LIST_PLAYLIST:  //自建歌单
                int listId = MyMusicUtil.getIntSharedPreference(MusicConstant.KEY_LIST_ID);
                musicInfoList = dbManager.getMusicListByPlaylist(listId);
                break;
            case MusicConstant.LIST_SINGER:  //歌手
                String singerName = MyMusicUtil.getStringSharedPreference(MusicConstant.KEY_LIST_ID);
                if (singerName == null) {
                    musicInfoList = dbManager.getAllMusicFromMusicTable();
                } else {
                    musicInfoList = dbManager.getMusicListBySinger(singerName);
                }
                break;
            case MusicConstant.LIST_ALBUM:  //专辑
                String albumName = MyMusicUtil.getStringSharedPreference(MusicConstant.KEY_LIST_ID);
                if (albumName == null) {
                    musicInfoList = dbManager.getAllMusicFromMusicTable();
                } else {
                    musicInfoList = dbManager.getMusicListByAlbum(albumName);
                }
                break;
            case MusicConstant.LIST_FOLDER:  //文件夹
                String folderName = MyMusicUtil.getStringSharedPreference(MusicConstant.KEY_LIST_ID);
                if (folderName == null) {
                    musicInfoList = dbManager.getAllMusicFromMusicTable();
                } else {
                    musicInfoList = dbManager.getMusicListByFolder(folderName);
                }
                break;
        }
        return musicInfoList;
    }


    /**
     * 播放下一首音乐
     */
    public static void playNextMusic(Context context) {
        //获取下一首ID
        DBManager dbManager = DBManager.getInstance(context);
        int playMode = MyMusicUtil.getIntSharedPreference(MusicConstant.KEY_MODE);
        int musicId = MyMusicUtil.getIntSharedPreference(MusicConstant.KEY_ID);
        List<MusicInfo> musicList = getCurrentPlayList(context);
        ArrayList<Integer> musicIdList = new ArrayList<>();
        for (MusicInfo info : musicList) {
            musicIdList.add(info.getId());
        }
        musicId = dbManager.getNextMusic(musicIdList, musicId, playMode);
        MyMusicUtil.setIntSharedPreference(MusicConstant.KEY_ID, musicId);
        if (musicId == -1) {
            Intent intent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
            intent.putExtra(MusicConstant.COMMAND, MusicConstant.COMMAND_STOP);
            context.sendBroadcast(intent);
            Toast.makeText(context, "歌曲不存在", Toast.LENGTH_LONG).show();
            return;
        }

        //获取播放歌曲路径
        String path = dbManager.getMusicPath(musicId);
        Log.d(TAG, "next path =" + path);
        //发送播放请求
        Log.d(TAG, "next  id = " + musicId + "path = " + path);
        Intent intent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
        intent.putExtra(MusicConstant.COMMAND, MusicConstant.COMMAND_PLAY);
        intent.putExtra(MusicConstant.KEY_PATH, path);
        context.sendBroadcast(intent);
    }


    /**
     * 播放前一首歌曲
     */
    public static void playPreMusic(Context context) {
        DBManager dbManager = DBManager.getInstance(context);
        int playMode = MyMusicUtil.getIntSharedPreference(MusicConstant.KEY_MODE);
        int musicId = MyMusicUtil.getIntSharedPreference(MusicConstant.KEY_ID);  //得到当前播放音乐id
        List<MusicInfo> musicList = getCurrentPlayList(context);  //得到当前播放列表
        ArrayList<Integer> musicIdList = new ArrayList<>();
        for (MusicInfo musicInfo : musicList) {
            musicIdList.add(musicInfo.getId());
        }
        musicId = dbManager.getPreMusic(musicIdList, musicId, playMode);
        MyMusicUtil.setIntSharedPreference(MusicConstant.KEY_ID, musicId);
        if (musicId == -1) {
            Intent intent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
            intent.putExtra(MusicConstant.COMMAND, MusicConstant.COMMAND_STOP);
            context.sendBroadcast(intent);
            Toast.makeText(context, "歌曲不存在", Toast.LENGTH_LONG).show();
            return;
        }

        String path = dbManager.getMusicPath(musicId);   //获取播放歌曲路径
        Intent intent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
        intent.putExtra(MusicConstant.COMMAND, MusicConstant.COMMAND_PLAY);
        intent.putExtra(MusicConstant.KEY_PATH, path);
        context.sendBroadcast(intent);
    }


    public static void setMusicMyLove(Context context, int musicId) {
        if (musicId == -1) {
            Toast.makeText(context, "歌曲不存在", Toast.LENGTH_LONG).show();
            return;
        }
        DBManager dbManager = DBManager.getInstance(context);
        dbManager.setMyLove(musicId);
    }

    //设置--铃声的具体方法
    public static void setMyRingtone(Context context) {
        DBManager dbManager = DBManager.getInstance(context);
        int musicId = MyMusicUtil.getIntSharedPreference(MusicConstant.KEY_ID);
        String path = dbManager.getMusicPath(musicId);
        File sdfile = new File(path);
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DATA, sdfile.getAbsolutePath());
        values.put(MediaStore.MediaColumns.TITLE, sdfile.getName());
        values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/*");
        values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
        values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
        values.put(MediaStore.Audio.Media.IS_ALARM, false);
        values.put(MediaStore.Audio.Media.IS_MUSIC, false);

        Uri uri = MediaStore.Audio.Media.getContentUriForPath(sdfile.getAbsolutePath());
        Uri newUri = context.getContentResolver().insert(uri, values);
        RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE, newUri);
        Toast.makeText(context, "设置来电铃声成功！", Toast.LENGTH_SHORT).show();
    }

    /**
     * 存储int型
     */
    public static void setIntSharedPreference(String key, int value) {
        SharedPreferences pref = MyApplication.getContext().getSharedPreferences("music", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    /**
     * 存储String型
     */
    public static void setStringSharedPreference(String key, String value) {
        SharedPreferences pref = MyApplication.getContext().getSharedPreferences("music", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key, value);
        editor.commit();
    }

    /**
     * 获取int型sharedPreference
     */
    public static int getIntSharedPreference(String key) {
        SharedPreferences pref = MyApplication.getContext().getSharedPreferences("music", Context.MODE_PRIVATE);
        int value;
        if (key.equals(MusicConstant.KEY_CURRENT)) {  //current
            value = pref.getInt(key, 0);
        } else {
            value = pref.getInt(key, -1);
        }
        return value;
    }


    public static String getStringSharedPreference(String key) {
        SharedPreferences pref = MyApplication.getContext().getSharedPreferences("music", Context.MODE_PRIVATE);
        String value;
        value = pref.getString(key, null);
        return value;
    }

    /**
     * 按照歌手分组
     */
    public static ArrayList<SingerInfo> groupBySinger(ArrayList list) {
        Map<String, List<MusicInfo>> musicMap = new HashMap<>();  //歌手-歌曲映射
        ArrayList<SingerInfo> singerInfoList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            MusicInfo musicInfo = (MusicInfo) list.get(i);
            if (musicMap.containsKey(musicInfo.getSinger())) {
                ArrayList singerList = (ArrayList) musicMap.get(musicInfo.getSinger());
                singerList.add(musicInfo);
            } else {
                ArrayList temp = new ArrayList();
                temp.add(musicInfo);
                musicMap.put(musicInfo.getSinger(), temp);
            }
        }

        for (Map.Entry<String, List<MusicInfo>> entry : musicMap.entrySet()) {  //遍历
            System.out.println("key= " + entry.getKey() + " and value= " + entry.getValue());
            SingerInfo singerInfo = new SingerInfo();
            singerInfo.setName(entry.getKey());
            singerInfo.setCount(entry.getValue().size());
            singerInfoList.add(singerInfo);
        }
        return singerInfoList;
    }

    /**
     * 按照专辑分组
     */
    public static ArrayList<AlbumInfo> groupByAlbum(ArrayList list) {
        Map<String, List<MusicInfo>> musicMap = new HashMap<>();
        ArrayList<AlbumInfo> albumInfoList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            MusicInfo musicInfo = (MusicInfo) list.get(i);
            if (musicMap.containsKey(musicInfo.getAlbum())) {
                ArrayList albumList = (ArrayList) musicMap.get(musicInfo.getAlbum());
                albumList.add(musicInfo);
            } else {
                ArrayList temp = new ArrayList();
                temp.add(musicInfo);
                musicMap.put(musicInfo.getAlbum(), temp);
            }
        }

        for (Map.Entry<String, List<MusicInfo>> entry : musicMap.entrySet()) {
            AlbumInfo albumInfo = new AlbumInfo();
            albumInfo.setName(entry.getKey());
            albumInfo.setSinger(entry.getValue().get(0).getSinger());
            albumInfo.setCount(entry.getValue().size());
            albumInfoList.add(albumInfo);
        }
        return albumInfoList;
    }


    /**
     * 按照文件夹分组
     */
    public static ArrayList<FolderInfo> groupByFolder(ArrayList list) {
        Map<String, List<MusicInfo>> musicMap = new HashMap<>();
        ArrayList<FolderInfo> folderInfoList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            MusicInfo musicInfo = (MusicInfo) list.get(i);
            if (musicMap.containsKey(musicInfo.getParentPath())) {
                ArrayList folderList = (ArrayList) musicMap.get(musicInfo.getParentPath());
                folderList.add(musicInfo);
            } else {
                ArrayList temp = new ArrayList();
                temp.add(musicInfo);
                musicMap.put(musicInfo.getParentPath(), temp);
            }
        }

        for (Map.Entry<String, List<MusicInfo>> entry : musicMap.entrySet()) {
            System.out.println("key= " + entry.getKey() + " and value= " + entry.getValue());
            FolderInfo folderInfo = new FolderInfo();
            File file = new File(entry.getKey());
            folderInfo.setName(file.getName());
            folderInfo.setPath(entry.getKey());
            folderInfo.setCount(entry.getValue().size());
            folderInfoList.add(folderInfo);
        }
        return folderInfoList;
    }


    /**
     * 设置主题
     */
    public static void setTheme(Context context, int position) {
        int preSelect = getTheme(context);
        SharedPreferences sharedPreferences = context.getSharedPreferences(MusicConstant.THEME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putInt("theme_select", position).commit();
        if (preSelect != ThemeActivity.THEME_SIZE - 1) {
            sharedPreferences.edit().putInt("pre_theme_select", preSelect).commit();
        }
    }


    /**
     * 得到选择主题的id
     */
    public static int getTheme(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(MusicConstant.THEME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt("theme_select", 0);
    }


    /**
     * 得到上一次的日间模式的主题
     */
    public static int getPreTheme(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(MusicConstant.THEME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt("pre_theme_select", 0);
    }


    /**
     * 设置夜间模式
     */
    public static void setNightMode(Context context, boolean mode) {
        if (mode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences(MusicConstant.THEME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("night", mode).commit();
    }


    /**
     * 得到是否是夜间模式
     */
    public static boolean getNightMode(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(MusicConstant.THEME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("night", false);
    }

    /**
     * 获取bing图片
     */
    public static String getBingSharedPreference() {
        SharedPreferences pref = MyApplication.getContext().getSharedPreferences("bing_pic", Context.MODE_PRIVATE);
        String value = pref.getString("pic", null);
        return value;
    }

    /**
     * 存储bing图片
     */
    public static void setBingSharedPreference(String value) {
        /*
        SharedPreferences是一种轻量级的数据存储方式，采用键值对的存储方式。
        SharedPreferences只能存储少量数据，大量数据不能使用该方式存储，支持存储的数据类型有booleans, floats, ints, longs, and strings。
        SharedPreferences存储到一个XML文件中的，路径在/data/data/<packagename>/shared_prefs/下
         */
        SharedPreferences pref = MyApplication.getContext().getSharedPreferences("bing_pic", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("pic", value);
        editor.commit();
    }

    /**
     * 判断是否是第一次启动
     */
    public static boolean getIsFirst() {
        SharedPreferences pref = MyApplication.getContext().getSharedPreferences("is_first", Context.MODE_PRIVATE);
        Boolean value = pref.getBoolean("is_first", true);
        return value;
    }


    /**
     * 存储第一次扫描
     */
    public static void setIsFirst(Boolean isFirst) {
        SharedPreferences pref = MyApplication.getContext().getSharedPreferences("is_first", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("is_first", isFirst);
        editor.commit();
    }


}
