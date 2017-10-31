package com.som.sombrero.activities;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.som.sombrero.BaseApplication;
import com.som.sombrero.services.BluetoothService;

public abstract class BluetoothActivity extends AppCompatActivity implements Handler.Callback {

    protected String TAG = "BluetoothActivity";

    protected OnReadListener onReadListener;
    protected OnWriteListener onWriteListener;
    protected OnToastListener onToastListener;
    protected OnErrorListener onErrorListener;
    protected OnConnectionListener onConnectionListener;

    /**
     * Service binding
     */
    protected boolean mBound = false;
    protected BluetoothService mService = null;
    protected ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            BluetoothService.BluetoothServiceBinder binder = (BluetoothService.BluetoothServiceBinder) service;
            mService = binder.getService();
            mBound = true;
            Log.d(TAG, "Service connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
            mService = null;
            Log.d(TAG, "Service disconnected");
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((BaseApplication) getApplication()).setHandlerCallback(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
            mService = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ((BaseApplication) getApplication()).setHandlerCallback(null);
    }

    protected void bindToBluetoothService() {
        Intent i = new Intent(this, BluetoothService.class);
        bindService(i, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public boolean handleMessage(Message msg) {
        Bundle data = msg.getData();
        switch(msg.what) {
            case BluetoothService.MessageContent.WRITE:
                if (onWriteListener != null) {
                    onWriteListener.onWrite(data);
                }
                break;
            case BluetoothService.MessageContent.READ:
                if (onReadListener != null) {
                    onReadListener.onRead(data);
                }
                break;
            case BluetoothService.MessageContent.TOAST:
                if (onToastListener != null) {
                    onToastListener.onToast(data);
                }
                break;
            case BluetoothService.MessageContent.ERROR:
                if (onErrorListener != null) {
                    onErrorListener.onError(data);
                }
                break;
            case BluetoothService.MessageContent.CONNECTION_OK:
                if (onConnectionListener != null) {
                    onConnectionListener.onConnection(data);
                }
        }
        return false;
    }

    interface OnReadListener {
        void onRead(Bundle args);
    }
    interface OnWriteListener {
        void onWrite(Bundle args);
    }
    interface OnToastListener {
        void onToast(Bundle args);
    }
    interface OnErrorListener {
        void onError(Bundle args);
    }
    interface OnConnectionListener {
        void onConnection(Bundle args);
    }
}
