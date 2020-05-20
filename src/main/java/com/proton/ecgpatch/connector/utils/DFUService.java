package com.proton.ecgpatch.connector.utils;

import android.app.Activity;

import com.proton.ecgpatch.connector.BuildConfig;

import no.nordicsemi.android.dfu.DfuBaseService;

public class DFUService extends DfuBaseService {

    @Override
    protected Class<? extends Activity> getNotificationTarget() {
        return null;
    }

    @Override
    protected boolean isDebug() {
        return BuildConfig.DEBUG;
    }
}
