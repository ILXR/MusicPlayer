package com.epic.localmusicnoserver.BlueTooth;

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

import com.epic.localmusicnoserver.util.EpicParams;

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
            bluetoothSocket = device.createRfcommSocketToServiceRecord(UUID.fromString(EpicParams.UUID));
        } catch (IOException e) {
            e.printStackTrace();
        }
        BlueToothDataManager.getInstance().init(4);
        bluetoothThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    bluetoothSocket.connect();
                    outStream = bluetoothSocket.getOutputStream();
                    inStream = bluetoothSocket.getInputStream();
                    sendUiMsg(EpicParams.MSG_CONNECT_SUCCEED);
                } catch (IOException e) {
                    e.printStackTrace();
                    sendUiMsg(EpicParams.MSG_CONNECT_FAILED);
                    return;
                }

                byte[] buffer = new byte[1024];
                int len;
                String content;
                while (!bluetoothThread.isInterrupted()) {
                    try {
                        if ((len = inStream.read(buffer)) != -1) {
                            content = new String(buffer, 0, len);
                            sendUiMsg(EpicParams.MSG_CLIENT_REV_NEW, content);
                            BlueToothDataManager.getInstance().processString(content);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        break;
                    }
                }
                //BtDataProcessor.getInstance().endProcess();
            }
        });
        bluetoothThread.start();

    }

    public void send(String data) {
        Log.e(TAG, "send: " + data);
        try {
            if (bluetoothSocket.isConnected() && outStream != null) {
                outStream.write(data.getBytes("utf-8"));
                outStream.flush();
            } else {
                Log.i(TAG, "send: bluetooth hasn't connected");
            }
            Log.i(TAG, "---------- send data ok " + data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendByteArray(byte[] array) {
        try {
            if (bluetoothSocket.isConnected() && outStream != null) {
                outStream.write(array);
                outStream.flush();
            } else {
                Log.i(TAG, "send: bluetooth hasn't connected");
            }
            Log.i(TAG, "---------- send byte data ok ");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendHexString(String data) {
        Log.e(TAG, "send hex string: " + data);
        int len = data.length();
        byte[] hexBytes = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            // 两位一组，表示一个字节,把这样表示的16进制字符串，还原成一个字节
            hexBytes[i / 2] = (byte) ((Character.digit(data.charAt(i), 16) << 4) + Character
                    .digit(data.charAt(i + 1), 16));
        }

        try {
            if (bluetoothSocket.isConnected() && outStream != null) {
                byte[] hex_txt = {(byte) 0xF0, (byte) 0x02, (byte) 0xA2, (byte) 0x0D, (byte) 0x0A};
                outStream.write(hex_txt);
                outStream.flush();
            } else {
                Log.i(TAG, "send: bluetooth hasn't connected");
            }
            Log.i(TAG, "---------- send hex data ok " + data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void Destroy() {
        disconnect();
        resetHandler();
    }
}
