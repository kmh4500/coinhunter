/* Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.redsandbox.treasure;

import android.content.Intent;
import android.database.ContentObserver;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.redsandbox.treasure.activity.InputActivity;
import com.redsandbox.treasure.activity.PlaceActivity;
import com.redsandbox.treasure.db.DataProviderContract;
import com.redsandbox.treasure.navigation.MyLocationManager;
import com.redsandbox.treasure.points.TreasurePoint;
import com.redsandbox.treasure.points.TreasurePointManager;

import java.util.ArrayList;
import java.util.List;


/**
 * This shows how to draw circles on a map.
 */
public class MainActivity extends SherlockFragmentActivity implements
        GoogleMap.OnMyLocationButtonClickListener,
        OnMapLongClickListener, GoogleMap.OnInfoWindowClickListener, ActionBar.TabListener {
    private static final double DEFAULT_RADIUS = 1000000;
    public static final double RADIUS_OF_EARTH_METERS = 6371009;

    private static final int LIST_TAB_POSITION = 0;
    private static final int MAP_TAB_POSITION = 1;

    private GoogleMap mMap;

    private MyObserver observer = new MyObserver(new Handler());
    private String[] mTabs;
    private ListView mList;
    private PointAdapter mAdapter;
    private MyLocationManager mLocationManager;

    private class PointAdapter extends BaseAdapter {

        private List<TreasurePoint> mPoints = new ArrayList<TreasurePoint>();

        @Override
        public int getCount() {
            return mPoints.size();
        }

        @Override
        public TreasurePoint getItem(int position) {
            return mPoints.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = View.inflate(MainActivity.this, R.layout.custom_info_contents, null);
            TextView title = (TextView) view.findViewById(R.id.title);
            TreasurePoint point = getItem(position);
            title.setText(point.text);
            return view;
        }

        public void setPoints(List<TreasurePoint> points) {
            this.mPoints = points;
        }
    }

    class MyObserver extends ContentObserver {

        public MyObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            this.onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            loadPoints();
        }
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        if (tab.getPosition() == LIST_TAB_POSITION) {
            mList.setVisibility(View.VISIBLE);
        } else {
            mList.setVisibility(View.GONE);
        }
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    /** Generate LatLng of radius marker */
    private static LatLng toRadiusLatLng(LatLng center, double radius) {
        double radiusAngle = Math.toDegrees(radius / RADIUS_OF_EARTH_METERS) /
                Math.cos(Math.toRadians(center.latitude));
        return new LatLng(center.latitude, center.longitude + radiusAngle);
    }

    private static double toRadiusMeters(LatLng center, LatLng radius) {
        float[] result = new float[1];
        Location.distanceBetween(center.latitude, center.longitude,
                radius.latitude, radius.longitude, result);
        return result[0];
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_map);


        mList = (ListView) findViewById(R.id.list);
        mAdapter = new PointAdapter();
        mList.setAdapter(mAdapter);
        mLocationManager = new MyLocationManager(this);

        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        mTabs = getResources().getStringArray(R.array.tab);
        for (int i = 0; i < mTabs.length; i++) {
            ActionBar.Tab tab = getSupportActionBar().newTab();
            tab.setText(mTabs[i]);
            tab.setTabListener(this);
            getSupportActionBar().addTab(tab);
        }

        setUpMapIfNeeded();

        getContentResolver().registerContentObserver(
                DataProviderContract.POINT_TABLE_CONTENTURI, true, observer);

    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        TreasurePointManager.getInstance().fetchPoints();
        mLocationManager.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mLocationManager.onPause();
    }

    @Override
    protected void onDestroy() {
        getContentResolver().unregisterContentObserver(observer);
        super.onDestroy();
    }

    private void loadPoints() {
        List<TreasurePoint> points = TreasurePointManager.getInstance().loadPoints();
        mAdapter.setPoints(points);
        mAdapter.notifyDataSetChanged();

        for (TreasurePoint point : points) {
            LatLng center = new LatLng(point.x, point.y);
            mMap.addMarker(new MarkerOptions()
                    .position(center)
                    .title(point.text));
        }
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {

        mLocationManager.setUpMap(mMap);
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMapLongClickListener(this);

        mMap.setOnInfoWindowClickListener(this);

        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());
    }

    @Override
    public void onMapLongClick(LatLng point) {
        // We know the center, let's place the outline at a point 3/4 along the view.
        View view = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                .getView();
        // ok create it
        Intent intent = new Intent(this, InputActivity.class);
        intent.putExtra(InputActivity.POINT_X, point.latitude);
        intent.putExtra(InputActivity.POINT_Y, point.longitude);
        startActivity(intent);
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Intent intent = new Intent(this, PlaceActivity.class);
        intent.putExtra(PlaceActivity.TITLE, marker.getTitle());
        Bundle args = new Bundle();
        args.putParcelable(PlaceActivity.POSITION, marker.getPosition());
        intent.putExtra(PlaceActivity.BUNDLE, args);
        startActivity(intent);
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    /** Demonstrates customizing the info window and/or its contents. */
    class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
        private final RadioGroup mOptions;

        // These a both viewgroups containing an ImageView with id "badge" and two TextViews with id
        // "title" and "snippet".
        private final View mWindow;

        CustomInfoWindowAdapter() {
            mWindow = getLayoutInflater().inflate(R.layout.custom_info_window, null);
            mOptions = (RadioGroup) findViewById(R.id.custom_info_window_options);
        }

        @Override
        public View getInfoWindow(Marker marker) {
            render(marker, mWindow);
            return mWindow;
        }

        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }

        private void render(Marker marker, View view) {
            int badge = R.drawable.badge_qld;
            ((ImageView) view.findViewById(R.id.badge)).setImageResource(badge);

            String title = marker.getTitle();
            TextView titleUi = ((TextView) view.findViewById(R.id.title));
            if (title != null) {
                // Spannable string allows us to edit the formatting of the text.
                SpannableString titleText = new SpannableString(title);
                titleText.setSpan(new ForegroundColorSpan(Color.RED), 0, titleText.length(), 0);
                titleUi.setText(titleText);
            } else {
                titleUi.setText("");
            }

            String snippet = marker.getSnippet();
            TextView snippetUi = ((TextView) view.findViewById(R.id.snippet));
            if (snippet != null && snippet.length() > 12) {
                SpannableString snippetText = new SpannableString(snippet);
                snippetText.setSpan(new ForegroundColorSpan(Color.MAGENTA), 0, 10, 0);
                snippetText.setSpan(new ForegroundColorSpan(Color.BLUE), 12, snippet.length(), 0);
                snippetUi.setText(snippetText);
            } else {
                snippetUi.setText("");
            }
        }
    }
}
