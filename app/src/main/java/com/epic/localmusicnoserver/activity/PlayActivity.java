package com.epic.localmusicnoserver.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ScaleDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.epic.localmusicnoserver.R;
import com.epic.localmusicnoserver.database.DBManager;
import com.epic.localmusicnoserver.fragment.PlayBarFragment;
import com.epic.localmusicnoserver.receiver.PlayerManagerReceiver;
import com.epic.localmusicnoserver.service.MusicPlayerService;
import com.epic.localmusicnoserver.util.MusicConstant;
import com.epic.localmusicnoserver.util.CustomAttrValueUtil;
import com.epic.localmusicnoserver.util.MyMusicUtil;
import com.epic.localmusicnoserver.view.PlayingPopWindow;

import java.util.Locale;

public class PlayActivity extends BaseActivity implements View.OnClickListener {

    private DBManager dbManager;
    private ImageView backImage;
    private ImageView playImage;
    private ImageView menuImage;
    private ImageView preImage;
    private ImageView nextImage;
    private ImageView modeImage;
    private TextView  curTimeText;
    private TextView  totalTimeText;
    private TextView  musicNameText;
    private TextView  singerNameText;
    private SeekBar   seekBar;

    private PlayReceiver mReceiver;

    private int mProgress;
    private int duration;
    private int current;

