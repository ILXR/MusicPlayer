package com.epic.localmusicnoserver.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.epic.localmusicnoserver.BlueTooth.BlueToothActivity;
import com.epic.localmusicnoserver.BlueTooth.BlueToothManager;
import com.epic.localmusicnoserver.R;
import com.epic.localmusicnoserver.adapter.HomeListViewAdapter;
import com.epic.localmusicnoserver.database.DBManager;
import com.epic.localmusicnoserver.entity.PlayListInfo;
import com.epic.localmusicnoserver.service.MusicPlayerService;
import com.epic.localmusicnoserver.util.MusicConstant;
import com.epic.localmusicnoserver.util.MyMusicUtil;

import java.util.List;


public class HomeActivity extends PlayBarBaseActivity {
    private static final String TAG = "HomeActivity";

    /**
     * 判断app是否是第一次启动
     */
    private boolean isFirst = true;

    private DBManager dbManager;

    private DrawerLayout drawerLayout;

    private NavigationView navView;

    private ImageView navHeadImage;

    private LinearLayout localMusicLayout;

    private LinearLayout lastPlayLayout;

    private LinearLayout myLoveLayout;

    private LinearLayout myListTitleLayout;

    private Toolbar toolbar;

    private TextView localMusicCountText;

    private TextView lastPlayCountText;

    private TextView myLoveCountText;

    private TextView myPlayListCountText;

    /**
     * 添加歌单+
     */
    private ImageView myPlayListAddImage;

    /**
     * 歌单列表view
     */
    private ListView listView;

    private HomeListViewAdapter adapter;

    /**
     * 歌单列表
     */
    private List<PlayListInfo> playListInfo;

    private int count;

    private long exitTime = 0;

