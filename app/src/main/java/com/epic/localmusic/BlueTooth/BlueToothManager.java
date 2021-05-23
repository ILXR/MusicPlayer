package com.epic.localmusic.BlueTooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.os.Handler;

public class BlueToothManager {
    private static final String TAG = "BlueToothManager";

    private Handler          uiHandler;
    private BluetoothAdapter bluetoothAdapter;


    private BluetoothSocket bluetoothSocket;
    private Thread          bluetoothThread;
    private OutputStream    outStream;
    private InputStream     inStream;
    private BluetoothDevice device;

    private static BlueToothManager Instance;

    public BlueToothManager() {
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public static BlueToothManager getInstance() {
        if (Instance == null) {
            Instance = new BlueToothManager();
        }
        return Instance;
    }

    public void setHandler(Handler handler) {
        this.uiHandler = handler;
    }

    public void resetHandler() {
        this.uiHandler = null;
    }

    public String getDeviceName() {
        if (device != null) {
            return device.getName();
        } else {
            return "";
        }
    }

    public BluetoothDevice getDevice() {
        return this.device;
    }

    public void disconnect() {
        device = null;
        try {
            if (bluetoothThread != null) {
                bluetoothThread.interrupt();
            }
            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
            }
            if (outStream != null) {
                outStream.close();
            }
            if (inStream != null) {
                inStream.close();
            }
            if (bluetoothSocket != null) {
                bluetoothSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "disconnect: Failed");
        }
    }

    private void sendUiMsg(int what) {
        if (uiHandler != null) {
            Message msg = new Message();
            msg.what = what;
            uiHandler.sendMessage(msg);
        }
    }

    private void sendUiMsg(int what, String obj) {
        if (uiHandler != null) {
            Message msg = new Message();
            msg.what = what;
            msg.obj = obj;
            uiHandler.sendMessage(msg);
        }
    }

    public void connect(BluetoothDevice device) {
        disconnect();
        this.device = device;
        try {
            bluetoothSocket = device.createRfcommSocketToServiceRecord(UUID.fromString(Params.UUID));
        } catch (IOException e) {
            e.printStackTrace();
        }

        bluetoothThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    bluetoothSocket.connect();
                    outStream = bluetoothSocket.getOutputStream();
                    inStream = bluetoothSocket.getInputStream();
                    sendUiMsg(Params.MSG_CONNECT_SUCCEED);
                } catch (IOException e) {
                    e.printStackTrace();
                    sendUiMsg(Params.MSG_CONNECT_FAILED);
                    return;
                }

                byte[] buffer = new byte[1024];
                int len;
                String content;
                while (!bluetoothThread.isInterrupted()) {
                    try {
                        if ((len = inStream.read(buffer)) != -1) {
                            content = new String(buffer, 0, len);
                            sendUiMsg(Params.MSG_CLIENT_REV_NEW, content);
                            Log.i(TAG, "------------- blueTooth read data : " + content);
                            // TODO 处理蓝牙接收数据
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        break;
                    }
                }
            }
        });
        bluetoothThread.start();

    }

    public void send(String data) {
        Log.e(TAG, "send: " + data);
        try {
            if (bluetoothSocket.isConnected() && outStream != null) {
                outStream.write(data.getBytes("utf-8"));
            } else {
                Log.i(TAG, "send: bluetooth hasn't connected");
            }
            Log.i(TAG, "---------- send data ok " + data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void Destroy() {
        disconnect();
        resetHandler();
    }
}
