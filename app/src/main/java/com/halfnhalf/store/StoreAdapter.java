package com.halfnhalf.store;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.halfnhalf.Profile;

import com.bumptech.glide.Glide;
import com.halfnhalf.R;

import java.util.ArrayList;

public class StoreAdapter extends RecyclerView.Adapter<StoreAdapter.MyViewHolder>{
    private ArrayList<Store> dataSet;
    private GradientDrawable mGradientDrawable;
    private Context mContext;



    public StoreAdapter(Context context, ArrayList<Store> data) {
        this.dataSet = data;
        this.mContext = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(mContext, LayoutInflater.from(mContext).
                inflate(R.layout.list_store, parent, false), mGradientDrawable);
    }


    @Override
    public void onBindViewHolder ( final MyViewHolder holder, final int listPosition){
        Store currentStore = dataSet.get(listPosition);
        holder.bindTo(currentStore);
    }

    @Override
    public int getItemCount () {
        return dataSet.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        //Member Variables for the holder data
        private TextView mTitleText;
        private TextView mInfoText;
        private TextView mAddress;
        private ImageView mStoreImage;
        private Context mContext;
        private Store mCurrentStore;
        private GradientDrawable mGradientDrawable;

        MyViewHolder(Context context, View itemView, GradientDrawable gradientDrawable) {
            super(itemView);

            //Initialize the views
            mTitleText = (TextView)itemView.findViewById(R.id.title);
            mInfoText = (TextView)itemView.findViewById(R.id.deals);
            mAddress = (TextView)itemView.findViewById(R.id.Address);
            mStoreImage = (ImageView)itemView.findViewById(R.id.storeImage);

            mContext = context;
            mGradientDrawable = gradientDrawable;

            //Set the OnClickListener to the whole view
            itemView.setOnClickListener(Profile.myOnClickListener);
        }

        void bindTo(Store currentStore){
            //Populate the textviews with data
            mTitleText.setText(currentStore.getName());
            mAddress.setText(currentStore.getAddress());
            mInfoText.setText(currentStore.getID());

            //Get the current sport
            mCurrentStore = currentStore;



            //Load the images into the ImageView using the Glide library
            Glide.with(mContext).load(currentStore.
                    getImageResource()).placeholder(mGradientDrawable).into(mStoreImage);
        }
    }
}
