package com.halfnhalf;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.halfnhalf.store.storeDeals;

import java.util.ArrayList;

public class DealAdapter extends RecyclerView.Adapter<DealAdapter.MyViewHolder> {
    private ArrayList<Deal> dataSet;
    private Context mContext;
    private int type = 0;

    public DealAdapter(Context context, ArrayList<Deal> data, int t) {
        this.dataSet = data;
        this.mContext = context;
        this.type = t;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(mContext, LayoutInflater.from(mContext).
                inflate(R.layout.list_deal, parent, false), this.type);
    }

    @Override
    public void onBindViewHolder (final DealAdapter.MyViewHolder holder, final int listPosition){
        Deal currentDeal = dataSet.get(listPosition);
        holder.bindTo(currentDeal);
    }

    @Override
    public int getItemCount () {
        return dataSet.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        //Member Variables for the holder data
        private TextView mWarning;
        private TextView mRate;
        private TextView mAmnt;
        private Context mContext;
        private Deal mCurrentDeal;


        MyViewHolder(Context context, View itemView, int type) {
            super(itemView);

            //Initialize the views
            mWarning = (TextView)itemView.findViewById(R.id.warning);
            mRate = (TextView)itemView.findViewById(R.id.rate);
            mAmnt = (TextView)itemView.findViewById(R.id.Amnt);

            mContext = context;
            //Set the OnClickListener to the whole view
            itemView.setOnClickListener(storeDeals.myOnClickListener);
        }

        void bindTo(Deal currentDeal){
            //Populate the textviews with data
            mWarning.setText(currentDeal.getText());
            mRate.setText(currentDeal.getRate() + "%");
            mAmnt.setText(currentDeal.getTotalAmnt());

            //Get the current sport
            mCurrentDeal = currentDeal;
        }
    }
}
