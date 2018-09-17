package com.halfnhalf.Messaging;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
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
import com.halfnhalf.Displayer;
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
  //String allMsg;
  String MsgID;
  int index;
  private int type = 0;
  private String col = "";
  private boolean firstContact = false;
  private boolean locked = true;
  private boolean processing = false;
  private String OGsnapShot = "";
  private String snapShot = "";
  //TODO: to save changes, replace instance of OGsnapShot in sellingData with the modified shapShot and re-upload (Same for buyingData)
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_chat_room);

    HomePage.getNewMsgs(false, ChatRoomActivity.this, 0);
    registerReceiver(receiver, new IntentFilter(
              messageListener.NOTIFICATION));

    message = findViewById(R.id.message);
    Bundle bundle = getIntent().getExtras();
    type = bundle.getInt("type");
    index = bundle.getInt("index");

    if(type == 100) { //First Contact
        type = 1;
        firstContact = true;
    }
      if(type == 0) {
          msgs = HomePage.Messages.get(index);
      }else if(type == 1) {
          msgs = HomePage.buyingMessages.get(index);
      }else if(type == 2) {
          msgs = HomePage.sellingMessages.get(index);
      }

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
    if(firstContact){
        sendMessage();
    }
      invalidateOptionsMenu();
      getData();
      startTimer();
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
        intent.putExtra("type", type);
        startActivity(intent);
        ChatRoomActivity.this.finish();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.chat_toolbar, menu);
        MenuItem modifydeal = menu.findItem(R.id.modifyDeal);
        MenuItem lock = menu.findItem(R.id.lock);
        MenuItem selectedDeal = menu.findItem(R.id.selectedDeal);
        MenuItem unlock = menu.findItem(R.id.unlock);
        if(type == 1){
            modifydeal.setVisible(true);
            lock.setVisible(false);
            unlock.setVisible(false);
            selectedDeal.setVisible(false);
        }else{
            modifydeal.setVisible(false);
            if(locked) {
                lock.setVisible(false);
                unlock.setVisible(true);
            }else{
                lock.setVisible(true);
                unlock.setVisible(false);
            }
            selectedDeal.setVisible(true);
        }
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            return true;
        }else{
            return super.onOptionsItemSelected(item);
        }
    }

    private void startTimer(){
        if(processing) {
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    startTimer();
                }
            }, MainLogin.DELAY_TIME);
        }else{
            Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
            setSupportActionBar(myToolbar);

        }
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


    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("INCOMMING MSG", "");
            HomePage.getNewMsgs(false, ChatRoomActivity.this, type);
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
            if(type == 0) {
                msgs = HomePage.Messages.get(index);
            }else if(type == 1) {
                msgs = HomePage.buyingMessages.get(index);
            }else if(type == 2) {
                msgs = HomePage.sellingMessages.get(index);
            }
            Log.i("Last Msg", "" + msgs.get(msgs.size()-1).toString());
            remakeChat();
        }
    }

    private void getData(){
        processing = true;
        String WhereClause = "name = " + "'" + receiving + "'";
        if(type == 2)
            WhereClause = "name = " + "'" + name + "'";

        DataQueryBuilder dataQuery = DataQueryBuilder.create();
        dataQuery.setWhereClause(WhereClause);
        Backendless.Data.of("Messages").find(dataQuery,
                new AsyncCallback<List<Map>>() {
                    @Override
                    public void handleResponse(List<Map> foundUsers) {
                        if (foundUsers.size() >= 0) {
                            if (foundUsers.get(0).get("sellingData") != null) {
                                snapShot = trim(foundUsers.get(0).get("sellingData").toString());
                                OGsnapShot = snapShot;
                                Log.i("SNAPSHOT", snapShot + "");
                                //Trim it down
                                processing = false;
                            } else {
                                Displayer.alertDisplayer("Error retreiving seller Profile: ", "Please Reload Chat", ChatRoomActivity.this);
                            }
                        }
                    }
                    @Override
                    public void handleFault(BackendlessFault fault) {
                        Log.e("TOKEN ISSUE: ", "" + fault.getMessage());
                        Displayer.alertDisplayer("Error retreiving seller Profile: ", "Please Reload Chat", ChatRoomActivity.this);
                    }
                });

    }

    private String trim(String str){
        int startIndex;
        String buyer;
        String seller;
        if(type == 1){
            buyer = name;
            seller = receiving;
        }else{
            buyer = receiving;
            seller = name;
        }
        if(str.contains("0" + "#" + buyer + "#" + seller + "#")) {
            locked = false;
            startIndex = str.indexOf("0" + "#" + buyer + "#" + seller + "#");
        }else{
            locked = true;
            startIndex = str.indexOf("1" + "#" + buyer + "#" + seller + "#");
        }
        String lengthval = str.substring(startIndex + new String("0" + "#" + buyer + "#" + seller + "#").length());
        lengthval = lengthval.substring(0, lengthval.indexOf("#"));
        int snapLength = Integer.parseInt(lengthval);
        if(type == 1){
            startIndex += 2; //The buyer CANNOT LOCK / UNLOCK THE DEAL
        }
        return str.substring(startIndex, snapLength);
    }

  //Image Button was pressed
  public void sendMessage(View view) {
      if(message.getText().toString().length() > 0)
          sendMessage();
  }

  //Enter was pressed
  public void sendMessage() {
      String m = "";
        if(!firstContact) {
            m = message.getText().toString();
        }else{
            m = "Hi, I'm interested in . . .";
            firstContact = false;
        }
    if(m.contains("#"))
        m.replaceAll("#", "~@");
    final Message temp = new Message();
    temp.setText(m);
    temp.setData(data);
    temp.setBelongsToCurrentUser(true);
    messageAdapter.add(temp);
    messagesView.setSelection(messagesView.getCount() - 1);

    final String saveData = temp.toString();
    final String otherUser = temp.getData().getReceiver();
    String path = "";
      if(type == 0) {
          path = getFilesDir() + "/messages/" + "allMsgs";
      }else if(type == 1) {
          path = getFilesDir() + "/messages/" + "buyingMsgs";
      }else if(type == 2) {
          path = getFilesDir() + "/messages/" + "sellingMsgs";
      }
    try {
      BufferedWriter out = new BufferedWriter(new FileWriter(path));
      out.append(saveData);
      out.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    col = "";
      if(type == 0) {
          col = "Received";
          if(HomePage.allMsgs == null){
              HomePage.allMsgs = saveData;
          }else {
              HomePage.allMsgs += saveData;
          }
      }else if(type == 1) {
          col = "sellingReceived";
          if(HomePage.buyingMsgs == null){
              HomePage.buyingMsgs = saveData;
          }else {
              HomePage.buyingMsgs += saveData;
          }
      }else if(type == 2) {
          col = "buyingReceived";
          if(HomePage.sellingMsgs == null){
              HomePage.sellingMsgs = saveData;
          }else {
              HomePage.sellingMsgs += saveData;
          }
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
                      if(response.get(0).get(col) != null){
                          temp = response.get(0).get(col).toString();
                      }
                      final String sendMsgs = temp + saveData;
                      response.get(0).put(col, sendMsgs);
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
                  if(type == 0) {
                      if(HomePage.allMsgs.length() > 6) {
                          response.put("allMsgs", HomePage.allMsgs);
                      }
                  }else if(type == 1) {
                      if(HomePage.buyingMsgs.length() > 6) {
                          response.put("buyingMsgs", HomePage.buyingMsgs);
                      }
                  }else if(type == 2) {
                      if(HomePage.sellingMsgs.length() > 6) {
                          response.put("sellingMsgs", HomePage.sellingMsgs);
                      }
                  }
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
              @Override
              public void handleFault( BackendlessFault fault )
              {
                fault.toString();   // an error has occurred, the error code can be retrieved with fault.getCode()
              }
            } );

    message.getText().clear();
  }

}