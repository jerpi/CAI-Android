package com.som.sombrero.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.som.sombrero.R;
import com.som.sombrero.exceptions.HandlerLaunchedException;
import com.som.sombrero.listeners.OnBallLeftScreenListener;
import com.som.sombrero.listeners.OnGoalScoredListener;
import com.som.sombrero.listeners.OnWallBounceListener;
import com.som.sombrero.views.BallView;

public class GameActivity extends AppCompatActivity implements OnBallLeftScreenListener, OnWallBounceListener, OnGoalScoredListener {

    private static final String TAG = "GameActivity";
    private static final int REQUEST_ENABLE_BT = 1;

    public static final String MULTI = "Multi";

    private BallView mBall;
    private boolean isMulti = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            isMulti = bundle.getBoolean(MULTI, false);
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBall.stopHandler();
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
    }
}
