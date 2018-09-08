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
import com.halfnhalf.Defaults;
import com.halfnhalf.R;

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

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_chat_room);

    message = findViewById(R.id.message);
    //messages = findViewById(R.id.messages);

    name = getIntent().getStringExtra("name");

    messageAdapter = new MessageAdapter(this);
    messagesView = (ListView) findViewById(R.id.messages_view);
    messagesView.setAdapter(messageAdapter);

    data = new MemberData( name, "FILL WITH RECIEVER");

    channel = Backendless.Messaging.subscribe(Defaults.DEFAULT_CHANNEL);
    channel.addJoinListener(new AsyncCallback<Void>() {
      @Override
      public void handleResponse(Void response) {
        Message temp = new Message();
        temp.setText( name + " has joined");
        temp.setData(data);
        temp.setBelongsToCurrentUser(true);
        Backendless.Messaging.publish(Defaults.DEFAULT_CHANNEL, wrapToColor(name) + " joined", new AsyncCallback<MessageStatus>() {
          @Override
          public void handleResponse(MessageStatus response) {
            Log.d(TAG, " " + response);
          }

          @Override
          public void handleFault(BackendlessFault fault) {
            ChatRoomActivity.this.handleFault(fault);
          }
        });
      }

      @Override
      public void handleFault(BackendlessFault fault) {
        ChatRoomActivity.this.handleFault(fault);
      }
    });
    channel.addMessageListener(new AsyncCallback<Message>(){

      @Override
      public void handleResponse(Message response) {
        boolean belongsToCurrentUser = response.getData().getSender().equals(name);
        Message temp = new Message();
        temp.setText(response.getText());
        temp.setData(response.getData());
        temp.setBelongsToCurrentUser(belongsToCurrentUser);
        messageAdapter.add(temp);
        messagesView.setSelection(messagesView.getCount() - 1);
        Log.i("INCOMMING MSG: ", "" + response.getText());

      }

      @Override
      public void handleFault(BackendlessFault fault) {
        ChatRoomActivity.this.handleFault(fault);
      }
    }, Message.class );

    message.setOnEditorActionListener(new TextView.OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        boolean handled = false;

        if ( actionId == EditorInfo.IME_ACTION_SEND || event.getKeyCode() == KeyEvent.KEYCODE_ENTER ) {
            sendMessage();
          handled = true;
        }

        return handled;
      }
    });

  }

  private void handleFault(BackendlessFault fault) {
    Log.e(TAG, fault.toString());
  }

  private String wrapToColor(String value) {
    return "<font color='" + color + "'>" + value + "</font>";
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

    if (channel != null)
      channel.leave();
  }

  public void sendMessage() {
    String m = message.getText().toString();
    final Message temp = new Message();
    temp.setText(m);
    temp.setData(data);
    temp.setBelongsToCurrentUser(true);
    if (m.length() > 0) {
      Backendless.Messaging.publish(Defaults.DEFAULT_CHANNEL, temp, new AsyncCallback<MessageStatus>() {
        @Override
        public void handleResponse(MessageStatus response) {
          Log.d(TAG, "Sent message " + response);
          message.setText("", TextView.BufferType.EDITABLE);
          message.setEnabled(true);
        }

        @Override
        public void handleFault(BackendlessFault fault) {
          message.setEnabled(true);
        }
      });
      message.getText().clear();
    }
  }

  public void sendMessage(View view) {
    String m = message.getText().toString();
    final Message temp = new Message();
    temp.setText(m);
    temp.setData(data);
    temp.setBelongsToCurrentUser(true);
      if (m.length() > 0) {
        Backendless.Messaging.publish(Defaults.DEFAULT_CHANNEL, temp, new AsyncCallback<MessageStatus>() {
          @Override
              public void handleResponse(MessageStatus response) {
                  Log.d(TAG, "Sent message " + response);
                  message.setText("", TextView.BufferType.EDITABLE);
                  message.setEnabled(true);
              }

              @Override
              public void handleFault(BackendlessFault fault) {
                  message.setEnabled(true);
              }
          });
          message.getText().clear();
      }
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