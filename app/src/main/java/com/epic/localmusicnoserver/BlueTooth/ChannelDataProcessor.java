package com.epic.localmusicnoserver.BlueTooth;

import java.util.ArrayDeque;
import java.util.ArrayList;

public class ChannelDataProcessor {
    private static final String TAG = "ChannelDataProcessor";

    // algorithm params
    private static final Double  startActionThreshold = 0.125d;
    private static final Integer minActionSize        = 20;
    private static final int     rollingMeanSize      = 10;
    private static final int     initSize             = 100;

    private       Double             meanValue;
    private final ArrayDeque<Double> cacheQueue;
    private       ArrayList<Double>  initArray;

    private boolean startRecordValue;
    private boolean hasInit;
    private boolean inAction;
    private boolean actionValid;
    private int     actionSize;
    private Double  maxActionValue;
    private Double  baseLine;

    public ChannelDataProcessor() {
        meanValue = 0d;
        actionSize = 0;
        maxActionValue = 0d;
        cacheQueue = new ArrayDeque<>();
        initArray = new ArrayList<>();
        inAction = false;
        hasInit = false;
        actionValid = false;
        startRecordValue = false;
    }

    public void addData(Double data) {
        if (!hasInit) {
            initArray.add(data);
            if (initArray.size() >= initSize) {
                hasInit = true;
                Double sum = 0d;
                for (Double dou : initArray) {
                    sum += dou;
                }
                baseLine = sum / initArray.size();
                initArray = null;
            }
            return;
        }
        data -= baseLine;
        addDataToQueue(data);
        addDataToList();
    }

    public boolean isInAction() {
        return inAction;
    }

    public boolean isActionValid() {
        return actionValid;
    }

    private void addDataToQueue(Double data) {
        int size = cacheQueue.size();
        if (size == rollingMeanSize) {
            Double front = cacheQueue.pollFirst();
            meanValue = meanValue - (front - data) / size;
        } else if (size == 0) {
            meanValue = data;
        } else {
            meanValue = (meanValue * size + data) / (size + 1);
        }
        cacheQueue.addLast(data);
    }

    private void addDataToList() {
        if (startRecordValue) {
            maxActionValue = Math.max(maxActionValue, meanValue);
        }
        if (inAction) {
            actionSize++;
        }
        if (meanValue > startActionThreshold && !inAction)
            inAction = true;
        if (meanValue < startActionThreshold && inAction) {
            inAction = false;
            if (actionSize >= minActionSize) {
                actionValid = true;
            }
        }
    }

    public void startRecord() {
        startRecordValue = true;
    }

    public Double getMaxActionValue() {
        return maxActionValue;
    }

    public void clearState() {
        actionSize = 0;
        maxActionValue = 0d;
        inAction = false;
        actionValid = false;
        startRecordValue = false;
    }
}
