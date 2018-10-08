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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.Spinner;
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
import java.util.Calendar;
import java.util.Collections;
import java.util.UUID;

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
        }else if (item.getItemId() == R.id.back) {
            onBackPressed();
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
        final TextView barVal, addOn, calendarAddOn;
        final SeekBar discount;
        final NumberPicker totalAmnt, currentAmnt;
        final Spinner dealType, period;
        final CheckBox limit, reoccuring;
        final DatePicker date;
        final String [] dealTypes = {"Discount", "At Cost"};
        final String [] periods = {"Weekly", "BiWeekly", "Monthly", "BiMonthly", "Quarterly", "Half Year", "Yearly", "BiYearly", "TriYearly"};
        final ArrayAdapter<String> dealTypeadapter = new ArrayAdapter<String>(storeDeals.this,
                android.R.layout.simple_spinner_item, dealTypes);
        final ArrayAdapter<String> periodsadapter = new ArrayAdapter<String>(storeDeals.this,
                android.R.layout.simple_spinner_item, periods);
        final String identity = UUID.randomUUID().toString();
        AlertDialog.Builder builder = new AlertDialog.Builder(storeDeals.this);
        LayoutInflater inflater = storeDeals.this.getLayoutInflater();
        View view = inflater.inflate(R.layout.activity_add_deal, null);
        builder.setView(view);

        text = view.findViewById(R.id.text);
        discount = view.findViewById(R.id.seekBar);
        totalAmnt = view.findViewById(R.id.totalAmnt);
        currentAmnt = view.findViewById(R.id.currentAmnt);
        barVal = view.findViewById(R.id.barVal);
        addOn = view.findViewById(R.id.addOn);
        calendarAddOn = view.findViewById(R.id.CalendarTitle);;
        dealType = view.findViewById(R.id.dealType);
        period = view.findViewById(R.id.period);
        limit = view.findViewById(R.id.Nolimit);
        reoccuring = view.findViewById(R.id.isReoccuring);
        date = view.findViewById(R.id.datePicker);

        dealTypeadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dealType.setAdapter(dealTypeadapter);

        periodsadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        period.setAdapter(periodsadapter);
        period.setVisibility(View.GONE);
        addOn.setVisibility(View.GONE);
        date.setVisibility(View.GONE);
        calendarAddOn.setVisibility(View.GONE);
        totalAmnt.setVisibility(View.GONE);
        currentAmnt.setVisibility(View.GONE);
        reoccuring.setVisibility(View.GONE);
        totalAmnt.setMinValue(1);
        totalAmnt.setMaxValue(40);
        currentAmnt.setMinValue(1);
        currentAmnt.setMaxValue(1);
        barVal.setText("50%");

        Calendar calendar = Calendar.getInstance();
        date.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth){

            }
        });

        dealType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 1){
                    addOn.setVisibility(View.VISIBLE);
                    discount.setProgress(5);
                }else{
                    addOn.setVisibility(View.GONE);
                    discount.setProgress(50);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        totalAmnt.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                currentAmnt.setMaxValue(newVal);
                currentAmnt.setValue(newVal);
            }
        });

        currentAmnt.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                if(newVal > totalAmnt.getValue()){
                    currentAmnt.setValue(totalAmnt.getValue());
                }
            }
        });

        limit.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    totalAmnt.setVisibility(View.VISIBLE);
                    currentAmnt.setVisibility(View.VISIBLE);
                    reoccuring.setVisibility(View.VISIBLE);
                }else{
                    totalAmnt.setVisibility(View.GONE);
                    currentAmnt.setVisibility(View.GONE);
                    reoccuring.setVisibility(View.GONE);
                }
            }
        });

        reoccuring.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    period.setVisibility(View.VISIBLE);
                    date.setVisibility(View.VISIBLE);
                    calendarAddOn.setVisibility(View.VISIBLE);
                }else{
                    period.setVisibility(View.GONE);
                    date.setVisibility(View.GONE);
                    calendarAddOn.setVisibility(View.GONE);
                }
            }
        });

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
                String totalAmount = "0";
                String currentAmount = "0";
                String periodstr = "0";
                String year = "0";
                String month = "0";
                String day = "0";
                boolean isAtCost = false;
                boolean reoccur;
                if(text.getText().length() == 0){
                    text.setText("No Limitations");
                }
                if(limit.isChecked()){
                    totalAmount = Integer.toString(totalAmnt.getValue());
                    currentAmount = Integer.toString(currentAmnt.getValue());
                    reoccur = reoccuring.isChecked();
                }else{
                    reoccur = false;
                    totalAmount = "50";
                    currentAmount = "50";
                }
                if(dealType.getSelectedItemPosition() == 1)
                    isAtCost = true;
                if(reoccuring.isChecked()){
                    periodstr = Integer.toString(period.getSelectedItemPosition());
                    year = Integer.toString(date.getYear());
                    month = Integer.toString(date.getMonth());
                    day = Integer.toString(date.getDayOfMonth());
                }else{
                    periodstr = "11";
                }
                    Log.e("CALENDAR", "YEAR " + year + " MONTH " + month + " DAY " + day);

                    Deal temp = new Deal(
                            Integer.toString(discount.getProgress()),
                            text.getText().toString(),
                            totalAmount,
                            currentAmount,
                            isAtCost,
                            reoccur,
                            periodstr,
                            year,
                            month,
                            day,
                            identity);
                Dealdataset.add(temp);

                int addItemAtListPosition = Dealdataset.size();
                Dealadapter.notifyItemInserted(addItemAtListPosition);
                isChanged = true;
                Log.e("SPINNER VAL: ", "" + dealType.getSelectedItemPosition());
            }
        })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        return builder.create();
    }

    private Dialog editDeal(Deal deal, int index){
        final String warn = deal.getText();
        final String dealID = deal.getId();
        final int i = index;
        final EditText text;
        final TextView barVal, addOn, calendarAddOn;
        final SeekBar discount;
        final NumberPicker totalAmnt, currentAmnt;
        final Spinner dealType, period;
        final CheckBox limit, reoccuring;
        final DatePicker date;
        final String [] dealTypes = {"Discount", "At Cost"};
        final String [] periods = {"Weekly", "BiWeekly", "Monthly", "BiMonthly", "Quarterly", "Half Year", "Yearly", "BiYearly", "TriYearly"};
        final ArrayAdapter<String> dealTypeadapter = new ArrayAdapter<String>(storeDeals.this,
                android.R.layout.simple_spinner_item, dealTypes);
        final ArrayAdapter<String> periodsadapter = new ArrayAdapter<String>(storeDeals.this,
                android.R.layout.simple_spinner_item, periods);

        AlertDialog.Builder builder = new AlertDialog.Builder(storeDeals.this);
        LayoutInflater inflater = storeDeals.this.getLayoutInflater();
        View view = inflater.inflate(R.layout.activity_add_deal, null);
        builder.setView(view);

        text = view.findViewById(R.id.text);
        discount = view.findViewById(R.id.seekBar);
        totalAmnt = view.findViewById(R.id.totalAmnt);
        currentAmnt = view.findViewById(R.id.currentAmnt);
        barVal = view.findViewById(R.id.barVal);
        addOn = view.findViewById(R.id.addOn);
        calendarAddOn = view.findViewById(R.id.CalendarTitle);
        dealType = view.findViewById(R.id.dealType);
        period = view.findViewById(R.id.period);
        limit = view.findViewById(R.id.Nolimit);
        reoccuring = view.findViewById(R.id.isReoccuring);
        date = view.findViewById(R.id.datePicker);

        dealTypeadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dealType.setAdapter(dealTypeadapter);

        periodsadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        period.setAdapter(periodsadapter);
        period.setVisibility(View.GONE);
        addOn.setVisibility(View.GONE);
        date.setVisibility(View.GONE);
        totalAmnt.setVisibility(View.GONE);
        currentAmnt.setVisibility(View.GONE);
        reoccuring.setVisibility(View.GONE);
        calendarAddOn.setVisibility(View.GONE);
        totalAmnt.setMinValue(1);
        totalAmnt.setMaxValue(40);
        currentAmnt.setMinValue(1);
        currentAmnt.setMaxValue(Integer.parseInt(deal.getTotalAmnt()));
        barVal.setText(deal.getRate() + "%");

        if(deal.getAtCost()){
            dealType.setSelection(1);
        }else{
            dealType.setSelection(0);
        }

        Calendar calendar = Calendar.getInstance();
        date.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

            }
        });

        limit.setChecked(deal.getLimit());
        if(limit.isChecked()){
            totalAmnt.setVisibility(View.VISIBLE);
            currentAmnt.setVisibility(View.VISIBLE);
            reoccuring.setVisibility(View.VISIBLE);
            totalAmnt.setValue(Integer.parseInt(deal.getTotalAmnt()));
            currentAmnt.setValue(Integer.parseInt(deal.getCurrentAmnt()));
            reoccuring.setChecked(deal.getReoccuring());
            if(reoccuring.isChecked()){
                period.setVisibility(View.VISIBLE);
                date.setVisibility(View.VISIBLE);
                calendarAddOn.setVisibility(View.VISIBLE);
                period.setSelection(Integer.parseInt(deal.getPeriodVal()));
                Calendar temp = deal.getResetDate();
                date.init(temp.get(Calendar.YEAR), temp.get(Calendar.MONTH), temp.get(Calendar.DAY_OF_MONTH), new DatePicker.OnDateChangedListener() {
                    @Override
                    public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                    }
                });
            }
        }

        dealType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 1){
                    addOn.setVisibility(View.VISIBLE);
                    discount.setProgress(5);
                }else{
                    addOn.setVisibility(View.GONE);
                    discount.setProgress(50);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        totalAmnt.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                currentAmnt.setMaxValue(newVal);
                currentAmnt.setValue(newVal);
            }
        });

        currentAmnt.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                if(newVal > totalAmnt.getValue()){
                    currentAmnt.setValue(totalAmnt.getValue());
                }
            }
        });

        limit.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    totalAmnt.setVisibility(View.VISIBLE);
                    currentAmnt.setVisibility(View.VISIBLE);
                    reoccuring.setVisibility(View.VISIBLE);
                }else{
                    totalAmnt.setVisibility(View.GONE);
                    currentAmnt.setVisibility(View.GONE);
                    reoccuring.setVisibility(View.GONE);
                }
            }
        });

        reoccuring.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    period.setVisibility(View.VISIBLE);
                    date.setVisibility(View.VISIBLE);
                    calendarAddOn.setVisibility(View.VISIBLE);
                }else{
                    period.setVisibility(View.GONE);
                    date.setVisibility(View.GONE);
                    calendarAddOn.setVisibility(View.GONE);
                }
            }
        });

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
                String totalAmount = "0";
                String currentAmount = "0";
                String periodstr = "0";
                Boolean isAtCost = false;
                String year = "0";
                String month = "0";
                String day = "0";
                boolean reoccur;
                if(text.getText().length() == 0){
                    text.setText(warn);
                }
                if(limit.isChecked()){
                    totalAmount = Integer.toString(totalAmnt.getValue());
                    currentAmount = Integer.toString(currentAmnt.getValue());
                    reoccur = reoccuring.isChecked();
                }else{
                    reoccur = false;
                    totalAmount = "50";
                    currentAmount = "50";
                }
                if(dealType.getSelectedItemPosition() == 1)
                    isAtCost = true;
                if(reoccuring.isChecked()){
                    periodstr = Integer.toString(period.getSelectedItemPosition());
                    year = Integer.toString(date.getYear());
                    month = Integer.toString(date.getMonth());
                    day = Integer.toString(date.getDayOfMonth());
                }else{
                    periodstr = "11";
                }
                Deal temp = new Deal(
                        Integer.toString(discount.getProgress()),
                        text.getText().toString(),
                        totalAmount,
                        currentAmount,
                        isAtCost,
                        reoccur,
                        periodstr,
                        year,
                        month,
                        day,
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