    private boolean isStartTheme = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        isFirst = MyMusicUtil.getIsFirst();
        Log.d("isFirst!!!!", String.valueOf(isFirst));
        if (isFirst == true) {  //app是第一次启动
            final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("EpicMusic");
            dialog.setMessage("第一次启动EpicMusic，是否先扫描本地音乐");
            dialog.setCancelable(true);
            dialog.setPositiveButton("是", new DialogInterface.OnClickListener() {  //跳转到扫描activity
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = new Intent(HomeActivity.this, ScanActivity.class);
                    startActivity(intent);
                }
            });
            dialog.setNegativeButton("否", new DialogInterface.OnClickListener() {  //直接消失
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });
            dialog.show();
            isFirst = false;
            MyMusicUtil.setIsFirst(isFirst);  //存储状态
        }

        dbManager = DBManager.getInstance(HomeActivity.this);

        toolbar = (Toolbar) findViewById(R.id.home_activity_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.drawer_menu);
        }

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navView = (NavigationView) findViewById(R.id.nav_view);
        View headerView = navView.getHeaderView(0);
        navHeadImage = (ImageView) headerView.findViewById(R.id.nav_head_bg_iv);
        loadBingPic();

        refreshNightModeTitle();

        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                drawerLayout.closeDrawers();
                switch (item.getItemId()) {
                    case R.id.nav_bluetooth: // 蓝牙设备
                        Intent intentBlueTooth = new Intent(HomeActivity.this, BlueToothActivity.class);
                        startActivity(intentBlueTooth);
                        break;
                    case R.id.nav_theme:  //主题中心
                        isStartTheme = true;
                        Intent intentTheme = new Intent(HomeActivity.this, ThemeActivity.class);
                        startActivity(intentTheme);
                        break;
                    case R.id.nav_night_mode:  //夜间模式
                        if (MyMusicUtil.getNightMode(HomeActivity.this)) {   //当前为夜间模式，则恢复之前的主题
                            MyMusicUtil.setNightMode(HomeActivity.this, false);  //点击之后是日间模式
                            int preTheme = MyMusicUtil.getPreTheme(HomeActivity.this);  //得到夜间模式之前的日间模式的颜色主题
                            MyMusicUtil.setTheme(HomeActivity.this, preTheme);
                        } else {  //当前为日间模式，则切换到夜间模式
                            MyMusicUtil.setNightMode(HomeActivity.this, true);
                            MyMusicUtil.setTheme(HomeActivity.this, ThemeActivity.THEME_SIZE - 1);
                        }
                        recreate();  //重新加载之后才能切换成功
                        refreshNightModeTitle();
                        break;
                    case R.id.nav_about_me:  //关于
                        Intent aboutTheme = new Intent(HomeActivity.this, AboutActivity.class);
                        startActivity(aboutTheme);
                        break;
                    case R.id.nav_logout:  //退出
                        finish();
                        Intent intentBroadcast = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
                        intentBroadcast.putExtra(MusicConstant.COMMAND, MusicConstant.COMMAND_RELEASE);
                        sendBroadcast(intentBroadcast);
                        Intent stopIntent = new Intent(HomeActivity.this, MusicPlayerService.class);
                        stopService(stopIntent);
                        break;
                }
                return true;
            }
        });

        init();

        Intent startIntent = new Intent(HomeActivity.this, MusicPlayerService.class);  //开启后台服务
        startService(startIntent);

    }

    /**
     * 滑动窗口显示的文字
     */
    private void refreshNightModeTitle() {
        if (MyMusicUtil.getNightMode(HomeActivity.this)) {
            navView.getMenu().findItem(R.id.nav_night_mode).setTitle("日间模式");
        } else {
            navView.getMenu().findItem(R.id.nav_night_mode).setTitle("夜间模式");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        count = dbManager.getMusicCount(MusicConstant.LIST_ALLMUSIC);
        localMusicCountText.setText(count + "");
        count = dbManager.getMusicCount(MusicConstant.LIST_LASTPLAY);
        lastPlayCountText.setText(count + "");
        count = dbManager.getMusicCount(MusicConstant.LIST_MYLOVE);
        myLoveCountText.setText(count + "");
        count = dbManager.getMusicCount(MusicConstant.LIST_MYPLAY);
        myPlayListCountText.setText("(" + count + ")");
        adapter.updateDataList();
    }

    /**
     * 初始化控件
     */
    private void init() {
        localMusicLayout = (LinearLayout) findViewById(R.id.home_local_music_ll);
        lastPlayLayout = (LinearLayout) findViewById(R.id.home_recently_music_ll);
        myLoveLayout = (LinearLayout) findViewById(R.id.home_my_love_music_ll);
        myListTitleLayout = (LinearLayout) findViewById(R.id.home_my_list_title_ll);
        listView = (ListView) findViewById(R.id.home_my_list_lv);
        localMusicCountText = (TextView) findViewById(R.id.home_local_music_count_tv);
        lastPlayCountText = (TextView) findViewById(R.id.home_recently_music_count_tv);
        myLoveCountText = (TextView) findViewById(R.id.home_my_love_music_count_tv);
        myPlayListCountText = (TextView) findViewById(R.id.home_my_list_count_tv);
        myPlayListAddImage = (ImageView) findViewById(R.id.home_my_pl_add_iv);

        localMusicLayout.setOnClickListener(new View.OnClickListener() {  //本地音乐
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, LocalMusicActivity.class);
                startActivity(intent);
            }
        });

        lastPlayLayout.setOnClickListener(new View.OnClickListener() {  //最近音乐
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, LastMyloveActivity.class);
                intent.putExtra(MusicConstant.LABEL, MusicConstant.LABEL_LAST);
                startActivity(intent);
            }
        });

        myLoveLayout.setOnClickListener(new View.OnClickListener() {  //我的喜爱
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, LastMyloveActivity.class);
                intent.putExtra(MusicConstant.LABEL, MusicConstant.LABEL_MYLOVE);
                startActivity(intent);
            }
        });

        playListInfo = dbManager.getMyPlayList();
        adapter = new HomeListViewAdapter(playListInfo, this, dbManager);
        listView.setAdapter(adapter);
        myPlayListAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                View view = LayoutInflater.from(HomeActivity.this).inflate(R.layout.dialog_create_playlist, null);
                final EditText playlistEdit = (EditText) view.findViewById(R.id.dialog_playlist_name_et);
                builder.setView(view);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = playlistEdit.getText().toString();
                        if (TextUtils.isEmpty(name)) {
                            Toast.makeText(HomeActivity.this, "请输入歌单名", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        dbManager.createPlaylist(name);
                        dialog.dismiss();
                        adapter.updateDataList();
                    }
                });

                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                builder.show();//配置好后再builder show
            }
        });

    }


    /**
     * 更新歌单的数量
     */
    public void updatePlaylistCount() {
        count = dbManager.getMusicCount(MusicConstant.LIST_MYPLAY);
        myPlayListCountText.setText("(" + count + ")");
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (isStartTheme) {  //如果去设置主题
            HomeActivity.this.finish();  //让活动结束，否则主题切换不成功
        }
        isStartTheme = false;
    }

    /**
     * 设置双次返回
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                drawerLayout.closeDrawer(Gravity.START);
                Toast.makeText(getApplicationContext(), "再按一次切换到桌面", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                moveTaskToBack(true);
            }
            return true;
        }
        else{
            return super.onKeyDown(keyCode, event);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BlueToothManager.getInstance().Destroy();
    }

    /**
     * 加载滑动窗口上方的图像
     */
    private void loadBingPic() {
        navHeadImage.setImageResource(R.drawable.bg_playlist);
    }
}
