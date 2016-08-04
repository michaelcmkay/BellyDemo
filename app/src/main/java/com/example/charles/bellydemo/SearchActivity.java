package com.example.charles.bellydemo;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.yelp.clientlib.connection.YelpAPI;
import com.yelp.clientlib.connection.YelpAPIFactory;
import com.yelp.clientlib.entities.Business;
import com.yelp.clientlib.entities.Category;
import com.yelp.clientlib.entities.Coordinate;
import com.yelp.clientlib.entities.SearchResponse;
import com.yelp.clientlib.entities.options.BoundingBoxOptions;
import com.yelp.clientlib.entities.options.CoordinateOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends FragmentActivity implements View.OnClickListener, OnMapReadyCallback{


    private EditText searchET;
    private LocationManager mLocationManager;
    private SupportMapFragment mapFragment;
    private GoogleMap map;
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);


        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.search_activity);
        findViewById(R.id.search_submit_button).setOnClickListener(this);
        searchET = (EditText) findViewById(R.id.search_ET);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if(mLocationManager == null)
            mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


    }

    public void onResume(){
        super.onResume();
        ArrayList<String> queryData = getQueryData();
        boolean networkAvailable = isNetworkAvailable();
        if(queryData != null){
            if(!networkAvailable)
                showResultsList(queryData);
            else
                showResultsOnMap(queryData);
        }else if(!networkAvailable){
            Toast.makeText(this, "Search unavailable because no connection is detected", Toast.LENGTH_LONG).show();
        }
    }

    private void showResultsOnMap(ArrayList<String> queryData){
        SearchQuery sq;
        if(map == null)
            return;
        map.clear();
        setSelfMarker();
        for(String s: queryData){
            sq = new SearchQuery(s);
            map.addMarker(new MarkerOptions()
                    .position(sq.getLocation())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

        }
    }

    public static final String KEY = "queryData";
    private ArrayList<String> getQueryData(){
        ArrayList<String> list = new ArrayList<>();
        SharedPreferences settings = getPreferences(0);
        String dataSet = settings.getString(KEY, new JSONArray().toString());
        try {
            JSONArray jData = new JSONArray(dataSet);
            String data;
            for(int i = 0; i < jData.length(); i++){
                data = jData.getString(i);
                list.add(data);
            }
        }catch(JSONException e){
            e.printStackTrace();
        }
        if(list.size() == 0)
            return null;
        return list;
    }

    private void storeData(ArrayList<String> list){
        SharedPreferences settings = getPreferences(0);


        JSONArray jArray = new JSONArray();
        for(String searchQuery: list){
            jArray.put(searchQuery);
        }

        settings.edit().putString(KEY, jArray.toString()).commit();
    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private static final int STREET_ZOOM = 15;
    private void setSelfMarker(){
        if(!gpsIsEnabled())
            return;
        Location loc = null;
        try {
            loc = getLastBestLocation();
        }catch(SecurityException se){
            setErrorText("Location services aren't enabled!");
            return;
        }
        LatLng currentLoc = new LatLng(loc.getLatitude(), loc.getLongitude());
        map.addMarker(new MarkerOptions()
                .position(currentLoc)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        map.moveCamera(CameraUpdateFactory.newLatLng(currentLoc));
        map.moveCamera(CameraUpdateFactory.zoomTo(STREET_ZOOM));
    }

    private boolean gpsIsEnabled(){
        return mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public void onMapReady(GoogleMap map){
        this.map = map;
        setSelfMarker();
    }


    public void onClick(View v){
        String searchTerms = searchET.getText().toString();
        if(searchTerms.length() == 0)
            return;
        query(searchTerms);
    }

    public static final String SEARCH_RESULTS = "search_results";
    private void query(String searchTerms){
        YelpAPIFactory apiFactory = new YelpAPIFactory("vigV2cqAXheYVR_rmf370A", "qKHMn9NHDJsmzaCSJjZEtoxTE_k", "SbG8IEPbrAediVFWaztQ2aXBRamEThUY", "6ZdSc8qync3lUokIzGSQoZM3tt0");
        YelpAPI yelpAPI = apiFactory.createAPI();

        Map<String, String> params = new HashMap<>();
        params.put("term", searchTerms);

        Call<SearchResponse> call;
        try {
            Location loc;
            loc = getLastBestLocation();
            CoordinateOptions coordinate = CoordinateOptions.builder()
                    .latitude(loc.getLatitude())
                    .longitude(loc.getLongitude()).build();
            call = yelpAPI.search(coordinate, params);
        }catch(SecurityException|NullPointerException se){
            if(map == null) {
                setErrorText("Please wait for map to load!");
                return;
            }
            LatLngBounds mapBounds = map.getProjection().getVisibleRegion().latLngBounds;
            BoundingBoxOptions bounds = BoundingBoxOptions.builder()
                    .swLatitude(mapBounds.southwest.latitude)
                    .swLongitude(mapBounds.southwest.longitude)
                    .neLatitude(mapBounds.northeast.latitude)
                    .neLongitude(mapBounds.northeast.longitude)
                    .build();
            call = yelpAPI.search(bounds, params);
        }


        Callback<SearchResponse> callback = new Callback<SearchResponse>() {
            public void onResponse(Call<SearchResponse> call, Response<SearchResponse> response) {
                SearchResponse searchResponse = response.body();

                ArrayList<String> queryData = new ArrayList<>();
                JSONObject jo;
                JSONArray jCategories;
                for(Business b: searchResponse.businesses()){
                    jo = new JSONObject();
                    try {
                        jo.put(SearchQuery.NAME, b.name());
                        jo.put(SearchQuery.URL, b.imageUrl());
                        jo.put(SearchQuery.IS_CLOSED, b.isClosed());
                        jo.put(SearchQuery.DISTANCE_AWAY, b.distance());

                        Coordinate c = b.location().coordinate();
                        jo.put(SearchQuery.LAT, c.latitude());
                        jo.put(SearchQuery.LONG, c.longitude());

                        jCategories = new JSONArray();
                        for(Category s: b.categories())
                            jCategories.put(s.name());
                        jo.put(SearchQuery.CATEGORIES, jCategories);
                    }catch(JSONException e){
                        e.printStackTrace();;
                    }
                    queryData.add(jo.toString());
                }
                storeData(queryData);
                showResultsList(queryData);
            }

            public void onFailure(Call<SearchResponse> call, Throwable t) {
                setErrorText(t.getMessage());
                //TODO add appropriate error messages
            }
        };

        call.enqueue(callback);
    }

    private void showResultsList(ArrayList<String> queryData){
        Intent i = new Intent(SearchActivity.this, ResultListActivity.class);
        i.putExtra(SEARCH_RESULTS, queryData);
        SearchActivity.this.startActivity(i);
    }

    private Location getLastBestLocation() throws SecurityException{
        Location locationGPS = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location locationNet = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        long GPSLocationTime = 0;
        if (locationGPS != null){
            GPSLocationTime = locationGPS.getTime();
        }

        long NetLocationTime = 0;

        if (locationNet != null) {
            NetLocationTime = locationNet.getTime();
        }

        if ( GPSLocationTime - NetLocationTime > 0 ) {
            return locationGPS;
        }
        else {
            return locationNet;
        }
    }

    private void setErrorText(String message){
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}















