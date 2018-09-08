package com.halfnhalf.Messaging;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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

    Backendless.setUrl(Defaults.SERVER_URL);
    Backendless.initApp(this, Defaults.APPLICATION_ID, Defaults.API_KEY);

    setContentView(R.layout.activity_start_char);

//    userNameEditText = findViewById(R.id.userName);
//
//    Button startChatButton = findViewById(R.id.start_chat_button);
//    startChatButton.setOnClickListener(new OnClickListener() {
//      @Override
//      public void onClick(View view) {
//      }
//    });
//    startChat();
    Bundle bundle = getIntent().getExtras();
    allMsg = bundle.getString("rawMessage");
    String getMessages = bundle.getString("MessageData");
    Gson gson = new Gson();
    Type type = new TypeToken<ArrayList<ArrayList<Message>>>() {
    }.getType();
    allMessages = gson.fromJson(getMessages, type);

    populateDataAndSetAdapter();

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
      ArrayList<Message> currentConvo = dataModel.get(selectedItemPosition).getMessages();
      String convData = new Gson().toJson(currentConvo);
      Intent intent;
      intent = new Intent(context, ChatRoomActivity.class);
      intent.putExtra("convo", convData);
      String name = Backendless.UserService.CurrentUser().getProperty("name").toString();
      intent.putExtra("name", name);
      intent.putExtra("othername", dataModel.get(selectedItemPosition).getName());
      intent.putExtra("rawMessage", allMsg);
      intent.putExtra("msgID", getIntent().getStringExtra("msgID"));
      startActivity(intent);
    }
  }
}