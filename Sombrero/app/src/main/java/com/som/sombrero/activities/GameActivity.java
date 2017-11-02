package com.som.sombrero.activities;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;

import com.som.sombrero.R;
import com.som.sombrero.exceptions.HandlerLaunchedException;
import com.som.sombrero.services.BluetoothService;
import com.som.sombrero.utils.ByteArrayConverter;
import com.som.sombrero.views.BallView;

import static com.som.sombrero.services.BluetoothService.READ_BYTES;

public class GameActivity extends BluetoothActivity implements BallView.GoalListener, BallView.OffScreenListener,
        BluetoothActivity.OnReadListener, BluetoothActivity.OnToastListener, BluetoothActivity.OnErrorListener {

    private static final String TAG = "GameActivity";

    /**
     * Bundle keys
     */
    public static final String DEVICE = "DEVICE";

    /**
     * Other keys
     */
    public static final String GOAL = "GOAL";
    public static final String OOB = "OUT_OF_BONDS";
    public static final int VELOCITY_X = 0;
    public static final int VELOCITY_Y = 1;
    public static final int POSITION_X = 2;

    /**
     * Views
     */
    private BallView mBall;
    private TextView mUserScoreView;
    private TextView mAdversaryScoreView;

    private boolean isHost = false;
    private boolean isMulti = false;
    private BluetoothDevice mDevice; // Might be used to display the adversary's "name"

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

        setUpViews();
        if (isMulti) {
            onToastListener = this;
            onErrorListener = this;
            onReadListener = this;
            bindToBluetoothService();
        }
    }

    private void setUpViews() {
        mUserScoreView = (TextView) findViewById(R.id.game_user_score);
        mUserScoreView.setText("0");
        mAdversaryScoreView = (TextView) findViewById(R.id.game_adversary_score);
        mAdversaryScoreView.setText("0");

        mBall = (BallView) findViewById(R.id.game_ball);
        mBall.setOnBallLeftScreenListener(this);
        mBall.setOnGoalScoredListener(this);

        if (!isMulti || isHost) {
            try {
                mBall.init();
            } catch (HandlerLaunchedException e) {
                Log.e(TAG, "Handler was already launched", e);
            }
        } else {
            mBall.pause();
        }
    }

    @SuppressLint("SetTextI18n")
    private void onGoalFromUser() { // Called when you score a goal
        mUserScore++;
        mUserScoreView.setText("" + mUserScore);
        // TODO? add fancy animation
    }

    private void onOOBFromAdversary(Bundle data) { // Called when the adversary sent the ball your way
        int readBytes = data.getInt(READ_BYTES, 0);
        byte[] bytes = data.getByteArray(BluetoothService.BUFFER);
        if (bytes == null || readBytes < 3*4) {
            return;
        }

        float[] floatValues = ByteArrayConverter.byteArray2FloatArray(bytes, readBytes);

        float velocityX = floatValues[VELOCITY_X];
        float velocityY = floatValues[VELOCITY_Y];
        float positionX = floatValues[POSITION_X];

        try {
            mBall.unPause(positionX, velocityX, velocityY); // LEFT <-> RIGHT | UP <-> DOWN
        } catch (HandlerLaunchedException e) {
            Log.e(TAG, "Handler was already launched", e);
        }
    }

    @Override
    public void onRead(Bundle args) { // not the cleanest way to do it but it's enough
        int readBytes = args.getInt(READ_BYTES, 0);

        if (readBytes == 3*4) { // each float is 4bytes, we need 3 of them (posX, velX, velY)
            onOOBFromAdversary(args);
        }
        if (readBytes == GOAL.getBytes().length) { // "GOAL" should be 4 bytes
            onGoalFromUser();
        }
    }

    @Override
    public void onToast(Bundle args) {
        Toast.makeText(this,
                args.getString(BluetoothService.MessageContent.KEY_MESSAGE),
                Toast.LENGTH_SHORT
        ).show();
    }

    @Override
    public void onError(Bundle args) {
        Toast.makeText(this,
                args.getString(BluetoothService.MessageContent.KEY_MESSAGE),
                Toast.LENGTH_LONG
        ).show();
        backToMenu();
    }

    protected void backToMenu() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.back_to_menu)
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        goToMenu();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        goToMenu();
                    }
                })
                .setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        goToMenu();
                        return true;
                    }
                })
                .show();
    }

    protected void goToMenu() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBall.stopHandler();
    }

    @Override
    public void onOffScreen() { // Called when the ball goes offscreen (the ball went beyond the top wall)
        if (mBound) {
            float positionX = mBall.getRelativeX();
            float velocityX = mBall.getVelocityX();
            float velocityY = mBall.getVelocityY();
            float[] data = new float[3];
            data[VELOCITY_X] = velocityX;
            data[VELOCITY_Y] = velocityY;
            data[POSITION_X] = positionX;

            byte[] dataAsByteArray = ByteArrayConverter.floatArray2ByteArray(data);
            mService.write(dataAsByteArray);
            mBall.pause();
        } else {
            onGoalFromUser();
        }
    }

    @SuppressLint("SetTextI18n") // remove warning on "" + int
    @Override
    public void onGoal() { // Called when a goal was scored by the adversary (the ball hit your bottom wall)
        mAdversaryScore++;
        mAdversaryScoreView.setText(""+ mAdversaryScore);
        if (mBound) {
            mService.write("GOAL".getBytes());
            //TODO? add fancy animation
        }
    }
}
