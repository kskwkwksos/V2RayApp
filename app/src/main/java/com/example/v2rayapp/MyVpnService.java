package com.example.v2rayapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.net.VpnService;
import android.os.Build;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;

import io.nekohasekai.libbox.CommandServer;
import io.nekohasekai.libbox.CommandServerHandler;
import io.nekohasekai.libbox.ConnectionOwner;
import io.nekohasekai.libbox.BridgeOptions;
import io.nekohasekai.libbox.BridgeSession;
import io.nekohasekai.libbox.InterfaceUpdateListener;
import io.nekohasekai.libbox.NeighborUpdateListener;
import io.nekohasekai.libbox.NetworkInterfaceIterator;
import io.nekohasekai.libbox.LocalDNSTransport;
import io.nekohasekai.libbox.PlatformInterface;
import io.nekohasekai.libbox.PlatformUser;
import io.nekohasekai.libbox.ShellSession;
import io.nekohasekai.libbox.StringIterator;
import io.nekohasekai.libbox.TunOptions;
import io.nekohasekai.libbox.WIFIState;
import io.nekohasekai.libbox.Libbox;
import io.nekohasekai.libbox.OverrideOptions;
import io.nekohasekai.libbox.SetupOptions;

public class MyVpnService extends VpnService
        implements PlatformInterface, CommandServerHandler {

    private ParcelFileDescriptor tun;
    private CommandServer commandServer;

    private static final String CHANNEL = "v2ray";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForegroundNotification();

        try {
            SetupOptions setup = new SetupOptions();
            setup.setBasePath(getFilesDir().getAbsolutePath());
            setup.setWorkingPath(getFilesDir().getAbsolutePath());
            setup.setTempPath(getCacheDir().getAbsolutePath());
            setup.setAppVersion("1");
            setup.setAppMarketingVersion("1.0");
            setup.setFixAndroidStack(true);
            setup.setDebug(false);

            Libbox.setup(setup);

            commandServer = new CommandServer(this, this);
            commandServer.start();

            String config =
                    "{\"log\":{\"level\":\"info\"}," +
                    "\"inbounds\":[{\"type\":\"tun\",\"tag\":\"tun-in\"," +
                    "\"address\":[\"172.19.0.1/30\"]," +
                    "\"auto_route\":true,\"strict_route\":true}]," +
                    "\"outbounds\":[{\"type\":\"direct\",\"tag\":\"direct\"}]}";

            commandServer.startOrReloadService(config, new OverrideOptions());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return START_STICKY;
    }

    private void startForegroundNotification() {
        NotificationManager nm =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= 26) {
            nm.createNotificationChannel(
                    new NotificationChannel(
                            CHANNEL,
                            "V2Ray",
                            NotificationManager.IMPORTANCE_LOW
                    )
            );
        }

        Notification.Builder builder =
                Build.VERSION.SDK_INT >= 26
                        ? new Notification.Builder(this, CHANNEL)
                        : new Notification.Builder(this);

        builder.setContentTitle("V2Ray App")
                .setContentText("VPN در حال اجراست")
                .setSmallIcon(android.R.drawable.stat_sys_warning)
                .setOngoing(true);

        startForeground(1, builder.build());
    }

    @Override
    public int openTun(TunOptions options) throws Exception {
        if (VpnService.prepare(this) != null)
            throw new IllegalStateException("VPN permission required");

        Builder builder = new Builder()
                .setSession("V2Ray App")
                .setMtu(options.getMTU());

        boolean hasIPv4 = false;
        boolean hasIPv6 = false;

        StringIterator ipv4 = options.getInet4Address();
        while (ipv4.hasNext()) {
            String a = ipv4.next();
            String[] p = a.split("/");
            builder.addAddress(p[0], Integer.parseInt(p[1]));
            hasIPv4 = true;
        }

        StringIterator ipv6 = options.getInet6Address();
        while (ipv6.hasNext()) {
            String a = ipv6.next();
            String[] p = a.split("/");
            builder.addAddress(p[0], Integer.parseInt(p[1]));
            hasIPv6 = true;
        }

        if (options.getAutoRoute()) {
            if (hasIPv4)
                builder.addRoute("0.0.0.0", 0);
            if (hasIPv6)
                builder.addRoute("::", 0);
        }

        tun = builder.establish();

        if (tun == null)
            throw new IllegalStateException("VPN establish failed");

        return tun.getFd();
    }

    @Override
    public void autoDetectInterfaceControl(int fd) {
        protect(fd);
    }

    @Override
    public boolean usePlatformAutoDetectInterfaceControl() {
        return true;
    }

    @Override public boolean includeAllNetworks() { return false; }
    @Override public boolean underNetworkExtension() { return false; }
    @Override public boolean usePlatformBridge() { return false; }
    @Override public boolean usePlatformShell() { return false; }
    @Override public boolean useProcFS() { return false; }

    @Override public NetworkInterfaceIterator getInterfaces() { return null; }
    @Override public LocalDNSTransport localDNSTransport() { return null; }
    @Override public ConnectionOwner findConnectionOwner(
            int a, String b, int c, String d, int e) { return null; }

    @Override public PlatformUser lookupUser(String username) { return null; }
    @Override public ShellSession openShellSession(
            PlatformUser user, String command, StringIterator env,
            String term, int rows, int cols) { return null; }

    @Override public BridgeSession createBridge(BridgeOptions options) { return null; }
    @Override public String lookupSFTPServer() { return null; }
    @Override public String readSystemSSHHostKey() { return null; }
    @Override public String tailscaleHostname() { return null; }
    @Override public WIFIState readWIFIState() { return null; }

    @Override public void checkPlatformShell() {}
    @Override public void clearDNSCache() {}
    @Override public void registerMyInterface(String name) {}
    @Override public void startDefaultInterfaceMonitor(InterfaceUpdateListener l) {}
    @Override public void closeDefaultInterfaceMonitor(InterfaceUpdateListener l) {}
    @Override public void startNeighborMonitor(NeighborUpdateListener l) {}
    @Override public void closeNeighborMonitor(NeighborUpdateListener l) {}

    @Override public void sendNotification(io.nekohasekai.libbox.Notification n) {}
    @Override public void cancelNotification(String id, int type) {}

    @Override public int connectSSHAgent() { return -1; }
    @Override public Object getSystemProxyStatus() { return null; }
    @Override public void serviceReload() {}
    @Override public void serviceStop() { stopSelf(); }
    @Override public void setSystemProxyEnabled(boolean enabled) {}
    @Override public void triggerNativeCrash() {}
    @Override public void writeDebugMessage(String message) {
        System.out.println(message);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public void onDestroy() {
        try {
            if (commandServer != null) {
                commandServer.closeService();
                commandServer.close();
            }
        } catch (Exception ignored) {}

        if (tun != null) {
            try { tun.close(); } catch (Exception ignored) {}
        }

        stopForeground(true);
        super.onDestroy();
    }
}
