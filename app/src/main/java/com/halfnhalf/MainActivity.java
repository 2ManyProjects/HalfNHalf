package com.halfnhalf;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.backendless.Backendless;
import com.backendless.IDataStore;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;

import java.util.HashMap;
import java.util.Map;
public class MainActivity extends Activity {
    private TextView status;
    private Button updateButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Backendless.setUrl(Defaults.SERVER_URL);
        Backendless.initApp(getApplicationContext(), Defaults.APPLICATION_ID, Defaults.API_KEY);

        status = (TextView) findViewById(R.id.status);
        updateButton = (Button) findViewById(R.id.update_button);
        updateButton.setEnabled(false);

        HashMap testObject = new HashMap<>();
        testObject.put("foo", "Hello World");
        final IDataStore<Map> testTableDataStore = Backendless.Data.of("TestTable");
        testTableDataStore.save(testObject, new AsyncCallback<Map>() {
            @Override
            public void handleResponse(final Map response) {
                subscribeForObjectUpdate(response, testTableDataStore);

                updateButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        updateValue(response, testTableDataStore);
                    }
                });

                status.setText("Object has been saved in the real-time database");
                changeSavedValue(response);
                updateButton.setEnabled(true);
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                MainActivity.this.handleFault(fault);
            }
        });


    }

    private void updateValue(Map response, IDataStore<Map> testTableDataStore) {
        updateButton.setEnabled(false);
        final EditText propertyValueText = (EditText) findViewById(R.id.property_value);
        response.put("foo", propertyValueText.getText().toString());
        testTableDataStore.save(response, new AsyncCallback<Map>() {
            @Override
            public void handleResponse(Map response) {
                Log.i("MYAPP", "saved " + response);
                updateButton.setEnabled(true);
                propertyValueText.setText("");
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                MainActivity.this.handleFault(fault);
            }
        });
    }

    private void subscribeForObjectUpdate(Map response, IDataStore<Map> testTableDataStore) {
        testTableDataStore.rt().addUpdateListener("objectId='" + response.get("objectId") + "'", new AsyncCallback<Map>() {
            @Override
            public void handleResponse(Map response) {
                changeSavedValue(response);
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                MainActivity.this.handleFault(fault);
            }
        });
    }

    private void handleFault(BackendlessFault fault) {
        String msg = "Server reported an error " + fault.getMessage();
        Log.e("MYAPP", msg);
        status.setText(msg);
        updateButton.setEnabled(true);
    }

    private void changeSavedValue(Map response) {
        TextView savedValueTextView = (TextView) findViewById(R.id.saved_value);
        savedValueTextView.setText((String) response.get("foo"));
    }
}
