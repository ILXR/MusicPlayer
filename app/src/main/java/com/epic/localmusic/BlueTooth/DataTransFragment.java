package com.epic.localmusic.BlueTooth;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.epic.localmusic.R;
import com.epic.localmusic.util.EpicParams;

/**
 * Created by Administrator on 2017/4/4.
 */
public class DataTransFragment extends Fragment {

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
     * ??????????????????(?????????)??????
     */
    public void receiveClient(BluetoothDevice clientDevice) {
        this.remoteDevice = clientDevice;
        connectNameTv.setText("????????????: " + remoteDevice.getName());
    }

    /**
     * ???????????????
     *
     * @param newMsg
     */
    public void updateDataView(String newMsg, int role) {
        if (role == EpicParams.REMOTE) {
            String remoteName = remoteDevice.getName() == null ? "???????????????" : remoteDevice.getName();
            newMsg = remoteName + " : " + newMsg;
        } else if (role == EpicParams.ME) {
            newMsg = "??? : " + newMsg;
        }
        dataListAdapter.insert(newMsg, 0);
        //dataListAdapter.add(newMsg);
    }

    /**
     * ?????????????????????????????????????????????
     *
     * @param serverDevice
     */
    public void connectServer(BluetoothDevice serverDevice) {
        this.remoteDevice = serverDevice;
        connectNameTv.setText("????????????: " + remoteDevice.getName());
    }
}
