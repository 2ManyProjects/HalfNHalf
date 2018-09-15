package com.halfnhalf.Messaging;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;
import android.os.Process;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static com.halfnhalf.HomePage.MsgID;

public class messageListener extends Service {
    private String data;
    public static String NOTIFICATION = "com.halfnhalf.Messaging.receiver";

    public messageListener(Context applicationContext) {
        super();
        Log.i("HERE", "here I am!");
    }

    public messageListener() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        startTimer();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("EXIT" , "ondestroy!");
        Intent broadcastIntent = new Intent("restartService");
        sendBroadcast(broadcastIntent);
        stoptimertask();
        //startService(new Intent(this, messageListener.class));
        //TODO get it to restart
    }

    private Timer timer;
    private TimerTask timerTask;

    public void startTimer() {
        timer = new Timer();

        initializeTimerTask();

        timer.schedule(timerTask, 1000, 4000); //
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("isMyServiceRunning?", true+"");
                return true;
            }
        }
        Log.i ("isMyServiceRunning?", false+"");
        return false;
    }


    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                getMsg();
            }
        };
    }

    public void stoptimertask() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void getMsg(){
        data = null;
        Backendless.Data.of("Messages").findById(MsgID,
                new AsyncCallback<Map>() {
                    @Override
                    public void handleResponse( Map response ) {
                        if(response.get("buyingReceived")!= null || response.get("sellingReceived") != null){
                            data = response.get("buyingReceived").toString() + response.get("sellingReceived").toString();
                            if (data.length() > 5)
                                publishResults();
                        }
                    }
                    @Override
                    public void handleFault( BackendlessFault fault )
                    {
                        Log.e("Response = null", "" + fault.toString());   // an error has occurred, the error code can be retrieved with fault.getCode()
                    }
                } );
    }

    private void publishResults() {
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra("newData", true);
        sendBroadcast(intent);
    }

}