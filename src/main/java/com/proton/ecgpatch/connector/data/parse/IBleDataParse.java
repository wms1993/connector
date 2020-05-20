package com.proton.ecgpatch.connector.data.parse;

import java.util.List;

/**
 * Created by 王梦思 on 2017/8/7.
 */

public interface IBleDataParse {
    /**
     * 解析电量
     */
    int parseBattery(byte[] value);

    /**
     * 解析剩余内存
     */
    int parseMemory(byte[] value);

    /**
     * 解析版本号
     */
    String parseHardVersion(byte[] value);

    /**
     * 解析序列号
     */
    String parseSerial(byte[] value);

    /**
     * 解析ecg数据
     */
    List<Float> parseEcgData(byte[] value, float gainMultiple);
}
