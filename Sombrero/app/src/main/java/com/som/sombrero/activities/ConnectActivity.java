package com.som.sombrero.activities;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.som.sombrero.R;
import com.som.sombrero.adapters.ConnectListViewAdapter;
import com.som.sombrero.services.BluetoothService;

public class ConnectActivity extends BluetoothActivity implements AdapterView.OnItemClickListener, View.OnClickListener,
        BluetoothActivity.OnConnectionListener, BluetoothActivity.OnToastListener, BluetoothActivity.OnErrorListener
{

    @SuppressWarnings("unused")
    private static final String TAG = "ConnectActivity";

    /**
     * Bundle keys
     */
    public static final String IS_HOST = "IS_HOST";

    /**
     * Request codes
     */
    private static final int REQUEST_BT_ENABLE = 1;
    private static final int REQUEST_BT_DISCOVERABLE = 2;

    /**
     * Views / Adapters
     */
    private Button mDiscoverableButton;
    private ConnectListViewAdapter mAdapter;
    private ListView mListView;
    private ProgressBar mProgressBar;

    /**
     * Bluetooth
     */
    private BluetoothAdapter mBluetoothAdapter;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mListView.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.GONE);
                mAdapter.addToDataSet(device);
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        onConnectionListener = this;
        onErrorListener = this;
        onToastListener = this;
        setUpViews();
        init();
    }

    private void setUpViews() {
        mDiscoverableButton = (Button) findViewById(R.id.connect_set_discoverable);
        mDiscoverableButton.setOnClickListener(this);

        mProgressBar = (ProgressBar) findViewById(R.id.connect_progress_bar);

        mListView = (ListView) findViewById(R.id.connect_list_view);
        mAdapter = new ConnectListViewAdapter();
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        mListView.setVisibility(View.GONE);
    }

    private void init() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.no_bluetooth)
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
                    }).show();
            return;
        }
        enableBluetooth();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data
    ) {
        switch(requestCode) {
            case REQUEST_BT_ENABLE:
                if (resultCode == RESULT_CANCELED) {
                    backToMenu();
                    return;
                }
                break;
            case REQUEST_BT_DISCOVERABLE:
                if (resultCode == RESULT_CANCELED) {
                    return;
                }
                break;
        }
    }

    private void enableBluetooth() {
        if (!mBluetoothAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_BT_ENABLE);
        } else {
            start();
        }
    }

    private void enableDiscoverable() {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivityForResult(intent, REQUEST_BT_DISCOVERABLE);
        mDiscoverableButton.setVisibility(View.GONE);
    }

    private void start() {
        bindToBluetoothService();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
        mBluetoothAdapter.startDiscovery();
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
        Intent intent = new Intent(ConnectActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mBound) {
            mService.connectToDevice(mAdapter.getItem(position).getAddress());
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.connect_set_discoverable:
                enableDiscoverable();
                break;
        }
    }

    @Override
    public void onConnection(Bundle args) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtras(args);
        startActivity(intent);
    }

    @Override
    public void onToast(Bundle args) {
        Toast.makeText(this, args.getString(BluetoothService.MessageContent.KEY_TOAST), Toast.LENGTH_SHORT)
                .show();
    }

    @Override
    public void onError(Bundle args) {
        Toast.makeText(this, args.getString(BluetoothService.MessageContent.KEY_TOAST), Toast.LENGTH_LONG)
                .show();
    }
}
