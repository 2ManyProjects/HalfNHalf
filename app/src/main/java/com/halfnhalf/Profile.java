package com.halfnhalf;

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
    private RecyclerView ProfilerecyclerView;
    public static ArrayList<Store> Profiledataset;
    private TypedArray ImageResources;
    private FloatingActionButton add;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

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

                Displayer.toaster(Integer.toString(Profileadapter.getItemCount()), "l", Profile.this);
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

}
