package com.halfnhalf;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;

public class storeDeals extends AppCompatActivity{

    public static RecyclerView.Adapter Dealadapter;
    private RecyclerView.LayoutManager DeallayoutManager;
    private RecyclerView DealrecyclerView;
    public static ArrayList<Deal> Dealdataset;
    private FloatingActionButton add;
    private Store store;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_deals);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        DealrecyclerView = (RecyclerView) findViewById(R.id.deal_recycler_view);
        DealrecyclerView.setHasFixedSize(true);

        DeallayoutManager = new LinearLayoutManager(this);
        DealrecyclerView.setLayoutManager(DeallayoutManager);
        DealrecyclerView.setItemAnimator(new DefaultItemAnimator());

        Dealdataset = new ArrayList<Deal>();

        Dealadapter = new DealAdapter(storeDeals.this, Dealdataset);
        DealrecyclerView.setAdapter(Dealadapter);

        //Helper class for creating swipe to dismiss and drag and drop functionality
        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback
                (ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT | ItemTouchHelper.DOWN
                        | ItemTouchHelper.UP, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target) {

                //Get the from and to position
                int from = viewHolder.getAdapterPosition();
                int to = target.getAdapterPosition();

                //Swap the items and notify the adapter
                Collections.swap(Dealdataset, from, to);
                Dealadapter.notifyItemMoved(from, to);
                return true;
            }


            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

                //Remove the item from the dataset
                Dealdataset.remove(viewHolder.getAdapterPosition());

                //Notify the adapter
                Dealadapter.notifyItemRemoved(viewHolder.getAdapterPosition());
            }
        });

        add = findViewById(R.id.addDeal);
        add.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                addDeal();
            }
        });

//        Bundle bundle = getIntent().getExtras();
//        String getStore = bundle.getString("Store");
//        Gson gson = new Gson();
//        Type type = new TypeToken<Store>() {
//        }.getType();
//        store = gson.fromJson(getStore, type);
//        Dealdataset = store.getData();
//        Dealadapter.notifyDataSetChanged();

        //Attach the helper to the RecyclerView
        helper.attachToRecyclerView(DealrecyclerView);
    }

    private void addDeal(){
        Deal temp = new Deal("75%", "not on watches", "5");
        //store.addDeal("75%", "not on watches", "5");
        Dealdataset.add(temp);
        Dealadapter.notifyDataSetChanged();
        Displayer.toaster(Integer.toString(Dealadapter.getItemCount()) + " " + temp.getText(), "l", storeDeals.this);
    }
}
