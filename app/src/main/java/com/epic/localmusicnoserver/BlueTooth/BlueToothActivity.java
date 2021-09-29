package com.epic.localmusicnoserver.BlueTooth;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.epic.localmusicnoserver.R;
import com.epic.localmusicnoserver.activity.BaseActivity;
import com.epic.localmusicnoserver.fragment.DataTransFragment;
import com.epic.localmusicnoserver.fragment.DeviceListFragment;
import com.epic.localmusicnoserver.util.EpicParams;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class BlueToothActivity extends BaseActivity {

    final String TAG = "BlueToothActivity";

    TabLayout      tabLayout;
    ViewPager      viewPager;
    MyPagerAdapter pagerAdapter;
    String[]       titleList    = new String[]{"设备列表", "数据传输"};
    List<Fragment> fragmentList = new ArrayList<>();

    DeviceListFragment deviceListFragment;
    DataTransFragment  dataTransFragment;

    Handler uiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EpicParams.MSG_REV_A_CLIENT:
                    Log.e(TAG, "--------- uihandler set device name, go to data frag");
                    BluetoothDevice clientDevice = (BluetoothDevice) msg.obj;
                    dataTransFragment.receiveClient(clientDevice);
                    viewPager.setCurrentItem(1);
                    break;
                case EpicParams.MSG_CONNECT_TO_SERVER:
                    Log.e(TAG, "--------- uihandler set device name, go to data frag");
                    BluetoothDevice serverDevice = (BluetoothDevice) msg.obj;
                    dataTransFragment.connectServer(serverDevice);
                    viewPager.setCurrentItem(1);
                    break;
                case EpicParams.MSG_SERVER_REV_NEW:
                    String newMsgFromClient = msg.obj.toString();
                    dataTransFragment.updateDataView(newMsgFromClient, EpicParams.REMOTE);
                    break;
                case EpicParams.MSG_CLIENT_REV_NEW:
                    String newMsgFromServer = msg.obj.toString();
                    if (dataTransFragment != null) {
                        dataTransFragment.updateDataView(newMsgFromServer, EpicParams.REMOTE);
                    }
                    break;
                case EpicParams.MSG_WRITE_DATA:
                    String dataSend = msg.obj.toString();
                    dataTransFragment.updateDataView(dataSend, EpicParams.ME);
                    deviceListFragment.writeData(dataSend);
                    break;

                case EpicParams.MSG_CONNECT_FAILED:
                    toast("蓝牙连接失败！");
                    break;
                case EpicParams.MSG_CONNECT_SUCCEED:
                    String name = BlueToothManager.getInstance().getDeviceName();
                    toast("蓝牙连接成功，设备" + name);
                    Log.i(TAG, "handleMessage: 蓝牙连接成功，设备" + name);
                    if (name.equals("RC1033")) {
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Log.i(TAG, "发送STARTCODE");
                                BlueToothManager.getInstance().sendByteArray(EpicParams.START_BYTE_ARRAY);
                            }
                        }, 1000);
                        break;
                    }
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case EpicParams.MY_PERMISSION_REQUEST_CONSTANT:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted 授予权限
                    //处理授权之后逻辑
                } else {
                    // Permission Denied 权限被拒绝
                    Toast.makeText(this, "权限被禁用", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        initUI();
        Toolbar bar = findViewById(R.id.toolBar);
        setSupportActionBar(bar);
        checkBTPermission();
    }

    private void checkBTPermission() {
        Log.d(TAG, "checkBTPermission: Start");
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            int permissionCheck = 0;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                permissionCheck = this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
                permissionCheck += this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
                if (permissionCheck != 0) {
                    this.requestPermissions(new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    }, 1001); //any number
                } else {
                    Log.d(TAG,
                            "checkBTPermissions: No need to check permissions.");
                }
            }
        }
        Log.d(TAG, "checkBTPermission: Finish");
    }

    /**
     * 返回 uiHandler
     *
     * @return
     */
    public Handler getUiHandler() {
        return uiHandler;
    }

    /**
     * 初始化界面
     */
    private void initUI() {
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);

        tabLayout.addTab(tabLayout.newTab().setText(titleList[0]));
        tabLayout.addTab(tabLayout.newTab().setText(titleList[1]));

        deviceListFragment = new DeviceListFragment();
        dataTransFragment = new DataTransFragment();
        fragmentList.add(deviceListFragment);
        fragmentList.add(dataTransFragment);

        pagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    /**
     * Toast 提示
     */
    public void toast(String str) {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    /**
     * ViewPager 适配器
     */
    public class MyPagerAdapter extends FragmentPagerAdapter {

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titleList[position];
        }
    }
}
