package com.example.v2rayapp;

import android.app.Activity;
import android.content.Intent;
import android.net.VpnService;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.LinearLayout;

public class MainActivity extends Activity {

    private TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 80, 40, 40);

        status = new TextView(this);
        status.setText("V2Ray App\nوضعیت: قطع");

        Button connect = new Button(this);
        connect.setText("اتصال");

        connect.setOnClickListener(v -> {
            Intent intent = VpnService.prepare(this);
            if (intent != null) {
                startActivityForResult(intent, 100);
            } else {
                startVpn();
            }
        });

        layout.addView(status);
        layout.addView(connect);

        setContentView(layout);
    }

    private void startVpn() {
        Intent intent = new Intent(this, MyVpnService.class);
        startService(intent);
        status.setText("V2Ray App\nوضعیت: در حال اتصال");
    }
}
