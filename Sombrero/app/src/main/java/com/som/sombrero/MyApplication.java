package com.som.sombrero;


import android.app.Application;
import android.os.Handler;

public class MyApplication extends Application {

    protected Handler.Callback callback = null;

    protected Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (callback != null) {
                callback.handleMessage(msg);
            }
        }
    };

    public Handler getHandler() {
        return handler;
    }

    public Handler.Callback getCallback() {
        return callback;
    }

    public void setCallback(Handler.Callback callback) {
        this.callback = callback;
    }
}
