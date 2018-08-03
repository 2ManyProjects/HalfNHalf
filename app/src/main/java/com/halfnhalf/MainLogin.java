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



import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class MainLogin extends Activity {

    private boolean isLoggedInBackendless = false;
    private CheckBox rememberLoginBox;


    // backendless
    private TextView registerLink, restoreLink;
    private EditText identityField, passwordField;
    private Button bkndlsLoginButton;


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
                                startLoginResult(currentUser);
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
        bkndlsLoginButton = (Button) findViewById( R.id.bkndlsLoginButton);

    }

    private void initUIBehaviour() {

        // backendless
        bkndlsLoginButton.setOnClickListener(new View.OnClickListener()
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

    private void startLoginResult(BackendlessUser user)
    {
        String msg = "ObjectId: " + user.getObjectId() + "\n"
                + "UserId: " + user.getUserId() + "\n"
                + "Email: " + user.getEmail() + "\n"
                + "Properties: " + "\n";

        for (Map.Entry<String, Object> entry : user.getProperties().entrySet())
            msg += entry.getKey() + " : " + entry.getValue() + "\n";


        Intent intent = new Intent(this, LoginResult.class);
        intent.putExtra(LoginResult.userInfo_key, msg);
        intent.putExtra(LoginResult.logoutButtonState_key, true);
        startActivity(intent);
    }

    private void startLoginResult(String msg, boolean logoutButtonState)
    {
        Intent intent = new Intent(this, LoginResult.class);
        intent.putExtra(LoginResult.userInfo_key, msg);
        intent.putExtra(LoginResult.logoutButtonState_key, logoutButtonState);
        startActivity(intent);
    }


    private void onLoginWithBackendlessButtonClicked()
    {
        String identity = identityField.getText().toString();
        String password = passwordField.getText().toString();
        boolean rememberLogin = rememberLoginBox.isChecked();

        Backendless.UserService.login( identity, password, new DefaultCallback<BackendlessUser>( MainLogin.this )
        {
            public void handleResponse( BackendlessUser backendlessUser )
            {
                super.handleResponse( backendlessUser );
                isLoggedInBackendless = true;
                startLoginResult(backendlessUser);
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                super.handleFault(fault);
                startLoginResult(fault.toString(), false);
            }
        }, rememberLogin );
    }

    private void onRegisterLinkClicked()
    {
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
