package com.halfnhalf;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

public class Permissons {

    //Request Permisson
    public static void Request_CAMERA(Activity act, int code){
        ActivityCompat.requestPermissions(act, new
                String[]{Manifest.permission.CAMERA},code);
    }

    public static void Request_FINE_LOCATION(Activity act, int code){
        ActivityCompat.requestPermissions(act, new
                String[]{Manifest.permission.ACCESS_FINE_LOCATION},code);
    }

    public static void Request_COARSE_LOCATION(Activity act, int code){
        ActivityCompat.requestPermissions(act, new
                String[]{Manifest.permission.ACCESS_COARSE_LOCATION},code);
    }

    public static void Request_RECORD_AUDIO(Activity act, int code){
        ActivityCompat.requestPermissions(act, new
                String[]{Manifest.permission.RECORD_AUDIO},code);
    }

    //Check Permisson
    public static boolean Check_CAMERA(Activity act){
        int result = ContextCompat.checkSelfPermission(act, Manifest.permission.CAMERA);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean Check_FINE_LOCATION(Activity act){
        int result = ContextCompat.checkSelfPermission(act, Manifest.permission.ACCESS_FINE_LOCATION);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean Check_COARSE_LOCATION(Activity act){
        int result = ContextCompat.checkSelfPermission(act, Manifest.permission.ACCESS_COARSE_LOCATION);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean Check_RECORD_AUDIO(Activity act){
        int result = ContextCompat.checkSelfPermission(act, Manifest.permission.RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED;
    }
}
