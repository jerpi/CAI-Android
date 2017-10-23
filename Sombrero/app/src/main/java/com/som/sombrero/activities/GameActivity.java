package com.som.sombrero.activities;

import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;

import com.som.sombrero.R;
import com.som.sombrero.exceptions.HandlerLaunchedException;
import com.som.sombrero.listeners.LeaveScreenListener;
import com.som.sombrero.listeners.WallBounceListener;
import com.som.sombrero.views.BallView;


/**
 * Created by Jérémy on 17/10/2017.
 */
public class GameActivity extends AppCompatActivity implements LeaveScreenListener, WallBounceListener {

    private static final int REQUEST_ENABLE_BT = 12;

    private static final String TAG = "GameActivity";

    private BluetoothAdapter mBluetoothAdapter;
    BallView mBall;

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
        mBall.setLeaveScreenListener(this);
        mBall.setWallBounceListener(this);

        init();

        /*pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
            }
        }
        */
    }

    private void init() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) {
            enableBluetooth();
        } else {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.no_bluetooth)
                    .show();
        }
    }

    private void enableBluetooth() {
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data
    ) {
        switch(requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK) {
                    //TODO
                } else {
                    backToMenu();
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        mBall.stopHandler();
        super.onDestroy();
    }

    @Override
    public void onWallBounced() {
        Log.d(TAG, "WallBounced");
    }

    @Override
    public void onScreenLeft() {
        Log.d(TAG, "onScreenLeft");
    }

    protected void goToMenu() {
        Intent intent = new Intent(GameActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
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
}
