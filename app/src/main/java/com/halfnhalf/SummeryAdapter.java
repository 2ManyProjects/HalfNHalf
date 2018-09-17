package com.halfnhalf;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import com.halfnhalf.Messaging.ChatRoomActivity;
import com.halfnhalf.Messaging.Message;
import com.halfnhalf.store.storeSummery;

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


        MyViewHolder(Context context, View itemView) {
            super(itemView);

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
            final storeSummery sum = store;
            final Context mContext = c;
            final String sellingUser = seller;
            final String currentUser = Backendless.UserService.CurrentUser().getProperty("name").toString();
            String WhereClause = "name = " + "'" + seller + "'";
            DataQueryBuilder dataQuery = DataQueryBuilder.create();
            dataQuery.setWhereClause(WhereClause);
            Backendless.Data.of("Messages").find(dataQuery,
                    new AsyncCallback<List<Map>>() {
                        @Override
                        public void handleResponse(List<Map> foundUsers) {
                            if (foundUsers.size() >= 0) {
                                String sellingData = "";
                                if (foundUsers.get(0).get("sellingData") != null) {
                                    sellingData = foundUsers.get(0).get("sellingData").toString();
                                    //Checking for any Open Deals
                                    if(sellingData.contains("0" + "#" + currentUser + "#" + sellingUser + "#") ||
                                            sellingData.contains("1" + "#" + currentUser + "#" + sellingUser + "#")){
                                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext)
                                                .setCancelable(false)
                                                .setTitle("Deal in Progess")
                                                .setMessage("Please Complete your current deal with: " + sellingUser)
                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.cancel();
                                                    }
                                                });
                                        AlertDialog ok = builder.create();
                                        ok.show();
                                    }else{
                                        launch(sum);
                                    }
                                } else {
                                    launch(sum);
                                }
                            }
                        }

                        @Override
                        public void handleFault(BackendlessFault fault) {
                            Log.e("TOKEN ISSUE: ", "" + fault.getMessage());
                        }
                    });
        }

        //Nested Saving then Launch, save the data to Buyers BuyingData then Sellers SellingData
        private void launch(storeSummery store){
            final String rec = store.getUserName();
            final String send = Backendless.UserService.CurrentUser().getProperty("name").toString();

            final storeSummery saveStore = store;
            String WhereClause = "name = " + "'" + send + "'";
            DataQueryBuilder dataQuery = DataQueryBuilder.create();
            dataQuery.setWhereClause(WhereClause);
            Backendless.Data.of("Messages").find(dataQuery,
                    new AsyncCallback<List<Map>>() {
                        @Override
                        public void handleResponse(List<Map> foundUsers) {
                            if (foundUsers.size() >= 0) {
                                String save = "";
                                if(foundUsers.get(0).get("buyingData") != null)
                                    save += foundUsers.get(0).get("buyingData").toString();
                                save += saveStore.getProfSnapshot();
                                foundUsers.get(0).put("buyingData", save);
                                Backendless.Persistence.of("Messages").save(foundUsers.get(0), new AsyncCallback<Map>() {
                                    @Override
                                    public void handleResponse(Map response) {

                                        String WhereClause = "name = " + "'" + rec + "'";
                                        DataQueryBuilder dataQuery = DataQueryBuilder.create();
                                        dataQuery.setWhereClause(WhereClause);
                                        Backendless.Data.of("Messages").find(dataQuery,
                                                new AsyncCallback<List<Map>>() {
                                                    @Override
                                                    public void handleResponse(List<Map> foundUsers) {
                                                        if (foundUsers.size() >= 0) {
                                                            String save = "";
                                                            if(foundUsers.get(0).get("sellingData") != null)
                                                                save += foundUsers.get(0).get("sellingData").toString();
                                                            save += saveStore.getProfSnapshot();
                                                            foundUsers.get(0).put("sellingData", save);
                                                            Backendless.Persistence.of("Messages").save(foundUsers.get(0), new AsyncCallback<Map>() {
                                                                @Override
                                                                public void handleResponse(Map response) {
                                                                    int index = HomePage.getIndexofMessage(send, rec, 1);
                                                                    if(index == -1){
                                                                        ArrayList<Message> tempMessage = new ArrayList<>();
                                                                        HomePage.buyingMessages.add(tempMessage);
                                                                        index = HomePage.buyingMessages.size() -1;
                                                                    }
                                                                    final Intent intent;
                                                                    intent = new Intent(mContext, ChatRoomActivity.class);
                                                                    intent.putExtra("name", send);
                                                                    intent.putExtra("othername", rec);
                                                                    intent.putExtra("type", 100);
                                                                    intent.putExtra("index", index);
                                                                    mContext.startActivity(intent);
                                                                }

                                                                @Override
                                                                public void handleFault(BackendlessFault fault) {
                                                                    // an error has occurred, the error code can be retrieved with fault.getCode()
                                                                }
                                                            });
                                                        }
                                                    }

                                                    @Override
                                                    public void handleFault(BackendlessFault fault) {
                                                        Log.e("TOKEN ISSUE: ", "" + fault.getMessage());
                                                    }
                                                });

                                    }

                                    @Override
                                    public void handleFault(BackendlessFault fault) {
                                        // an error has occurred, the error code can be retrieved with fault.getCode()
                                    }
                                });
                            }
                        }

                        @Override
                        public void handleFault(BackendlessFault fault) {
                            Log.e("TOKEN ISSUE: ", "" + fault.getMessage());
                        }
                    });

        }
    }
}