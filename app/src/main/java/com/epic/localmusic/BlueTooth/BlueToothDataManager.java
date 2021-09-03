package com.epic.localmusic.BlueTooth;

import android.util.Log;

import com.epic.localmusic.util.EpicParams;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class BlueToothDataManager {
    private final String TAG = "BlueToothDataManager";

    // capacity data
    private int                             ChannelNum;
    private boolean                         hasInit;
    private ArrayList<ChannelDataProcessor> channelManagers;

    // string buffer data
    private       boolean      startProcess;
    private       boolean      anyInAction;
    private       StringBuffer recvBuffer;
    private final int          minValidStrLength   = 20;
    private final int          minProcessStrLength = 100;


    private static BlueToothDataManager _INSTANCE;

    public static synchronized BlueToothDataManager getInstance() {
        if (_INSTANCE == null) {
            _INSTANCE = new BlueToothDataManager();
        }
        return _INSTANCE;
    }

    public BlueToothDataManager() {
        ChannelNum = 0;
        hasInit = false;
    }

    public void init(int channelNum) {
        if (channelNum > 0) {
            hasInit = true;
            anyInAction = false;
            startProcess = true;
            ChannelNum = channelNum;
            recvBuffer = new StringBuffer();
            channelManagers = new ArrayList<>();
            for (int i = 0; i < channelNum; i++) {
                channelManagers.add(new ChannelDataProcessor());
            }
        } else {
            Log.e(TAG, "init: channel cannot be less than one");
        }
    }

    private void addString(String str) {
        if (!hasInit)
            return;
        if (recvBuffer.length() == 0) {
            int start = str.indexOf('#');
            if (start != -1) {
                str = str.substring(start);
            } else {
                return;
            }
        }
        recvBuffer.append(str);
    }

    private boolean anyInAction() {
        boolean inAction = false;
        for (ChannelDataProcessor channel : channelManagers) {
            inAction |= channel.isInAction();
        }
        return inAction;
    }

    private boolean anyActionValid() {
        boolean isValid = false;
        for (ChannelDataProcessor channel : channelManagers) {
            isValid |= channel.isActionValid();
        }
        return isValid;
    }

    private void startRecord() {
        for (ChannelDataProcessor channel : channelManagers) {
            channel.startRecord();
        }
    }

    private void clearState() {
        anyInAction = false;
        for (ChannelDataProcessor channel : channelManagers) {
            channel.clearState();
        }
    }

    public void processString(String str) {
        if (!hasInit || !startProcess)
            return;
        addString(str);
        while (recvBuffer.length() > minProcessStrLength) {
            ArrayList<Double> data;
            if ((data = getNextData()) != null) {
                for (int i = 0; i < ChannelNum; i++) {
                    channelManagers.get(i).addData(data.get(i));
                }
                boolean tem = anyInAction();
                if (tem && !anyInAction) {
                    // TODO Action 开始
                    anyInAction = true;
                    startRecord();
                    Log.i(TAG, "processString: Action Start");
                } else if (!tem && anyInAction) {

                    Log.i(TAG, "processString: Action End");
                    if (anyActionValid()) {
                        Log.i(TAG, "processString: Valid Data");
                        ArrayList<Double> maxValues = new ArrayList<>();
                        for (ChannelDataProcessor channel : channelManagers) {
                            maxValues.add(channel.getMaxActionValue());
                            CommandParsing.ActionType type = CommandParsing.getInstance().commandParseOld(maxValues);
                            if (type != null)
                                CommandParsing.getInstance().act(type);
                            Log.i(TAG, "processString: channel max data - "+channel.getMaxActionValue());
                        }
                        // TODO Action 结束,判断动作类型
                    } else {
                        Log.i(TAG, "processString: invalid Data");
                    }
                    clearState();
                }
            }
        }
    }

    private ArrayList<Double> formatString(String str) {
        try {
            ArrayList<Double> result = new ArrayList<>();
            List<String> tem = Arrays.asList(str.split(","));
            List<String> temValue = Arrays.asList(tem.get(2).split(" "));
            Double[] param = EpicParams.BtValueParams;
            boolean isInitial = true;
            for (String s : temValue.subList(0, temValue.size() - 1)) {
                Double val = Double.parseDouble(s);
                if (!val.equals(-1.36d)) {
                    isInitial = false;
                }
                //double res = param[0] * Math.pow(val, 4) + param[1] * Math.pow(val, 3) + param[2] * Math.pow(val, 2) + param[3] * val + param[4];
                //result.add(Math.round(res * 100) / 100d);
                result.add(val);
            }
            if (isInitial) {
                return null;
            } else
                return result;
        } catch (Exception e) {
            Log.e(TAG, "formatString: Error");
            e.printStackTrace();
            return null;
        }
    }

    private ArrayList<Double> getNextData() {
        int index;
        if (recvBuffer.length() > 0 && (index = recvBuffer.indexOf("#", 1)) != -1) {
            String str = recvBuffer.substring(0, index).replace("\n", "");
            ArrayList<Double> result;
            recvBuffer.delete(0, index);
            if (str.length() > minValidStrLength && (result = formatString(str)) != null && result.size() > 0) {
                return result;
            }
        }
        return null;
    }
}
