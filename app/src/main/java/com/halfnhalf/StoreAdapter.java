package com.halfnhalf;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import java.util.ArrayList;

public class StoreAdapter extends RecyclerView.Adapter<StoreAdapter.MyViewHolder> {
    private ArrayList<Store> dataSet;
    private GradientDrawable mGradientDrawable;
    private Context mContext;



    StoreAdapter(Context context, ArrayList<Store> data) {
        this.dataSet = data;
        this.mContext = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext())
//                .inflate(R.layout.list_item, parent, false);
//
//        view.setOnClickListener(Profile.myOnClickListener);
//
//        MyViewHolder myViewHolder = new MyViewHolder(view);
//        return myViewHolder;
        return new MyViewHolder(mContext, LayoutInflater.from(mContext).
                inflate(R.layout.list_item, parent, false), mGradientDrawable);

//        return new MyViewHolder(LayoutInflater.from(mContext).inflate(R.layout.list_item, parent, false));
    }


    @Override
    public void onBindViewHolder ( final MyViewHolder holder, final int listPosition){

//        TextView name = holder.Name;
//        TextView deals = holder.Deals;
//
//        name.setText("Name: " + dataSet.get(listPosition).getName());
//        deals.setText("Deal: " + Integer.toString(dataSet.get(listPosition).getDealNum()));
        Store currentStore = dataSet.get(listPosition);
        holder.bindTo(currentStore);
    }

    @Override
    public int getItemCount () {
        return dataSet.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{


        TextView Name;
        TextView Deals;
//
//        public MyViewHolder(View itemView) {
//            super(itemView);
//            this.Name = (TextView) itemView.findViewById(R.id.name);
//            this.Deals = (TextView) itemView.findViewById(R.id.deals);
//        }

        //Member Variables for the holder data
        private TextView mTitleText;
        private TextView mInfoText;
        private ImageView mStoreImage;
        private Context mContext;
        private Store mCurrentStore;
        private GradientDrawable mGradientDrawable;

        /**
         * Constructor for the SportsViewHolder, used in onCreateViewHolder().
         * @param itemView The rootview of the list_item.xml layout file
         */
        MyViewHolder(Context context, View itemView, GradientDrawable gradientDrawable) {
            super(itemView);

            //Initialize the views
            mTitleText = (TextView)itemView.findViewById(R.id.title);
            mInfoText = (TextView)itemView.findViewById(R.id.deals);
            mStoreImage = (ImageView)itemView.findViewById(R.id.storeImage);

            mContext = context;
            mGradientDrawable = gradientDrawable;

            //Set the OnClickListener to the whole view
            itemView.setOnClickListener(this);
        }

        void bindTo(Store currentStore){
            //Populate the textviews with data
            mTitleText.setText(currentStore.getName());
            mInfoText.setText(currentStore.getID());

            //Get the current sport
            mCurrentStore = currentStore;



            //Load the images into the ImageView using the Glide library
            Glide.with(mContext).load(currentStore.
                    getImageResource()).placeholder(mGradientDrawable).into(mStoreImage);
        }

        @Override
        public void onClick(View view) {

//            //Set up the detail intent
//            Intent detailIntent = Sport.starter(mContext, mCurrentSport.getTitle(),
//                    mCurrentSport.getImageResource());
//
//
//            //Start the detail activity
//            mContext.startActivity(detailIntent);
        }
    }
}
