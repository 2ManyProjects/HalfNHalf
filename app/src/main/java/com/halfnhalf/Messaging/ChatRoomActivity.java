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
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.files.BackendlessFile;
import com.backendless.persistence.DataQueryBuilder;
import com.backendless.rt.messaging.Channel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.halfnhalf.Displayer;
import com.halfnhalf.HomePage;
import com.halfnhalf.MainLogin;
import com.halfnhalf.R;
import com.halfnhalf.store.Store;
import com.halfnhalf.store.storeSummery;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class ChatRoomActivity extends AppCompatActivity {

  private EditText message;
  private ListView messagesView;
  private MessageAdapter messageAdapter;
  private MemberData data;
  static String name = "";
  static String receiving = "";
  ArrayList<Message> msgs;
  //String allMsg;
  String MsgID;
  int index;
  public static int type = 0;
  private String col = "";
  private boolean firstContact = false;
  public static boolean locked = true;
  public static boolean processing = false;
  private static boolean completed = false;
  private static boolean sellercompleted = false;
  private static boolean buyercompleted = false;
  private static String seller, buyer;
  private static String sellerlink, buyerlink, selleruploadlink, buyeruploadlink;
  public static Store dealGson;
  public static ArrayList<Store> buyerGson, sellerGson;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_chat_room);
    buyerGson = new ArrayList<Store>();
    sellerGson = new ArrayList<Store>();
    dealGson = null;
    locked = true;
    processing = false;
    type = 0;
    buyercompleted = false;
    sellercompleted = false;
    completed = false;

    HomePage.getNewMsgs(false, ChatRoomActivity.this, 0);
    registerReceiver(receiver, new IntentFilter(
              messageListener.NOTIFICATION));

    message = findViewById(R.id.message);
    Bundle bundle = getIntent().getExtras();
    type = bundle.getInt("type");
    index = bundle.getInt("index");
      name = bundle.getString("name");
      receiving = bundle.getString("othername");
      MsgID = HomePage.MsgID;

    if(type == 100) { //First Contact
        type = 1;
        firstContact = true;
    }
      if(type == 1) {
          msgs = HomePage.buyingMessages.get(index);
          buyer = MainLogin.getUser().getProperty("name").toString();
          seller = receiving;
      }else if(type == 2) {
          msgs = HomePage.sellingMessages.get(index);
          seller = MainLogin.getUser().getProperty("name").toString();
          buyer = receiving;
      }
      getLinks();

    messageAdapter = new MessageAdapter(this);
    messagesView = (ListView) findViewById(R.id.messages_view);
    messagesView.setAdapter(messageAdapter);

    data = new MemberData(name, receiving);

    populateChat();
    message.setOnEditorActionListener(new TextView.OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        boolean handled = false;

        if ( actionId == EditorInfo.IME_ACTION_SEND || event.getKeyCode() == KeyEvent.KEYCODE_ENTER ) {
          if(v.getText().toString().length() > 0)
            sendMessage(false);
          handled = true;
        }

        return handled;
      }
    });
    if(firstContact){
        sendMessage(false);
    }
      getData(ChatRoomActivity.this);
      startTimer(0);
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
        MenuItem complete = menu.findItem(R.id.DealCompleted);
        MenuItem modifydeal = menu.findItem(R.id.modifyDeal);
        MenuItem lock = menu.findItem(R.id.lock);
        MenuItem selectedDeal = menu.findItem(R.id.selectedDeal);
        MenuItem unlock = menu.findItem(R.id.unlock);
        MenuItem completeDeal = menu.findItem(R.id.completeDeal);
        MenuItem undo = menu.findItem(R.id.undoCompletion);
        if(!completed) {
            complete.setVisible(false);
            if (type == 1) {
                if (buyercompleted) {
                    undo.setVisible(true);
                    completeDeal.setVisible(false);
                } else {
                    undo.setVisible(false);
                    completeDeal.setVisible(true);
                }
                lock.setVisible(false);
                unlock.setVisible(false);
                if (locked) {
                    modifydeal.setVisible(false);
                    selectedDeal.setVisible(true);
                } else {
                    modifydeal.setVisible(true);
                    selectedDeal.setVisible(false);
                }
            } else {
                if (sellercompleted) {
                    undo.setVisible(true);
                    completeDeal.setVisible(false);
                } else {
                    undo.setVisible(false);
                    completeDeal.setVisible(true);
                }
                modifydeal.setVisible(false);
                if (locked) {
                    lock.setVisible(false);
                    unlock.setVisible(true);
                } else {
                    lock.setVisible(true);
                    unlock.setVisible(false);
                }
                selectedDeal.setVisible(true);
            }
        }else{
            modifydeal.setVisible(false);
            lock.setVisible(false);
            selectedDeal.setVisible(false);
            unlock.setVisible(false);
            completeDeal.setVisible(false);
            undo.setVisible(false);
            complete.setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem complete = menu.findItem(R.id.DealCompleted);
        MenuItem modifydeal = menu.findItem(R.id.modifyDeal);
        MenuItem lock = menu.findItem(R.id.lock);
        MenuItem selectedDeal = menu.findItem(R.id.selectedDeal);
        MenuItem unlock = menu.findItem(R.id.unlock);
        MenuItem completeDeal = menu.findItem(R.id.completeDeal);
        MenuItem undo = menu.findItem(R.id.undoCompletion);
        if(!completed) {
            complete.setVisible(false);
            if (type == 1) {
                if (buyercompleted) {
                    undo.setVisible(true);
                    completeDeal.setVisible(false);
                } else {
                    undo.setVisible(false);
                    completeDeal.setVisible(true);
                }
                lock.setVisible(false);
                unlock.setVisible(false);
                if (locked) {
                    modifydeal.setVisible(false);
                    selectedDeal.setVisible(true);
                } else {
                    modifydeal.setVisible(true);
                    selectedDeal.setVisible(false);
                }
            } else {
                if (sellercompleted) {
                    undo.setVisible(true);
                    completeDeal.setVisible(false);
                } else {
                    undo.setVisible(false);
                    completeDeal.setVisible(true);
                }
                modifydeal.setVisible(false);
                if (locked) {
                    lock.setVisible(false);
                    unlock.setVisible(true);
                } else {
                    lock.setVisible(true);
                    unlock.setVisible(false);
                }
                selectedDeal.setVisible(true);
            }
        }else{
            modifydeal.setVisible(false);
            lock.setVisible(false);
            selectedDeal.setVisible(false);
            unlock.setVisible(false);
            completeDeal.setVisible(false);
            undo.setVisible(false);
            complete.setVisible(true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
      if(!completed) {
          if (item.getItemId() == R.id.lock) {
//              snapShotdata[0] = "1";
              dealGson.setDealProgression(1);
              locked = true;
              saveData();
              startTimer(1);
              return true;
          } else if (item.getItemId() == R.id.unlock) {
              dealGson.setDealProgression(0);
              locked = false;
              saveData();
              startTimer(2);
              return true;
          } else if (item.getItemId() == R.id.modifyDeal) {
              Intent intent;
              intent = new Intent(ChatRoomActivity.this, dealSelection.class);
              startActivityForResult(intent, 1);
              return true;
          } else if (item.getItemId() == R.id.selectedDeal) {
              int t = 2;
              Intent intent;
              intent = new Intent(ChatRoomActivity.this, dealSelection.class);
              startActivityForResult(intent, 2);
              return true;
          } else if (item.getItemId() == R.id.completeDeal) {
              if (type == 1) {
                  if (sellercompleted) {
                      dealGson.setDealProgression(6);
                      invalidateOptionsMenu();
                  }else {
                      dealGson.setDealProgression(4);
                  }
              } else if (type == 2) {
                  if (buyercompleted) {
                      dealGson.setDealProgression(6);
                      completed = false;
                      invalidateOptionsMenu();
                  }else {
                      dealGson.setDealProgression(5);
                  }
              }
              saveData();
              startTimer(4);
              return true;
          } else if (item.getItemId() == R.id.undoCompletion) {
              if (type == 1) {
                  if (sellercompleted) {
                      dealGson.setDealProgression(5);
                      completed = false;
                      buyercompleted = false;
                      invalidateOptionsMenu();
                  }else if (locked) {
                      dealGson.setDealProgression(1);
                      completed = false;
                      buyercompleted = false;
                      invalidateOptionsMenu();
                  }else {
                      dealGson.setDealProgression(0);
                      completed = false;
                      buyercompleted = false;
                      invalidateOptionsMenu();
                  }
              } else if (type == 2) {
                  if (buyercompleted) {
                      dealGson.setDealProgression(4);
                      completed = false;
                      sellercompleted = false;
                      invalidateOptionsMenu();
                  }else if (locked) {
                      dealGson.setDealProgression(1);
                      completed = false;
                      sellercompleted = false;
                      invalidateOptionsMenu();
                  }else {
                      dealGson.setDealProgression(0);
                      completed = false;
                      sellercompleted = false;
                      invalidateOptionsMenu();
                  }
              }
              saveData();
              startTimer(5);
              return true;
          } else {
              return super.onOptionsItemSelected(item);
          }
      }else if (item.getItemId() == R.id.DealCompleted) {
          Displayer.alertDisplayer("This Deal is already Complete", "please start a new one", getApplicationContext());
      }
        return super.onOptionsItemSelected(item);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if(resultCode == RESULT_OK) {
                Bundle bundle = data.getExtras();
                boolean back = bundle.getBoolean("back");
                if(!back) {
                    saveData();
                    startTimer(3);
                }
            }
        }else if (requestCode == 2) {
            if(resultCode == RESULT_OK) {
            }
        }else if(requestCode == 3){
            if(resultCode == RESULT_OK) {
            }
        }
    }

    private static void downloadFile(String path, int type, Context c){
        final Context mContext = c;
        final String p = path;
        final int t = type;
        new Thread(new Runnable() {
            public void run() {
                URL url = null;
                try {
                    url = new URL(p);
                    String tempData = mContext.getFilesDir() + "/tempData/" + "Gson.txt";
                    downloadFromUrl(url, tempData, t, mContext);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    private static void downloadFromUrl(URL url, String localFilename, int t, Context c) throws IOException {
        InputStream is = null;
        FileOutputStream fos = null;

        try {
            URLConnection urlConn = url.openConnection();//connect

            is = urlConn.getInputStream();               //get connection inputstream
            fos = new FileOutputStream(localFilename);   //open outputstream to local file

            byte[] buffer = new byte[4096];              //declare 4KB buffer
            int len;

            //while we have availble data, continue downloading and storing to local file
            while ((len = is.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
            BufferedReader in = new BufferedReader(new FileReader(localFilename));
            String data = in.readLine();
            //if(data.length() > 8) {
                Gson gson = new Gson();
                Type type = new TypeToken<ArrayList<Store>>() {
                }.getType();
                ArrayList<Store> temp = gson.fromJson(data, type);
                fos.close();
                is.close();
                in.close();
                if (t == 1) {
                    for (int i = 0; i < temp.size(); i++) {
                        if (temp.get(i).getSeller().equals(seller) &&
                                temp.get(i).getBuyer().equals(buyer) &&
                                temp.get(i).getDealProgression() != 6) {
                            dealGson = temp.get(i);
                            break;
                        }
                    }
                    if (dealGson != null) {
                        locked = (dealGson.getDealProgression() == 1);
                        buyercompleted = (dealGson.getDealProgression() == 4);
                        sellercompleted = (dealGson.getDealProgression() == 5);
                        completed = (dealGson.getDealProgression() == 6);
                    }
                    if (completed)
                        Displayer.alertDisplayer("THIS DEAL IS COMPLETE", "Please start a new one", c);

                    processing = false;
                } else if (t == 2) {
                    sellerGson = temp;
                    for (int i = 0; i < sellerGson.size(); i++) {
                        if (sellerGson.get(i).equals(dealGson)) {
                            sellerGson.set(i, dealGson);
                            break;
                        }
                    }
                    processing = true;
                    downloadFile(buyerlink, 3, c);
                } else if (t == 3) {
                    buyerGson = temp;
                    for (int i = 0; i < buyerGson.size(); i++) {
                        if (buyerGson.get(i).equals(dealGson)) {
                            buyerGson.set(i, dealGson);
                            break;
                        }
                    }
                    processing = false;
                }
           // }
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } finally {
                if (fos != null) {
                    fos.close();
                }
            }
        }
    }


    private void startTimer(int x){
      final int y = x;
        if(processing) {
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    startTimer(y);
                }
            }, MainLogin.DELAY_TIME);
        }else{
            if(!completed) {
                if (y == 0) {
                    Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
                    setSupportActionBar(myToolbar);
                    invalidateOptionsMenu();
                } else if (y == 1) {
                    Displayer.setSnackBar(findViewById(R.id.messages_view), "You've locked this Deal");
                    invalidateOptionsMenu();
                    message.setText(name + " has locked the deal");
                    sendMessage(true);
                } else if (y == 2) {
                    Displayer.setSnackBar(findViewById(R.id.messages_view), "You've unlocked this Deal");
                    invalidateOptionsMenu();
                    message.setText(name + " has unlocked the deal");
                    sendMessage(true);
                } else if (y == 3) {
                    Displayer.setSnackBar(findViewById(R.id.messages_view), "You've modified this Deal");
                    invalidateOptionsMenu();
                    message.setText(name + " has modified the deal");
                    sendMessage(true);
                } else if (y == 4) {
                    Displayer.setSnackBar(findViewById(R.id.messages_view), "You've Completed this Deal");
                    invalidateOptionsMenu();
                    message.setText(name + " has Completed the deal");
                    sendMessage(true);
                } else if (y == 5) {
                    Displayer.setSnackBar(findViewById(R.id.messages_view), "You've undone your Completion");
                    invalidateOptionsMenu();
                    message.setText(name + " has Undone their Completion");
                    sendMessage(true);
                } else if (y == 6) {
                    if(dealGson.getDealProgression() == 6) {
                        completed = true;
                        invalidateOptionsMenu();
                    }
                    saveGson();
                }else {
                    invalidateOptionsMenu();
                }
            }
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
    }
        messageAdapter.push();
        messagesView.setSelection(messagesView.getCount() - 1);
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
        }
        //messageAdapter.newSet(msgs);
        messageAdapter.push();
        messagesView.setSelection(messagesView.getCount() - 1);
    }


    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("INCOMMING MSG", "");
            HomePage.getNewMsgs(false, ChatRoomActivity.this, type);
            getData(ChatRoomActivity.this);
            displayNewMsgs();
        }
    };

    private void displayNewMsgs(){
        if(HomePage.processing) {
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    displayNewMsgs();
                }
            }, MainLogin.DELAY_TIME);
        }else{
            if(type == 1) {
                msgs = HomePage.buyingMessages.get(index);
            }else if(type == 2) {
                msgs = HomePage.sellingMessages.get(index);
            }
            invalidateOptionsMenu();
            remakeChat();
        }
    }

    public static void getData(Context c){
        final Context mContext = c;
        processing = true;
        String WhereClause = "name = " + "'" + receiving + "'";
        if(type == 2)
            WhereClause = "name = " + "'" + name + "'";

        DataQueryBuilder dataQuery = DataQueryBuilder.create();
        dataQuery.setWhereClause(WhereClause);
        Backendless.Data.of("Messaging").find(dataQuery,
                new AsyncCallback<List<Map>>() {
                    @Override
                    public void handleResponse(List<Map> foundUsers) {
                        if (foundUsers.size() >= 0) {
                            String gsonPath = foundUsers.get(0).get("sellingDataGson").toString();
                            downloadFile(gsonPath, 1, mContext);
                        }
                    }
                    @Override
                    public void handleFault(BackendlessFault fault) {
                        Log.e("TOKEN ISSUE: ", "" + fault.getMessage());
                        Displayer.alertDisplayer("Error retreiving seller Profile: ", "Please Reload Chat", mContext);
                    }
                });

    }

  //Image Button was pressed
  public void sendMessage(View view) {
      if(message.getText().toString().length() > 0)
          sendMessage(false);
  }

  //Enter was pressed
  public void sendMessage(boolean System) {
      final Gson gson = new Gson();
      final Type dataType = new TypeToken<ArrayList<Message>>() {
      }.getType();
      String m = "";
        if(!firstContact) {
            m = message.getText().toString();
        }else{
            m = "Hi, I'm interested in . . .";
            firstContact = false;
        }
    final Message temp = new Message();
    temp.setText(m);
    temp.setData(data);
    temp.setBelongsToCurrentUser(true);
    temp.getData().setSystem(System);
    messageAdapter.add(temp);
    messagesView.setSelection(messagesView.getCount() - 1);
    final ArrayList<Message> msg = new ArrayList<Message>();
    final String otherUser = temp.getData().getReceiver();
    String path = "";
      if(type == 1) {
          path = getFilesDir() + "/messages/" + "buyingMsgs";
      }else if(type == 2) {
          path = getFilesDir() + "/messages/" + "sellingMsgs";
      }
    try {
        BufferedReader in = new BufferedReader(new FileReader(path));
        String data = in.readLine();
        if(data != null && data.length() > 6) {
            Log.e("DATA", "" + data + " Length: " + data.length());
            //if(data.length() > 8) {
            ArrayList<Message> t = gson.fromJson(data, dataType);
            for (int i = 0; i < t.size(); i++) {
                msg.add(t.get(i));
            }
        }
        in.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    msg.add(temp);
      try {
          BufferedWriter out = new BufferedWriter(new FileWriter(path));
          out.write(new Gson().toJson(msg).toString());
          out.close();
      } catch (IOException e) {
          e.printStackTrace();
      }
      col = "";
      if(type == 1) {
          col = "sellingReceived";
          if(HomePage.buyingMsgs == null){
              HomePage.buyingMsgs = msg;
          }else {
              HomePage.buyingMsgs.add(temp);
          }
      }else if(type == 2) {
          col = "buyingReceived";
          if(HomePage.sellingMsgs == null){
              HomePage.sellingMsgs = msg;
          }else {
              HomePage.sellingMsgs.add(temp);
          }
      }

      String WhereClause = "name = " + "'" + otherUser + "'";
      DataQueryBuilder dataQuery = DataQueryBuilder.create();
      dataQuery.setWhereClause(WhereClause);
      Backendless.Data.of("Messaging").find(dataQuery,
              new AsyncCallback<List<Map>>() {
                  @Override
                  public void handleResponse( List<Map> response )
                  {
                      String str = "";
                      ArrayList<Message> pastMsgs = new ArrayList<Message>();
                      if(response.get(0).get(col) != null && !response.get(0).get(col).equals("")){

                          str = response.get(0).get(col).toString();
                          Log.e("STR", "" + str);
                          Gson json = new Gson();
                          pastMsgs = json.fromJson(str, dataType);
                      }
                      pastMsgs.add(temp);
                      final String sendMsgs = new Gson().toJson(pastMsgs).toString();
                      response.get(0).put(col, sendMsgs);
                      Backendless.Persistence.of("Messaging").save(response.get(0), new AsyncCallback<Map>() {
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

    Backendless.Data.of("Messaging").findById(MsgID,
            new AsyncCallback<Map>() {
              @Override
              public void handleResponse( Map response )
              {
                  if(type == 1) {
                      if(HomePage.buyingMsgs != null && HomePage.buyingMsgs.size() > 0) {
                          response.put("buyingMsgs", new Gson().toJson(HomePage.buyingMsgs).toString());
                      }
                  }else if(type == 2) {
                      if(HomePage.sellingMsgs != null && HomePage.sellingMsgs.size() > 0) {
                          response.put("sellingMsgs", new Gson().toJson(HomePage.sellingMsgs).toString());
                      }
                  }
                  Backendless.Persistence.of("Messaging").save(response, new AsyncCallback<Map>() {
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

  private void getLinks(){
      final String w1 = "name = " + "'" + seller + "'";
      final String w2 = "name = " + "'" + buyer + "'";
      DataQueryBuilder dataQuery = DataQueryBuilder.create();
      dataQuery.setWhereClause(w1);
      Backendless.Data.of("Messaging").find(dataQuery,
              new AsyncCallback<List<Map>>() {
                  @Override
                  public void handleResponse(List<Map> foundUsers) {
                      if (foundUsers.size() >= 0) {
                          sellerlink = foundUsers.get(0).get("sellingDataGson").toString();
                          selleruploadlink = "/profileData/" +
                                  foundUsers.get(0).get("userID").toString() + "/";
                          DataQueryBuilder query = DataQueryBuilder.create();
                          query.setWhereClause(w2);
                          Backendless.Data.of("Messaging").find(query,
                                  new AsyncCallback<List<Map>>() {
                                      @Override
                                      public void handleResponse(List<Map> foundUsers) {
                                          if (foundUsers.size() >= 0) {
                                              buyerlink = foundUsers.get(0).get("buyingDataGson").toString();
                                              buyeruploadlink = "/profileData/" +
                                                                foundUsers.get(0).get("userID").toString() + "/";
                                          }
                                      }
                                      @Override
                                      public void handleFault(BackendlessFault fault) {
                                          Log.e("TOKEN ISSUE: ", "" + fault.getMessage());
                                          Displayer.alertDisplayer("Error retreiving seller Profile: ", "Please Reload Chat", ChatRoomActivity.this);
                                      }
                                  });
                      }
                  }
                  @Override
                  public void handleFault(BackendlessFault fault) {
                      Log.e("TOKEN ISSUE: ", "" + fault.getMessage());
                      Displayer.alertDisplayer("Error retreiving seller Profile: ", "Please Reload Chat", ChatRoomActivity.this);
                  }
              });
  }

    private void getGson(){
        processing = true;
        startTimer(6);
        downloadFile(sellerlink, 2, ChatRoomActivity.this);

    }

  private void saveGson(){
      final String sellergsonData = new Gson().toJson(sellerGson).toString();
      final String buyergsonData = new Gson().toJson(buyerGson).toString();
      Log.e("SELLER DATA", "" + sellergsonData);
      Log.e("BUYER DATA", "" + buyergsonData);

      try {
          String path = getFilesDir() + "/tempData/" + "sellingDataGson.txt";
          FileOutputStream writer = new FileOutputStream (path);
          writer.write(sellergsonData.getBytes());
          writer.close();
          final File sellergsonFile = new File(path);
          String p = getFilesDir() + "/tempData/" + "buyingDataGson.txt";
          FileOutputStream w = new FileOutputStream (p);
          w.write(buyergsonData.getBytes());
          w.close();
          final File buyergsonFile = new File(p);
          Log.i("File Closed ", "File Stuff");
          Backendless.Files.upload( sellergsonFile, selleruploadlink, true, new AsyncCallback<BackendlessFile>(){
              @Override
              public void handleResponse(BackendlessFile response) {
                  final String location = response.getFileURL();
                  Log.i("Location ", location);
                  sellergsonFile.delete();
                  Log.i("File Closed ", "File Stuff");
                  Backendless.Files.upload( buyergsonFile, buyeruploadlink, true, new AsyncCallback<BackendlessFile>(){
                      @Override
                      public void handleResponse(BackendlessFile response) {
                          final String location = response.getFileURL();
                          Log.i("Location ", location);
                          buyergsonFile.delete();
                      }

                      @Override
                      public void handleFault(BackendlessFault fault) {
                          Log.e("TOKEN ISSUE: ", "" + fault.getMessage());
                      }
                  });
              }

              @Override
              public void handleFault(BackendlessFault fault) {
                  Log.e("TOKEN ISSUE: ", "" + fault.getMessage());
              }
          });
      } catch (IOException e) {
          e.printStackTrace();
      }

  }

  private void saveData(){
      getGson();
  }



}