package com.halfnhalf.store;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.halfnhalf.R;

import java.util.List;


public class StoreFinderAdapter extends RecyclerView.Adapter<StoreFinderAdapter.MyViewHolder> {

    private List<StoreModel> models;
    private Context mContext;


    public StoreFinderAdapter(List<StoreModel> storeModels, Context context) {
        this.models = storeModels;
        this.mContext = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(mContext)
                .inflate(R.layout.find_store_list, parent, false);

        return new MyViewHolder(mContext, view);
    }

    @Override
    public void onBindViewHolder(final StoreFinderAdapter.MyViewHolder holder, final int position) {
        StoreModel currentStore = models.get(position);
        holder.setData(holder, currentStore);
    }


    @Override
    public int getItemCount() {
        return models.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {


        TextView txtStoreName;
        TextView txtStoreAddr;
        TextView txtStoreDist;
        StoreModel model;
        private Context mContext;


        public MyViewHolder(Context context, View itemView) {
            super(itemView);
            mContext = context;
            this.txtStoreDist = (TextView) itemView.findViewById(R.id.txtStoreDist);
            this.txtStoreName = (TextView) itemView.findViewById(R.id.txtStoreName);
            this.txtStoreAddr = (TextView) itemView.findViewById(R.id.txtStoreAddr);

            itemView.setOnClickListener(FindStore.myOnClickListener);


        }


        public void setData(MyViewHolder holder, StoreModel storeModel) {


            this.model = storeModel;

            holder.txtStoreDist.setText(model.distance + "\n" + model.duration);
            holder.txtStoreName.setText(storeModel.name);
            holder.txtStoreAddr.setText(storeModel.address);


        }

    }
}
