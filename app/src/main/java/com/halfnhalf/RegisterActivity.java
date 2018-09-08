package com.halfnhalf;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;
import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends Activity {
    private final static java.text.SimpleDateFormat SIMPLE_DATE_FORMAT = new java.text.SimpleDateFormat("yyyy/MM/dd");

    private EditText nameField;
    private EditText emailField;
    private EditText passwordField;
    private Button registerButton;

    private String name;
    private String email;
    private String password;
    private String profileData;
    private BackendlessUser user;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initUI();
    }

    private void initUI() {
        nameField = (EditText) findViewById(R.id.nameField);
        emailField = (EditText) findViewById(R.id.emailField);
        passwordField = (EditText) findViewById(R.id.passwordField);
        registerButton = (Button) findViewById(R.id.registerButton);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRegisterButtonClicked();
            }
        });
    }

    private void onRegisterButtonClicked() {
        String nameText = nameField.getText().toString().trim();
        String emailText = emailField.getText().toString().trim();
        String passwordText = passwordField.getText().toString().trim();

        if (emailText.isEmpty()) {
            Displayer.toaster("Field 'email' cannot be empty.", "s", this);
            return;
        }else if(!isEmailValid(emailText)){
            Displayer.toaster("'email' must be a valid address.", "s", this);
            return;
        }else {
            email = emailText;
        }
        if (passwordText.isEmpty()) {
            Toast.makeText(this, "Field 'password' cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }else if(!isPasswordValid(passwordText)){
            Displayer.toaster("Passwords must be atleast 6 characters long with a Capital, number and special character, Passphrases need to be atleast 12 characters long, with no special requirements", "s", this);
            return;
        }else{
            password = passwordText;
        }

        if (!nameText.isEmpty()) {
            name = nameText;
        }

        user = new BackendlessUser();

        if (email != null) {
            user.setEmail(email);
        }

        if (password != null) {
            user.setPassword(password);
        }

        if (name != null) {
            user.setProperty("name", name);
        }
        profileData = new String(name + "#" + email + "#" + Integer.toString(0) + "#" + Integer.toString(0) + "#");
        user.setProperty("profileData", profileData);
        Displayer.toaster("Attempting Register: " + profileData, "s", this);
        Backendless.UserService.register(user, new AsyncCallback<BackendlessUser>() {
            @Override
            public void handleResponse(BackendlessUser response) {
                final BackendlessUser user = response;
                Resources resources = getResources();
                String message = String.format(resources.getString(R.string.registration_success_message), resources.getString(R.string.app_name));

                AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                builder.setMessage(message).setTitle(R.string.registration_success);
                AlertDialog dialog = builder.create();
                dialog.show();
                HashMap msging = new HashMap();
                msging.put("Received", "");
                msging.put("allMsgs", "");
                Backendless.Persistence.of("Messages").save(msging, new AsyncCallback<Map>() {
                    @Override
                    public void handleResponse(Map msg) {
                        final String id = msg.get("objectId").toString();
                        Log.e("Message id", "" + id);
                        String whereClause = "name = " + "'" + name + "'";
                        DataQueryBuilder dataQuery = DataQueryBuilder.create();
                        dataQuery.setWhereClause(whereClause);
                        Backendless.Data.of(BackendlessUser.class).find(dataQuery,
                                new AsyncCallback<List<BackendlessUser>>() {
                                    @Override
                                    public void handleResponse(List<BackendlessUser> foundUser) {
                                        foundUser.get(0).setProperty("messageID", id);
                                        Backendless.UserService.update( foundUser.get(0), new AsyncCallback<BackendlessUser>()
                                        {
                                            @Override
                                            public void handleResponse( BackendlessUser backendlessUser )
                                            {
                                                Intent intent = new Intent(RegisterActivity.this, MainLogin.class);
                                                startActivity(intent);
                                            }

                                            @Override
                                            public void handleFault( BackendlessFault backendlessFault )
                                            {

                                            }
                                        }  );
                                    }

                                    @Override
                                    public void handleFault(BackendlessFault fault) {
                                        Log.e("TOKEN ISSUE: ", "" + fault.getMessage());
                                    }
                                });

                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {

                    }
                });
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                builder.setMessage(fault.getMessage()).setTitle(R.string.registration_error);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    private boolean isPasswordValid(String newPassword) {
        //TODO: REMOVE THIS FOR RELEASE
        if(newPassword.equals("test"))
            return true;
        if(newPassword.length() >= 12)
            return true;
        return newPassword.length()>= 6 && isValidPass(newPassword);
    }

    public static boolean isValidPass(final String password) {

        Pattern pattern;
        Matcher matcher;
        final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{4,}$";
        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);

        return matcher.matches();

    }

    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}