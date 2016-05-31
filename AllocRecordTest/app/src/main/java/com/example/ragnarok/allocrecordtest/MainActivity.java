package com.example.ragnarok.allocrecordtest;

import android.graphics.Bitmap;
import android.os.Process;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    List<Object> objectList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.loadLibrary("test");
            }
        });

//        Log.d("AllocRecordJNI", "dalvik.vm.allocTrackerMax is " + )

        findViewById(R.id.dump).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dumpAllocRecordData();
            }
        });

        Log.i("AllocRecordJNI", "vm version: " + System.getProperty("java.vm.version") + ", " + System.getProperty("java.vm.specification.version"));

        final Random random = new Random();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i("AllocRecordJNI", "threadId: " + Process.myTid());
                for (;;) {
                    objectList.add(new byte[random.nextInt(100) + 10]);
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

    }

    private static native void dumpAllocRecordData();
}
