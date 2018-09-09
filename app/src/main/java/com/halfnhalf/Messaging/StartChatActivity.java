package com.halfnhalf.Messaging;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.backendless.Backendless;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.halfnhalf.Defaults;
import com.halfnhalf.HomePage;
import com.halfnhalf.MainLogin;
import com.halfnhalf.R;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


public class StartChatActivity extends AppCompatActivity{



  private static RecyclerView recyclerView;
  private RecyclerView.LayoutManager layoutManager;
  ArrayList<ConversationModel> dataModel;
  ArrayList<ArrayList<Message>> allMessages;
  public static RecyclerView.Adapter mAdapter;
  public static View.OnClickListener myOnClickListener;
  private String allMsg = "";
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_start_char);

    Bundle bundle = getIntent().getExtras();
    allMsg = bundle.getString("rawMessage");
    allMessages = HomePage.Messages;

    populateDataAndSetAdapter();
    init();
  }

  public void init(){

    recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
    recyclerView.setHasFixedSize(true);

    layoutManager = new LinearLayoutManager(this);
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setItemAnimator(new DefaultItemAnimator());

    mAdapter = new ConversationAdapter(StartChatActivity.this, dataModel);
    recyclerView.setAdapter(mAdapter);

    myOnClickListener = new MyOnClickListener(this);

  }


  @Override
  public void onResume(){
      super.onResume();
      HomePage.getNewMsgs(false, this);
      Log.e("test", "on resume");
      startTimer(0, 1);
  }

  private void startTimer(int x, int loops){
    final int i = x;
    final int l = loops;
    if(x < l) {
      new Handler().postDelayed(new Runnable() {
        int y = i;

        @Override
        public void run() {

          startTimer(y++, l);
        }
      }, MainLogin.DELAY_TIME);
    }else{
      populateDataAndSetAdapter();
      mAdapter.notifyDataSetChanged();
    }

  }


  private void populateDataAndSetAdapter() {
    dataModel = new ArrayList<>();

    for(int i = 0; i < allMessages.size(); i++){
      String otherUser = "";
      if(Backendless.UserService.CurrentUser().getProperty("name").toString().equals(allMessages.get(i).get(0).getData().getSender())){
        otherUser = allMessages.get(i).get(0).getData().getReceiver();
      }else{
        otherUser = allMessages.get(i).get(0).getData().getSender();
      }
      ConversationModel temp = new ConversationModel(otherUser, allMessages.get(i));
      dataModel.add(temp);
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
      intent.putExtra("rawMessage", allMsg);
      intent.putExtra("msgID", getIntent().getStringExtra("msgID"));
      startActivity(intent);
    //  StartChatActivity.this.finish();
    }
  }
}