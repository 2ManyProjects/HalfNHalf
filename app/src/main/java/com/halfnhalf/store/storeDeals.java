package com.halfnhalf.store;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.halfnhalf.Deal;
import com.halfnhalf.DealAdapter;
import com.halfnhalf.Displayer;
import com.halfnhalf.Profile;
import com.halfnhalf.R;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;

public class storeDeals extends AppCompatActivity{

    public static RecyclerView.Adapter Dealadapter;
    private RecyclerView.LayoutManager DeallayoutManager;
    private RecyclerView DealrecyclerView;
    public ArrayList<Deal> Dealdataset;
    private FloatingActionButton add;
    private Store store;
    private boolean isChanged = false;
    private ArrayList<String> removed = new ArrayList<>();
    private int toDisplay;
    private Toolbar myToolbar;

    public static View.OnClickListener myOnClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_deals);
        myOnClickListener = new MyOnClickListener(this);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            String getStore = bundle.getString("Store");
            toDisplay = bundle.getInt("type");
            Gson gson = new Gson();
            Type type = new TypeToken<Store>() {
            }.getType();
            store = gson.fromJson(getStore, type);
            if(toDisplay == 1){
                myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
                setSupportActionBar(myToolbar);
            }
        }else{
            Log.e("Error retreiving store data", "");
            finish();
        }

        DealrecyclerView = (RecyclerView) findViewById(R.id.deal_recycler_view);
        DealrecyclerView.setHasFixedSize(true);

        DeallayoutManager = new LinearLayoutManager(this);
        DealrecyclerView.setLayoutManager(DeallayoutManager);
        DealrecyclerView.setItemAnimator(new DefaultItemAnimator());

        Dealdataset = new ArrayList<Deal>();

        if(toDisplay == 1) {
            Dealadapter = new DealAdapter(storeDeals.this, Dealdataset, 0);
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
                    isChanged = true;
                    return true;
                }


                @Override
                public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

                    removed.add(Dealdataset.get(viewHolder.getAdapterPosition()).getId());
                    //Remove the item from the dataset
                    Dealdataset.remove(viewHolder.getAdapterPosition());

                    //Notify the adapter
                    Dealadapter.notifyItemRemoved(viewHolder.getAdapterPosition());
                    isChanged = true;
                }
            });
            add = findViewById(R.id.addDeal);
            add.show();
            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    addDeal().show();
                }
            });
            //Attach the helper to the RecyclerView
            helper.attachToRecyclerView(DealrecyclerView);
        }else{

            Dealadapter = new DealAdapter(storeDeals.this, Dealdataset, 1);
            DealrecyclerView.setAdapter(Dealadapter);

            add = findViewById(R.id.addDeal);
            add.hide();
        }
        init();
    }

    private void init(){
        for(int i = 0; i < store.getData().size(); i++){
            Dealdataset.add(store.getData().get(i));
            Dealadapter.notifyDataSetChanged();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.save) {

                Intent intent = new Intent(this, Profile.class);
                String id = store.getID();
                String dealData = new Gson().toJson(Dealdataset);
                String isremoved = new Gson().toJson(removed);
                intent.putExtra("isChanged", isChanged);
                intent.putExtra("id", id);
                intent.putExtra("Deals", dealData);
                intent.putExtra("removedID", isremoved);
                setResult(RESULT_OK,intent);
                finish();
                return true;
        }else{
            return super.onOptionsItemSelected(item);
        }
    }
    private class MyOnClickListener implements View.OnClickListener {

        private final Context context;

        private MyOnClickListener(Context context) {
            this.context = context;
        }

        @Override
        public void onClick(View v) {
            click(v);
            return;
        }
        private void click(View v){
            int selectedItemPosition = DealrecyclerView.getChildPosition(v);
            Deal mCurrentDeal = Dealdataset.get(selectedItemPosition);
            Displayer.toaster(mCurrentDeal.getId(), "l", context);
            if(toDisplay == 1) {
                editDeal(mCurrentDeal, selectedItemPosition).show();
            }
        }
    }
    private Dialog addDeal(){
        final EditText text;
        final TextView barVal;
        final SeekBar discount;
        final NumberPicker amnt;

        AlertDialog.Builder builder = new AlertDialog.Builder(storeDeals.this);
        LayoutInflater inflater = storeDeals.this.getLayoutInflater();
        View view = inflater.inflate(R.layout.activity_add_deal, null);
        builder.setView(view);

        text = view.findViewById(R.id.text);
        discount = view.findViewById(R.id.seekBar);
        amnt = view.findViewById(R.id.numberPicker);
        barVal = view.findViewById(R.id.barVal);

        amnt.setMinValue(1);
        amnt.setMaxValue(20);
        barVal.setText("50%");

        discount.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                barVal.setText(Integer.toString(seekBar.getProgress()) + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                if(text.getText().length() == 0){
                    text.setText("No Limitations");
                }
                Deal temp = new Deal(
                        Integer.toString(discount.getProgress()),
                        text.getText().toString(),
                        Integer.toString(amnt.getValue()));
                Dealdataset.add(temp);

                int addItemAtListPosition = Dealdataset.size();
                Dealadapter.notifyItemInserted(addItemAtListPosition);
                isChanged = true;
            }
        })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        return builder.create();
    }

    private Dialog editDeal(Deal deal, int index){
        final EditText text;
        final TextView barVal;
        final SeekBar discount;
        final NumberPicker amnt;
        final String warn = deal.getText();
        final String dealID = deal.getId();
        final int i = index;

        AlertDialog.Builder builder = new AlertDialog.Builder(storeDeals.this);
        LayoutInflater inflater = storeDeals.this.getLayoutInflater();
        View view = inflater.inflate(R.layout.activity_add_deal, null);
        builder.setView(view);

        text = view.findViewById(R.id.text);
        discount = view.findViewById(R.id.seekBar);
        amnt = view.findViewById(R.id.numberPicker);
        barVal = view.findViewById(R.id.barVal);

        text.setHint(deal.getText());
        amnt.setMinValue(1);
        amnt.setMaxValue(20);
        barVal.setText(deal.getRate());
        amnt.setValue(Integer.parseInt(deal.getAmnt()));
        discount.setProgress(Integer.parseInt(deal.getRate()));
        discount.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                barVal.setText(Integer.toString(seekBar.getProgress()) + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                if(text.getText().length() == 0){
                    text.setText(warn);
                }
                Deal temp = new Deal(
                        Integer.toString(discount.getProgress()),
                        text.getText().toString(),
                        Integer.toString(amnt.getValue()),
                        dealID);
                Dealdataset.set(i, temp);

                Dealadapter.notifyItemChanged(i);
                isChanged = true;
            }
        })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        return builder.create();
    }
}
