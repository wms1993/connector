package com.proton.ecgpatch.connector.data.parse;

import com.proton.ecgpatch.connector.utils.BleUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 王梦思 on 2017/8/7.
 * 心电贴数据解析
 */

public class EcgPatchBleDataParse implements IBleDataParse {
    /**
     * 解析电量
     */
    @Override
    public int parseBattery(byte[] value) {
        return value[0];
    }

    @Override
    public int parseMemory(byte[] value) {
        return value[0];
    }

    @Override
    public String parseHardVersion(byte[] value) {
        return new String(value);
    }

    @Override
    public String parseSerial(byte[] value) {
        return new String(value);
    }

    /**
     * 解析ecg数据
     */
    @Override
    public List<Float> parseEcgData(byte[] value, float gainMultiple) {
        List<Float> data = new ArrayList<>();
        String hexString = BleUtils.bytesToHexString(value);
        for (int i = 0; i < hexString.length(); i += 3) {
            data.add((Integer.parseInt(hexString.substring(i, i + 3), 16) * 3.6f / 2048.0f - 3.6f) * (1000 / gainMultiple));
        }
        return data;
    }
}
