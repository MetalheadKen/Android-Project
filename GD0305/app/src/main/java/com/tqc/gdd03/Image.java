package com.tqc.gdd03;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class Image extends Activity {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image);

        /* 取得 Broadcast Receiver 所傳入的 Bundle 物件 */
        Bundle bundle = getIntent().getExtras();
        Object[] pdus = (Object[]) bundle.get("pdus");

        /* 拆解原生簡訊傳送封包，以取得傳送者的門號與簡訊內容 */
        for (int i = 0; i < pdus.length; i++) {
            SmsMessage smsMessage = null;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                smsMessage = SmsMessage.createFromPdu((byte[]) pdus[i], bundle.getString("format"));
            } else {
                smsMessage = SmsMessage.createFromPdu((byte[]) pdus[i]);
            }

            /* 讀取簡訊 */
            String message = smsMessage.getDisplayMessageBody();
            String sender  = smsMessage.getDisplayOriginatingAddress();

            /* 顯示簡訊內容 */
            Toast.makeText(Image.this, String.format("SenderNum:%s, message:%s", sender, message), Toast.LENGTH_LONG).show();
        }
    }
}