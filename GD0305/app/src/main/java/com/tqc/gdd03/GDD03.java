package com.tqc.gdd03;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

public class GDD03 extends Activity {

    private static final int REQUEST_SMS_PERMISSION = 999;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        CheckPermission();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_SMS_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    /* 取得權限，程式繼續 */
                    return;
                } else {
                    /* 使用者拒絕權限，程式結束 */
                    Toast.makeText(GDD03.this, "本程式無法接收簡訊，程式結束", Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    private void CheckPermission() {
        //Check For Permissions
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            /* 未取得權限，向使用者要求權限 */
            ActivityCompat.requestPermissions(
                    this,
                    new String[] {Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS, Manifest.permission.BROADCAST_SMS},
                    REQUEST_SMS_PERMISSION
            );
        }

        //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
    }

}
