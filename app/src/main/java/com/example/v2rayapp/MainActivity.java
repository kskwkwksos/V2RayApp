package com.example.v2rayapp;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.LinearLayout;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 80, 40, 40);

        TextView status = new TextView(this);
        status.setText("V2Ray App\nوضعیت: قطع");

        Button connect = new Button(this);
        connect.setText("اتصال");

        layout.addView(status);
        layout.addView(connect);

        setContentView(layout);
    }
}
