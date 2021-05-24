package com.epic.localmusic.BlueTooth;

import android.util.Log;

import com.epic.localmusic.WebSocket.WebSocketHandler;

public class BtDataProcessor {
    private static final String TAG = "BtDataProcessor";

    private static       BtDataProcessor Instance;
    private static final int             MaxCount = 50;

    private StringBuffer bufferToSend;// 完整的数据
    private StringBuffer bufferLast; // 不完整的数据
    private int          count;// 数据组数
    private boolean      start;

    public BtDataProcessor() {
        this.count = 0;
        this.bufferLast = new StringBuffer();
        this.bufferToSend = new StringBuffer();
        this.start = false;
    }

    public static BtDataProcessor getInstance() {
        if (Instance == null) {
            Instance = new BtDataProcessor();
        }
        return Instance;
    }

    private void addString(String str) {
        if (bufferLast.length() == 0) {
            int start = str.indexOf('#');
            if (start != -1) {
                str = str.substring(start);
            } else {
                return;
            }
        }
        bufferLast.append(str);
    }

    public void processString(String str) {
        if (!this.start) {
            return;
        }

        addString(str);
        if (bufferLast.length() > 0) {
            int index = 0, temp;
            while ((temp = bufferLast.indexOf("#", index + 1)) != -1 && count < MaxCount) {
                index = temp;
                count++;
            }
            bufferToSend.append(bufferLast.substring(0, index));
            bufferLast.delete(0, index);
        }
        if (count == MaxCount) {
            sendStringBuffer();
        }
    }

    private static byte[] ConvertToASCII(String string) {
        char[] ch = string.toCharArray();
        byte[] tmp = new byte[ch.length];
        for (int i = 0; i < ch.length; i++) {
            tmp[i] = (byte) Integer.valueOf(ch[i]).intValue();
        }
        return tmp;
    }

    private void sendStringBuffer() {
        String length = Integer.toString(bufferToSend.length());
        int num = 8 - length.length();
        bufferToSend.insert(0, Params.WS_HEADER);
        bufferToSend.insert(8, length);
        while (num > 0) {
            bufferToSend.insert(8, "0");
            num--;
        }
        // TODO 需要将字符串(4个字节)转为字节 已经转了，但是不知道server接收是否正确
        //WebSocketHandler.getInstance().send(bufferToSend.toString());
        WebSocketHandler.getInstance().send(ConvertToASCII(bufferToSend.toString()));
        bufferToSend.setLength(0);
        count = 0;
    }

    private void clearBuffer() {
        bufferLast.setLength(0);
        bufferToSend.setLength(0);
        count = 0;
    }

    public void startProcess() {
        clearBuffer();
        this.start = true;
    }

    public void endProcess() {
        this.start = false;
        clearBuffer();
    }

}
