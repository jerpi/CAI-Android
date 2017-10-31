package com.som.sombrero.activities;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.TextView;

import com.som.sombrero.R;
import com.som.sombrero.exceptions.HandlerLaunchedException;
import com.som.sombrero.services.BluetoothService;
import com.som.sombrero.utils.ByteArrayConverter;
import com.som.sombrero.views.BallView;

public class GameActivity extends BluetoothActivity implements BallView.GoalListener, BallView.OffScreenListener,
        BluetoothActivity.OnReadListener, BluetoothActivity.OnToastListener, BluetoothActivity.OnErrorListener {

    private static final String TAG = "GameActivity";

    /**
     * Bundle keys
     */
    public static final String DEVICE = "DEVICE";

    /**
     * Read keys
     */
    public static final String GOAL = "GOAL";
    public static final String OOB = "OUT_OF_BONDS";
    public static final String VELOCITY_X = "VELOCITY_X";
    public static final String VELOCITY_Y = "VELOCITY_Y";
    public static final String POSITION_X = "POSITION_X";

    /**
     * Views
     */
    private BallView mBall;
    private TextView mUserScoreView;
    private TextView mAdversaryScoreView;

    private boolean isHost = false;
    private boolean isMulti = false;
    private BluetoothDevice mDevice;

    /**
     * Score
     */
    private int mUserScore = 0;
    private int mAdversaryScore = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mDevice = bundle.getParcelable(DEVICE);
            if (mDevice != null) {
                isMulti = true;
                isHost = bundle.getBoolean(ConnectActivity.IS_HOST, false);
            }
        }

        mUserScoreView = (TextView) findViewById(R.id.game_user_score);
        mUserScoreView.setText("0");
        mAdversaryScoreView = (TextView) findViewById(R.id.game_adversary_score);
        mAdversaryScoreView.setText("0");

        mBall = (BallView) findViewById(R.id.game_ball);

        try {
            mBall.startHandler();
        } catch (HandlerLaunchedException e) {
            Log.d(TAG, "Handler was already launched", e);
        }

        mBall.setOnBallLeftScreenListener(this);
        mBall.setOnGoalScoredListener(this);

        if (isMulti) {
            onToastListener = this;
            onErrorListener = this;
            onReadListener = this;
            bindToBluetoothService();
            if (isHost) {
                //initiate the ball
            } else {
                //hide the ball
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void onGoalFromUser() { // Called when you score a goal
        mUserScore++;
        mUserScoreView.setText("" + mUserScore);
        // TODO? add fancy animation
    }

    private void onOOBFromAdversary(Bundle data) { // Called when the adversary sent the ball your way
        float velocityX = data.getFloat(VELOCITY_X);
        float velocityY = data.getFloat(VELOCITY_Y);
        float positionX = data.getFloat(POSITION_X, 0.5f);
        //TODO compute positionX according to the screen's width

        if (positionX < 0) {
            positionX = 0;
        }
        if (positionX > 1080) { // TODO change to screen's width
            positionX = 1080;
        }
        mBall.setX(positionX);
        mBall.setVelocityX(-velocityX); // LEFT <-> RIGHT
        mBall.setVelocityY(-velocityY); // UP <-> DOWN
    }

    @Override
    public void onRead(Bundle args) {
        String read = args.getString(BluetoothService.MessageContent.KEY_READ, null);
        switch (read) {
            case GOAL:
                onGoalFromUser();
                break;
            case OOB:
                onOOBFromAdversary(args);
                break;
        }
    }


    @Override
    public void onToast(Bundle args) {

    }

    @Override
    public void onError(Bundle args) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBall.stopHandler();
    }

    @Override
    public void onOffScreen() { // Called when the ball goes offscreen (the ball went beyond the top wall)
        if (mBound) {
            float positionX = mBall.getX();
            float velocityX = mBall.getVelocityX();
            float velocityY = mBall.getVelocityY();

            float[] data = {positionX, velocityX, velocityY};
            byte[] dataAsByteArray = ByteArrayConverter.floatArray2ByteArray(data);

            mService.write(dataAsByteArray);
        } else {
            onGoalFromUser();
        }
        Log.d(TAG, "onOffScreen");
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onGoal() { // Called when a goal was scored by the adversary (the ball hit your bottom wall)
        mAdversaryScore++;
        mAdversaryScoreView.setText(""+ mAdversaryScore);
        Log.d(TAG, "onGoalFromUser");
        if (mBound) {
            mService.write("GOAL".getBytes());
            //TODO? add fancy animation
        }
    }
}
