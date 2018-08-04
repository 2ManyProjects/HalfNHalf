package com.halfnhalf;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;



public class LoginResult extends Activity {
    public static Profile userProfile = new Profile();
    static final String userInfo_key = "BackendlessUserInfo";
    static final String logoutButtonState_key = "LogoutButtonState";
    static final String store_key = "";

    private EditText backendlessUserInfo;
    private Button bkndlsLogoutButton;

    private String userInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_result);

        initUI();
        initUIBehaviour();

        Intent intent = getIntent();
        String message = intent.getStringExtra(userInfo_key);
        userProfile.setUserData(intent.getStringExtra(store_key));
        message = message == null ? "" : message;
        boolean logoutButtonState = intent.getBooleanExtra(logoutButtonState_key, true);

        if (logoutButtonState) {
            bkndlsLogoutButton.setVisibility(View.VISIBLE);
            backendlessUserInfo.setTextColor(ResourcesCompat.getColor(getResources(), android.R.color.black, null));
        }
        else {
            bkndlsLogoutButton.setVisibility(View.INVISIBLE);
            backendlessUserInfo.setTextColor(ResourcesCompat.getColor(getResources(), android.R.color.holo_red_dark, null));
        }
        backendlessUserInfo.setText(message);
        Displayer.toaster(userProfile.getUserData(), "5", getApplicationContext());
    }

    private void initUI() {
        backendlessUserInfo = (EditText) findViewById(R.id.editText_bkndlsBackendlessUserInfo);
        bkndlsLogoutButton = (Button) findViewById(R.id.button_bkndlsBackendlessLogout);
    }

    private void initUIBehaviour() {
        bkndlsLogoutButton.setOnClickListener(new View.OnClickListener() {
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
                backendlessUserInfo.setTextColor(ResourcesCompat.getColor(getResources(), android.R.color.black, null));
                backendlessUserInfo.setText("");
                bkndlsLogoutButton.setVisibility(View.INVISIBLE);
                finish();
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                backendlessUserInfo.setTextColor(ResourcesCompat.getColor(getResources(), android.R.color.holo_red_dark, null));
                backendlessUserInfo.setText(fault.toString() + "This is the fault");
            }
        });
    }
}

