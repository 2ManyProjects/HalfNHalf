package com.halfnhalf;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
        private Context mContext;
        private storeSummery mCurrentStore;


        MyViewHolder(Context context, View itemView) {
            super(itemView);

            //Initialize the views
            mUserName = (TextView)itemView.findViewById(R.id.UserName);
            mstoreName = (TextView)itemView.findViewById(R.id.storeName);
            mdeals = (TextView)itemView.findViewById(R.id.deals);

            mContext = context;

            //Set the OnClickListener to the whole view
            itemView.setOnClickListener(HomePage.myOnClickListener);
        }

        void bindTo(storeSummery currentStore){
            //Populate the textviews with data
            mUserName.setText(currentStore.getUserName());
            mstoreName.setText(currentStore.getName());
            mdeals.setText(currentStore.getDeals()[0] + "   " + currentStore.getDeals()[1] + "   " + currentStore.getDeals()[2]);

            //Get the current sport
            mCurrentStore = currentStore;
        }
    }
}