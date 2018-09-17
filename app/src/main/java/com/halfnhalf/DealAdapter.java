package com.halfnhalf;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.halfnhalf.Messaging.dealSelection;
import com.halfnhalf.store.storeDeals;

import java.util.ArrayList;
import java.util.Calendar;

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
        private TextView mCurrentAmnt;
        private TextView mAtCost;
        private TextView mReoccur;
        private TextView mDate;
        private Context mContext;
        private FloatingActionButton selected, deselected;
        private Deal mCurrentDeal;
        private NumberPicker selectedVal;
        private int type;


        MyViewHolder(Context context, View itemView, int type) {
            super(itemView);
            this.type = type;
            //Initialize the views
            mWarning = (TextView)itemView.findViewById(R.id.warning);
            mRate = (TextView)itemView.findViewById(R.id.rate);
            mAmnt = (TextView)itemView.findViewById(R.id.Amnt);
            mCurrentAmnt = (TextView)itemView.findViewById(R.id.currentAmnt);
            mAtCost = (TextView)itemView.findViewById(R.id.atCost);
            mReoccur = (TextView)itemView.findViewById(R.id.reoccur);
            mDate = (TextView)itemView.findViewById(R.id.date);
            deselected = (FloatingActionButton) itemView.findViewById(R.id.deSel);
            selected = (FloatingActionButton) itemView.findViewById(R.id.Sel);
            selectedVal = (NumberPicker) itemView.findViewById(R.id.selectedVal);

            mContext = context;
            //Set the OnClickListener to the whole view
            if(type == 0) {
                itemView.setOnClickListener(storeDeals.myOnClickListener);
            }else{
                itemView.setOnClickListener(dealSelection.myOnClickListener);
            }
        }

        void bindTo(Deal currentDeal){
            final Deal deal = currentDeal;
            //Populate the textviews with data
            mWarning.setText(deal.getText());
            mRate.setText(deal.getRate() + "%");
            mAmnt.setText(deal.getTotalAmnt());
            selected.hide();
            deselected.hide();
            if(type == 0 || type == 2){
                selectedVal.setVisibility(View.GONE);
            }
            if(deal.getAtCost()){
                mAtCost.setText("At Cost +");
            }else{
                mAtCost.setText("Reg Discount");
            }
            if(!deal.getLimit()){
                mAmnt.setText("50");
                mCurrentAmnt.setText("50");
                mReoccur.setVisibility(View.GONE);
                mDate.setVisibility(View.GONE);
                selectedVal.setMaxValue(50);
            }else{
                mAmnt.setText(deal.getTotalAmnt());
                mCurrentAmnt.setText(deal.getCurrentAmnt());
                if(deal.getReoccuring()) {
                    mReoccur.setVisibility(View.VISIBLE);
                    mDate.setVisibility(View.VISIBLE);
                    mDate.setText(deal.getResetDate().get(Calendar.YEAR) + "-" +
                            deal.getResetDate().get(Calendar.MONTH) + "-" +
                            deal.getResetDate().get(Calendar.DAY_OF_MONTH));
                }
                selectedVal.setMaxValue(Integer.parseInt(deal.getCurrentAmnt()));
            }

            if(type > 0){
                if(deal.getSelected() == 1){
                    deselected.show();
                    selected.hide();
                    selectedVal.setVisibility(View.VISIBLE);
                }else{
                    selected.show();
                    deselected.hide();
                    selectedVal.setVisibility(View.GONE);
                }
            }

            if(type == 1){
                selectedVal.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                    @Override
                    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                        deal.setSelectedAmnt(newVal);
                    }
                });

                selected.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deal.setSelected(1);
                        deal.setSelectedAmnt(0);
                        selected.hide();
                        selectedVal.setVisibility(View.VISIBLE);
                        deselected.show();
                    }
                });
                deselected.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deal.setSelected(0);
                        deal.setSelectedAmnt(0);
                        selectedVal.setVisibility(View.GONE);
                        Log.e("CURRENTAMNT: ", "" + deal.getCurrentAmnt() );
                        deselected.hide();
                        selected.show();
                    }
                });
            }

            //Get the current sport
            mCurrentDeal = deal;
        }
    }
}
