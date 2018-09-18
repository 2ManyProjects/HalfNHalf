package com.halfnhalf.Messaging;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class restartListener extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(restartListener.class.getSimpleName(), "Service Stops! RESTARTING!");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(new Intent(context, messageListener.class));
        } else {
            context.startService(new Intent(context, messageListener.class));
        }
    }
}
