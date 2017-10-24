package com.som.sombrero.activities;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.som.sombrero.MyApplication;

public abstract class AbstractBluetoothActivity extends AppCompatActivity {
    public static final int MESSAGE_STATE_CHANGE = 0;
    public static final int MESSAGE_TOAST = 1;
    public static final int MESSAGE_WRITE = 2;
    public static final String TOAST = "btToast";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MyApplication) getApplication()).setCallback(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch(msg.what) {
                    case MESSAGE_TOAST:
                        Toast.makeText(
                                AbstractBluetoothActivity.this,
                                msg.getData().getString(AbstractBluetoothActivity.TOAST),
                                Toast.LENGTH_SHORT
                        ).show();
                        break;
                }
                return false;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ((MyApplication) getApplication()).setCallback(null);
    }
}
