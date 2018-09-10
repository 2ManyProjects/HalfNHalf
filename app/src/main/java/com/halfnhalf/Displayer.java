package com.halfnhalf;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.widget.Toast;

public class Displayer {

    public static void alertDisplayer(String title, String message, Context context){
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        AlertDialog ok = builder.create();
        ok.show();
    }

    public static void toaster(String message, @NonNull String duration, Context context){
        int toastDuration;
        if (duration.equals("s")){toastDuration = Toast.LENGTH_SHORT;}
        else{toastDuration = Toast.LENGTH_LONG;}
        Toast.makeText(context ,message,toastDuration).show();
    }
}
