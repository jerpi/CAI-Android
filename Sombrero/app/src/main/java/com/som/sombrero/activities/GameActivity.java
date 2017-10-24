package com.som.sombrero.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.som.sombrero.R;
import com.som.sombrero.exceptions.HandlerLaunchedException;
import com.som.sombrero.listeners.BallLeftScreenListener;
import com.som.sombrero.listeners.WallBounceListener;
import com.som.sombrero.views.BallView;

public class GameActivity extends AppCompatActivity implements BallLeftScreenListener, WallBounceListener {

    private static final String TAG = "GameActivity";
    private static final int REQUEST_ENABLE_BT = 1;

    private BallView mBall;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        mBall = (BallView) findViewById(R.id.game_ball);
        try {
            mBall.startHandler();
        } catch (HandlerLaunchedException e) {
            Log.d(TAG, e.getLocalizedMessage());
        }
        mBall.setBallLeftScreenListener(this);
        mBall.setWallBounceListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBall.stopHandler();
    }







    @Override
    public void onWallBounced() {
        Log.d(TAG, "WallBounced");
    }

    @Override
    public void onScreenLeft() {
        Log.d(TAG, "onScreenLeft");
    }




}
