package com.example.charles.bellydemo;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ResultListActivity extends AppCompatActivity {

    private static final int QUERIES_PER_PAGE = 5;

    private ArrayList<SearchQuery> queries;
    private GestureDetectorCompat swipeDetector;
    private MyGestureListener gestureListener;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar);
        setContentView(R.layout.list_activity);
        gestureListener = new MyGestureListener(this);
        swipeDetector = new GestureDetectorCompat(this, gestureListener);


        ArrayList<String> queryData = getIntent().getStringArrayListExtra(SearchActivity.SEARCH_RESULTS);
        queries = translateQueryData(queryData);

        mViewPager = new ViewPager(this);

        addDotsToView();
        showQueries();
    }


    private static ArrayList<SearchQuery> translateQueryData(ArrayList<String> queryData){
        ArrayList<SearchQuery> queries = new ArrayList<>();
        JSONObject jo;
        SearchQuery sq;
        for(String queryEncoding: queryData){
            sq = new SearchQuery(queryEncoding);

            queries.add(sq);
        }
        return queries;
    }


    public void onBackPressed(){
        if(!hasQueryData())
            return;
        super.onBackPressed();
    }

    private boolean hasQueryData() {
        SharedPreferences settings = getPreferences(0);
        return settings.getString(SearchActivity.KEY, null) == null;
    }

    private ViewPager mViewPager;
    private List<ImageView> dots;
    private int currentPage = 0;
    public void addDotsToView() {
        if(queries == null)
            return;
        int results = queries.size();
        int numberOfDots = results / QUERIES_PER_PAGE;
        if(results % QUERIES_PER_PAGE != 0)  //this is to account for that last page, for example 26 queries needs 6 pages, not 5
            numberOfDots ++;

        dots = new ArrayList<>();
        LinearLayout dotsLayout = (LinearLayout)findViewById(R.id.dots);

        for(int i = 0; i < numberOfDots; i++) {
            ImageView dot = new ImageView(this);
            dot.setImageDrawable(getDrawable(R.drawable.pager_dot_not_selected));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            int margins = 40;
            params.rightMargin = margins;
            params.leftMargin = margins;
            dotsLayout.addView(dot, params);

            dots.add(dot);
            dot.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    ResultListActivity.this.currentPage = dots.indexOf(view);
                    showQueries();
                }
            });
        }
    }

    private void showQueries(){
        int begin = currentPage * QUERIES_PER_PAGE;
        int end   = Math.min((currentPage + 1) * QUERIES_PER_PAGE, queries.size());
        SearchResultAdapter searchResultAdapter = new SearchResultAdapter(this, queries.subList(begin, end));
        ListView lv = (ListView) findViewById(R.id.list_list);
        lv.setAdapter(searchResultAdapter);
        lv.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return swipeDetector.onTouchEvent(motionEvent);
            }
        });
        selectDot();
    }

    public void selectDot() {
        for (int i = 0; i < dots.size(); i++) {
            int drawableId = (i == currentPage) ? (R.drawable.pager_dot_selected) : (R.drawable.pager_dot_not_selected);
            Drawable drawable = getDrawable(drawableId);
            dots.get(i).setImageDrawable(drawable);
        }
    }


    public boolean onTouchEvent(MotionEvent e){
        swipeDetector.onTouchEvent(e);
        return super.onTouchEvent(e);
    }


    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final String DEBUG_TAG = "Gestures";

        private ResultListActivity a;
        public MyGestureListener(ResultListActivity a){
            this.a = a;
        }

        public boolean onDown(MotionEvent event) {
            Log.d(DEBUG_TAG,"onDown: " + event.toString());
            return true;
        }

        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

            final float xDistance = Math.abs(e1.getX() - e2.getX());

            //if(xDistance > this.swipe_Max_Distance || yDistance > this.swipe_Max_Distance)
            //  return false;

            velocityX = Math.abs(velocityX);
            boolean result = false;

            if(velocityX > 100 && xDistance > 100){
                if(e1.getX() > e2.getX()) // right to left
                    a.swipeLeft();
                else
                    a.swipeRight();

                result = true;
            }

            return result;
        }


    }

    private void swipeLeft(){
        currentPage ++;
        if(currentPage >= dots.size())
            currentPage = dots.size() - 1;
        showQueries();
    }

    private void swipeRight(){
        currentPage --;
        if(currentPage < 0)
            currentPage = 0;
        showQueries();
    }
}
