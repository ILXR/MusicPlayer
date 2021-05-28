package com.epic.localmusic.BlueTooth;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.epic.localmusic.WebSocket.WebSocketHandler;
import com.epic.localmusic.util.EpicParams;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class BtDataProcessor {
    private static final String TAG = "BtDataProcessor";

    private static       BtDataProcessor Instance;
    private static final int             WinSize         = 10;
    private static final int             DataSize        = 130;
    private static final Double          actionThreshold = 0.3;

    private       boolean                       startProcess; // 开始处理
    private       boolean                       startAction; // 开始搜集动作数据
    private final ArrayDeque<ArrayList<Double>> window; // 滑动窗口
    private final ArrayList<ArrayList<Double>>  listToSend; // 要发送的数据队列
    private final StringBuffer                  bufferLast; // String缓存
    private final Random                        random;


    public BtDataProcessor() {
        this.bufferLast = new StringBuffer();
        this.listToSend = new ArrayList<>();
        this.window = new ArrayDeque<>();
        this.startProcess = false;
        this.startAction = false;
        this.random = new Random(System.currentTimeMillis());
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

    private ArrayList<Double> formatString(String str) {
        try {
            ArrayList<Double> result = new ArrayList<>();
            List<String> tem = Arrays.asList(str.split(","));
            List<String> temValue = Arrays.asList(tem.get(2).split(" "));
            Double[] param = EpicParams.BtValueParams;
            for (String s : temValue.subList(0, temValue.size() - 1)) {
                Double val = Double.parseDouble(s);
                double res = param[0] * Math.pow(val, 4) + param[1] * Math.pow(val, 3) + param[2] * Math.pow(val, 2) + param[3] * val + param[4];
                result.add((double) Math.round(res * 100) / 100);
            }
            return result;
        } catch (Exception e) {
            Log.e(TAG, "formatString: Error");
            e.printStackTrace();
            return null;
        }
    }

    public void processString(String str) {
        if (!startProcess) {
            return;
        }
        addString(str);
        if (startAction) {
            // 检测到动作，开始搜集发送数据
            if (bufferLast.length() > 0) {
                ArrayList<Double> data;
                while (listToSend.size() < DataSize && (data = getNextData()) != null) {
                    listToSend.add(data);
                }
                if (listToSend.size() == DataSize) {
                    sendDataList();
                }
            }
        } else {
            // 滑动窗口检测电容值波动
            if (bufferLast.length() > 0) {
                ArrayList<Double> data;
                while (window.size() < WinSize && (data = getNextData()) != null) {
                    // 填充滑动窗口
                    window.add(data);
                }
                // 检测异常
                if (window.size() == WinSize) {
                    int randIndex = random.nextInt(window.getFirst().size());
                    if (window.getLast().get(randIndex) - window.getFirst().get(randIndex) > actionThreshold) {
                        while (window.size() > 0) {
                            listToSend.add(window.getFirst());
                            window.pop();
                        }
                        startAction = true;
                    } else {
                        window.pop(); // 弹出第一个
                    }
                }
            }
        }

    }


    private ArrayList<Double> getNextData() {
        int index;
        if (bufferLast.length() > 0 && (index = bufferLast.indexOf("#", 1)) != -1) {
            String str = bufferLast.substring(0, index).replace("\n", "");
            ArrayList<Double> result;
            bufferLast.delete(0, index);
            if (str.length() > 8 && (result = formatString(str)) != null && result.size() > 0) {
                return result;
            }
        }
        return null;
    }

    private void sendDataList() {
        // TODO 给服务器发送的json字符串
        JSONObject data = new JSONObject();
        data.put("data", JSONArray.parseArray(JSON.toJSONString(listToSend)));
        Log.i(TAG, "sendDataList: " + data.toJSONString());
        WebSocketHandler.getInstance().send(data.toJSONString());
        clear();
    }

    private void clear() {
        bufferLast.setLength(0);
        startAction = false;
        listToSend.clear();
        window.clear();
    }

    public void startProcess() {
        clear();
        startProcess = true;
    }

    public void endProcess() {
        startProcess = false;
        clear();
    }

}
