package com.halfnhalf;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.async.callback.BackendlessCallback;
import com.backendless.exceptions.BackendlessFault;
import com.google.gson.Gson;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

//TODO CHANGE IDENTIFIER TO EMAIL IN BACKEND (TABLE SCHEMA)

public class MainLogin extends Activity {

    private boolean isLoggedInBackendless = false;
    private CheckBox rememberLoginBox;


    // backendless
    private TextView registerLink, restoreLink;
    private EditText identityField, passwordField;
    private Button LoginButton;
    private String testPass;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_login);

        Backendless.initApp( this, getString(R.string.backendless_AppId), getString(R.string.backendless_ApiKey));
        Backendless.setUrl(getString(R.string.backendless_ApiHost));

        initUI();
        initUIBehaviour();

        Backendless.UserService.isValidLogin(new DefaultCallback<Boolean>(this) {
            @Override
            public void handleResponse(Boolean isValidLogin) {
                super.handleResponse(null);
                if (isValidLogin && Backendless.UserService.CurrentUser() == null) {
                    String currentUserId = Backendless.UserService.loggedInUser();

                    if (!currentUserId.equals("")) {
                        Backendless.UserService.findById(currentUserId, new DefaultCallback<BackendlessUser>(MainLogin.this, "Logging in...") {
                            @Override
                            public void handleResponse(BackendlessUser currentUser) {
                                super.handleResponse(currentUser);
                                isLoggedInBackendless = true;
                                Backendless.UserService.setCurrentUser(currentUser);
                                startHomePage(currentUser,passwordField.getText().toString());
                            }
                        });
                    }
                }
                super.handleResponse(isValidLogin);
            }
        });
    }

    private void initUI() {
        rememberLoginBox = (CheckBox) findViewById( R.id.rememberLoginBox );


        // backendless
        registerLink = (TextView) findViewById( R.id.registerLink );
        restoreLink = (TextView) findViewById( R.id.restoreLink );
        identityField = (EditText) findViewById( R.id.identityField );
        passwordField = (EditText) findViewById( R.id.passwordField );
        LoginButton = (Button) findViewById( R.id.LoginButton);

    }

    private void initUIBehaviour() {

        // backendless
        LoginButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick( View view )
            {
                onLoginWithBackendlessButtonClicked();
            }
        } );
        registerLink.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View view )
            {
                onRegisterLinkClicked();
            }
        } );
        restoreLink.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View view )
            {
                onRestoreLinkClicked();
            }
        } );

    }

    private void startHomePage(BackendlessUser user, String password)
    {
        String msg = "ObjectId: " + user.getObjectId() + "\n"
                + "UserId: " + user.getUserId() + "\n"
                + "Email: " + user.getEmail() + "\n"
                + "LastLogin: " + user.getProperty("lastLogin") + "\n"
                + "Profile Data: " + user.getProperty("profileData");
        String userStoreData = (String)user.getProperty("profileData");

        for (Map.Entry<String, Object> entry : user.getProperties().entrySet())
            msg.concat(entry.getKey() + " : " + entry.getValue() + "\n");


        Intent intent = new Intent(this, HomePage.class);
        intent.putExtra("objectID", user.getObjectId());
        intent.putExtra("password", password);
        intent.putExtra(HomePage.userInfo_key, msg);
        intent.putExtra("data", userStoreData);
        intent.putExtra(HomePage.logoutButtonState_key, true);
        startActivity(intent);
    }

    private void startHomePage(String msg, boolean logoutButtonState){
        Intent intent = new Intent(this, HomePage.class);
        intent.putExtra(HomePage.userInfo_key, msg);
        intent.putExtra(HomePage.logoutButtonState_key, logoutButtonState);
        startActivity(intent);
    }


    private void onLoginWithBackendlessButtonClicked()
    {
        String identity = identityField.getText().toString();
        String password = passwordField.getText().toString();
        boolean rememberLogin = rememberLoginBox.isChecked();
        testPass = password;
        Backendless.UserService.login( identity, password, new DefaultCallback<BackendlessUser>( MainLogin.this )
        {
            public void handleResponse( BackendlessUser backendlessUser ) {
                super.handleResponse( backendlessUser );
                isLoggedInBackendless = true;
                startHomePage(backendlessUser, testPass);
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                super.handleFault(fault);
                startHomePage(fault.toString(), false);
            }
        }, rememberLogin );
    }

    private void onRegisterLinkClicked()
    {
        Displayer.toaster("Register Button Clicked", "3", getApplicationContext());
        startActivity( new Intent( this, RegisterActivity.class ) );
    }

    private void onRestoreLinkClicked()
    {
        startActivity( new Intent( this, RestorePasswordActivity.class ) );
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


    }
}
