package com.dzg.kurozenframe;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {
    private final Handler handler = new Handler();
    private final ArrayList<File> photos = new ArrayList<File>();
    private TextView clock, date, status, photoCount;
    private ImageView photoView;
    private View adminPanel;
    private int photoIndex = 0;
    private Bitmap currentBitmap;

    private final Runnable ticker = new Runnable() {
        @Override public void run() {
            Date now = new Date();
            clock.setText(new SimpleDateFormat("h:mm", Locale.US).format(now));
            date.setText(new SimpleDateFormat("EEEE  •  MMMM d", Locale.US).format(now).toUpperCase(Locale.US));
            handler.postDelayed(this, 1000);
        }
    };

    private final Runnable slideshow = new Runnable() {
        @Override public void run() {
            showNextPhoto();
            handler.postDelayed(this, 10000);
        }
    };

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        setContentView(R.layout.activity_main);

        clock = (TextView) findViewById(R.id.clock);
        date = (TextView) findViewById(R.id.date);
        status = (TextView) findViewById(R.id.status);
        photoCount = (TextView) findViewById(R.id.photo_count);
        photoView = (ImageView) findViewById(R.id.photo);
        adminPanel = findViewById(R.id.admin_panel);
        Button refresh = (Button) findViewById(R.id.refresh);
        Button settings = (Button) findViewById(R.id.settings);

        View brand = findViewById(R.id.brand);
        brand.setOnLongClickListener(new View.OnLongClickListener() {
            @Override public boolean onLongClick(View v) {
                adminPanel.setVisibility(adminPanel.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                return true;
            }
        });

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                loadPhotos();
                if (!photos.isEmpty()) showPhoto(0);
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

        loadPhotos();
        if (!photos.isEmpty()) showPhoto(0);
    }

    private void loadPhotos() {
        photos.clear();
        File root = Environment.getExternalStorageDirectory();
        scan(new File(root, "Pictures"));
        scan(new File(root, "DCIM"));
        scan(new File(root, "Download"));
        Collections.sort(photos);
        photoIndex = 0;
        updateStatus();
    }

    private void scan(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory()) return;
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (file.isDirectory()) {
                scan(file);
            } else {
                String name = file.getName().toLowerCase(Locale.US);
                if (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".webp")) {
                    photos.add(file);
                }
            }
        }
    }

    private void updateStatus() {
        int count = photos.size();
        photoCount.setText(count + (count == 1 ? " PHOTO FOUND" : " PHOTOS FOUND"));
        status.setText(count > 0 ? "KUROZEN FRAME  •  SLIDESHOW ONLINE" : "KUROZEN FRAME  •  ADD PHOTOS TO PICTURES");
    }

    private void showNextPhoto() {
        if (photos.isEmpty()) return;
        photoIndex = (photoIndex + 1) % photos.size();
        showPhoto(photoIndex);
    }

    private void showPhoto(int index) {
        if (photos.isEmpty()) return;
        File file = photos.get(index);
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), bounds);

        int sample = 1;
        while (bounds.outWidth / sample > 1600 || bounds.outHeight / sample > 1600) sample *= 2;

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = sample;
        Bitmap next = BitmapFactory.decodeFile(file.getAbsolutePath(), opts);
        if (next != null) {
            Bitmap old = currentBitmap;
            currentBitmap = next;
            photoView.setImageBitmap(currentBitmap);
            if (old != null && old != currentBitmap && !old.isRecycled()) old.recycle();
        }
    }

    @Override protected void onResume() {
        super.onResume();
        handler.removeCallbacks(ticker);
        handler.removeCallbacks(slideshow);
        handler.post(ticker);
        handler.postDelayed(slideshow, 10000);
    }

    @Override protected void onPause() {
        handler.removeCallbacks(ticker);
        handler.removeCallbacks(slideshow);
        super.onPause();
    }
}
