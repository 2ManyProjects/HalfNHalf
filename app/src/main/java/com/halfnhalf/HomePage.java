package com.halfnhalf;

import android.app.ActivityManager;
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
import com.halfnhalf.Messaging.StartChatActivity;
import com.halfnhalf.Messaging.messageListener;
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
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class HomePage extends AppCompatActivity {

    private FloatingActionButton profile,  messenger, buying, selling;

    private String storeID;
    private String Userlist;
    private String[] UserIDs;
    private ArrayList<String> userProfiles;
    private ArrayList<String []> userProfilesdata;
    private ArrayList<storeSummery> stores;
    private static String profileData;
    private static String objectID;
    private File mPath;
    public static String MsgID = "";
    public static String allMsgs = "";
    public static String sellingMsgs = "";
    public static String buyingMsgs = "";
    public static ArrayList<ArrayList<Message>> Messages;
    public static ArrayList<ArrayList<Message>> sellingMessages;
    public static ArrayList<ArrayList<Message>> buyingMessages;

    public static RecyclerView.Adapter Summeryadapter;
    private RecyclerView.LayoutManager SummerylayoutManager;
    private static RecyclerView SummeryrecyclerView;
    public static View.OnClickListener myOnClickListener;
    private boolean firstLaunch = false;
    public static boolean processing = false;
    private messageListener mMessages;
    private Intent mServiceIntent;
    Context ctx;


    EditText editText;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        ctx = this;
        mMessages = new messageListener(getCtx());
        mServiceIntent = new Intent(getCtx(), mMessages.getClass());
        if (!isMyServiceRunning(mMessages.getClass())) {
            startService(mServiceIntent);
        }
        initUI();
        Messages = new ArrayList<ArrayList<Message>>();
        sellingMessages = new ArrayList<ArrayList<Message>>();
        buyingMessages = new ArrayList<ArrayList<Message>>();
        Bundle bundle = getIntent().getExtras();
        profileData = bundle.getString("data");
        objectID = bundle.getString("objectID");

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
            deleteRecursive(mPath);
            firstLaunch = true;
            mPath.mkdirs();
            getMsgs();
        }else if(firstLaunch){
            getNewMsgs(false, HomePage.this, 0);
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
            inputString = "";
            path = getFilesDir() + "/messages/" + "sellingMsgs";
            try {
                BufferedReader in = new BufferedReader(new FileReader(path));
                inputString = in.readLine();
            } catch (IOException e) {

            }
            sellingMsgs = inputString;
            inputString = "";
            path = getFilesDir() + "/messages/" + "buyingMsgs";
            try {
                BufferedReader in = new BufferedReader(new FileReader(path));
                inputString = in.readLine();
            } catch (IOException e) {

            }
            buyingMsgs = inputString;
            getNewMsgs(false, HomePage.this, 0);
        }


        stores = new ArrayList<>();

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launch(1, HomePage.this);
            }
        });

        messenger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getNewMsgs(true, HomePage.this, 0);
            }
        });

        buying.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getNewMsgs(true, HomePage.this, 1);
            }
        });

        selling.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getNewMsgs(true, HomePage.this, 2);
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

    public Context getCtx() {
        return ctx;
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("isMyServiceRunning?", true+"");
                return true;
            }
        }
        Log.i ("isMyServiceRunning?", false+"");
        return false;
    }

    @Override
    protected void onDestroy() {
        stopService(mServiceIntent);
        Log.i("MAINACT", "onDestroy!");
        super.onDestroy();

    }

    @Override
    protected void onPause(){
        super.onPause();
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
                        }else {
                            allMsgs = "";
                        }
                        if(response.get("buyingMsgs") != null) {
                            buyingMsgs = response.get("buyingMsgs").toString();
                        }else {
                            buyingMsgs = "";
                        }
                        if(response.get("sellingMsgs") != null) {
                            sellingMsgs = response.get("sellingMsgs").toString();
                        }else {
                            sellingMsgs = "";
                        }
                        String path = getFilesDir() + "/messages/" + "LastLogin";
                        try {
                            BufferedWriter out = new BufferedWriter(new FileWriter(path));
                            out.write(Backendless.UserService.CurrentUser().getProperty("name").toString());
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        getNewMsgs(false, HomePage.this, 0);
                    }
                    @Override
                    public void handleFault( BackendlessFault fault )
                    {
                        fault.toString();   // an error has occurred, the error code can be retrieved with fault.getCode()
                    }
                } );
    }

    public static void getNewMsgs(boolean launch, Context c, int type){
        processing = true;
        final boolean tolaunch = launch;
        final Context context = c;
        final int t = type;
        Backendless.Data.of("Messages").findById(MsgID,
                new AsyncCallback<Map>() {
                    @Override
                    public void handleResponse( Map response )
                    {
                        if(response.get("Received") != null) {
                            allMsgs += response.get("Received").toString();
                            String path = context.getFilesDir() + "/messages/" + "allMsgs";
                            try {
                                BufferedWriter out = new BufferedWriter(new FileWriter(path));
                                out.write(allMsgs);
                                out.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            //                            Log.e("TOTAL MSG", "" +allMsgs);
                        }

                        if(response.get("buyingReceived") != null) {


                            buyingMsgs += response.get("buyingReceived").toString();
                            String path = context.getFilesDir() + "/messages/" + "buyingMsgs";
                            try {
                                BufferedWriter out = new BufferedWriter(new FileWriter(path));
                                out.write(buyingMsgs);
                                out.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }

                        if(response.get("sellingReceived") != null) {


                            sellingMsgs += response.get("sellingReceived").toString();
                            String path = context.getFilesDir() + "/messages/" + "sellingMsgs";
                            try {
                                BufferedWriter out = new BufferedWriter(new FileWriter(path));
                                out.write(sellingMsgs);
                                out.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                        if(allMsgs.length() > 6)
                            response.put("allMsgs", allMsgs);
                        if(sellingMsgs.length() > 6)
                            response.put("sellingMsgs", sellingMsgs);
                        if(buyingMsgs.length() > 6)
                            response.put("buyingMsgs", buyingMsgs);
//                                Backendless.Persistence.of("Messages").save(response, new AsyncCallback<Map>() {
//                                    @Override
//                                    public void handleResponse(Map response) {
//                                        // Contact objecthas been updated
//                                    }
//
//                                    @Override
//                                    public void handleFault(BackendlessFault fault) {
//                                        // an error has occurred, the error code can be retrieved with fault.getCode()
//                                    }
//                                });


//                        if(response.get("Received") == null || response.get("buyingReceived") == null || response.get("sellingReceived") == null) {
//                            if(allMsgs.length() > 6 || sellingMsgs.length() > 6 || buyingMsgs.length() > 6) {
//                                if(allMsgs.length() > 6)
//                                    response.put("allMsgs", allMsgs);
//                                if(sellingMsgs.length() > 6)
//                                    response.put("sellingMsgs", sellingMsgs);
//                                if(buyingMsgs.length() > 6)
//                                    response.put("buyingMsgs", buyingMsgs);
////                                Backendless.Persistence.of("Messages").save(response, new AsyncCallback<Map>() {
////                                    @Override
////                                    public void handleResponse(Map response) {
////                                        if(tolaunch)
////                                            launch(2, context);
////                                    }
////
////                                    @Override
////                                    public void handleFault(BackendlessFault fault) {
////                                        // an error has occurred, the error code can be retrieved with fault.getCode()
////                                    }
////                                });
//                            }
//                        }



                        if(allMsgs != null) {
                            buildMessages(allMsgs, 0);
                        }
                        if(buyingMsgs != null) {
                            buildMessages(buyingMsgs, 1);
                        }
                        if(sellingMsgs != null) {
                            buildMessages(sellingMsgs, 2);
                        }

                        response.put("Received", "");
                        response.put("buyingReceived", "");
                        response.put("sellingReceived", "");
                        Backendless.Persistence.of("Messages").save( response, new AsyncCallback<Map>() {
                            @Override
                            public void handleResponse( Map response )
                            {
                                processing = false;
                                if(tolaunch) {
                                    if(t == 0) {
                                        launch(2, context);
                                    }else if(t == 1) {
                                        launch(3, context);
                                    }else if(t == 2) {
                                        launch(4, context);
                                    }
                                }
                            }
                            @Override
                            public void handleFault( BackendlessFault fault )
                            {
                                Log.e("TESTING", " Some Fault " + fault.toString());
                                // an error has occurred, the error code can be retrieved with fault.getCode()
                            }
                        } );

                    }
                    @Override
                    public void handleFault( BackendlessFault fault )
                    {

                        processing = false;
                        Log.e("Response = null", "" + fault.toString());   // an error has occurred, the error code can be retrieved with fault.getCode()
                    }
                } );
    }

    public void deleteRecursive(File Directory) {

        if (Directory.isDirectory()) {
            for(int i = 0; i < Directory.listFiles().length; i++){
                File child = Directory.listFiles()[i];
                deleteRecursive(child);
            }
        }
        Directory.delete();
    }

    private static void buildMessages(String data, int type){
        if(data.length() > 6) {
            if (type == 0) {
                Messages.clear();
            }else if(type == 1){
                buyingMessages.clear();
            }else{
                sellingMessages.clear();
            }
            String[] MessageData = data.split("#");
            for (int i = 0; i < MessageData.length; i++) {
//            Log.i("Message Data: ", "" + MessageData[i] + " " +  MessageData[i + 1] + " " +  MessageData[i + 2]);
                Message temp = new Message();
                temp.setData(MessageData[i], MessageData[i + 1]);

                temp.setText(MessageData[i + 2].replaceAll("~@", "#"));
                temp.setBelongsToCurrentUser(Backendless.UserService.CurrentUser().getProperty("name").toString().equals(MessageData[i]));
                if (i == 0) {
                    ArrayList<Message> tempMessage = new ArrayList<>();
                    tempMessage.add(temp);
                    if (type == 0) {
                        Messages.add(tempMessage);
                    }else if(type == 1){
                        buyingMessages.add(tempMessage);
                    }else{
                        sellingMessages.add(tempMessage);
                    }
                } else {
                    int index = getIndexofMessage(temp.getData().getSender(), temp.getData().getReceiver(), type);
//                    Log.i("INDEX: ", "" + index);
                    if (index != -1) {
                        if (type == 0) {
                            Messages.get(index).add(temp);
                        }else if(type == 1){
                            buyingMessages.get(index).add(temp);
                        }else{
                            sellingMessages.get(index).add(temp);
                        }
                    } else {
                        ArrayList<Message> tempMessage = new ArrayList<>();
                        tempMessage.add(temp);
                        if (type == 0) {
                            Messages.add(tempMessage);
                        }else if(type == 1){
                            buyingMessages.add(tempMessage);
                        }else{
                            sellingMessages.add(tempMessage);
                        }
                    }
                }
                i += 2;
            }


        }
    }

    public static int getIndexofMessage(String sender, String receiver, int type){
        String name = Backendless.UserService.CurrentUser().getProperty("name").toString();
//        Log.i(Messages.size() + " Name:" + name, "Sender: " + sender + " Receiver: " + receiver);
        int size = 0;
        if (type == 0) {
            size = Messages.size();
        }else if(type == 1){
            size = buyingMessages.size();
        }else{
            size = sellingMessages.size();
        }
        if(sender.equals(name)){
            for(int i = 0; i < size; i++){
                if(type == 0){
                    if (Messages.get(i).get(0).getData().getReceiver().equals(receiver) || Messages.get(i).get(0).getData().getSender().equals(receiver))
                        return i;
                }else if(type == 1){
                    if (buyingMessages.get(i).get(0).getData().getReceiver().equals(receiver) || buyingMessages.get(i).get(0).getData().getSender().equals(receiver))
                        return i;
                }else{
                    if (sellingMessages.get(i).get(0).getData().getReceiver().equals(receiver) || sellingMessages.get(i).get(0).getData().getSender().equals(receiver))
                        return i;
                }
            }
        }else{
            for(int i = 0; i < size; i++){
                if(type == 0) {
                    if (Messages.get(i).get(0).getData().getSender().equals(sender) || Messages.get(i).get(0).getData().getReceiver().equals(sender))
                        return i;
                }if(type == 1){
                    if (buyingMessages.get(i).get(0).getData().getSender().equals(sender) || buyingMessages.get(i).get(0).getData().getReceiver().equals(sender))
                        return i;
                }else{
                    if (sellingMessages.get(i).get(0).getData().getSender().equals(sender) || sellingMessages.get(i).get(0).getData().getReceiver().equals(sender))
                        return i;
                }
            }
        }
        return -1;
    }

    private static void launch(int x, Context c){
        Intent intent;
        switch (x){
            case 1:
                intent = new Intent(c, Profile.class);
                intent.putExtra("String", profileData);
                intent.putExtra("objectID", objectID);
                c.startActivity(intent);
                break;
            case 2:
                intent = new Intent(c, StartChatActivity.class);
                intent.putExtra("type", 0);
                intent.putExtra("msgID", MsgID);
                c.startActivity(intent);
                break;
            case 3:
                intent = new Intent(c, StartChatActivity.class);
                intent.putExtra("type", 1);
                intent.putExtra("msgID", MsgID);
                c.startActivity(intent);
                break;
            case 4:
                intent = new Intent(c, StartChatActivity.class);
                intent.putExtra("type", 2);
                intent.putExtra("msgID", MsgID);
                c.startActivity(intent);
                break;

            default:
                break;
        }
    }

    private void initUI() {
        profile = (FloatingActionButton) findViewById(R.id.fab_profileBtn);
        messenger = (FloatingActionButton) findViewById(R.id.fab_Msg);
        buying = (FloatingActionButton) findViewById(R.id.fab_buy);
        selling = (FloatingActionButton) findViewById(R.id.fab_sell);
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
                            startTimer(UserIDs, 0);
                        }
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        Log.e("TOKEN ISSUE: ", "" + fault.getMessage());
                    }
                });
    }

    private void startTimer(String [] id, int x){
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
                                        startTimer(userID, y);
                                    }

                                    @Override
                                    public void handleFault(BackendlessFault fault) {
                                        Log.e("TOKEN ISSUE: ", "" + fault.getMessage());
                                    }
                                });
                    }else {
                        y++;
                        startTimer(userID, y);
                    }
                }
            }, MainLogin.DELAY_TIME);
        }else{
            for(int f = 0; f < userProfiles.size(); f++) {
                userProfilesdata.add(userProfiles.get(f).split("#"));
                int startindex = findIndex(userProfilesdata.get(f), storeID);
                if (startindex == -1) {
                    Log.e("STORE WAS NOT FOUND IN PROFILE", "PROFILE ERROR  ");
                    break;
                }
                Store tempStore = makeFirstStore(userProfilesdata.get(f), startindex);
//                if (Integer.parseInt(userProfilesdata.get(f)[startindex+3]) > 0) {
                    String snapShot = "0" + "#"
                            + Backendless.UserService.CurrentUser().getProperty("name") + "#"
                            + userProfilesdata.get(f)[0] + "#"
                            + userProfilesdata.get(f)[startindex] + "#"
                            + userProfilesdata.get(f)[startindex + 1] + "#"
                            + userProfilesdata.get(f)[startindex + 2] + "#"
                            + userProfilesdata.get(f)[startindex + 3] + "#";
                    int snapShotCounter = startindex + 4;
                    for(int v = 0; v < Integer.parseInt(userProfilesdata.get(f)[startindex + 3]); v++){
                        snapShot += userProfilesdata.get(f)[snapShotCounter] + "#";     // rate
                        snapShot += userProfilesdata.get(f)[snapShotCounter + 1] + "#"; // Constraint
                        snapShot += userProfilesdata.get(f)[snapShotCounter + 2] + "#"; // TotalAmnt
                        snapShot += userProfilesdata.get(f)[snapShotCounter + 3] + "#"; // CurrentAmnt
                        snapShot += "0" + "#";                                          // SelectedAmnt
                        snapShot += userProfilesdata.get(f)[snapShotCounter + 4] + "#"; // AtCost
                        snapShot += userProfilesdata.get(f)[snapShotCounter + 5] + "#"; // Reoccuring
                        snapShot += userProfilesdata.get(f)[snapShotCounter + 6] + "#"; // Period
                        snapShot += userProfilesdata.get(f)[snapShotCounter + 7] + "#"; // Reoccure Date
                        snapShot += "0" + "#";                                          // Deal Selector
                        snapShotCounter += 8;
                    }
                    Calendar getDat = Calendar.getInstance();
                    snapShot += Integer.toString(getDat.get(Calendar.YEAR))
                            + "~" + Integer.toString(getDat.get(Calendar.MONTH))
                            + "~" + Integer.toString(getDat.get(Calendar.DAY_OF_MONTH)) + "#";
                    int snapLength = snapShot.length();
                    String snaplen = Integer.toString(snapLength + Integer.toString(snapLength).length() + 1) + "#";
                    int startIndex = 2
                            + new String(Backendless.UserService.CurrentUser().getProperty("name") + "#").length()
                            + new String(userProfilesdata.get(f)[0] + "#").length();
                    snapShot = snapShot.substring(0, startIndex) + snaplen + snapShot.substring(startIndex);
                    storeSummery temp = new storeSummery(userProfilesdata.get(f)[0], userProfilesdata.get(f)[startindex + 1], remakeString(userProfilesdata.get(f)[startindex + 2]));
                    temp.setStore(tempStore);
                    temp.setProfSnapshot(snapShot);
                    Log.e("PROFILE SNAPSHOT" , " " + temp.getProfSnapshot());
                    int baseval = startindex + 4;//get to the first %
                    List deals = new ArrayList();
                    for (int d = 0; d < Integer.parseInt(userProfilesdata.get(f)[startindex + 3]); d++){
                        deals.add(Integer.parseInt(userProfilesdata.get(f)[baseval]));
                        baseval += 8;
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
            for (int x = i + 4; x < (i + 4) + Integer.parseInt(arrayData[i + 3]) * 8; x++) {
                temp.addDeal(arrayData[x],
                        remakeString(arrayData[x + 1]),
                        arrayData[x + 2],
                        arrayData[x + 3],
                        Boolean.parseBoolean(arrayData[x + 4]),
                        Boolean.parseBoolean(arrayData[x + 5]),
                        arrayData[x + 6],
                        arrayData[x + 7]);
                x += 7;
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
            //launch(v);
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
