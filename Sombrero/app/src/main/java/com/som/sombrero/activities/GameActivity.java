package com.som.sombrero.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.som.sombrero.R;
import com.som.sombrero.exceptions.HandlerLaunchedException;
import com.som.sombrero.listeners.OnBallLeftScreenListener;
import com.som.sombrero.listeners.OnGoalScoredListener;
import com.som.sombrero.listeners.OnWallBounceListener;
import com.som.sombrero.services.BluetoothService;
import com.som.sombrero.views.BallView;

public class GameActivity extends AbstractBluetoothActivity implements OnBallLeftScreenListener, OnWallBounceListener, OnGoalScoredListener {

    private static final String TAG = "GameActivity";

    public static final String MULTI = "Multi";
    public static final String MAC_ADDRESS = "MacAddress";

    private BallView mBall;
    private boolean isMulti = false;
    private String macAddress = null;

    private boolean mBound = false;
    private BluetoothService mService = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            isMulti = bundle.getBoolean(MULTI, false);
            if (isMulti) {
                macAddress = bundle.getString(MAC_ADDRESS, null);
                if (macAddress == null) { isMulti = false; }
            }
        }

        mBall = (BallView) findViewById(R.id.game_ball);
        try {
            mBall.startHandler();
        } catch (HandlerLaunchedException e) {
            Log.d(TAG, e.getLocalizedMessage());
        }

        mBall.setOnBallLeftScreenListener(this);
        mBall.setOnWallBounceListener(this);
        mBall.setOnGoalScoredListener(this);

        if (isMulti) {
            Intent i = new Intent(this, BluetoothService.class);
            i.putExtra(BluetoothService.MAC_ADDRESS, macAddress);
            startService(i);
            bindService(i, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBall.stopHandler();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBound) {
            unbindService(mConnection);
            Intent i = new Intent(this, BluetoothService.class);
            stopService(i);
            mBound = false;
        }
    }


    @Override
    public void onBounce() {
        Log.d(TAG, "WallBounced");
    }

    @Override
    public void onScreenLeft() {
        Log.d(TAG, "onScreenLeft");
    }

    @Override
    public void onGoalScored() {

        Log.d(TAG, "onGoalScored");
        if (mBound && mService != null) {
            BluetoothService.write("Hello".getBytes());
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            BluetoothService.BluetoothServiceBinder binder = (BluetoothService.BluetoothServiceBinder) service;
            mService = binder.getService();
            mBound = true;
            Log.d(TAG, "Service connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
            Log.d(TAG, "ServiceDisconnected");
        }
    };
}
