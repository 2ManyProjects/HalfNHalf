package com.halfnhalf.Messaging;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.halfnhalf.R;

import java.util.List;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.MyViewHolder> {
    private Context mContext;
    private List<ConversationModel> data;

    public ConversationAdapter(Context mContext, List<ConversationModel> data) {
        this.mContext = mContext;
        this.data = data;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(mContext, LayoutInflater.from(mContext).
                inflate(R.layout.list_conversations, parent, false));
    }

    @Override
    public void onBindViewHolder (final ConversationAdapter.MyViewHolder holder, final int listPosition){
        ConversationModel currentStore = data.get(listPosition);
        holder.bindTo(currentStore);
    }

    @Override
    public int getItemCount () {
        return data.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        //Member Variables for the holder data
        private TextView mUserName;
        private TextView last;
        private Context mContext;
        private ConversationModel mCurrentConvo;


        MyViewHolder(Context context, View itemView) {
            super(itemView);

            //Initialize the views
            mUserName = (TextView)itemView.findViewById(R.id.Username);
            last = (TextView)itemView.findViewById(R.id.lastMsg);

            mContext = context;

            //Set the OnClickListener to the whole view
            itemView.setOnClickListener(StartChatActivity.myOnClickListener);
        }

        void bindTo(ConversationModel currentconvo){
            //Populate the textviews with data
            mUserName.setText(currentconvo.getName());
            last.setText(currentconvo.getMessages().get(currentconvo.getMessages().size()-1).getText());

            //Get the current sport
            mCurrentConvo = currentconvo;
        }
    }
}
