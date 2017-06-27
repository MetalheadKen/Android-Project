package com.tqc.gdd03;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class SMSreceiver extends BroadcastReceiver {
    String TAG = "SMSreceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive");
        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            Intent it = new Intent(context, Image.class);
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            it.putExtras(intent.getExtras());
            context.startActivity(it);
        }
    }
}

