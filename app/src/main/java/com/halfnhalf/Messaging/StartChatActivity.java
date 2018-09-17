package com.halfnhalf.Messaging;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.backendless.Backendless;
import com.halfnhalf.HomePage;
import com.halfnhalf.MainLogin;
import com.halfnhalf.R;

import java.util.ArrayList;


public class StartChatActivity extends AppCompatActivity{



  private static RecyclerView recyclerView;
  private RecyclerView.LayoutManager layoutManager;
  ArrayList<ConversationModel> dataModel;
  ArrayList<ArrayList<Message>> allMessages;
  public static RecyclerView.Adapter mAdapter;
  public static View.OnClickListener myOnClickListener;
  private int type = 0;
  private static boolean backpressed = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    backpressed = false;
    setContentView(R.layout.activity_start_char);
    HomePage.getNewMsgs(false, StartChatActivity.this, 0);
    registerReceiver(receiver, new IntentFilter(
            messageListener.NOTIFICATION));

    Bundle bundle = getIntent().getExtras();
    type = bundle.getInt("type");
    if(type == 0) {
        allMessages = HomePage.Messages;
    }else if(type == 1) {
        allMessages = HomePage.buyingMessages;
    }else if(type == 2) {
        allMessages = HomePage.sellingMessages;
    }

    populateDataAndSetAdapter();
    initUI();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
  }

  @Override
  public void onPause(){
    super.onPause();
    if(!backpressed)
        unregisterReceiver(receiver);
  }

  @Override
  public void onBackPressed(){
      backpressed = true;
      final Intent intent;
      intent = new Intent(StartChatActivity.this, HomePage.class);
      unregisterReceiver(receiver);
      startActivity(intent);
      StartChatActivity.this.finish();
  }

  @Override
  public void onResume(){
    super.onResume();
    backpressed = false;
      Bundle bundle = getIntent().getExtras();
      type = bundle.getInt("type");
      if(type == 0) {
          allMessages = HomePage.Messages;
      }else if(type == 1) {
          allMessages = HomePage.buyingMessages;
      }else if(type == 2) {
          allMessages = HomePage.sellingMessages;
      }
      HomePage.getNewMsgs(false, StartChatActivity.this, 0);
      startTimer();
    registerReceiver(receiver, new IntentFilter(
            messageListener.NOTIFICATION));
  }

  private BroadcastReceiver receiver = new BroadcastReceiver() {

    @Override
    public void onReceive(Context context, Intent intent) {
      Log.e("INCOMMING MSG", "");
      HomePage.getNewMsgs(false, StartChatActivity.this, 0);
      startTimer();
    }
  };

  private void startTimer(){
    if(HomePage.processing) {
      new Handler().postDelayed(new Runnable() {

        @Override
        public void run() {
          startTimer();
        }
      }, MainLogin.DELAY_TIME);
    }else{
        if(type == 0) {
            allMessages = HomePage.Messages;
        }else if(type == 1) {
            allMessages = HomePage.buyingMessages;
        }else if(type == 2) {
            allMessages = HomePage.sellingMessages;
        }
      populateDataAndSetAdapter();
      initUI();
      mAdapter.notifyDataSetChanged();
    }
  }

  public void initUI(){

    recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
    recyclerView.setHasFixedSize(true);

    layoutManager = new LinearLayoutManager(this);
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setItemAnimator(new DefaultItemAnimator());

    mAdapter = new ConversationAdapter(StartChatActivity.this, dataModel);
    recyclerView.setAdapter(mAdapter);

    myOnClickListener = new MyOnClickListener(this);

  }

  private void populateDataAndSetAdapter() {
    dataModel = new ArrayList<>();
    if(!allMessages.isEmpty()) {
        for (int i = 0; i < allMessages.size(); i++) {
            String otherUser = "";
            if (Backendless.UserService.CurrentUser().getProperty("name").toString().equals(allMessages.get(i).get(0).getData().getSender())) {
                otherUser = allMessages.get(i).get(0).getData().getReceiver();
            } else {
                otherUser = allMessages.get(i).get(0).getData().getSender();
            }
            ConversationModel temp = new ConversationModel(otherUser, allMessages.get(i));
            dataModel.add(temp);
        }
    }
  }

  private class MyOnClickListener implements View.OnClickListener {

    private final Context context;

    private MyOnClickListener(Context context) {
      this.context = context;
    }

    @Override
    public void onClick(View v) {
      launch(v);
      return;
    }
    private void launch(View v){
      int selectedItemPosition = recyclerView.getChildPosition(v);
      final Intent intent;
      intent = new Intent(context, ChatRoomActivity.class);
      intent.putExtra("index", selectedItemPosition);
      String name = Backendless.UserService.CurrentUser().getProperty("name").toString();
      intent.putExtra("name", name);
      intent.putExtra("othername", dataModel.get(selectedItemPosition).getName());
      intent.putExtra("type", type);
      intent.putExtra("msgID", getIntent().getStringExtra("msgID"));
      startActivity(intent);
    //  StartChatActivity.this.finish();
    }
  }
}