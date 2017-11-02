package com.som.sombrero.services;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.som.sombrero.BaseApplication;
import com.som.sombrero.R;
import com.som.sombrero.activities.ConnectActivity;
import com.som.sombrero.activities.GameActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothService extends Service {

    private static final String TAG = "BluetoothService";
    private static final String NAME = "SombreroBT";
    private static final UUID M_UUID = UUID.fromString("50c7679c-bd8d-11e7-abc4-cec278b6b50a");

    /**
     * Bundle keys
     */
    public static final String READ_BYTES = "READ_BYTES";
    public static final String BUFFER = "BUFFER";

    /**
     * Messages
     */
    private Handler mHandler;

    public interface MessageContent {
        int READ = 0;
        int WRITE = 1;
        int TOAST = 2;
        int ERROR = 3;
        int CONNECTION_OK = 4;

        String KEY_MESSAGE = "MESSAGE";
   }

    /**
     * Binder
     */
    private final IBinder mBinder = new BluetoothServiceBinder();
    public class BluetoothServiceBinder extends Binder {
        public BluetoothService getService() {
            return BluetoothService.this;
        }
    }

    /**
     * Threads
     */
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private AcceptConnectionThread mAcceptConnectionThread;

    /**
     * BluetoothAdapter
     */
    private BluetoothAdapter mBluetoothAdapter;

    @Override
    public IBinder onBind(Intent intent) {
        mHandler = ((BaseApplication) getApplication()).getHandler();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) {
            listenForConnections();
        }
        return mBinder;
    }


    @Override
    public void onDestroy() {
        stopSelf();
        super.onDestroy();
    }

    private synchronized void listenForConnections() {
        if (mAcceptConnectionThread != null && mAcceptConnectionThread.isAlive()) {
            return;
        }

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        mAcceptConnectionThread = new AcceptConnectionThread();
        mAcceptConnectionThread.start();
    }

    public synchronized void connectToDevice(String macAddress) {
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(macAddress);

        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
    }

    public synchronized void write(byte[] bytes) {
        if (mConnectedThread != null && mConnectedThread.isAlive()) {
            mConnectedThread.write(bytes);
        }
    }

    @Override
    public boolean stopService(Intent name) {
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.cancelDiscovery();
        }
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
        }
        if (mConnectThread != null) {
            mConnectThread.cancel();
        }
        if (mAcceptConnectionThread != null) {
            mAcceptConnectionThread.cancel();
        }
        return super.stopService(name);
    }

    public void manageConnectedSocket(BluetoothSocket socket, boolean isHost) {
        mConnectedThread = new ConnectedThread(socket, isHost);
        mConnectedThread.start();
    }


    /**
     * This is the SERVER thread, it is called when an incoming connection is received
     */
    private class AcceptConnectionThread extends Thread {
        private final BluetoothServerSocket mServerSocket;

        AcceptConnectionThread() {
            BluetoothServerSocket tmp = null;
            try {
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, M_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's listen() method failed", e);
            }
            mServerSocket = tmp;
        }

        @Override
        public void run() {
            BluetoothSocket socket;
            while(true) {
                try {
                    socket = mServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Socket's accept() method failed", e);
                    break;
                }
                if (socket != null) {
                    manageConnectedSocket(socket, true);
                    try {
                        mServerSocket.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Socket's close() method failed", e);
                    }
                    break;
                }
            }
        }

        void cancel() {
            try {
                mServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Socket's close() method failed", e);
            }
        }
    }

    /**
     * This is the CLIENT thread, it is called to initiate a Bluetooth connection to another device
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mSocket;

        ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmpSocket = null;
            try {
                tmpSocket = device.createRfcommSocketToServiceRecord(M_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mSocket = tmpSocket;
        }

        @Override
        public void run() {
            mBluetoothAdapter.cancelDiscovery();
            try {
                mSocket.connect();
            } catch (IOException connectException) {
                Log.d(TAG, "Socket's connect() method failed", connectException);
                try {
                    mSocket.close();
                } catch (IOException e) {
                    Log.e(TAG, "Socket's close() method failed", e);
                }
                Message message = mHandler.obtainMessage(MessageContent.ERROR);
                Bundle data = new Bundle();
                data.putString(MessageContent.KEY_MESSAGE, getResources().getString(R.string.connection_failed));
                message.setData(data);
                message.sendToTarget();
                return;
            }
            manageConnectedSocket(mSocket, false);
        }

        void cancel() {
            try {
                mSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Socket's close() method failed", e);
            }
        }
    }

    private void sendDisconnectionMessage() {
        Message message = mHandler.obtainMessage(MessageContent.ERROR);
        Bundle data = new Bundle();
        data.putString(MessageContent.KEY_MESSAGE, getString(R.string.connection_failed));
        message.setData(data);
        message.sendToTarget();
    }


    /**
     * This is the thread which will be use to communicate with the other device
     * It's the same for both the SERVER and the CLIENT
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mSocket;
        private final InputStream mInStream;
        private final OutputStream mOutStream;
        private final boolean isHost;
        private byte[] mBuffer;

        ConnectedThread(BluetoothSocket socket, boolean host) {
            mSocket = socket;
            isHost = host;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = mSocket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = mSocket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
                e.printStackTrace();
            }

            mInStream = tmpIn;
            mOutStream = tmpOut;

            Message message = mHandler.obtainMessage(
                    MessageContent.CONNECTION_OK
            );
            Bundle data = new Bundle();
            data.putBoolean(ConnectActivity.IS_HOST, isHost);
            data.putParcelable(GameActivity.DEVICE, socket.getRemoteDevice());
            message.setData(data);
            message.sendToTarget();
        }

        @Override
        public void run() {
            mBuffer = new byte[1024];
            int readBytes;
            while(true) {
                try {
                    readBytes = mInStream.read(mBuffer);
                    Message message = mHandler.obtainMessage(
                            MessageContent.READ,
                            readBytes,
                            -1,
                            mBuffer
                    );
                    Bundle data = new Bundle();
                    data.putInt(READ_BYTES, readBytes);
                    data.putByteArray(BUFFER, mBuffer);
                    message.setData(data);
                    message.sendToTarget();
                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    break;
                }
            }
            sendDisconnectionMessage();
        }

        void write(byte[] bytes) {
            try {
                mOutStream.write(bytes);
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);
                sendDisconnectionMessage();
            }
        }

        void cancel() {
            try {
                mSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Socket's close() method failed");
            }
        }
    }
}