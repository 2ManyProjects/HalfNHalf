package com.halfnhalf;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;

public class HomePage extends AppCompatActivity {
    static final String userInfo_key = "BackendlessUserInfo";
    static final String logoutButtonState_key = "LogoutButtonState";
    static final String store_key = "";

    private TextView UserInfo;
    private Button LogoutButton;
    private FloatingActionButton profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        initUI();
        initUIBehaviour();

        Intent intent = getIntent();
        String message = intent.getStringExtra(userInfo_key);
        message = message == null ? "" : message;
        boolean logoutButtonState = intent.getBooleanExtra(logoutButtonState_key, true);

        if (logoutButtonState) {
            LogoutButton.setVisibility(View.VISIBLE);
            UserInfo.setTextColor(ResourcesCompat.getColor(getResources(), android.R.color.black, null));
        }
        else {
            LogoutButton.setVisibility(View.INVISIBLE);
            UserInfo.setTextColor(ResourcesCompat.getColor(getResources(), android.R.color.holo_red_dark, null));
        }
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                launch();
                //launchActivity("MapTest");
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
            }
        });
        UserInfo.setText(message);
    }

    private void launch(){
        Intent i = getIntent();
        Intent intent;
        intent = new Intent(this, Profile.class);
        intent.putExtra("String", i.getStringExtra(store_key));
        startActivity(intent);
    }
    private void initUI() {
        UserInfo = (TextView) findViewById(R.id.textView_UserInfo);
        LogoutButton = (Button) findViewById(R.id.button_Logout);
        profile = (FloatingActionButton) findViewById(R.id.fab_profileBtn);
    }

    private void initUIBehaviour() {
        LogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutFromBackendless();
            }
        });
    }

    private void logoutFromBackendless(){
        Backendless.UserService.logout(new AsyncCallback<Void>() {
            @Override
            public void handleResponse(Void response) {
                UserInfo.setTextColor(ResourcesCompat.getColor(getResources(), android.R.color.black, null));
                UserInfo.setText("");
                LogoutButton.setVisibility(View.INVISIBLE);
                finish();
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                UserInfo.setTextColor(ResourcesCompat.getColor(getResources(), android.R.color.holo_red_dark, null));
                UserInfo.setText(fault.toString().concat("This is the fault"));
            }
        });
    }
}
