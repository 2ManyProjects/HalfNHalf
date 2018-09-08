package com.halfnhalf.Messaging;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.messaging.MessageStatus;
import com.backendless.rt.messaging.Channel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.halfnhalf.Defaults;
import com.halfnhalf.R;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;



public class ChatRoomActivity extends AppCompatActivity {

  public static final String TAG = "RTChat";
  private EditText message;
  private ListView messagesView;
  private MessageAdapter messageAdapter;
  private Channel channel;
  private String color = ColorPickerUtility.next();
  private MemberData data;
  String name = "";
  String receiver = "";
  ArrayList<Message> msgs;
  String allMsg;
  String MsgID;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_chat_room);

    message = findViewById(R.id.message);
    //messages = findViewById(R.id.messages);
    Bundle bundle = getIntent().getExtras();
    String getMessages = bundle.getString("convo");
    allMsg = bundle.getString("rawMessage");
    Gson gson = new Gson();
    Type type = new TypeToken<ArrayList<Message>>() {
    }.getType();
    msgs = gson.fromJson(getMessages, type);

    name = bundle.getString("name");
    receiver = bundle.getString("othername");
    MsgID = bundle.getString("msgID");

    messageAdapter = new MessageAdapter(this);
    messagesView = (ListView) findViewById(R.id.messages_view);
    messagesView.setAdapter(messageAdapter);

    data = new MemberData( name, receiver);

    populateChat();
    message.setOnEditorActionListener(new TextView.OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        boolean handled = false;

        if ( actionId == EditorInfo.IME_ACTION_SEND || event.getKeyCode() == KeyEvent.KEYCODE_ENTER ) {
          if(v.getText().toString().length() > 0)
            sendMessage();
          handled = true;
        }

        return handled;
      }
    });
  }

  //TODO: write a service that checks the received messages every Xseconds

  private void populateChat(){
    for(int i = 0; i < msgs.size(); i++){
      boolean belongsToCurrentUser = msgs.get(i).getData().getSender().equals(name);
      Message temp = new Message();
      temp.setText( msgs.get(i).getText());
      temp.setData( msgs.get(i).getData());
      temp.setBelongsToCurrentUser(belongsToCurrentUser);
      messageAdapter.add(temp);
      messagesView.setSelection(messagesView.getCount() - 1);
    }
  }

  private void handleFault(BackendlessFault fault) {
    Log.e(TAG, fault.toString());
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

    if (channel != null)
      channel.leave();
  }


  //Image Button
  public void sendMessage(View view) {
    String m = message.getText().toString();
    final Message temp = new Message();
    temp.setText(m);
    temp.setData(data);
    temp.setBelongsToCurrentUser(true);
    messageAdapter.add(temp);
    messagesView.setSelection(messagesView.getCount() - 1);

    final String saveData = temp.toString();
    String path = getFilesDir() + "/messages/" + "allMsgs";
    try {
      BufferedWriter out = new BufferedWriter(new FileWriter(path));
      out.append(saveData);
      out.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    Backendless.Data.of("Messages").findById(MsgID,
            new AsyncCallback<Map>() {
              @Override
              public void handleResponse( Map response )
              {
                if(allMsg.length() > 6) {
                  allMsg += saveData;
                  response.put("allMsgs", allMsg);
                  Backendless.Persistence.of("Messages").save(response, new AsyncCallback<Map>() {
                    @Override
                    public void handleResponse(Map response) {
                      // Contact objecthas been updated
                      message.setText("", TextView.BufferType.EDITABLE);
                      message.setEnabled(true);
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                      // an error has occurred, the error code can be retrieved with fault.getCode()
                    }
                  });
                }
              }
              @Override
              public void handleFault( BackendlessFault fault )
              {
                fault.toString();   // an error has occurred, the error code can be retrieved with fault.getCode()
              }
            } );

    message.getText().clear();
  }

  //Enter
  public void sendMessage() {
    String m = message.getText().toString();
    final Message temp = new Message();
    temp.setText(m);
    temp.setData(data);
    temp.setBelongsToCurrentUser(true);
    messageAdapter.add(temp);
    messagesView.setSelection(messagesView.getCount() - 1);

    final String saveData = temp.toString();
    String path = getFilesDir() + "/messages/" + "allMsgs";
    try {
      BufferedWriter out = new BufferedWriter(new FileWriter(path));
      out.append(saveData);
      out.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    Backendless.Data.of("Messages").findById(MsgID,
            new AsyncCallback<Map>() {
              @Override
              public void handleResponse( Map response )
              {
                if(allMsg.length() > 6) {
                  allMsg += saveData;
                  response.put("allMsgs", allMsg);
                  Backendless.Persistence.of("Messages").save(response, new AsyncCallback<Map>() {
                    @Override
                    public void handleResponse(Map response) {
                      // Contact objecthas been updated
                      message.setText("", TextView.BufferType.EDITABLE);
                      message.setEnabled(true);
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                      // an error has occurred, the error code can be retrieved with fault.getCode()
                    }
                  });
                }
              }
              @Override
              public void handleFault( BackendlessFault fault )
              {
                fault.toString();   // an error has occurred, the error code can be retrieved with fault.getCode()
              }
            } );

    message.getText().clear();
  }

  private String getRandomColor() {
    Random r = new Random();
    StringBuffer sb = new StringBuffer("#");
    while(sb.length() < 7){
      sb.append(Integer.toHexString(r.nextInt()));
    }
    return sb.toString().substring(0, 7);
  }

}