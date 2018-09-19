package com.halfnhalf.Messaging;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.halfnhalf.Deal;
import com.halfnhalf.DealAdapter;
import com.halfnhalf.Displayer;
import com.halfnhalf.MainLogin;
import com.halfnhalf.R;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;

public class dealSelection extends AppCompatActivity {

    public static RecyclerView.Adapter Dealadapter;
    private RecyclerView.LayoutManager DeallayoutManager;
    private RecyclerView DealrecyclerView;
    public ArrayList<Deal> Dealdataset;
    private ArrayList<String> removed = new ArrayList<>();
    private int type;
    private Toolbar myToolbar;
    private String [] data;
    private String headerData = "";
    private String footerData = "";
    public static View.OnClickListener myOnClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deal_selection);
        Bundle bundle = getIntent().getExtras();
        type = ChatRoomActivity.type;
        data = ChatRoomActivity.snapShotdata;
        Displayer.toaster("Type" + Integer.toString(type), "l", this);

        myOnClickListener = new MyOnClickListener(this);

        DealrecyclerView = (RecyclerView) findViewById(R.id.deal_recycler_view);
        DealrecyclerView.setHasFixedSize(true);

        DeallayoutManager = new LinearLayoutManager(this);
        DealrecyclerView.setLayoutManager(DeallayoutManager);
        DealrecyclerView.setItemAnimator(new DefaultItemAnimator());

        Dealdataset = new ArrayList<Deal>();


        Dealadapter = new DealAdapter(dealSelection.this, Dealdataset, type);
        DealrecyclerView.setAdapter(Dealadapter);
        init();
        myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
    }

    private void init(){
        int startindex = 8;
        for(int x = 0; x < startindex; x++){
            headerData += data[x] + "#";
        }
//        for(int x = 0; x < data.length; x++){
//            Log.e("DATA: ", " " + x + ": " + data[x] );
//        }
        for(int i = startindex; i < data.length - 2; i++){
            String [] date = data[i + 8].split("~");
                    Deal temp = new Deal(
                    data[i],
                    data[i + 1],
                    data[i + 2],
                    data[i + 3],
                    Boolean.parseBoolean(data[i + 5]),
                    Boolean.parseBoolean(data[i + 6]),
                    data[i + 7],
                    date[0],
                    date[1],
                    date[2]
            );
            temp.setSelectedAmnt(Integer.parseInt(data[i+4]));
            temp.setSelected(Integer.parseInt(data[i+9]));
            Dealdataset.add(temp);
            Dealadapter.notifyDataSetChanged();
            i+=9;
        }
        footerData = data[data.length - 2] + "#" + data[data.length - 1] + "#";
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        MenuItem save = menu.findItem(R.id.save);
        MenuItem back = menu.findItem(R.id.back);
        back.setVisible(true);
        save.setVisible(false);
        if(type == 1){
            if(ChatRoomActivity.locked) {
                save.setVisible(false);
            }else{
                save.setVisible(true);
            }
        }else{
            back.setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem save = menu.findItem(R.id.save);
        MenuItem back = menu.findItem(R.id.back);
        back.setVisible(true);
        save.setVisible(false);
        if(ChatRoomActivity.locked) {
            save.setVisible(false);
        }else{
            save.setVisible(true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    private void save(){
        String tempData = headerData;
        for(int i = 0; i <Dealdataset.size();i++){
            tempData += Dealdataset.get(i).toStringSelection();
        }
        tempData += footerData;
        data = null;
        data = tempData.split("#");

    }

    private void startTimer(int x){
        final int y = x;
        if(ChatRoomActivity.processing) {
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    startTimer(y);
                }
            }, MainLogin.DELAY_TIME);
        }else{
            if(y == 0) {
                if(ChatRoomActivity.locked && type == 1){
                    invalidateOptionsMenu();
                    Displayer.alertDisplayer("The Seller has Locked or Completed this deal", "", dealSelection.this);
                }else if(type == 1){
                    save();
                    ChatRoomActivity.snapShotdata = data;
                    Intent intent = new Intent(this, ChatRoomActivity.class);
                    intent.putExtra("back", false);
                    setResult(RESULT_OK,intent);
                    finish();
                }
            }else if(y == 1){
                Intent intent = new Intent(this, ChatRoomActivity.class);
                intent.putExtra("back", true);
                setResult(RESULT_OK,intent);
                finish();
            }
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.save) {
            ChatRoomActivity.getData(dealSelection.this);
            startTimer(0);
            return true;
        }else if (item.getItemId() == R.id.back) {
            startTimer(1);
            return true;
        }else{
            return super.onOptionsItemSelected(item);
        }
    }
    private class MyOnClickListener implements View.OnClickListener {

        private final Context context;

        private MyOnClickListener(Context context) {
            this.context = context;
        }

        @Override
        public void onClick(View v) {
            click(v);
            return;
        }
        private void click(View v){
            int selectedItemPosition = DealrecyclerView.getChildPosition(v);
            Deal mCurrentDeal = Dealdataset.get(selectedItemPosition);
            Displayer.toaster(mCurrentDeal.getId(), "l", context);
        }
    }
}
