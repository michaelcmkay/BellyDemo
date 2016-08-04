package com.example.charles.bellydemo;


import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SearchQuery implements Comparable<SearchQuery> {

    public static final String NAME            = "name";
    public static final String IS_CLOSED       = "is closed";
    public static final String URL             = "image url";
    public static final String DISTANCE_AWAY   = "distance";
    public static final String CATEGORIES      = "categories";
    public static final String LAT             = "latitude";
    public static final String LONG            = "longitude";

    private String mImageUrl;
    private String mTitle;
    private boolean isClosed;
    private double distance; //in miles
    private ArrayList<String> categories;


    public SearchQuery(String queryEncoding){
        JSONObject jo;
        try {
            jo = new JSONObject(queryEncoding);

            setTitle(jo.getString(SearchQuery.NAME));
            setCategories(jo.getJSONArray(SearchQuery.CATEGORIES));
            setImage(jo.getString(SearchQuery.URL));
            setClosed(jo.getBoolean(SearchQuery.IS_CLOSED));
            setDistance(jo.getDouble(SearchQuery.DISTANCE_AWAY));  //distance from yelp is given in meters
            setLocation(jo.getString(SearchQuery.LAT), jo.getString(SearchQuery.LONG));
        }catch(JSONException e){
            e.printStackTrace();
        }
    }

    public String getTitle(){
        return mTitle;
    }

    public String getImageURL(){
        return mImageUrl;
    }

    public boolean isOpen(){
        return !isClosed;
    }

    public LatLng getLocation(){
        return new LatLng(lat_d, long_d);
    }

    public String getCategoryText(){
        return categories.get(0);
    }

    public String getDistanceAway(){
        return String.format("%.2f", distance) + " miles away";
    }

    public void setTitle(String title){
        this.mTitle = title;
    }

    public void setCategories(JSONArray jCategories){
        categories = new ArrayList<>();
        for(int i = 0; i < jCategories.length(); i++){
            categories.add(getString(jCategories, i));
        }
    }

    private double lat_d;
    private double long_d;
    public void setLocation(String lat_s, String long_s){
        lat_d = Double.parseDouble(lat_s);
        long_d = Double.parseDouble(long_s);
    }

    public void setImage(String url){
        this.mImageUrl = url;
    }

    public void setClosed(boolean isClosed){
        this.isClosed = isClosed;
    }

    private static double meters_to_mile = 0.621371;
    public void setDistance(double distance){
        //distance is in meters,
        this.distance = distance * meters_to_mile * 0.001;
    }


    private String getString(JSONArray jArray, int position){
        try{
            return jArray.getString(position);
        }catch(JSONException e){
            return null;
        }
    }

    public int compareTo(SearchQuery sq){
        return Double.compare(distance, sq.distance);
    }
}
