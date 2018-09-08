package com.halfnhalf.store;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import com.halfnhalf.APIClient;
import com.halfnhalf.ApiInterface;
import com.halfnhalf.HomePage;
import com.halfnhalf.PlacesPOJO;
import com.halfnhalf.Profile;
import com.halfnhalf.R;
import com.halfnhalf.ResultDistanceMatrix;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class FindStore extends AppCompatActivity {
    public static RecyclerView.Adapter adapterStores;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView recyclerView;

    private int type = 0;
    private String storeName;
    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsRejected = new ArrayList<>();
    private ArrayList<String> permissions = new ArrayList<>();
    private final static int ALL_PERMISSIONS_RESULT = 101;
    List<StoreModel> storeModels;
    ApiInterface apiService;

    String latLngString;

    EditText editText;
    Button button;
    List<PlacesPOJO.CustomA> results;


    static View.OnClickListener myOnClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_store);
        storeModels = new ArrayList<StoreModel>();
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            type = Integer.parseInt(bundle.getString("FindingUsers"));
            storeName = bundle.getString("StoreName");
            //init();
        }else{
            type = 0;
        }

        myOnClickListener = new MyOnClickListener(this, type);

        permissions.add(ACCESS_FINE_LOCATION);
        permissions.add(ACCESS_COARSE_LOCATION);

        permissionsToRequest = findUnAskedPermissions(permissions);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {


            if (permissionsToRequest.size() > 0)
                requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
            else {
                latLngString = getLocation(FindStore.this);
            }
        } else {
            latLngString = getLocation(FindStore.this);
        }


        apiService = APIClient.getClient().create(ApiInterface.class);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapterStores = new StoreFinderAdapter(storeModels, FindStore.this);
        recyclerView.setAdapter(adapterStores);

        editText = (EditText) findViewById(R.id.editText);
        button = (Button) findViewById(R.id.button);

        if(type == 1){
            button.setVisibility(View.INVISIBLE);
            editText.setVisibility(View.INVISIBLE);
            storeModels.clear();
            adapterStores.notifyDataSetChanged();
            latLngString = getLocation(FindStore.this);
            fetchStores(storeName);
        }else {

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    storeModels.clear();
                    adapterStores.notifyDataSetChanged();
                    String s = editText.getText().toString().trim();
                    latLngString = getLocation(FindStore.this);
                    fetchStores(s);
                }
            });
        }

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                //dealData = new ArrayList<>();
                Bundle bundle = data.getExtras();
                String userID = bundle.getString("id");
                Intent intent = new Intent(this, Profile.class);
                intent.putExtra("id", userID);
                setResult(RESULT_OK, intent);
                finish();
            }
        }
    }

    private static String getLocation(Context c){
        Geocoder geocoder;
        List<Address> user = null;
        double lat;
        double lng;
        LocationManager mLocationManager = (LocationManager)c.getSystemService(LOCATION_SERVICE);

        if (c.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return null;
        }
        //Location location = lm.getLastKnownLocation(bestProvider);
        Location location = getLastKnownLocation(mLocationManager, c);

        if (location == null){
            Toast.makeText(c,"Location Not found", Toast.LENGTH_LONG).show();
            return null;
        }else{
            geocoder = new Geocoder(c);
            try {
                user = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                lat=(double)user.get(0).getLatitude();
                lng=(double)user.get(0).getLongitude();
                System.out.println(" DDD lat: " +lat+",  longitude: "+lng);

                String t = Double.toString(lat) + "," + Double.toString(lng);
                return t;
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static Location getLastKnownLocation(LocationManager mLocationManager, Context c) {
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            if (c.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return null;
            }
            Location l = mLocationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }

    private void fetchStores(String businessName) {
        Log.v("LAT LONG: ", "Data: " + latLngString);
        if(latLngString == null){
            latLngString = getLocation(FindStore.this);
        }
        Log.v("LAT LONG: ", "2 Data: " + latLngString);
        Call<PlacesPOJO.Root> call = apiService.doPlaces(latLngString, businessName, "distance", APIClient.GOOGLE_PLACE_API_KEY);
        call.enqueue(new Callback<PlacesPOJO.Root>() {
            @Override
            public void onResponse(Call<PlacesPOJO.Root> call, Response<PlacesPOJO.Root> response) {
                PlacesPOJO.Root root = response.body();
                if (response.isSuccessful()) {

                    if (root.status.equals("OK")) {

                        results = root.customA;
                        if(type == 1) {
//                            List<PlacesPOJO.CustomA> data = new ArrayList<>();
                            String WhereClause = "StoreID IN (";
                            for (int i = 0; i < results.size(); i++) {
                                if (i == 30) {
                                    break;
                                }
                                PlacesPOJO.CustomA info = results.get(i);
//                                data.add(info);
                                WhereClause += "'" + info.id + "'";
                                if (i == 29 || i == results.size() - 1) {
                                    WhereClause += ")";
                                } else {
                                    WhereClause += ", ";
                                }
                            }
                            filterList(WhereClause);
                        }else {
                            for (int i = 0; i < results.size(); i++) {

                                if (i == 10 && type == 0) {
                                    break;
                                } else if (i == 30) {
                                    break;
                                }
                                PlacesPOJO.CustomA info = results.get(i);
                                fetchDistance(info);
                            }
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "No matches found near you", Toast.LENGTH_SHORT).show();
                    }
                } else if (response.code() != 200) {
                    Toast.makeText(getApplicationContext(), "Error " + response.code() + " found.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<PlacesPOJO.Root> call, Throwable t) {
                Log.e("Google Query Failed: ", "" + t.getMessage());
                call.cancel();
            }
        });
    }

    private void filterList(String clause){
        DataQueryBuilder dataQuery = DataQueryBuilder.create();
        dataQuery.setWhereClause(clause);
        Backendless.Data.of("Stores").find(dataQuery,
                new AsyncCallback<List<Map>>() {
                    @Override
                    public void handleResponse(List<Map> foundStore) {
                        Log.e("STORE EXISTS, COMPILE LIST", " " + foundStore.size() + " R.size" + results.size());
                        if (foundStore.size() > 0) {
                            for(int i = 0; i < results.size(); i++){
                                boolean match = false;
                                for(int x = 0; x < foundStore.size(); x++){
                                    if(results.get(i).id.equals(foundStore.get(x).get("StoreID"))){
                                        match = true;
                                        break;
                                    }
                                }
                                if(!match){
                                    results.remove(i);
                                    i--;
                                }else{
                                    PlacesPOJO.CustomA info = results.get(i);
                                    fetchDistance(info);
                                }
                            }
                            Log.e("STORE EXISTS, COMPILE LIST",  " R.size" + results.size());
                        }
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        Log.e("TOKEN ISSUE: ", "" + fault.getMessage());
                    }
                });
    }

    private class MyOnClickListener implements View.OnClickListener {

        private final Context context;
        private int type = 0;

        private MyOnClickListener(Context context, int i) {
            this.context = context;
            this.type = i;
        }

        @Override
        public void onClick(View v) {
            click(v);
            return;
        }
        private void click(View v){
            int selectedItemPosition = recyclerView.getChildPosition(v);
            StoreModel mCurrentStore = storeModels.get(selectedItemPosition);
            if(type == 0) {
                String id = mCurrentStore.id;
                String name = mCurrentStore.name;
                String address = mCurrentStore.address;
                Intent intent = new Intent(context, Profile.class);
                intent.putExtra("id", id);
                intent.putExtra("name", name);
                intent.putExtra("address", address);
                setResult(RESULT_OK, intent);
                finish();
            }else if(type == 1){
                Intent intent = new Intent(context, HomePage.class);
                intent.putExtra("StoreID", mCurrentStore.id);
                setResult(RESULT_OK, intent);
                finish();
            }

        }
    }

    private ArrayList<String> findUnAskedPermissions(ArrayList<String> wanted) {
        ArrayList<String> result = new ArrayList<>();

        for (String perm : wanted) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }

        return result;
    }

    private boolean hasPermission(String permission) {
        if (canMakeSmores()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
            }
        }
        return true;
    }

    private boolean canMakeSmores() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }


    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {

            case ALL_PERMISSIONS_RESULT:
                for (String perms : permissionsToRequest) {
                    if (!hasPermission(perms)) {
                        permissionsRejected.add(perms);
                    }
                }

                if (permissionsRejected.size() > 0) {


                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                            showMessageOKCancel("These permissions are mandatory for the application. Please allow access.",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissions(permissionsRejected.toArray(new String[permissionsRejected.size()]), ALL_PERMISSIONS_RESULT);
                                            }
                                        }
                                    });
                            return;
                        }
                    }

                } else {

                    latLngString = getLocation(FindStore.this);
                }
                break;
        }

    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(FindStore.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }
    private void fetchDistance(final PlacesPOJO.CustomA info) {

        Call<ResultDistanceMatrix> call = apiService.getDistance(APIClient.GOOGLE_PLACE_API_KEY, latLngString, info.geometry.locationA.lat + "," + info.geometry.locationA.lng);
        call.enqueue(new Callback<ResultDistanceMatrix>() {
            @Override
            public void onResponse(Call<ResultDistanceMatrix> call, Response<ResultDistanceMatrix> response) {

                ResultDistanceMatrix resultDistance = response.body();
                if ("OK".equalsIgnoreCase(resultDistance.status)) {

                    ResultDistanceMatrix.InfoDistanceMatrix infoDistanceMatrix = resultDistance.rows.get(0);
                    ResultDistanceMatrix.InfoDistanceMatrix.DistanceElement distanceElement = infoDistanceMatrix.elements.get(0);
                    if ("OK".equalsIgnoreCase(distanceElement.status)) {
                        ResultDistanceMatrix.InfoDistanceMatrix.ValueItem itemDuration = distanceElement.duration;
                        ResultDistanceMatrix.InfoDistanceMatrix.ValueItem itemDistance = distanceElement.distance;
                        String totalDistance = String.valueOf(itemDistance.text);
                        String totalDuration = String.valueOf(itemDuration.text);
                        storeModels.add(new StoreModel(info.name, info.vicinity, totalDistance, totalDuration, info.id));
                        Log.i("Sizes", "StoreM: " + storeModels.size() + " Results: " + results.size());
                        adapterStores.notifyItemInserted(storeModels.size());
                    }
                }
            }

            @Override
            public void onFailure(Call<ResultDistanceMatrix> call, Throwable t) {
                call.cancel();
            }
        });

    }
}
