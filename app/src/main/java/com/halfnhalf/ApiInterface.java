package com.halfnhalf;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;


public interface ApiInterface {
    //TODO removed Open Now
    @GET("place/nearbysearch/json?")
    Call<PlacesPOJO.Root> doPlaces(@Query(value = "location", encoded = true) String location, @Query(value = "name", encoded = true) String name, @Query(value = "opennow", encoded = true) boolean opennow, @Query(value = "rankby", encoded = true) String rankby, @Query(value = "key", encoded = true) String key);


    @GET("distancematrix/json") // origins/destinations:  LatLng as string
    Call<ResultDistanceMatrix> getDistance(@Query("key") String key, @Query("origins") String origins, @Query("destinations") String destinations);
}
