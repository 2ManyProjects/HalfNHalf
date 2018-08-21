package com.halfnhalf;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;

public class Profile extends AppCompatActivity {

    private String email;
    private String username;
    private int numStores = 0;
    private String userData;
    private String [] arrayData;
    private int versionNum;

    public static RecyclerView.Adapter Profileadapter;
    private RecyclerView.LayoutManager ProfilelayoutManager;
    private static RecyclerView ProfilerecyclerView;
    public static ArrayList<Store> Profiledataset;
    private TypedArray ImageResources;
    private FloatingActionButton add;
    static View.OnClickListener myOnClickListener;
   // private ArrayList<Deal> dealData;


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if(resultCode == RESULT_OK) {
                //dealData = new ArrayList<>();
                Bundle bundle = data.getExtras();
                String ID = bundle.getString("id");
                String getDeals = bundle.getString("Deals");
                boolean isChanged = bundle.getBoolean("isChanged");
                Gson gson = new Gson();
                Type type = new TypeToken<ArrayList<Deal>>() {
                }.getType();
                ArrayList<Deal> dealData = gson.fromJson(getDeals, type);
                if(isChanged) {
                    String getRemoved = bundle.getString("removedID");
                    Gson mgson = new Gson();
                    Type mtype = new TypeToken<ArrayList<String>>() {
                    }.getType();
                    ArrayList<String> isRemoved = mgson.fromJson(getRemoved, mtype);
                    fillStore(dealData, ID, isRemoved);
                }
            }
        }
    }

    private void fillStore(ArrayList<Deal> dealData, String id, ArrayList<String> removed){
        int index = -1;
        for(int i = 0; i < Profiledataset.size(); i++){
            if(Profiledataset.get(i).getID().equals(id)){
                index = i;
                break;
            }
        }
//        Displayer.toaster("Deals: " + Integer.toString(dealData.size()) + " ID: " + id, "h", this);
        if(index == -1){
            return;
        }else{
            for(int i = 0; i < Profiledataset.get(index).getData().size(); i++){
                if(removed.contains(Profiledataset.get(index).getData(i).getId())) {
                    Profiledataset.get(index).getData().remove(i);
                }
            }
            for(int i = 0; i < dealData.size(); i++){
                Profiledataset.get(index).changeDeal(i, dealData.get(i).getRate(), dealData.get(i).getText(), dealData.get(i).getAmnt(), dealData.get(i).getId());
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        myOnClickListener = new MyOnClickListener(this);

        ProfilerecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        ProfilerecyclerView.setHasFixedSize(true);

        ProfilelayoutManager = new LinearLayoutManager(this);
        ProfilerecyclerView.setLayoutManager(ProfilelayoutManager);
        ProfilerecyclerView.setItemAnimator(new DefaultItemAnimator());

        Profiledataset = new ArrayList<Store>();

        Profileadapter = new StoreAdapter(Profile.this, Profiledataset);
        ProfilerecyclerView.setAdapter(Profileadapter);

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
                Collections.swap(Profiledataset, from, to);
                Profileadapter.notifyItemMoved(from, to);
                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

                //Remove the item from the dataset
                Profiledataset.remove(viewHolder.getAdapterPosition());

                //Notify the adapter
                Profileadapter.notifyItemRemoved(viewHolder.getAdapterPosition());
            }
        });

        add = findViewById(R.id.addStore);
        add.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                addStore("asasfasf" + Integer.toString(numStores), "Wally's Mart");
                //testInterupt().show();
            }
        });
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            this.userData = bundle.getString("String");
            Displayer.toaster(userData, "l", this);
            init();
        }else{
            Displayer.toaster("Error getting user Data", "l", this);
            finish();
        }
        //Attach the helper to the RecyclerView
        helper.attachToRecyclerView(ProfilerecyclerView);
    }

    public String getUserData() {
        return userData;
    }

    public void init(){
        arrayData = userData.split("#");
        username = arrayData[0];
        email = arrayData[1];
        versionNum = Integer.parseInt(arrayData[2]);
        numStores = Integer.parseInt(arrayData[3]);
        populateProfile();
    }

    public String createProfile()
    {
        String temp = username + "#" + email + "#" + Integer.toString(versionNum++) + "#" + Integer.toString(numStores) + "#";
        for(int i = 0; i < Profiledataset.size(); i++){
            temp.concat(Profiledataset.get(i).getID() + "#" + Profiledataset.get(i).getName() + "#");
            for(int x = 0; x < Profiledataset.get(i).getDealNum(); x++){
                String rate = Profiledataset.get(i).storeDeals.get(x).getRate() + "#";
                String text = Profiledataset.get(i).storeDeals.get(x).getText() + "#";
                String amnt = Profiledataset.get(i).storeDeals.get(x).getAmnt() + "#";
                temp.concat(rate.concat(text.concat(amnt)));
            }
        }
        return temp;
    }

    public void addStore(String ID, String Name) {
        if (Profiledataset.size() < 1) {
            Store temp = new Store(ID, Name, ImageResources.getResourceId((numStores % 10),0));
            Profiledataset.add(temp);
            Profileadapter.notifyDataSetChanged();
            numStores += 1;
            return;
        } else {
            boolean found = false;
            for (int i = 0; i < Profiledataset.size(); i++) {
                if (Profiledataset.get(i).getID().equals(ID)) {
                    found = true;
                    break;
                }
            }
            if (found) {
                return;
            } else {
                Store temp = new Store(ID, Name, ImageResources.getResourceId((numStores % 10),0));
                Profiledataset.add(temp);
                Profileadapter.notifyDataSetChanged();
                numStores += 1;
                return;
            }
        }
    }

    private void populateProfile(){
        ImageResources = getResources().obtainTypedArray(R.array.images);
        int num = 0;
        for(int i = 4; i < arrayData.length; i++){
            int counter = 1;//Accomodate STOREID and NAME
            Store temp = new Store(arrayData[i], arrayData[i+1], ImageResources.getResourceId(num,0));
            num++;
            i++;
            for(int x = i + 2; x < (i + 2) + Integer.parseInt(arrayData[i + 1]) * 3; x++){
                counter += 3;
                temp.addDeal(arrayData[x], arrayData[x + 1], arrayData[x+2]);
                x += 2;
            }
            Profiledataset.add(temp);
            i += counter;
        }

        //Recycle the typed array
//        ImageResources.recycle();

        //Notify the adapter of the change
        Profileadapter.notifyDataSetChanged();
    }

    private class MyOnClickListener implements View.OnClickListener {

        private final Context context;

        private MyOnClickListener(Context context) {
            this.context = context;
        }

        @Override
        public void onClick(View v) {
            launch(v);
            return;
        }
        private void launch(View v){
            int selectedItemPosition = ProfilerecyclerView.getChildPosition(v);
            Store mCurrentStore = Profiledataset.get(selectedItemPosition);
            String storeData = new Gson().toJson(mCurrentStore);
            Intent intent;
            intent = new Intent(context, storeDeals.class);
            intent.putExtra("Store", storeData);
            startActivityForResult(intent, 1);
        }
    }

}
