package com.proton.ecgpatch.connector.utils;

import android.content.Context;
import android.text.TextUtils;

import java.io.File;

import no.nordicsemi.android.dfu.DfuProgressListener;
import no.nordicsemi.android.dfu.DfuProgressListenerAdapter;
import no.nordicsemi.android.dfu.DfuServiceController;
import no.nordicsemi.android.dfu.DfuServiceInitiator;
import no.nordicsemi.android.dfu.DfuServiceListenerHelper;

/**
 * Created by 王梦思 on 2018-11-06.
 * <p/>
 */
public class PatchFirmwareUpdateManager {
    private String filePath;
    private String macAddress;
    private DfuServiceController controller;
    private Context mContext;
    private OnFirewareUpdateListener onFirewareUpdateListener;
    private DfuProgressListener mDfuProgressListener = new DfuProgressListenerAdapter() {

        @Override
        public void onDeviceConnected(String deviceAddress) {
            super.onDeviceConnected(deviceAddress);
            if (onFirewareUpdateListener != null) {
                onFirewareUpdateListener.onConnectSuccess();
            }
        }

        @Override
        public void onProgressChanged(String deviceAddress, int percent, float speed, float avgSpeed, int currentPart, int partsTotal) {
            if (onFirewareUpdateListener != null) {
                onFirewareUpdateListener.onProgress(percent / 100.0f);
            }
        }

        @Override
        public void onDeviceDisconnecting(String deviceAddress) {
        }

        @Override
        public void onDfuCompleted(String deviceAddress) {
            if (onFirewareUpdateListener != null) {
                onFirewareUpdateListener.onSuccess();
            }
        }

        @Override
        public void onError(String deviceAddress, int error, int errorType, String message) {
            updateFail(message);
        }
    };

    public PatchFirmwareUpdateManager(Context context, String filePath, String macaddress) {
        this.mContext = context;
        this.filePath = filePath;
        this.macAddress = macaddress;
    }

    public PatchFirmwareUpdateManager update() {
        if (TextUtils.isEmpty(filePath) || !new File(filePath).exists()) {
            updateFail("固件不存在");
            return this;
        }

        DfuServiceInitiator starter = new DfuServiceInitiator(macAddress);
        starter.setUnsafeExperimentalButtonlessServiceInSecureDfuEnabled(true);
        starter.setZip(filePath);
        starter.setDisableNotification(true);
        starter.setForeground(false);
        DfuServiceListenerHelper.registerProgressListener(mContext, mDfuProgressListener);
        controller = starter.start(mContext, DFUService.class);
        return this;
    }

    public void stopUpdate() {
        if (controller != null) {
            DfuServiceListenerHelper.unregisterProgressListener(mContext, mDfuProgressListener);
            controller.abort();
        }
    }

    private void updateFail(String msg) {
        if (onFirewareUpdateListener != null) {
            onFirewareUpdateListener.onFail(msg);
        }
        stopUpdate();
    }

    public void setOnFirewareUpdateListener(OnFirewareUpdateListener onFirewareUpdateListener) {
        this.onFirewareUpdateListener = onFirewareUpdateListener;
    }

    public interface OnFirewareUpdateListener {

        void onConnectSuccess();

        void onConnectFail();

        void onSuccess();

        void onFail(String msg);

        void onProgress(float progress);
    }
}
