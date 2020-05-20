package com.proton.ecgpatch.connector.data.uuid;

/**
 * Created by 王梦思 on 2017/8/7.
 * 心电卡uuid
 */

public class EcgPatchUUID implements IDeviceUUID {
    /**
     * 服务
     */
    private static final String SERVICE_UUID = "0000fff6-0000-1000-8000-00805f9b34fb";
    /**
     * 可读特征
     */
    private static final String CHARACTOR_NOTIFY = "0000fff7-0000-1000-8000-00805f9b34fb";
    /**
     * 可读特征
     */
    private static final String CHARACTOR_WRITE = "0000fff8-0000-1000-8000-00805f9b34fb";
    /**
     * 服务:设备信息服务
     */
    private static final String SERVICE_DEVICE_INFO = "0000180a-0000-1000-8000-00805f9b34fb";
    /**
     * 特征:设备版本号（可读）
     */
    private static final String CHARACTOR_VERSION = "00002a26-0000-1000-8000-00805f9b34fb";
    /**
     * 特征:序列号（可读）
     */
    private static final String CHARACTOR_SEARIAL = "00002a25-0000-1000-8000-00805f9b34fb";
    /**
     * 特征:电量（可读可订阅）
     */
    private static final String CHARACTOR_BATTERY = "0000fff9-0000-1000-8000-00805f9b34fb";

    @Override
    public String getServiceUUID() {
        return SERVICE_UUID;
    }

    @Override
    public String getNotifyCharactor() {
        return CHARACTOR_NOTIFY;
    }

    @Override
    public String getWriteCharactor() {
        return CHARACTOR_WRITE;
    }

    @Override
    public String getDeviceInfoServiceUUID() {
        return SERVICE_DEVICE_INFO;
    }

    @Override
    public String getCharactorVersionUUID() {
        return CHARACTOR_VERSION;
    }

    @Override
    public String getCharactorBatteryUUID() {
        return CHARACTOR_BATTERY;
    }

    @Override
    public String getCharactorSearialUUID() {
        return CHARACTOR_SEARIAL;
    }
}
