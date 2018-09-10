package com.halfnhalf;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.backendless.Backendless;
import com.halfnhalf.Messaging.ChatRoomActivity;
import com.halfnhalf.Messaging.Message;
import com.halfnhalf.store.storeSummery;

import java.util.ArrayList;

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
                    String rec = currentStore.getUserName();
                    String send = Backendless.UserService.CurrentUser().getProperty("name").toString();
                    int index = HomePage.getIndexofMessage(send, rec);
                    if(index == -1){
                        ArrayList<Message> tempMessage = new ArrayList<>();
                        HomePage.Messages.add(tempMessage);
                        index = HomePage.Messages.size() -1;
                    }
                    final Intent intent;
                    intent = new Intent(mContext, ChatRoomActivity.class);
                    String name = Backendless.UserService.CurrentUser().getProperty("name").toString();
                    intent.putExtra("name", send);
                    intent.putExtra("othername", rec);
                    intent.putExtra("rawMessage", HomePage.allMsgs);
                    intent.putExtra("index", index);
                    mContext.startActivity(intent);

                }
            });
            //Get the current sport
            mCurrentStore = currentStore;
        }
    }
}