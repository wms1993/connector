package com.proton.ecgpatch.connector.data.uuid;

/**
 * Created by 王梦思 on 2017/8/7.
 */

public interface IDeviceUUID {
    /**
     * ECG心电数据服务uuid
     */
    String getServiceUUID();

    /**
     * 写的uuid
     */
    String getNotifyCharactor();

    /**
     * 读的uuid
     */
    String getWriteCharactor();
    /**
     * 设备信息服务uuid
     */
    String getDeviceInfoServiceUUID();

    /**
     * 硬件版本Charactor uuid
     */
    String getCharactorVersionUUID();

    /**
     * 电量uuid
     */
    String getCharactorBatteryUUID();

    /**
     * 序列号uuid
     */
    String getCharactorSearialUUID();
}
