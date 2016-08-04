package com.example.charles.bellydemo;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SearchResultAdapter extends BaseAdapter {

    Context c;
    List<SearchQuery> data;
    private static LayoutInflater inflater = null;
    public SearchResultAdapter(Context c, List<SearchQuery> searchQueries) {
        this.c = c;
        this.data = searchQueries;
        Collections.sort(searchQueries);
        inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        SearchQuery item = data.get(position);
        View rowView = inflater.inflate(R.layout.query_result_view, null);
        rowView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                ((FragmentActivity) c).onBackPressed(); //which goes to the map view
            }
        });

        setImage((ImageView) rowView.findViewById(R.id.item_imageView), item.getImageURL());

        TextView title = (TextView) rowView.findViewById(R.id.item_titleTextView);
        title.setText(item.getTitle());

        TextView distanceAwayTV = (TextView) rowView.findViewById(R.id.item_distanceTextView);
        distanceAwayTV.setText(item.getDistanceAway());

        setCategoryImage(((ImageView) rowView.findViewById(R.id.item_categoryImage)));
        TextView categoryTV = (TextView) rowView.findViewById(R.id.item_categoryTextView);
        categoryTV.setText(item.getCategoryText());

        TextView isClosedTV = (TextView) rowView.findViewById(R.id.item_isClosedTextView);
        String text;
        String color;
        if(item.isOpen()){
            text = "OPEN";
            color = "#008000";
        }else {
            text = "CLOSED";
            color = "#B6B6B6";
        }
        isClosedTV.setText(text);
        isClosedTV.setTextColor(Color.parseColor(color));



        return rowView;
    }

    private void setCategoryImage(ImageView categoryImage){
        categoryImage.setImageResource(R.drawable.question_mark);
    }

    private void setImage(ImageView icon, String url){
        new DownloadImageTask(icon).execute(url);
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    public int getCount(){
        return data.size();
    }

    public Object getItem(int posiition){
        return posiition;
    }

    public long getItemId(int position){
        return position;
    }
    
}
