package com.dzg.kurozenframe;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {
    private final Handler handler = new Handler();
    private TextView clock;
    private TextView date;

    private final Runnable ticker = new Runnable() {
        @Override public void run() {
            Date now = new Date();
            clock.setText(new SimpleDateFormat("h:mm", Locale.US).format(now));
            date.setText(new SimpleDateFormat("EEEE  •  MMMM d", Locale.US).format(now).toUpperCase(Locale.US));
            handler.postDelayed(this, 1000);
        }
    };

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        setContentView(R.layout.activity_main);

        clock = (TextView) findViewById(R.id.clock);
        date = (TextView) findViewById(R.id.date);
        Button vPhoto = (Button) findViewById(R.id.vphoto);
        Button settings = (Button) findViewById(R.id.settings);

        vPhoto.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                launchVPhoto();
            }
        });

        settings.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                try {
                    startActivity(new Intent(Settings.ACTION_SETTINGS));
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Settings unavailable", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void launchVPhoto() {
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(
                    "com.waophoto.smartframe",
                    "com.waophoto.smartframe.ui.SplashActivity"));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception first) {
            try {
                Intent fallback = getPackageManager().getLaunchIntentForPackage("com.waophoto.smartframe");
                if (fallback != null) {
                    startActivity(fallback);
                } else {
                    throw new Exception("No launch intent");
                }
            } catch (Exception second) {
                Toast.makeText(this, "V Photo could not be opened", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override protected void onResume() {
        super.onResume();
        handler.removeCallbacks(ticker);
        handler.post(ticker);
    }

    @Override protected void onPause() {
        handler.removeCallbacks(ticker);
        super.onPause();
    }
}
