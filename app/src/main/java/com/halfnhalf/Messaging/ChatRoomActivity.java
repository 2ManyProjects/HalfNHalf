package com.halfnhalf.Messaging;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
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
import com.backendless.persistence.DataQueryBuilder;
import com.backendless.rt.messaging.Channel;
import com.halfnhalf.HomePage;
import com.halfnhalf.MainLogin;
import com.halfnhalf.R;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class ChatRoomActivity extends AppCompatActivity {

  private EditText message;
  private ListView messagesView;
  private MessageAdapter messageAdapter;
  private MemberData data;
  String name = "";
  String receiving = "";
  ArrayList<Message> msgs;
  String allMsg;
  String MsgID;
  int index;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_chat_room);

    HomePage.getNewMsgs(false, ChatRoomActivity.this);
    registerReceiver(receiver, new IntentFilter(
              messageListener.NOTIFICATION));

    message = findViewById(R.id.message);
    Bundle bundle = getIntent().getExtras();
    allMsg = bundle.getString("rawMessage");
    index = bundle.getInt("index");
    msgs = HomePage.Messages.get(index);

    name = bundle.getString("name");
    receiving = bundle.getString("othername");
    MsgID = HomePage.MsgID;

    messageAdapter = new MessageAdapter(this);
    messagesView = (ListView) findViewById(R.id.messages_view);
    messagesView.setAdapter(messageAdapter);

    data = new MemberData( name, receiving);

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

  @Override
    public void onResume(){
        super.onResume();
      registerReceiver(receiver, new IntentFilter(
              messageListener.NOTIFICATION));
    }

  @Override
  public void onPause(){
      super.onPause();
      unregisterReceiver(receiver);
  }

    @Override
    protected void onDestroy() {
        Log.i("MAINACT", "onDestroy!");
        super.onDestroy();

    }

    @Override
    public void onBackPressed(){
        final Intent intent;
        intent = new Intent(ChatRoomActivity.this, StartChatActivity.class);
        intent.putExtra("rawMessage", allMsg);
        startActivity(intent);
        ChatRoomActivity.this.finish();
    }

  private void populateChat(){
    for(int i = 0; i < msgs.size(); i++){
      boolean belongsToCurrentUser = msgs.get(i).getData().getSender().equals(name);
      Message temp = new Message();
      temp.setText( msgs.get(i).getText());
      temp.setData( msgs.get(i).getData());
      temp.setBelongsToCurrentUser(belongsToCurrentUser);
      messageAdapter.add(temp);
      messageAdapter.push();
      messagesView.setSelection(messagesView.getCount() - 1);
    }
  }

    private void remakeChat(){
      messageAdapter.clear();
        for(int i = 0; i < msgs.size(); i++){
            boolean belongsToCurrentUser = msgs.get(i).getData().getSender().equals(name);
            Message temp = new Message();
            temp.setText( msgs.get(i).getText());
            temp.setData( msgs.get(i).getData());
            temp.setBelongsToCurrentUser(belongsToCurrentUser);
            messageAdapter.add(temp);
            messagesView.setSelection(messagesView.getCount() - 1);
        }
      messageAdapter.push();
    }

  private void handleFault(BackendlessFault fault) {
    Log.e("chatroom ", fault.toString());
  }

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("INCOMMING MSG", "");
            HomePage.getNewMsgs(false, ChatRoomActivity.this);
            displayNewMsgs();
        }
    };

    private void displayNewMsgs(){
        Log.i("Timer", "Looping ");
        if(HomePage.processing) {
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    displayNewMsgs();
                }
            }, MainLogin.DELAY_TIME);
        }else{
            msgs = HomePage.Messages.get(index);
            Log.i("Last Msg", "" + msgs.get(msgs.size()-1).toString());
            remakeChat();
        }
    }

  //Image Button was pressed
  public void sendMessage(View view) {
      sendMessage();
  }

  //Enter was pressed
  public void sendMessage() {
    String m = message.getText().toString();
    final Message temp = new Message();
    temp.setText(m);
    temp.setData(data);
    temp.setBelongsToCurrentUser(true);
    messageAdapter.add(temp);
    messagesView.setSelection(messagesView.getCount() - 1);
      Log.e("EMOJI MMESSAGE ", "" + m);
    //HomePage.allMsgs += temp.toString();

    final String saveData = temp.toString();
    final String otherUser = temp.getData().getReceiver();
    String path = getFilesDir() + "/messages/" + "allMsgs";
    try {
      BufferedWriter out = new BufferedWriter(new FileWriter(path));
      out.append(saveData);
      out.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

      String WhereClause = "name = " + "'" + otherUser + "'";
      DataQueryBuilder dataQuery = DataQueryBuilder.create();
      dataQuery.setWhereClause(WhereClause);
      Backendless.Data.of("Messages").find(dataQuery,
              new AsyncCallback<List<Map>>() {
                  @Override
                  public void handleResponse( List<Map> response )
                  {
                      String temp = "";
                      if(response.get(0).get("Received") != null){
                          temp = response.get(0).get("Received").toString();
                      }
                      final String sendMsgs = temp + saveData;
                      response.get(0).put("Received", sendMsgs);
                      Backendless.Persistence.of("Messages").save(response.get(0), new AsyncCallback<Map>() {
                          @Override
                          public void handleResponse(Map response) {
                          }

                          @Override
                          public void handleFault(BackendlessFault fault) {
                              // an error has occurred, the error code can be retrieved with fault.getCode()
                          }
                      });
                  }
                  @Override
                  public void handleFault( BackendlessFault fault )
                  {
                      fault.toString();   // an error has occurred, the error code can be retrieved with fault.getCode()
                  }
              } );

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

}