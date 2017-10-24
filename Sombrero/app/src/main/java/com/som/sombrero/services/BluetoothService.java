package com.som.sombrero.services;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.som.sombrero.MyApplication;
import com.som.sombrero.activities.AbstractBluetoothActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothService extends Service {

    private static final String TAG = "btDevice";
    public static final String MAC_ADDRESS = "MacAddress";

    public static final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    public static final int STATE_NONE = 0; // we're doing nothing
    public static final int STATE_LISTEN = 1; // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3; // now connected to a remote

    public static int mState = STATE_NONE;

    private BluetoothAdapter mBluetoothAdapter;

    private ConnectThread mConnectThread;
    private static ConnectedThread mConnectedThread;

    private static Handler mHandler = null;
    private Handler handler = new MyHandler();

    @Override
    public void onCreate() {
        Log.d(TAG, "Service started");
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        mHandler = ((MyApplication) getApplication()).getHandler();
        return mBinder;
    }

    private final IBinder mBinder = new Binder() {
        BluetoothService getService() {
            return BluetoothService.this;
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("PrinterService", "Onstart Command");
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) {
            String macAddress = intent.getStringExtra(MAC_ADDRESS);
            if (macAddress != null && macAddress.length() > 0) {
                connectToDevice(macAddress);
            } else {
                stopSelf();
                return START_NOT_STICKY;
            }
        }
        String stopService = intent.getStringExtra("stopService");
        if (stopService != null && stopService.length() > 0) {
            stop();
        }
        return START_STICKY;
    }

    private synchronized void connectToDevice(String macAddress) {
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(macAddress);
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    private void setState(int state) {
        BluetoothService.mState = state;
        if (mHandler != null) {
            mHandler.obtainMessage(AbstractBluetoothActivity.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
        }
    }

    public synchronized void stop() {
        setState(STATE_NONE);
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.cancelDiscovery();
        }
        stopSelf();
    }

    @Override
    public boolean stopService(Intent name) {
        setState(STATE_NONE);
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        mBluetoothAdapter.cancelDiscovery();
        return super.stopService(name);
    }

    private void connectionFailed() {
        stop();
        Message msg = mHandler.obtainMessage(AbstractBluetoothActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(AbstractBluetoothActivity.TOAST, "Failed");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    private void connectionLost() {
        stop();
        Message msg = mHandler.obtainMessage(AbstractBluetoothActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(AbstractBluetoothActivity.TOAST, "Connection lost");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    private static final Object obj = new Object();

    public static void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (obj) {
            if (mState != STATE_CONNECTED)
                return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }

    private synchronized void connected(BluetoothSocket mmSocket, BluetoothDevice mmDevice) {
        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();

        // Message msg =
        // mHandler.obtainMessage(AbstractActivity.MESSAGE_DEVICE_NAME);
        // Bundle bundle = new Bundle();
        // bundle.putString(AbstractActivity.DEVICE_NAME, "p25");
        // msg.setData(bundle);
        // mHandler.sendMessage(msg);
        setState(STATE_CONNECTED);

    }

    private static class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {//
            if (!Thread.currentThread().isInterrupted()) {
                switch (msg.what) {
                    case 3:
                        break;
                    case 4:
                        break;
                    case 5:
                        break;
                    case -1:
                        break;
                }
            }
            super.handleMessage(msg);
        }
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        ConnectThread(BluetoothDevice device) {
            this.mmDevice = device;
            BluetoothSocket tmp = null;
            try {
                tmp = device.createRfcommSocketToServiceRecord(UUID.fromString(SPP_UUID));
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmSocket = tmp;
        }

        @Override
        public void run() {
            setName("ConnectThread");
            mBluetoothAdapter.cancelDiscovery();
            try {
                mmSocket.connect();
            } catch (IOException e) {
                try {
                    mmSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                connectionFailed();
                return;

            }
            synchronized (BluetoothService.this) {
                mConnectThread = null;
            }
            connected(mmSocket, mmDevice);
        }

        void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e("PrinterService", "close() of connect socket failed", e);
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Sockets exception", e);
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    //if (!encodeData(mmInStream)) {
                    //    mState = STATE_NONE;
                    //    connectionLost();
                    //    break;
                    //}
                    // mHandler.obtainMessage(AbstractActivity.MESSAGE_READ,
                    // bytes, -1, buffer).sendToTarget();
                } catch (Exception e) {
                    e.printStackTrace();
                    connectionLost();
                    BluetoothService.this.stop();
                    break;
                }

            }
        }

        void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);

                // Share the sent message back to the UI Activity
                mHandler.obtainMessage(AbstractBluetoothActivity.MESSAGE_WRITE, buffer.length, -1, buffer).sendToTarget();
            } catch (IOException e) {
                Log.e("PrinterService", "Exception during write", e);
            }
        }

        void cancel() {
            try {
                mmSocket.close();

            } catch (IOException e) {
                Log.e("PrinterService", "close() of connect socket failed", e);
            }
        }
    }

    @Override
    public void onDestroy() {
        stop();
        super.onDestroy();
    }

    private void sendMsg(int flag) {
        Message msg = new Message();
        msg.what = flag;
        handler.sendMessage(msg);
    }
}