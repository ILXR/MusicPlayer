package com.epic.localmusicnoserver.fragment;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.epic.localmusicnoserver.BlueTooth.BlueToothActivity;
import com.epic.localmusicnoserver.R;
import com.epic.localmusicnoserver.util.EpicParams;


public class DataTransFragment extends Fragment {

    TextView             connectNameTv;
    ListView             showDataLv;
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
        connectNameTv = view.findViewById(R.id.device_name_tv);
        showDataLv = view.findViewById(R.id.show_data_lv);
        dataListAdapter = new ArrayAdapter<>(getContext(), R.layout.layout_item_new_data);
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
