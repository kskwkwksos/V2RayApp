package com.example.v2rayapp;

import android.net.VpnService;
import android.content.Intent;
import android.os.IBinder;

import io.nekohasekai.libbox.Libbox;
import io.nekohasekai.libbox.SetupOptions;

public class MyVpnService extends VpnService {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            SetupOptions options = new SetupOptions();

            options.setBasePath(getFilesDir().getAbsolutePath());
            options.setWorkingPath(getFilesDir().getAbsolutePath());
            options.setTempPath(getCacheDir().getAbsolutePath());
            options.setAppVersion("1");
            options.setAppMarketingVersion("1.0");
            options.setFixAndroidStack(true);
            options.setDebug(false);

            Libbox.setup(options);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }
}
