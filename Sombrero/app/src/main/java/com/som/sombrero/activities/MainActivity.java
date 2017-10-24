package com.som.sombrero.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.som.sombrero.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private static final int PERMISSIONS_COARSE_LOCATION = 1;

    TextView mainText;
    Button mainButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainText = (TextView) findViewById(R.id.main_textview);
        mainButton = (Button) findViewById(R.id.main_button);
        mainButton.setOnClickListener(this);

        askPermissions();
    }


    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.main_button:
                startConnectActivity();
                break;
            default:
                break;
        }
    }

    public void startConnectActivity() {
        Intent i = new Intent(this, ConnectActivity.class);
        startActivity(i);
    }

    public void startGameActivity() {
        Intent i = new Intent(this, GameActivity.class);
        startActivity(i);
    }

    public boolean checkPermissions() {
        int hasPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        return hasPermission == PackageManager.PERMISSION_GRANTED;
    }

    public void askPermissions() {
        if (checkPermissions()) { return; }

        mainButton.setEnabled(false);
        ActivityCompat.requestPermissions(
                this,
                new String[]{ Manifest.permission.ACCESS_COARSE_LOCATION },
                PERMISSIONS_COARSE_LOCATION
        );
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String permissions[],
            @NonNull int[] grantResults
    ) {
        switch (requestCode) {
            case PERMISSIONS_COARSE_LOCATION: {
                if (grantResults.length > 0) {
                    for (int grantResult : grantResults) {
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
                            showPermissionsMessage();
                            return;
                        }
                    }
                    mainButton.setEnabled(true);
                } else {
                    showPermissionsMessage();
                }
                break;
            }
        }
    }

    protected void showPermissionsMessage() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.permissions_required)
                .setPositiveButton(R.string.settings, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        openSettings();
                    }
                })
                .show();
    }

    private void openSettings() {
        final Intent i = new Intent();
        i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        i.setData(Uri.parse("package:" + getPackageName()));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        startActivity(i);
    }
}
