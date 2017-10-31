package com.som.sombrero;


import android.app.Application;
import android.os.Handler;
import android.os.Message;

public class BaseApplication extends Application {

    private static class BluetoothHandler extends Handler {
        Handler.Callback mCallback = null;

        @Override
        public void handleMessage(Message msg) {
            if (mCallback != null) {
                mCallback.handleMessage(msg);
            }
        }

        void setCallback(Callback mCallback) {
            this.mCallback = mCallback;
        }
    }

    protected BluetoothHandler mHandler = new BluetoothHandler();

    public BluetoothHandler getHandler() {
        return mHandler;
    }

    public void setHandlerCallback(Handler.Callback callback) {
        mHandler.setCallback(callback);
    }

}
