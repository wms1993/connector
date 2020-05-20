package com.proton.ecgpatch.connector.callback;

import java.util.List;

/**
 * 数据接收监听器
 */
public abstract class DataListener {
    /**
     * 接收ecg滤波处理后数据
     *
     * @param ecgDatas 算法处理后的数据每一包
     */
    public void receiveEcgFilterData(List<Float> ecgDatas) {
    }

    /**
     * 接收ecg源数据
     *
     * @param sourceData 源数据
     */
    public void receiveEcgSourceData(List<Float> sourceData) {
    }

    /**
     * 接收体温
     */
    public void receiveTemp(float temp) {
    }

    /**
     * 实时心率
     *
     * @param rate 算法处理后的数据集大小
     */
    public void receiverHeartRate(int rate) {
    }

    /**
     * 接收包序号
     */
    public void receivePackageNum(int packageNum) {
    }

    /**
     * 接收数据校验
     */
    public void receiveDataValidate(boolean isValidate) {
    }

    /**
     * 接收是否跌倒
     */
    public void receiveFallDown(boolean isFallDown) {
    }

    /**
     * 接收导电脱落
     */
    public void receiveFallOff(boolean isFallOff) {
    }

    /**
     * 接收采样率
     */
    public void receiveSample(int sample) {
    }

    /**
     * 信号是否干扰
     *
     * @param signalQualityIndex 0 Pvalue<2.5, 1 2.5<=Pvalue<4, 2 Pvalue>4
     *                           0，信号质量好, 1，信号质量差, 2，信号质量差，数据没有意义
     */
    public void signalInterference(int signalQualityIndex) {
    }

    /**
     * 读取电量
     */
    public void receiveBattery(Integer battery) {
    }

    /**
     * 读取内存
     */
    public void receiveMemory(Integer memory) {
    }

    /**
     * 读取序列号
     */
    public void receiveSerial(String serial) {
    }

    /**
     * 读取硬件版本号
     */
    public void receiveHardVersion(String hardVersion) {
    }

}