package com.halfnhalf;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.halfnhalf.Messaging.Message;
import com.halfnhalf.store.FindStore;
import com.halfnhalf.store.Store;
import com.halfnhalf.store.storeDeals;
import com.halfnhalf.store.storeSummery;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class HomePage extends AppCompatActivity {
    static final String userInfo_key = "BackendlessUserInfo";
    static final String logoutButtonState_key = "LogoutButtonState";
    static String store_key = "";

    private FloatingActionButton profile;

    private String storeID;
    private String Userlist;
    private String[] UserIDs;
    private ArrayList<String> userProfiles;
    private ArrayList<String []> userProfilesdata;
    private ArrayList<storeSummery> stores;
    private File mPath;
    private String MsgID;
    private String allMsgs;
    private ArrayList<ArrayList<Message>> Messages;

    public static RecyclerView.Adapter Summeryadapter;
    private RecyclerView.LayoutManager SummerylayoutManager;
    private static RecyclerView SummeryrecyclerView;
    public static View.OnClickListener myOnClickListener;
    private boolean firstLaunch = false;


    EditText editText;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        initUI();
        Messages = new ArrayList<ArrayList<Message>>();
        Bundle bundle = getIntent().getExtras();
        MsgID = bundle.getString("Msgs");
        Log.i("Msg ID", "" + MsgID);
        mPath = new File(getFilesDir() + "/messages/");

        if (!mPath.exists()) {
            firstLaunch = true;
            mPath.mkdirs();
            getMsgs();
        }

        if(!lastUser()){
            Log.e("DIFFERENT USER", "DELETING DATA");
            File temp = new File(getFilesDir() + "/messages/");
            deleteRecursive(temp);
            firstLaunch = true;
            mPath.mkdirs();
            getMsgs();
        }

        if(!firstLaunch){

            String inputString = "";
            String path = getFilesDir() + "/messages/" + "allMsgs";
            try {
                BufferedReader in = new BufferedReader(new FileReader(path));
                inputString = in.readLine();
            } catch (IOException e) {

            }
            allMsgs = inputString;
            buildMessages(allMsgs);
        }


        stores = new ArrayList<>();

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launch();
            }
        });

        editText = (EditText) findViewById(R.id.editText);
        button = (Button) findViewById(R.id.button);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = editText.getText().toString().trim();
                Intent intent;
                intent = new Intent(HomePage.this, FindStore.class);
                intent.putExtra("FindingUsers", "1");
                intent.putExtra("StoreName", s);
                startActivityForResult(intent, 3);
            }
        });

        myOnClickListener = new MyOnClickListener(this);

        SummeryrecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        SummeryrecyclerView.setHasFixedSize(true);

        SummerylayoutManager = new LinearLayoutManager(this);
        SummeryrecyclerView.setLayoutManager(SummerylayoutManager);
        SummeryrecyclerView.setItemAnimator(new DefaultItemAnimator());

        Summeryadapter = new SummeryAdapter(HomePage.this, stores);
        SummeryrecyclerView.setAdapter(Summeryadapter);


        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
    }

    private boolean lastUser(){
        String inputString = "";
        String path = getFilesDir() + "/messages/" + "LastLogin";
        try {
            BufferedReader in = new BufferedReader(new FileReader(path));
            inputString = in.readLine();
        } catch (IOException e) {
            Log.e("NO LAST USER FOUND, DELETING SAVES", "");
            return false;
        }
        return inputString.equals(Backendless.UserService.CurrentUser().getProperty("name").toString());
    }

    private void getMsgs(){
        Backendless.Data.of("Messages").findById(MsgID,
                new AsyncCallback<Map>() {
                    @Override
                    public void handleResponse( Map response )
                    {
                        if(response.get("allMsgs") != null) {
                            allMsgs = response.get("allMsgs").toString();
                            String path = getFilesDir() + "/messages/" + "allMsgs";
                            try {
                                BufferedWriter out = new BufferedWriter(new FileWriter(path));
                                out.write(allMsgs);
                                out.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            path = getFilesDir() + "/messages/" + "LastLogin";
                            try {
                                BufferedWriter out = new BufferedWriter(new FileWriter(path));
                                out.write(Backendless.UserService.CurrentUser().getProperty("name").toString());
                                out.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            buildMessages(allMsgs);
                        }
                    }
                    @Override
                    public void handleFault( BackendlessFault fault )
                    {
                        fault.toString();   // an error has occurred, the error code can be retrieved with fault.getCode()
                    }
                } );
    }

    public void deleteRecursive(File Directory) {

        if (Directory.isDirectory()) {
            for (File child : Directory.listFiles()) {
                deleteRecursive(child);
            }
        }

        Directory.delete();
    }

    private void buildMessages(String data){
        String [] MessageData = data.split("#");
        for(int i = 0; i < MessageData.length; i++){
            Message temp = new Message();
            temp.setData(MessageData[i], MessageData[i + 1]);
            temp.setText(MessageData[i + 2]);
            temp.setBelongsToCurrentUser(Backendless.UserService.CurrentUser().getProperty("name").toString().equals(MessageData[i]));
            if(Messages.size() == 0){
                ArrayList<Message> tempMessage = new ArrayList<>();
                tempMessage.add(temp);
                Messages.add(tempMessage);
            }else{
                int index = getIndexofMessage(temp.getData().getSender(), temp.getData().getReceiver());
                if(index != -1) {
                    Messages.get(index).add(temp);
                }else{
                    ArrayList<Message> tempMessage = new ArrayList<>();
                    tempMessage.add(temp);
                    Messages.add(tempMessage);
                }
            }
            i += 2;
        }
    }

    private int getIndexofMessage(String sender, String receiver){
        String name = Backendless.UserService.CurrentUser().getProperty("name").toString();
        if(sender.equals(name)){
            for(int i = 0; i < Messages.size(); i++){
                if(Messages.get(i).get(0).getData().getReceiver().equals(receiver))
                    return i;
            }
        }else{
            for(int i = 0; i < Messages.size(); i++){
                if(Messages.get(i).get(0).getData().getSender().equals(sender))
                    return i;
            }
        }
        return -1;
    }

    private void launch(){
        Bundle bundle = getIntent().getExtras();
        String password = bundle.getString("password");
        Intent i = getIntent();
        Intent intent;
        intent = new Intent(this, Profile.class);
        intent.putExtra("String", bundle.getString("data"));
        intent.putExtra("objectID", bundle.getString("objectID"));
        intent.putExtra("password", password);
        startActivity(intent);
    }
    private void initUI() {
        profile = (FloatingActionButton) findViewById(R.id.fab_profileBtn);
    }


    private void logoutFromBackendless(){
        Backendless.UserService.logout(new AsyncCallback<Void>() {
            @Override
            public void handleResponse(Void response) {
                finish();
            }

            @Override
            public void handleFault(BackendlessFault fault) {

            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if(resultCode == RESULT_OK) {
            }
        }else if (requestCode == 2) {
            if(resultCode == RESULT_OK) {
            }
        }else if(requestCode == 3){
            if(resultCode == RESULT_OK) {
                Bundle bundle = data.getExtras();
                storeID = bundle.getString("StoreID");
                userProfiles = new ArrayList<>();
                userProfilesdata = new ArrayList<>();
                userProfiles.clear();
                userProfilesdata.clear();
                stores.clear();
                Summeryadapter.notifyDataSetChanged();
                findData(storeID);
            }
        }
    }

    private void findData(String storeID){
        String WhereClause = "StoreID = " + "'" + storeID + "'";
        DataQueryBuilder dataQuery = DataQueryBuilder.create();
        dataQuery.setWhereClause(WhereClause);
        Backendless.Data.of("Stores").find(dataQuery,
                new AsyncCallback<List<Map>>() {
                    @Override
                    public void handleResponse(List<Map> foundUsers) {
                        if (foundUsers.size() >= 0) {
                            Userlist = foundUsers.get(0).get("UserList").toString();
                            UserIDs = Userlist.split("#");
                            startTimer(UserIDs, 0, userProfiles);
                        }
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        Log.e("TOKEN ISSUE: ", "" + fault.getMessage());
                    }
                });

    }

    private void startTimer(String [] id, int x, ArrayList<String> profiles){
        final String [] userID = id;
        final int i = x;
        if(x < id.length) {
            new Handler().postDelayed(new Runnable() {
                int y = i;
                @Override
                public void run() {
                    final String ID = userID[y];
                    if(!ID.equals(Backendless.UserService.CurrentUser().getUserId())) {
                        Backendless.Data.of(BackendlessUser.class).findById(ID,
                                new AsyncCallback<BackendlessUser>() {
                                    @Override
                                    public void handleResponse(BackendlessUser foundUser) {
                                        userProfiles.add(foundUser.getProperty("profileData").toString());
                                        y++;
                                        startTimer(userID, y, userProfiles);
                                    }

                                    @Override
                                    public void handleFault(BackendlessFault fault) {
                                        Log.e("TOKEN ISSUE: ", "" + fault.getMessage());
                                    }
                                });
                    }else {
                        y++;
                        startTimer(userID, y, userProfiles);
                    }
                }
            }, MainLogin.DELAY_TIME);
        }else{
            //Loops Done now start Building Display
            Log.i("Amnt of Users Found", " " + userProfiles.size());
            for(int f = 0; f < userProfiles.size(); f++) {
                userProfilesdata.add(userProfiles.get(f).split("#"));
                int startindex = findIndex(userProfilesdata.get(f), storeID);
                if (startindex == -1) {
                    Log.e("STORE WAS NOT FOUND IN PROFILE", "PROFILE ERROR  ");
                    break;
                }
                Store tempStore = makeFirstStore(userProfilesdata.get(f), startindex);
//                if (Integer.parseInt(userProfilesdata.get(f)[startindex+3]) > 0) {
                    storeSummery temp = new storeSummery(userProfilesdata.get(f)[0], userProfilesdata.get(f)[startindex + 1], remakeString(userProfilesdata.get(f)[startindex + 2]));
                    temp.setStore(tempStore);
                    int baseval = startindex + 4;//get to the first %
                    List deals = new ArrayList();
                    for (int d = 0; d < Integer.parseInt(userProfilesdata.get(f)[startindex + 3]); d++) {
                        deals.add(Integer.parseInt(userProfilesdata.get(f)[baseval]));
                        baseval += 3;
                    }
                    Collections.sort(deals);
                    Collections.reverse(deals);
                    int size = 3;
                    if (deals.size() < 3)
                        size = deals.size();
                    for (int h = 0; h < size; h++) {
                        temp.addDeal((deals.get(h).toString()));
                    }
                    stores.add(temp);
                    Summeryadapter.notifyItemInserted(stores.size());
//                }else if (userProfiles.size() == 1){
//                    Log.e("Profile Display error", "Profile Has no available deals");
//                    Displayer.toaster("The only profile has no available deals", "l", this);
//                }
            }
        }
    }

    private Store makeFirstStore(String [] arrayData, int i){
        Store temp = new Store(arrayData[i], arrayData[i+1], remakeString(arrayData[i+2]));
        temp.setNew(false);
        if(Integer.parseInt(arrayData[i+3]) != 0){
            for (int x = i + 4; x < (i + 4) + Integer.parseInt(arrayData[i + 3]) * 3; x++) {
                temp.addDeal(arrayData[x], remakeString(arrayData[x + 1]), arrayData[x + 2]);
                x += 2;
            }
        }

        return temp;
    }

    private String remakeString(String str){
        return str.replaceAll("~@", "#");
    }
    private int findIndex(String [] str, String token){
        for(int i = 0; i < str.length; i++){
            if(str[i].equals(token)){
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_homepage, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            logoutFromBackendless();
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
            launch(v);
            return;
        }
        private void launch(View v){
            int selectedItemPosition = SummeryrecyclerView.getChildPosition(v);
            storeSummery mCurrentStore = stores.get(selectedItemPosition);
            String storeData = new Gson().toJson(mCurrentStore.getStore());
            Intent intent;
            intent = new Intent(context, storeDeals.class);
            intent.putExtra("Store", storeData);
            intent.putExtra("type", 2);
            startActivity(intent);
        }
    }
}