    //本地广播接收器
    private IntentFilter      intentFilter;
    private BroadcastReceiver localReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        setStyle();
        dbManager = DBManager.getInstance(PlayActivity.this);
        initView();
        register();
        //注册本地广播监视器
        intentFilter = new IntentFilter();
        intentFilter.addAction("playMusic");
        localReceiver = new LocalReceiver();
        registerReceiver(localReceiver, intentFilter);
    }

    class LocalReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, "received playMusic", Toast.LENGTH_SHORT).show();
            play();
        }
    }


    /**
     * 初始化控件
     */
    private void initView() {
        backImage = (ImageView) findViewById(R.id.iv_back);
        playImage = (ImageView) findViewById(R.id.iv_play);
        menuImage = (ImageView) findViewById(R.id.iv_menu);
        preImage = (ImageView) findViewById(R.id.iv_prev);
        nextImage = (ImageView) findViewById(R.id.iv_next);
        modeImage = (ImageView) findViewById(R.id.iv_mode);
        curTimeText = (TextView) findViewById(R.id.tv_current_time);
        totalTimeText = (TextView) findViewById(R.id.tv_total_time);
        musicNameText = (TextView) findViewById(R.id.tv_title);
        singerNameText = (TextView) findViewById(R.id.tv_artist);
        seekBar = (SeekBar) findViewById(R.id.activity_play_seekbar);
        backImage.setOnClickListener(this);
        playImage.setOnClickListener(this);
        menuImage.setOnClickListener(this);
        preImage.setOnClickListener(this);
        nextImage.setOnClickListener(this);
        modeImage.setOnClickListener(this);

        setSeekBarBackground();
        initPlayMode();
        initTitle();
        initPlayImage();

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {  //滑动条进行改变
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int musicId = MyMusicUtil.getIntSharedPreference(MusicConstant.KEY_ID);
                if (musicId == -1) {
                    Intent intent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
                    intent.putExtra("cmd", MusicConstant.COMMAND_STOP);
                    sendBroadcast(intent);
                    Toast.makeText(PlayActivity.this, "歌曲不存在", Toast.LENGTH_LONG).show();
                    return;
                }

                //发送播放请求
                Intent intent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
                intent.putExtra("cmd", MusicConstant.COMMAND_PROGRESS);
                intent.putExtra("current", mProgress);
                sendBroadcast(intent);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mProgress = progress;
                initTime();
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                onBackPressed();
                break;
            case R.id.iv_mode:
                switchPlayMode();
                break;
            case R.id.iv_play:  //点击中间的播放/暂停
                play();
                break;
            case R.id.iv_next:
                MyMusicUtil.playNextMusic(this);
                break;
            case R.id.iv_prev:
                MyMusicUtil.playPreMusic(this);
                break;
            case R.id.iv_menu:
                showPopFormBottom();
                break;
        }
    }


    /**
     * 设置播放图片
     */
    private void initPlayImage() {
        int status = PlayerManagerReceiver.status;
        switch (status) {
            case MusicConstant.STATUS_STOP:
                playImage.setSelected(false);
                break;
            case MusicConstant.STATUS_PLAY:
                playImage.setSelected(true);
                break;
            case MusicConstant.STATUS_PAUSE:
                playImage.setSelected(false);
                break;
            case MusicConstant.STATUS_RUN:
                playImage.setSelected(true);
                break;
        }
    }


    /**
     * 设置播放模式
     */
    private void initPlayMode() {
        int playMode = MyMusicUtil.getIntSharedPreference(MusicConstant.KEY_MODE);
        if (playMode == -1) {
            playMode = 0;
        }
        modeImage.setImageLevel(playMode);
    }


    /**
     * 设置标题
     */
    private void initTitle() {
        int musicId = MyMusicUtil.getIntSharedPreference(MusicConstant.KEY_ID);
        if (musicId == -1) {
            musicNameText.setText("听听音乐");
            singerNameText.setText("好音质");
        } else {
            musicNameText.setText(dbManager.getMusicInfo(musicId).get(1));
            singerNameText.setText(dbManager.getMusicInfo(musicId).get(2));
        }
    }


    /**
     * 设置歌曲的播放时间
     */
    private void initTime() {
        curTimeText.setText(formatTime(current));
        totalTimeText.setText(formatTime(duration));
    }


    private String formatTime(long time) {
        return formatTime("mm:ss", time);
    }


    public static String formatTime(String pattern, long milli) {
        int m = (int) (milli / DateUtils.MINUTE_IN_MILLIS);
        int s = (int) ((milli / DateUtils.SECOND_IN_MILLIS) % 60);
        String mm = String.format(Locale.getDefault(), "%02d", m);
        String ss = String.format(Locale.getDefault(), "%02d", s);
        return pattern.replace("mm", mm).replace("ss", ss);
    }


    private void switchPlayMode() {
        int playMode = MyMusicUtil.getIntSharedPreference(MusicConstant.KEY_MODE);
        switch (playMode) {
            case MusicConstant.PLAYMODE_SEQUENCE:
                MyMusicUtil.setIntSharedPreference(MusicConstant.KEY_MODE, MusicConstant.PLAYMODE_RANDOM);
                break;
            case MusicConstant.PLAYMODE_RANDOM:
                MyMusicUtil.setIntSharedPreference(MusicConstant.KEY_MODE, MusicConstant.PLAYMODE_SINGLE_REPEAT);
                break;
            case MusicConstant.PLAYMODE_SINGLE_REPEAT:
                MyMusicUtil.setIntSharedPreference(MusicConstant.KEY_MODE, MusicConstant.PLAYMODE_SEQUENCE);
                break;
        }
        initPlayMode();
    }


    /**
     * 设置滑动条背景
     */
    private void setSeekBarBackground() {
        try {
            int progressColor = CustomAttrValueUtil.getAttrColorValue(R.attr.colorPrimary, R.color.colorAccent, this);
            LayerDrawable layerDrawable = (LayerDrawable) seekBar.getProgressDrawable();
            ScaleDrawable scaleDrawable = (ScaleDrawable) layerDrawable.findDrawableByLayerId(android.R.id.progress);
            GradientDrawable drawable = (GradientDrawable) scaleDrawable.getDrawable();
            drawable.setColor(progressColor);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void play() {
        int musicId;
        musicId = MyMusicUtil.getIntSharedPreference(MusicConstant.KEY_ID);
        if (musicId == -1 || musicId == 0) {
            musicId = dbManager.getFirstId(MusicConstant.LIST_ALLMUSIC);
            Intent intent = new Intent(MusicConstant.MP_FILTER);
            intent.putExtra(MusicConstant.COMMAND, MusicConstant.COMMAND_STOP);
            sendBroadcast(intent);
            Toast.makeText(PlayActivity.this, "歌曲不存在", Toast.LENGTH_SHORT).show();
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


    public void showPopFormBottom() {
        PlayingPopWindow playingPopWindow = new PlayingPopWindow(PlayActivity.this);
        playingPopWindow.showAtLocation(findViewById(R.id.activity_play), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.alpha = 0.7f;
        getWindow().setAttributes(params);

        playingPopWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams params = getWindow().getAttributes();
                params.alpha = 1f;
                getWindow().setAttributes(params);
            }
        });
    }


    private void register() {
        mReceiver = new PlayReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PlayBarFragment.ACTION_UPDATE_UI_PlAYBAR);
        intentFilter.addAction(MusicConstant.UPDATE_PLAY_MODE);
        registerReceiver(mReceiver, intentFilter);
    }


    private void unRegister() {
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
        if (localReceiver != null) {
            unregisterReceiver(localReceiver);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegister();
    }


    class PlayReceiver extends BroadcastReceiver {
        int status;

        /**
         * 接收广播：ACTION_UPDATE_UI_PlAYBAR
         */
        @Override
        public void onReceive(Context context, Intent intent) {  //根据PlayBar的状态来设置图片
            String action = intent.getAction();
            if (action == null)
                return;
            switch (action) {
                case MusicConstant.UPDATE_PLAY_MODE:
                    initPlayMode();
                    break;
                case PlayBarFragment.ACTION_UPDATE_UI_PlAYBAR:
                    initTitle();
                    status = intent.getIntExtra(MusicConstant.STATUS, 0);
                    current = intent.getIntExtra(MusicConstant.KEY_CURRENT, 0);
                    duration = intent.getIntExtra(MusicConstant.KEY_DURATION, 100);
                    switch (status) {  //根据状态来切换图片以及seekBar
                        case MusicConstant.STATUS_STOP:
                            playImage.setSelected(false);
                            break;
                        case MusicConstant.STATUS_PLAY:
                            playImage.setSelected(true);
                            break;
                        case MusicConstant.STATUS_PAUSE:
                            playImage.setSelected(false);
                            break;
                        case MusicConstant.STATUS_RUN:
                            playImage.setSelected(true);
                            seekBar.setMax(duration);
                            seekBar.setProgress(current);
                            break;
                        default:
                            break;
                    }
            }
        }
    }


    /**
     * 设置顶部状态栏
     */
    private void setStyle() {
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
    }
}
