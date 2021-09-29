package com.epic.localmusicnoserver.fragment;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.epic.localmusicnoserver.BlueTooth.BlueToothActivity;
import com.epic.localmusicnoserver.BlueTooth.BlueToothManager;
import com.epic.localmusicnoserver.R;
import com.epic.localmusicnoserver.util.EpicParams;
import com.epic.localmusicnoserver.util.MyMusicUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class DeviceListFragment extends Fragment {

    final String TAG = "DeviceListFragment";

    ListView              listView;
    MyListAdapter         listAdapter;
    List<BluetoothDevice> deviceList = new ArrayList<>();
    private final BroadcastReceiver btReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Log.e(TAG, "onReceive: " + intent.getAction());
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                toast("开始搜索 ...");
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                toast("搜索结束");
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (isNewDevice(device)) {
                    deviceList.add(device);
                    listAdapter.notifyDataSetChanged();
                    Log.e(TAG, "---------------- " + device.getName());
                }
            }
        }
    };
    IntentFilter     intentFilter;
    BluetoothAdapter bluetoothAdapter;

    BlueToothActivity mainActivity;
    Handler           uiHandler;
    Activity          mContext;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mContext = getActivity();

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(mContext, "您的设备未找到蓝牙驱动！", Toast.LENGTH_SHORT).show();
            mContext.finish();
        }

        intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        if (mContext != null) {
            mContext.registerReceiver(btReceiver, intentFilter);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_bt_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        listView = view.findViewById(R.id.device_list_view);
        listAdapter = new MyListAdapter();
        listView.setAdapter(listAdapter);
        listAdapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mainActivity = (BlueToothActivity) getActivity();
        if (mainActivity != null) {
            uiHandler = mainActivity.getUiHandler();
        }
        // 蓝牙总管理器
        BlueToothManager.getInstance().setHandler(uiHandler);
    }

    @Override
    public void onResume() {
        super.onResume();
        sendConnectedDevice(BlueToothManager.getInstance().getDevice());

        // 蓝牙未打开，询问打开
        if (!bluetoothAdapter.isEnabled()) {
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), EpicParams.REQUEST_ENABLE_BT);
        }

        // 蓝牙已开启
        if (bluetoothAdapter.isEnabled()) {
            showBondDevice();
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BluetoothDevice device = deviceList.get(position);
                BlueToothManager.getInstance().connect(device);
                sendConnectedDevice(device);
            }
        });
    }

    private void sendConnectedDevice(BluetoothDevice device) {
        // 通知 ui 连接的服务器端设备
        if (uiHandler != null && device != null) {
            Message message = new Message();
            message.what = EpicParams.MSG_CONNECT_TO_SERVER;
            message.obj = device;
            uiHandler.sendMessage(message);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mContext != null) {
            mContext.unregisterReceiver(btReceiver);
        }
        BlueToothManager.getInstance().resetHandler();
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.menu_bluetooth, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.enable_visibility:
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                enableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 600);
                startActivityForResult(enableIntent, EpicParams.REQUEST_ENABLE_VISIBILITY);
                break;
            case R.id.discovery:
                if (!isLocationEnabled()) {
                    MyMusicUtil.showToast("定位权限未打开，无法搜索");
                }
                if (bluetoothAdapter.isDiscovering()) {
                    bluetoothAdapter.cancelDiscovery();
                }
                bluetoothAdapter.startDiscovery();
                break;
            case R.id.disconnect:
                BlueToothManager.getInstance().disconnect();
                showBondDevice();
                listAdapter.notifyDataSetChanged();
                toast("蓝牙连接已关闭");
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean isLocationEnabled() {
        LocationManager locManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        return locManager != null && locManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case EpicParams.REQUEST_ENABLE_BT: {
                if (resultCode == RESULT_OK) {
                    showBondDevice();
                }
                break;
            }
            case EpicParams.REQUEST_ENABLE_VISIBILITY: {
                if (resultCode == 600) {
                    toast("蓝牙已设置可见");
                } else if (resultCode == RESULT_CANCELED) {
                    toast("蓝牙设置可见失败,请重试");
                }
                break;
            }
        }
    }

    /**
     * 用户打开蓝牙后，显示已绑定的设备列表
     */
    private void showBondDevice() {
        deviceList.clear();
        Set<BluetoothDevice> tmp = bluetoothAdapter.getBondedDevices();
        deviceList.addAll(tmp);
        listAdapter.notifyDataSetChanged();
    }

    /**
     * Toast 提示
     */
    public void toast(String str) {
        Toast.makeText(getContext(), str, Toast.LENGTH_SHORT).show();
    }

    /**
     * 向 socket 写入发送的数据
     *
     * @param dataSend
     */
    public void writeData(String dataSend) {
        BlueToothManager.getInstance().send(dataSend);
    }

    /**
     * 判断搜索的设备是新蓝牙设备，且不重复
     *
     * @param device
     * @return
     */
    private boolean isNewDevice(BluetoothDevice device) {
        boolean repeatFlag = false;
        for (BluetoothDevice d :
                deviceList) {
            if (d.getAddress().equals(device.getAddress())) {
                repeatFlag = true;
            }
        }
        //不是已绑定状态，且列表中不重复
        return device.getBondState() != BluetoothDevice.BOND_BONDED && !repeatFlag;
    }

    /**
     * 与 adapter 配合的 viewholder
     */
    static class ViewHolder {
        public TextView deviceName;
        public TextView deviceMac;
        public TextView deviceState;
    }

    /**
     * 设备列表的adapter
     */
    private class MyListAdapter extends BaseAdapter {

        public MyListAdapter() {
        }

        @Override
        public int getCount() {
            return deviceList.size();
        }

        @Override
        public Object getItem(int position) {
            return deviceList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.layout_item_bt_device, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.deviceName = convertView.findViewById(R.id.device_name);
                viewHolder.deviceMac = convertView.findViewById(R.id.device_mac);
                viewHolder.deviceState = convertView.findViewById(R.id.device_state);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            int code = deviceList.get(position).getBondState();
            String name = deviceList.get(position).getName();
            String mac = deviceList.get(position).getAddress();
            String state;
            if (name == null || name.length() == 0) {
                name = "未命名设备";
            }
            if (code == BluetoothDevice.BOND_BONDED) {
                state = "ready";
                viewHolder.deviceState.setTextColor(getResources().getColor(R.color.green));
            } else {
                state = "new";
                viewHolder.deviceState.setTextColor(getResources().getColor(R.color.red));
            }
            if (mac == null || mac.length() == 0) {
                mac = "未知 mac 地址";
            }
            viewHolder.deviceName.setText(name);
            viewHolder.deviceMac.setText(mac);
            viewHolder.deviceState.setText(state);
            return convertView;
        }
    }
}
