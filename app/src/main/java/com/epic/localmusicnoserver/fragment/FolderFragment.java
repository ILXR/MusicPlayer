package com.epic.localmusicnoserver.fragment;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.epic.localmusicnoserver.BlueTooth.BlueToothActivity;
import com.epic.localmusicnoserver.BlueTooth.BlueToothManager;
import com.epic.localmusicnoserver.R;
import com.epic.localmusicnoserver.activity.ModelActivity;
import com.epic.localmusicnoserver.adapter.FolderAdapter;
import com.epic.localmusicnoserver.database.DBManager;
import com.epic.localmusicnoserver.entity.FolderInfo;
import com.epic.localmusicnoserver.util.EpicParams;
import com.epic.localmusicnoserver.util.MyMusicUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;


public class FolderFragment extends Fragment {

    private RecyclerView recyclerView;

    private FolderAdapter adapter;

    private List<FolderInfo> folderInfoList = new ArrayList<>();

    private DBManager dbManager;

    private Context mContext;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_singer,container,false);
        dbManager = DBManager.getInstance(getContext());
        recyclerView = (RecyclerView)view.findViewById(R.id.singer_recycler_view);
        adapter = new FolderAdapter(getContext(),folderInfoList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new FolderAdapter.OnItemClickListener() {
            @Override
            public void onContentClick(View content, int position) {
                Intent intent = new Intent(mContext,ModelActivity.class);
                intent.putExtra(ModelActivity.KEY_TITLE,folderInfoList.get(position).getName());
                intent.putExtra(ModelActivity.KEY_TYPE, ModelActivity.FOLDER_TYPE);
                intent.putExtra(ModelActivity.KEY_PATH,folderInfoList.get(position).getPath());
                mContext.startActivity(intent);
            }
        });
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        folderInfoList.clear();
        folderInfoList.addAll(MyMusicUtil.groupByFolder((ArrayList)dbManager.getAllMusicFromMusicTable()));
        adapter.notifyDataSetChanged();
    }

    /**
     * Created by Administrator on 2017/4/4.
     */
    public static class DataTransFragment extends Fragment {

        TextView             connectNameTv;
        ListView             showDataLv;
        EditText             inputEt;
        Button               sendBt;
        ArrayAdapter<String> dataListAdapter;

        BlueToothActivity mainActivity;
        Handler           uiHandler;

        BluetoothDevice remoteDevice;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.layout_data_trans, container, false);
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            connectNameTv = (TextView) view.findViewById(R.id.device_name_tv);
            showDataLv = (ListView) view.findViewById(R.id.show_data_lv);
            //inputEt = (EditText) view.findViewById(R.id.input_et);
            //sendBt = (Button) view.findViewById(R.id.send_bt);
            //sendBt.setOnClickListener(new View.OnClickListener() {
            //    @Override
            //    public void onClick(View v) {
            //        String msgSend = inputEt.getText().toString();
            //        Message message = new Message();
            //        message.what = EpicParams.MSG_WRITE_DATA;
            //        message.obj = msgSend;
            //        uiHandler.sendMessage(message);
            //
            //        inputEt.setText("");
            //    }
            //});

            dataListAdapter = new ArrayAdapter<String>(getContext(), R.layout.layout_item_new_data);
            showDataLv.setAdapter(dataListAdapter);
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            mainActivity = (BlueToothActivity) getActivity();
            uiHandler = mainActivity.getUiHandler();
        }

        /**
         * 显示连接远端(客户端)设备
         */
        public void receiveClient(BluetoothDevice clientDevice) {
            this.remoteDevice = clientDevice;
            connectNameTv.setText("连接设备: " + remoteDevice.getName());
        }

        /**
         * 显示新消息
         *
         * @param newMsg
         */
        public void updateDataView(String newMsg, int role) {
            if (role == EpicParams.REMOTE) {
                String remoteName = remoteDevice.getName() == null ? "未命名设备" : remoteDevice.getName();
                newMsg = remoteName + " : " + newMsg;
            } else if (role == EpicParams.ME) {
                newMsg = "我 : " + newMsg;
            }
            dataListAdapter.insert(newMsg, 0);
            //dataListAdapter.add(newMsg);
        }

        /**
         * 客户端连接服务器端设备后，显示
         *
         * @param serverDevice
         */
        public void connectServer(BluetoothDevice serverDevice) {
            this.remoteDevice = serverDevice;
            connectNameTv.setText("连接设备: " + remoteDevice.getName());
        }
    }

    public static class DeviceListFragment extends Fragment {

        final String TAG = "DeviceListFragment";

        ListView              listView;
        MyListAdapter         listAdapter;
        List<BluetoothDevice> deviceList = new ArrayList<>();

        BluetoothAdapter bluetoothAdapter;
        MyBtReceiver     btReceiver;
        IntentFilter     intentFilter;

        BlueToothActivity mainActivity;
        Handler  uiHandler;
        Activity mContext;


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
            btReceiver = new MyBtReceiver();
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
            listView = (ListView) view.findViewById(R.id.device_list_view);
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
                    if (bluetoothAdapter.isDiscovering()) {
                        bluetoothAdapter.cancelDiscovery();
                    }
                    if (Build.VERSION.SDK_INT >= 6.0) {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},
                                EpicParams.MY_PERMISSION_REQUEST_CONSTANT);
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
            for (BluetoothDevice d :
                    tmp) {
                deviceList.add(d);
            }
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
                    viewHolder.deviceName = (TextView) convertView.findViewById(R.id.device_name);
                    viewHolder.deviceMac = (TextView) convertView.findViewById(R.id.device_mac);
                    viewHolder.deviceState = (TextView) convertView.findViewById(R.id.device_state);
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

        /**
         * 与 adapter 配合的 viewholder
         */
        static class ViewHolder {
            public TextView deviceName;
            public TextView deviceMac;
            public TextView deviceState;
        }

        /**
         * 广播接受器
         */
        private class MyBtReceiver extends BroadcastReceiver {
            @Override
            public void onReceive(Context context, Intent intent) {
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
    }
}
