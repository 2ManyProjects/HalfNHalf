package com.halfnhalf.Messaging;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;
import android.os.Process;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.halfnhalf.HomePage;
import com.halfnhalf.R;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static android.app.NotificationManager.IMPORTANCE_HIGH;
import static com.halfnhalf.HomePage.MsgID;

public class messageListener extends Service {
    private String data;
    public static String NOTIFICATION = "com.halfnhalf.Messaging.receiver";
    public int counter = 0;
    public static boolean isloggedIn = true;

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
            startForeground(1, makeNotification());
        else
            startForeground(1, new Notification());
    }

    public void setIsloggedIn(boolean bool){
        this.isloggedIn = bool;
        Log.e("Current Value: ",  "" + this.isloggedIn);
    }

    public messageListener(Context applicationContext) {
        super();
        Log.i("Loading ", "Message Listener");
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
        if(isloggedIn) {
            Intent broadcastIntent = new Intent("restartingService");
            broadcastIntent.setClass(this, restartListener.class);
            this.sendBroadcast(broadcastIntent);
            Log.i("EXIT", "is Logged in, Restart!");
            stoptimertask();
        }else{
            Log.i("EXIT", "ondestroy!");
            stoptimertask();
        }
        super.onDestroy();
    }

    private Notification makeNotification(){

        String CHANNEL_ONE_ID = "com.halfnhalf.messaging";
        String CHANNEL_ONE_NAME = "Channel One";
        NotificationChannel notificationChannel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(CHANNEL_ONE_ID,
                    CHANNEL_ONE_NAME, IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setShowBadge(true);
            notificationChannel.setSound(null, null);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(notificationChannel);
        }

        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        Notification notification = new Notification.Builder(getApplicationContext())
                .setChannelId(CHANNEL_ONE_ID)
                .setSound(null)
                .setContentTitle(getString(R.string.NotificationTitle))
                .setContentText(getString(R.string.NotificationContent))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(icon)
                .build();

        Intent notificationIntent = new Intent(getApplicationContext(), HomePage.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        notification.contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);
        return notification;
    }


    private Timer timer;
    private TimerTask timerTask;

    public void startTimer() {
        timer = new Timer();

        initializeTimerTask();

        timer.schedule(timerTask, 1000, 4000); //
    }



    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                Log.i("still Running", "" + (counter++));
                getMsg();
            }
        };
    }

    public void stoptimertask() {
        if (timer != null){
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