package com.som.sombrero.activities;


import android.support.v7.app.AppCompatActivity;

public abstract class AbstractBluetoothActivity extends AppCompatActivity {
    public static final int MESSAGE_STATE_CHANGE = 0;
    public static final int MESSAGE_TOAST = 1;
    public static final int MESSAGE_WRITE = 2;
    public static final String TOAST = "btToast";
}
