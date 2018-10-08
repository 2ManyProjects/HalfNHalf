package com.halfnhalf;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.files.BackendlessFile;
import com.backendless.persistence.DataQueryBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.halfnhalf.store.FindStore;
import com.halfnhalf.store.Store;
import com.halfnhalf.store.StoreAdapter;
import com.halfnhalf.store.storeDeals;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Profile extends AppCompatActivity {

    private String [] userInfo = {"", "", ""};
    private int numStores = 0;
    private String userData;
    private String [] arrayData;
    private int versionNum;


    public static RecyclerView.Adapter Profileadapter;
    private RecyclerView.LayoutManager ProfilelayoutManager;
    private static RecyclerView ProfilerecyclerView;
    public static ArrayList<Store> Profiledataset;
    private TypedArray ImageResources;
    private FloatingActionButton add;
    public static View.OnClickListener myOnClickListener;
    private ArrayList<String> removed;
    private boolean fromJson = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        fromJson = false;
        removed = new ArrayList<>();

        myOnClickListener = new MyOnClickListener(this);

        ProfilerecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        ProfilerecyclerView.setHasFixedSize(true);

        ProfilelayoutManager = new LinearLayoutManager(this);
        ProfilerecyclerView.setLayoutManager(ProfilelayoutManager);
        ProfilerecyclerView.setItemAnimator(new DefaultItemAnimator());

        Profiledataset = new ArrayList<Store>();

        Profileadapter = new StoreAdapter(Profile.this, Profiledataset);
        ProfilerecyclerView.setAdapter(Profileadapter);
        userInfo[2] = MainLogin.getUser().getObjectId();
        this.userData = (String) MainLogin.getUser().getProperty("profileData");
        String profileID = (String) MainLogin.getUser().getProperty("profile");
        if(profileID == null)
            saveProfile(1);
        else
            initUserProfile();


        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        //Helper class for creating swipe to dismiss and drag and drop functionality
        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback
                (ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT | ItemTouchHelper.DOWN
                        | ItemTouchHelper.UP, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target) {

                //Get the from and to position
                int from = viewHolder.getAdapterPosition();
                int to = target.getAdapterPosition();

                //Swap the items and notify the adapter
                Collections.swap(Profiledataset, from, to);
                Profileadapter.notifyItemMoved(from, to);
                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                numStores --;
                //Remove the item from the dataset
                if(!Profiledataset.get(viewHolder.getAdapterPosition()).isNew()) {
                    removed.add(Profiledataset.get(viewHolder.getAdapterPosition()).getID());
                }
                Profiledataset.remove(viewHolder.getAdapterPosition());

                //Notify the adapter
                Profileadapter.notifyItemRemoved(viewHolder.getAdapterPosition());
            }
        });

        add = findViewById(R.id.addStore);
        add.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                findStore();
            }
        });
        //Attach the helper to the RecyclerView
        helper.attachToRecyclerView(ProfilerecyclerView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.save) {
            saveProfile(0);
            return true;
        }else{
            return super.onOptionsItemSelected(item);
        }
    }

    private void saveProfile(int type){
        final int t = type;
        final String temp = createProfile();
        final String gsonData = new Gson().toJson(Profiledataset).toString();

        Log.i("Gson Data: ", gsonData);
        try {
            String path = getFilesDir() + "/tempData/" + MainLogin.getUser().getObjectId() + ".txt";
            FileOutputStream writer = new FileOutputStream (path);
            writer.write(gsonData.getBytes());
            writer.close();
            final File gsonFile = new File(path);
            Log.i("File Closed ", "File Stuff");
            Backendless.Files.upload( gsonFile, "/profileData/" + MainLogin.getUser().getObjectId(), true, new AsyncCallback<BackendlessFile>(){
                @Override
                public void handleResponse(BackendlessFile response) {
                    final String location = response.getFileURL();
                    Log.i("Location ", location);
                    Backendless.Data.of(BackendlessUser.class).findById(Backendless.UserService.CurrentUser().getObjectId(),
                            new AsyncCallback<BackendlessUser>() {
                                @Override
                                public void handleResponse(BackendlessUser foundUser) {
                                    foundUser.setProperty("profileData", temp);
                                    foundUser.setProperty("profile", location);
                                    Backendless.UserService.update( foundUser, new AsyncCallback<BackendlessUser>()
                                    {
                                        @Override
                                        public void handleResponse( BackendlessUser backendlessUser )
                                        {
                                            Log.i("UPDATED PROFILE", " " + temp);
                                            gsonFile.delete();
                                            if(t == 1) {
                                                MainLogin.reloadUserData();
                                                startTimer(1);
                                            }
                                        }

                                        @Override
                                        public void handleFault( BackendlessFault backendlessFault )
                                        {

                                        }
                                    }  );
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

    @Override
    protected void onResume(){
        super.onResume();
//        MainLogin.reloadUserData();
//        startTimer(1);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Really Exit?")
                .setMessage("Are you sure you want to exit?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        Profile.super.onBackPressed();
                        //logoutFromBackendless();
                        Intent intent = new Intent(Profile.this, HomePage.class);
                        startActivity(intent);
                        Profile.super.finish();
                    }
                }).create().show();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if(resultCode == RESULT_OK) {
                //dealData = new ArrayList<>();
                Bundle bundle = data.getExtras();
                String ID = bundle.getString("id");
                String getDeals = bundle.getString("Deals");
                boolean isChanged = bundle.getBoolean("isChanged");
                Gson gson = new Gson();
                Type type = new TypeToken<ArrayList<Deal>>() {
                }.getType();
                ArrayList<Deal> dealData = gson.fromJson(getDeals, type);
                if(isChanged) {
                    String getRemoved = bundle.getString("removedID");
                    Gson mgson = new Gson();
                    Type mtype = new TypeToken<ArrayList<String>>() {
                    }.getType();
                    ArrayList<String> isRemoved = mgson.fromJson(getRemoved, mtype);
                    fillStore(dealData, ID, isRemoved);
                }
            }
        }else if (requestCode == 2) {
            if(resultCode == RESULT_OK) {
                //dealData = new ArrayList<>();
                Bundle bundle = data.getExtras();
                String ID = bundle.getString("id");
                String name = bundle.getString("name");
                String address = bundle.getString("address");
                addStore(ID, name, address);
            }
        }
    }

    public void initUserProfile(){
        fromJson = false;
        startTimer(0);
        arrayData = userData.split("#");
        userInfo[0] = MainLogin.getUser().getProperty("name").toString();
        userInfo[1] = MainLogin.getUser().getEmail().toString();
        versionNum = Integer.parseInt(arrayData[2]);
        numStores = Integer.parseInt(arrayData[3]);
        populateProfile();
    }

    public String createProfile()
    {
        ArrayList<String> savedStores = new ArrayList<>();
        for(int p = 0; p < Profiledataset.size(); p++){
            if(Profiledataset.get(p).isNew()) {
                savedStores.add(Profiledataset.get(p).getID());
                Profiledataset.get(p).setNew(false);
            }
        }
        String temp = userInfo[0] + "#" + userInfo[1] + "#" + Integer.toString(versionNum++) + "#" + Integer.toString(numStores) + "#";
        for(int i = 0; i < Profiledataset.size(); i++){
            temp += Profiledataset.get(i).toString();
        }
        queryStoreIDs(savedStores, 0, true, true);
        queryStoreIDs(removed, 0, false, true);
        return temp;
    }

    public void downloadFile( String path ){
        final String p = path;
        new Thread(new Runnable() {
            public void run() {
                URL url = null;
                try {
                    url = new URL(p);
                    String tempData = Profile.this.getFilesDir() + "/tempData/" + "profileGson.txt";
                    downloadFromUrl(url, tempData, 0);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void downloadFromUrl(URL url, String localFilename, int x) throws IOException {
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
                for (int i = 0; i < temp.size(); i++) {
                    numStores++;
                    Profiledataset.add(temp.get(i));
                }
                if (x == 0)
                    fromJson = true;
                File file = new File(localFilename);
                file.delete();
                fos.close();
                is.close();
                in.close();
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
        if(x == 0) {
            if(!fromJson) {
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        startTimer(y);
                    }
                }, MainLogin.DELAY_TIME);
            } else {
                Profileadapter.notifyDataSetChanged();
            }
        }else if(x == 1){
            if (MainLogin.processing) {
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        startTimer(y);
                    }
                }, MainLogin.DELAY_TIME);
            } else {
                initUserProfile();
            }
        }
    }

        private void populateProfile(){
            Log.i("Building", "" + "Profile");
        ImageResources = getResources().obtainTypedArray(R.array.images);
        downloadFile(MainLogin.getUser().getProperty("profile").toString());
    }

    private void queryStoreIDs(ArrayList<String> id, int x, boolean addingStore, boolean resume){
        final boolean toAdd = addingStore;
        final ArrayList<String> storeID = id;
        final int i = x;
        final boolean toContinue = resume;
        if(x < id.size()) {
            new Handler().postDelayed(new Runnable() {
                int y = i;
                boolean toResume = toContinue;

                @Override
                public void run() {
                    if(toResume) {
                        final String ID = storeID.get(y);
                        toResume = false;
                        y++;
                        String whereClause = "StoreID = " + "'" + ID + "'";
                        Log.e("WhereClause", "" + whereClause);

                        DataQueryBuilder dataQuery = DataQueryBuilder.create();
                        dataQuery.setWhereClause(whereClause);
                        Backendless.Data.of("Stores").find(dataQuery,
                                new AsyncCallback<List<Map>>() {
                                    @Override
                                    public void handleResponse(List<Map> foundStore) {
                                        Log.e("STORE EXIST, ADD USER", " ");
                                        if (foundStore.size() == 1) {
                                            Log.e("STORE IS NOT EMPTY, KeySets", " FoundStoreSize " + foundStore.size());
                                            String Users;
                                            String whereClause = "StoreID = " + "'" + ID + "'";

                                            DataQueryBuilder dataQuery = DataQueryBuilder.create();
                                            dataQuery.setWhereClause(whereClause);
                                            if (toAdd) {
                                                Users = foundStore.get(0).get("UserList").toString() + userInfo[2] + "#";
                                            } else {
                                                Users = foundStore.get(0).get("UserList").toString().replaceAll(userInfo[2] + "#", "");
                                            }
                                            Log.e("Users", " : " + Users);
                                            foundStore.get(0).put("UserList", Users);
                                            Log.e("WhereClause", "" + whereClause);
                                            Map<String, Object> changes = new HashMap<>();
                                            changes.put("UserList", Users);
                                            if (!Users.isEmpty()) {
                                                Backendless.Data.of("Stores").update(whereClause, changes, new AsyncCallback<Integer>() {
                                                    @Override
                                                    public void handleResponse(Integer objectsUpdated) {
                                                        toResume = true;
                                                        Log.i("MYAPP", "Server has updated " + objectsUpdated + " objects");
                                                    }

                                                    @Override
                                                    public void handleFault(BackendlessFault fault) {
                                                        Log.e("MYAPP", "Server reported an error - " + fault);
                                                    }
                                                });
                                            } else {
                                                Backendless.Data.of("Stores").remove(whereClause, new AsyncCallback<Integer>() {
                                                    @Override
                                                    public void handleResponse(Integer objectsDeleted) {
                                                        toResume = true;
                                                        Log.i("MYAPP", "Server has deleted " + objectsDeleted + " objects");
                                                    }

                                                    @Override
                                                    public void handleFault(BackendlessFault fault) {
                                                        Log.e("MYAPP", "Server reported an error - " + fault);
                                                    }
                                                });
                                            }
                                        } else {
                                            HashMap newStore = new HashMap();
                                            newStore.put("StoreID", ID);
                                            newStore.put("UserList", userInfo[2] + "#");
                                            Backendless.Persistence.of("Stores").save(newStore, new AsyncCallback<Map>() {
                                                @Override
                                                public void handleResponse(Map response) {
                                                    toResume = true;
                                                }

                                                @Override
                                                public void handleFault(BackendlessFault fault) {

                                                }
                                            });
                                        }
                                    }

                                    @Override
                                    public void handleFault(BackendlessFault fault) {
                                        Log.e("TOKEN ISSUE: ", "" + fault.getMessage());
                                    }
                                });
                        queryStoreIDs(storeID, y, toAdd, toResume);
                    }
                }
            }, MainLogin.DELAY_TIME);
        }else if(!addingStore){
            removed.clear();
        }

    }


    private String fixString(String str){
        return str.replaceAll("#", "~@");
    }

    private String remakeString(String str){
        return str.replaceAll("~@", "#");
    }

    private void fillStore(ArrayList<Deal> dealData, String id, ArrayList<String> removed){
        int index = -1;
        for(int i = 0; i < Profiledataset.size(); i++){
            if(Profiledataset.get(i).getID().equals(id)){
                index = i;
                break;
            }
        }
//        Displayer.toaster("Deals: " + Integer.toString(dealData.size()) + " ID: " + id, "h", this);
        if(index == -1){
            return;
        }else{
            for(int i = 0; i < Profiledataset.get(index).getData().size(); i++){
                if(removed.contains(Profiledataset.get(index).getData(i).getId())) {
                    Profiledataset.get(index).getData().remove(i);
                }
            }
            for(int i = 0; i < dealData.size(); i++){
                Profiledataset.get(index).changeDeal(i,
                        dealData.get(i).getRate(),
                        dealData.get(i).getText(),
                        dealData.get(i).getTotalAmnt(),
                        dealData.get(i).getCurrentAmnt(),
                        dealData.get(i).getAtCost(),
                        dealData.get(i).getReoccuring(),
                        dealData.get(i).getPeriodVal(),
                        Integer.toString(dealData.get(i).getResetDate().get(Calendar.YEAR)),
                        Integer.toString(dealData.get(i).getResetDate().get(Calendar.MONTH)),
                        Integer.toString(dealData.get(i).getResetDate().get(Calendar.DAY_OF_MONTH)),
                        dealData.get(i).getId());
            }
        }
    }

    private void findStore(){
        Intent intent;
        intent = new Intent(Profile.this, FindStore.class);
        startActivityForResult(intent, 2);
    }

    public void addStore(String ID, String Name, String address) {
        if (Profiledataset.size() < 1) {
            Store temp = new Store(ID, Name, address, ImageResources.getResourceId((numStores % 10),0));
            temp.setNew(true);
            temp.setSeller(MainLogin.getUser().getProperty("name").toString());
            Profiledataset.add(temp);
            Profileadapter.notifyDataSetChanged();
            numStores += 1;
            return;
        } else {
            boolean found = false;
            for (int i = 0; i < Profiledataset.size(); i++) {
                if (Profiledataset.get(i).getID().equals(ID)) {
                    found = true;
                    break;
                }
            }
            if (found) {
                return;
            } else {
                Store temp = new Store(ID, Name, address, ImageResources.getResourceId((numStores % 10),0));
                temp.setNew(true);
                Profiledataset.add(temp);
                Profileadapter.notifyDataSetChanged();
                numStores += 1;
                return;
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
            int selectedItemPosition = ProfilerecyclerView.getChildPosition(v);
            Store mCurrentStore = Profiledataset.get(selectedItemPosition);
            String storeData = new Gson().toJson(mCurrentStore);
            Intent intent;
            intent = new Intent(context, storeDeals.class);
            intent.putExtra("Store", storeData);
            intent.putExtra("type", 1);
            startActivityForResult(intent, 1);
        }
    }
}
