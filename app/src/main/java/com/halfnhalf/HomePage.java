package com.halfnhalf;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
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
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class HomePage extends AppCompatActivity {

    private FloatingActionButton profile,  buying, selling;

    private String storeID;
    private String Userlist;
    private String[] UserIDs;
    private ArrayList<String> userProfiles;
    private ArrayList<Store> userProfilesData;
    private ArrayList<storeSummery> stores;
    private File mPath;
    public static String MsgID = "";
    public static String sellingMsgs = "";
    public static String buyingMsgs = "";
    public static ArrayList<ArrayList<Message>> sellingMessages;
    public static ArrayList<ArrayList<Message>> buyingMessages;

    private static ArrayList<String> gsonFiles;
    public static RecyclerView.Adapter Summeryadapter;
    private RecyclerView.LayoutManager SummerylayoutManager;
    private static RecyclerView SummeryrecyclerView;
    public static View.OnClickListener myOnClickListener;
    private boolean firstLaunch = false;
    public static boolean processing = false;
    private static boolean looping = false;
    private messageListener mMessages;
    private Intent mServiceIntent;
    Context ctx;


    EditText editText;
    Button button;
    //TODO: If there is a Completed Deal in the Buying or SellingData Move the deal over to BuyingHistory (Or selling History) as needed and update the Deal on the Sellers Profile

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = this;
        gsonFiles = new ArrayList<String>();
        setContentView(R.layout.activity_home_page);
        mMessages = new messageListener(getCtx());
        mServiceIntent = new Intent(getCtx(), mMessages.getClass());
        if (!isMyServiceRunning(mMessages.getClass())) {
            startService(mServiceIntent);
        }
        mMessages.setIsloggedIn(true);
        initUI();
        sellingMessages = new ArrayList<ArrayList<Message>>();
        buyingMessages = new ArrayList<ArrayList<Message>>();

        MsgID = (String) MainLogin.getUser().getProperty("messageID");
        Log.i("Msg ID", "" + MsgID);
        mPath = new File(getFilesDir() + "/messages/");

        if (!mPath.exists()) {
            firstLaunch = true;
            mPath.mkdirs();
            getMsgs();
        }
        if(!new File(getFilesDir() + "/tempData/").exists()){
            new File(getFilesDir() + "/tempData/").mkdirs();
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
            String path = getFilesDir() + "/messages/" + "sellingMsgs";
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

        completedDeals();
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
        Log.i("HomePage ", "onDestroy!");
        super.onDestroy();

    }

    @Override
    protected void onPause(){
        super.onPause();
    }
    @Override
    protected void onResume() {
        completedDeals();
        super.onResume();
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
        Backendless.Data.of("Messaging").findById(MsgID,
                new AsyncCallback<Map>() {
                    @Override
                    public void handleResponse( Map response )
                    {
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
        Backendless.Data.of("Messaging").findById(MsgID,
                new AsyncCallback<Map>() {
                    @Override
                    public void handleResponse( Map response )
                    {
                        if(response.get("buyingReceived") != null) {

                            if(buyingMsgs == null){
                                buyingMsgs = response.get("buyingReceived").toString();
                            }else {
                                buyingMsgs += response.get("buyingReceived").toString();
                            }
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

                            if(sellingMsgs == null){
                                sellingMsgs = response.get("sellingReceived").toString();
                            }else {
                                sellingMsgs += response.get("sellingReceived").toString();
                            }
                            String path = context.getFilesDir() + "/messages/" + "sellingMsgs";
                            try {
                                BufferedWriter out = new BufferedWriter(new FileWriter(path));
                                out.write(sellingMsgs);
                                out.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                        if(sellingMsgs.length() > 6)
                            response.put("sellingMsgs", sellingMsgs);
                        if(buyingMsgs.length() > 6)
                            response.put("buyingMsgs", buyingMsgs);


                        if(buyingMsgs != null) {
                            buildMessages(buyingMsgs, 1);
                        }
                        if(sellingMsgs != null) {
                            buildMessages(sellingMsgs, 2);
                        }

                        response.put("Received", "");
                        response.put("buyingReceived", "");
                        response.put("sellingReceived", "");
                        Backendless.Persistence.of("Messaging").save( response, new AsyncCallback<Map>() {
                            @Override
                            public void handleResponse( Map response )
                            {
                                processing = false;
                                if(tolaunch) {
                                    if(t == 1) {
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
            if(type == 1){
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
                    if(type == 1){
                        buyingMessages.add(tempMessage);
                    }else{
                        sellingMessages.add(tempMessage);
                    }
                } else {
                    int index = getIndexofMessage(temp.getData().getSender(), temp.getData().getReceiver(), type);
//                    Log.i("INDEX: ", "" + index);
                    if (index != -1) {
                        if(type == 1){
                            buyingMessages.get(index).add(temp);
                        }else{
                            sellingMessages.get(index).add(temp);
                        }
                    } else {
                        ArrayList<Message> tempMessage = new ArrayList<>();
                        tempMessage.add(temp);
                        if(type == 1){
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
        if(type == 1){
            size = buyingMessages.size();
        }else{
            size = sellingMessages.size();
        }
        if(sender.equals(name)){
            for(int i = 0; i < size; i++){
                if(type == 1){
                    if (buyingMessages.get(i).get(0).getData().getReceiver().equals(receiver) || buyingMessages.get(i).get(0).getData().getSender().equals(receiver))
                        return i;
                }else{
                    if (sellingMessages.get(i).get(0).getData().getReceiver().equals(receiver) || sellingMessages.get(i).get(0).getData().getSender().equals(receiver))
                        return i;
                }
            }
        }else{
            for(int i = 0; i < size; i++){
                if(type == 1){
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
                c.startActivity(intent);
                break;
            case 2:
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
                userProfiles.clear();
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
                            getUserProfiles(UserIDs, 0);
                        }
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        Log.e("TOKEN ISSUE: ", "" + fault.getMessage());
                    }
                });
    }

    public void downloadFile( String path, int i){
        final String p = path;
        final int index = i;
        looping = true;
        new Thread(new Runnable() {
            public void run() {
                URL url = null;
                try {
                    url = new URL(p);
                    String tempData = HomePage.this.getFilesDir() + "/tempData/" + "Gson.txt";
                    downloadFromUrl(url, tempData, index);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    private void downloadFromUrl(URL url, String localFilename, int i) throws IOException {
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
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<Store>>(){}.getType();
            ArrayList<Store> temp = gson.fromJson(data, type);
            File file = new File(localFilename);
            file.delete();
            fos.close();
            is.close();
            in.close();
            for(int x = 0; x < temp.size(); x++)
            {
                if(temp.get(i).getID().equals(storeID)) {
                    userProfilesData.add(temp.get(i));
                    break;
                }
            }
            looping = false;
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

    private void startTimer(int type, int iterator){
        final int t = type;
        final int i = iterator;
        if(looping) {
            if (Looper.myLooper() == null){
                Looper.prepare();
            }
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    startTimer(t, i);
                }
            }, MainLogin.DELAY_TIME);
        }else{
            if(t == 1 && iterator < gsonFiles.size()) {
                downloadFile(gsonFiles.get(i), i);
                startTimer(t, i+1);
            }else{
                for(int v = 0; v < userProfilesData.size(); v++) {
                    Log.i("[" + v + "]", " " + userProfilesData.get(v).toString());
                    for(int ind = 0; ind < userProfilesData.get(v).getData().size(); ind++){
                        userProfilesData.get(v).getData().get(ind).setSelectedAmnt(0);
                        userProfilesData.get(v).getData().get(ind).setSelected(0);
                        userProfilesData.get(v).setBuyer(MainLogin.getUser().getProperty("name").toString());
                        userProfilesData.get(v).setDealProgression(0);
                    }
                    for(int index = 0; index < userProfilesData.size(); index++){
                        Log.i("ProfileData", "" + userProfilesData.get(index).toString());
                    }
                    for(int f = 0; f < userProfilesData.size(); f++) {

                        Store tempStore = userProfilesData.get(f);
                        tempStore.setBuyer(MainLogin.getUser().getProperty("name").toString());

                        storeSummery temp = new storeSummery(tempStore.getSeller(), tempStore.getName(), tempStore.getAddress());
                        temp.setStore(tempStore);

                        String storeGson = new Gson().toJson(tempStore).toString();
                        ArrayList<skewedDeals> deals = new ArrayList<>();
                        for (int d = 0; d < userProfilesData.get(f).getData().size(); d++){
                            skewedDeals tempDeal = new skewedDeals(Integer.parseInt(userProfilesData.get(f).getData().get(d).getRate()), userProfilesData.get(f).getData().get(d).getAtCost());
                            deals.add(tempDeal);
                        }
                        Collections.sort(deals, new Comparator<skewedDeals>() {
                            @Override
                            public int compare(skewedDeals o1, skewedDeals o2) {
                                return o1.compareTo(o2);
                            }
                        });
                        Collections.reverse(deals);
                        int size = 3;
                        if (deals.size() < 3)
                            size = deals.size();
                        for (int h = 0; h < size; h++) {
                            temp.addDeal((deals.get(h).toString()), (deals.get(h).getAtCost()));
                        }
                        stores.add(temp);
                        stores.get(f).setsnapshotGson(storeGson);
                        Summeryadapter.notifyItemInserted(stores.size());
                    }
                }
            }
        }
    }

    private void getUserProfiles(String [] id, int x){
        processing = true;
        final String [] userID = id;
        final int i = x;
        if(x == 0 && userProfilesData != null)
            userProfilesData.clear();
        if(userProfilesData == null)
            userProfilesData = new ArrayList<Store>();
        if(x < id.length) {
            new Handler().postDelayed(new Runnable() {
                int y = i;
                @Override
                public void run() {
                    boolean found = false;
                    final String ID = userID[y];
                    if(!ID.equals(Backendless.UserService.CurrentUser().getUserId())) {
                        Backendless.Data.of(BackendlessUser.class).findById(ID,
                                new AsyncCallback<BackendlessUser>() {
                                    @Override
                                    public void handleResponse(BackendlessUser foundUser) {
                                        //if(foundUser.getProperty("profile") != null)
                                        gsonFiles.add(foundUser.getProperty("profile").toString());
                                        for(int i = 0; i < gsonFiles.size(); i++){
                                            Log.i("Gson[" + i + "]", " " + gsonFiles.get(i));
                                        }
                                        //downloadFile(foundUser.getProperty("profile").toString(), 1, userID, y);
                                        userProfiles.add(foundUser.getProperty("profileData").toString());
                                        y++;
                                        getUserProfiles(userID, y);
                                    }

                                    @Override
                                    public void handleFault(BackendlessFault fault) {
                                        Log.e("TOKEN ISSUE: ", "" + fault.getMessage());
                                    }
                                });
                    }else {
                        y++;
                        getUserProfiles(userID, y);
                    }
                }
            }, MainLogin.DELAY_TIME);
        }else{
            startTimer(1, 0);
        }
    }

    private String remakeString(String str){
        return str.replaceAll("~@", "#");
    }

    public void completedDeals(){//TODO fix this to work with Gson Files
        Backendless.Data.of("Messaging").findById(MsgID, new AsyncCallback<Map>() {
            @Override
            public void handleResponse(Map response) {
                String buyingData = "";
                String sellingData = "";
                int changed = 0;
                if(response.get("buyingData") != null) {
                    if(buyingData.length() > 5){
                        Log.e("BUYINGDAT ", " STARTED");
                        if(buyingData.contains("6#" + Backendless.UserService.CurrentUser().getProperty("name").toString() + "#")) {
                            String[] data = buyingData.split("#");
                            String index = getnewIndex(data,  Backendless.UserService.CurrentUser().getProperty("name").toString(), 0);
                            if(!index.equals("-1")){
                                int [] indexData = { Integer.parseInt(index.split("#")[0]) ,Integer.parseInt(index.split("#")[1]) };
                                int length = Integer.parseInt(data[indexData[0] + 3]);
                                int startLength = indexData[1];
                                String dataToMove = buyingData.substring(startLength, length + startLength);
                                buyingData = buyingData.replace(dataToMove, "");
                                if(response.get("buyingHistory") != null)
                                    if(response.get("buyingHistory").toString().length() > 5)
                                        dataToMove = response.get("buyingHistory").toString() + dataToMove;
                                response.put("buyingData", buyingData);
                                response.put("buyingHistory", dataToMove);
                                changed = 1;
                            }
                        }
                    }
                }
                if(response.get("sellingData") != null) {
                    if(sellingData.length() > 5){
                        if(sellingData.contains("6#")) {
                            String[] data = sellingData.split("#");
                            String index = getnewIndex(data,  Backendless.UserService.CurrentUser().getProperty("name").toString(), 1);
                            if(!index.equals("-1")){
                                int [] indexData = { Integer.parseInt(index.split("#")[0]) ,Integer.parseInt(index.split("#")[1]) };
                                int length = Integer.parseInt(data[indexData[0] + 3]);
                                int startLength = indexData[1];
                                String dataToMove = sellingData.substring(startLength, length + startLength);
                                sellingData = sellingData.replace(dataToMove, "");
                                if(response.get("sellingHistory") != null)
                                    if(response.get("sellingHistory").toString().length() > 5)
                                        dataToMove = response.get("sellingHistory").toString() + dataToMove;
                                response.put("sellingData", sellingData);
                                response.put("sellingHistory", dataToMove);
                                changed = 2;
                            }
                        }
                    }
                }

                if(changed > 0){
                    processing = true;
                    Backendless.Data.of("Messaging").save(response, new AsyncCallback<Map>() {
                        @Override
                        public void handleResponse(Map response) {
                            processing = false;
                            completedDeals();
                        }

                        @Override
                        public void handleFault(BackendlessFault fault) {
                            processing = false;
                        }
                    });
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {

            }
        });
    }

    private String getnewIndex(String [] data, String compare, int type){
        int length = 0;
        int typeLength = 1;
        if(type == 0)
            typeLength = 1;
        else if(type == 1)
            typeLength = 2;

        for (int i = 0; i < data.length; i++) {
            if (data[i].equals("6") && i + 2 < data.length) {
                Log.e("INDEX TESTING", " Name: " + data[i + typeLength] + " Name: " + compare );
                if (data[i + typeLength].equals(compare)) {
                    return Integer.toString(i) + "#" + Integer.toString(length);
                }
            }
            length+= data[i].length() + 1;
        }
        return "-1";
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_homepage, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            mMessages.setIsloggedIn(false);
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
class skewedDeals implements Comparable<skewedDeals>{
    int value = 0;
    boolean atCost = false;
    public skewedDeals(int val, boolean atCost){
        this.value = val;
        this.atCost = atCost;
    }

    public boolean getAtCost(){
        return atCost;
    }

    public int getValue() {
        return value;
    }

    @Override
    public int compareTo(@NonNull skewedDeals o) {
        if(o.getValue() == value)
            return 0;
        else if(value > o.getValue())
            return 1;
        else
            return -1;
    }
    @Override
    public String toString(){
        return Integer.toString(value);
    }
}
