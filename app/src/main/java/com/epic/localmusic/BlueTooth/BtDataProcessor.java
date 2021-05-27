package com.epic.localmusic.BlueTooth;


import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.epic.localmusic.WebSocket.WebSocketHandler;

import java.util.ArrayList;

public class BtDataProcessor {
    private static final String TAG = "BtDataProcessor";

    private static       BtDataProcessor Instance;
    private static final int             MaxCount = 50;

    private final ArrayList<String> listToSend;
    private final StringBuffer      bufferLast; // 不完整的数据
    private       int               count; // 数据组数
    private       boolean           start;

    public BtDataProcessor() {
        this.count = 0;
        this.bufferLast = new StringBuffer();
        this.listToSend = new ArrayList<>();
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
                String data = bufferLast.substring(index, temp).replace("\n", "");
                if (data.length() > 50) {
                    // 丢弃错误数据
                    listToSend.add(data);
                    count++;
                }
                index = temp;
            }
            bufferLast.delete(0, index);
        }
        Log.i(TAG, "processString: length"+bufferLast.length());
        Log.i(TAG, "processString: size"+listToSend.size());
        if (count == MaxCount) {
            sendStringList();
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

    private void sendStringList() {
        // TODO 给服务器发送的json字符串
        JSONObject data = new JSONObject();
        JSONArray array = JSONArray.parseArray(JSON.toJSONString(listToSend));
        data.put("data", array);
        WebSocketHandler.getInstance().send(data.toJSONString());
        listToSend.clear();
        count = 0;
    }

    private void clear() {
        bufferLast.setLength(0);
        listToSend.clear();
        count = 0;
    }

    public void startProcess() {
        clear();
        this.start = true;
        //new Thread(new Runnable() {
        //    @Override
        //    public void run() {
        //        while(start){
        //            Thread.sleep(2000);
        //        }
        //    }
        //}).start();
    }

    public void endProcess() {
        this.start = false;
        clear();
    }

}
