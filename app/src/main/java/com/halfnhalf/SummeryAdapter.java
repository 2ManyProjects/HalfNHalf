package com.halfnhalf;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.files.BackendlessFile;
import com.backendless.persistence.DataQueryBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.halfnhalf.Messaging.ChatRoomActivity;
import com.halfnhalf.Messaging.Message;
import com.halfnhalf.store.Store;
import com.halfnhalf.store.storeSummery;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SummeryAdapter extends RecyclerView.Adapter<SummeryAdapter.MyViewHolder> {
    private ArrayList<storeSummery> dataSet;
    private Context mContext;



    public SummeryAdapter(Context context, ArrayList<storeSummery> data) {
        this.dataSet = data;
        this.mContext = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(mContext, LayoutInflater.from(mContext).
                inflate(R.layout.list_profile, parent, false));
    }

    @Override
    public void onBindViewHolder (final SummeryAdapter.MyViewHolder holder, final int listPosition){
        storeSummery currentStore = dataSet.get(listPosition);
        holder.bindTo(currentStore);
    }

    @Override
    public int getItemCount () {
        return dataSet.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        //Member Variables for the holder data
        private TextView mUserName;
        private TextView mstoreName;
        private TextView mdeals;
        private FloatingActionButton msg;
        private Context mContext;
        private storeSummery mCurrentStore;
        private ArrayList<Store> buyingstoreList = new ArrayList<Store>();
        private ArrayList<Store> sellingstoreList = new ArrayList<Store>();
        private String Seller;
        private String currentUser = MainLogin.getUser().getProperty("name").toString();
        private View view;


        MyViewHolder(Context context, View itemView) {
            super(itemView);
            view = itemView;

            //Initialize the views
            mUserName = (TextView)itemView.findViewById(R.id.UserName);
            mstoreName = (TextView)itemView.findViewById(R.id.storeName);
            mdeals = (TextView)itemView.findViewById(R.id.deals);
            msg = (FloatingActionButton)itemView.findViewById(R.id.msg);

            mContext = context;

            //Set the OnClickListener to the whole view
            itemView.setOnClickListener(HomePage.myOnClickListener);
        }

        void bindTo(final storeSummery currentStore){
            //Populate the textviews with data
            mUserName.setText(currentStore.getUserName());
            mstoreName.setText(currentStore.getName());
            mdeals.setText(currentStore.getDeals()[0] + "   " + currentStore.getDeals()[1] + "   " + currentStore.getDeals()[2]);

            msg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openDeal(currentStore.getUserName(), currentStore, mContext);
                }
            });
            //Get the current sport
            mCurrentStore = currentStore;
        }

        private void openDeal(String seller, storeSummery store, Context c){
            mCurrentStore = store;
            Seller = seller;
            getBuyingData();
        }

        public void downloadFile( String path, storeSummery sum, int type){
            final storeSummery summery = sum;
            final String p = path;
            final int t = type;
            new Thread(new Runnable() {
                public void run() {
                    URL url = null;
                    try {
                        url = new URL(p);
                        String tempData = mContext.getFilesDir() + "/tempData/" + "Gson.txt";
                        downloadFromUrl(url, tempData, summery, t);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
        private void downloadFromUrl(URL url, String localFilename, storeSummery sum, int t) throws IOException {
            InputStream is = null;
            FileOutputStream fos = null;
            final storeSummery summery = sum;
            Log.i("URL", "" + url.toString());

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
                    if (t == 0) {
                        buyingstoreList = temp;
                        if (checkPassedDeals() == false)
                            getSellingData();
                    } else if (t == 1) {
                        sellingstoreList = temp;
                        launch();
                    }
                //}
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

        private boolean checkPassedDeals(){
            for(int i = 0; i < buyingstoreList.size(); i++){
                Store thisStore = buyingstoreList.get(i);
                if(thisStore.getSeller().equals(Seller)){
                    if (Looper.myLooper() == null){
                        Looper.prepare();
                    }
                    if(thisStore.getDealProgression() == 0){
                        Displayer.setSnackBar(view, "Please Complete your current deal with: " + Seller);
                    }if(thisStore.getDealProgression() == 1){
                        Displayer.setSnackBar(view, "You have an Open Locked Deal with: " + Seller + ". Please \n complete that first");
                    }else if(thisStore.getDealProgression() == 4){
                        Displayer.setSnackBar(view, "You have Completed the deal but  " + Seller + " has not");
                    }else if(thisStore.getDealProgression() == 5){
                        Displayer.setSnackBar(view, Seller + " Has Completed the deal you have not");
                    }
                    return true;
                }
            }
            return false;
        }
        private void getBuyingData(){
            Backendless.Data.of("Messaging").findById(MainLogin.getUser().getProperty("messageID").toString(),
                    new AsyncCallback<Map>() {
                        @Override
                        public void handleResponse(Map foundUser) {
                            if(foundUser.get("buyingDataGson") != null)
                                downloadFile(foundUser.get("buyingDataGson").toString(), mCurrentStore, 0);
                            else
                                launch();
                        }

                        @Override
                        public void handleFault(BackendlessFault fault) {
                            Log.e("TOKEN ISSUE: ", "" + fault.getMessage());
                        }
                    });
        }

        private void getSellingData(){
            String WhereClause = "name = " + "'" + Seller + "'";
            DataQueryBuilder dataQuery = DataQueryBuilder.create();
            dataQuery.setWhereClause(WhereClause);
            Backendless.Data.of("Messaging").find(dataQuery,
                    new AsyncCallback<List<Map>>() {
                        @Override
                        public void handleResponse(List<Map> foundUsers) {
                            if (foundUsers.size() >= 0) {
                                if(foundUsers.get(0).get("sellingDataGson") != null)
                                    downloadFile(foundUsers.get(0).get("sellingDataGson").toString(), mCurrentStore, 1);
                                else
                                    launch();
                            }
                        }

                        @Override
                        public void handleFault(BackendlessFault fault) {
                            Log.e("TOKEN ISSUE: ", "" + fault.getMessage());
                        }
                    });
        }

        //Nested Saving then Launch, save the data to Buyers BuyingData then Sellers SellingData
        private void launch(){
            buyingstoreList.add(mCurrentStore.getStore());
            sellingstoreList.add(mCurrentStore.getStore());
            final String buyingDataGson = new Gson().toJson(buyingstoreList).toString();
            final String sellingDataGson = new Gson().toJson(sellingstoreList).toString();
            String WhereClause = "name = " + "'" + currentUser + "'";
            final DataQueryBuilder dataQuery = DataQueryBuilder.create();
            dataQuery.setWhereClause(WhereClause);

            try {
                String path = mContext.getFilesDir() + "/tempData/" + "buyingDataGson" + ".txt";
                FileOutputStream writer = new FileOutputStream(path);
                writer.write(buyingDataGson.getBytes());
                writer.close();
                final File buyingDataGsonFile = new File(path);
                path = mContext.getFilesDir() + "/tempData/" + "sellingDataGson" + ".txt";
                writer = new FileOutputStream(path);
                writer.write(sellingDataGson.getBytes());
                writer.close();
                final File sellingDataGsonFile = new File(path);
                //Uploading the new Buying Gson File to the Buyer
                Backendless.Files.upload( buyingDataGsonFile, "/profileData/" + MainLogin.getUser().getObjectId() + "/", true, new AsyncCallback<BackendlessFile>(){
                    @Override
                    public void handleResponse(BackendlessFile response) {
                        final String location = response.getFileURL();
                        Log.i("Location ", location);
                        Backendless.Data.of("Messaging").find(dataQuery,
                                new AsyncCallback<List<Map>>() {
                                    @Override
                                    public void handleResponse(List<Map> foundUser) {
                                        foundUser.get(0).put("buyingDataGson", location);
                                        Backendless.Persistence.of("Messaging").save( foundUser.get(0), new AsyncCallback<Map>()
                                        {
                                            @Override
                                            public void handleResponse( Map backendlessUser )
                                            {
                                                buyingDataGsonFile.delete();
                                                MainLogin.reloadUserData();
                                                String Clause = "name = " + "'" + Seller + "'";
                                                final DataQueryBuilder query = DataQueryBuilder.create();
                                                query.setWhereClause(Clause);
                                                //Uploading the new Selling Gson File to the Seller
                                                Backendless.Data.of("Messaging").find(query,
                                                        new AsyncCallback<List<Map>>() {
                                                            @Override
                                                            public void handleResponse(List<Map> foundUser) {
                                                                final Map saveSeller = foundUser.get(0);
                                                                Backendless.Files.upload( sellingDataGsonFile, "/profileData/" + saveSeller.get("userID") + "/", true, new AsyncCallback<BackendlessFile>(){
                                                                    @Override
                                                                    public void handleResponse(BackendlessFile response) {
                                                                        final String location = response.getFileURL();
                                                                        Log.i("Location ", location);
                                                                        saveSeller.put("sellingDataGson", location);
                                                                        Backendless.Persistence.of("Messaging").save( saveSeller, new AsyncCallback<Map>()
                                                                        {

                                                                            @Override
                                                                            public void handleResponse(Map response) {
                                                                                sellingDataGsonFile.delete();
                                                                                launchChatRoom();

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
                                            }

                                            @Override
                                            public void handleFault(BackendlessFault fault) {

                                            }
                                        });
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
            }catch(IOException e){

            }

        }

        private void launchChatRoom(){
            Log.i("LAUNCHING CHAT ROOM", "" + " BUILT DATA");
            int index = HomePage.getIndexofMessage(currentUser, Seller, 1);
            if(index == -1){
                ArrayList<Message> tempMessage = new ArrayList<>();
                HomePage.buyingMessages.add(tempMessage);
                index = HomePage.buyingMessages.size() -1;
            }
            final Intent intent;
            intent = new Intent(mContext, ChatRoomActivity.class);
            intent.putExtra("name", currentUser);
            intent.putExtra("othername", Seller);
            intent.putExtra("type", 100);
            intent.putExtra("index", index);
            mContext.startActivity(intent);
        }
    }
}