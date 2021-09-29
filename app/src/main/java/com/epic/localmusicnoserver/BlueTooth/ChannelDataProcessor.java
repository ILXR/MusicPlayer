package com.epic.localmusicnoserver.BlueTooth;

import java.util.ArrayDeque;
import java.util.Deque;

public class ChannelDataProcessor {
    private static final String TAG = "ChannelDataProcessor";

    // algorithm params
    private static final Double  startActionThreshold = 0.12d;
    private static final Integer minActionSize        = 20;
    private static final int     rollingMeanSize      = 5;
    private static final int     initSize             = 120;
    private static final double  stableThreshold      = 0.08d;

    private final Deque<Double> cacheQueue;
    private final Deque<Double> baselineQue;
    private       Double        meanValue;
    private       boolean       startRecordValue;
    private       boolean       hasInit;
    private       boolean       inAction;
    private       boolean       actionValid;
    private       int           actionSize;
    private       Double        maxActionValue;
    private       Double        baseLine;

    public ChannelDataProcessor() {
        meanValue = 0d;
        actionSize = 0;
        maxActionValue = 0d;
        cacheQueue = new ArrayDeque<>();
        baselineQue = new ArrayDeque<>();
        inAction = false;
        hasInit = false;
        actionValid = false;
        startRecordValue = false;
    }

    public static double range(Deque<Double> in) {
        if (in == null) {
            throw new java.lang.NumberFormatException();
        }
        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;
        for (Double item : in) {
            max = Math.max(max, item);
            min = Math.min(min, item);
        }
        return max - min;
    }

    public void addData(Double data) {
        baselineQue.offerLast(data);
        if (baselineQue.size() > initSize) {
            baselineQue.pollFirst();
            double sum = 0d, max = Double.MIN_VALUE, min = Double.MAX_VALUE;
            for (Double item : baselineQue) {
                max = Math.max(max, item);
                min = Math.min(min, item);
                sum += item;
            }
            if (max - min <= stableThreshold) {
                hasInit = true;
                baseLine = sum / initSize;
            }
        }
        if (hasInit) {
            data -= baseLine;
            addDataToQueue(data);
            addDataToList();
        }
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
        if (meanValue <= startActionThreshold && inAction) {
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
