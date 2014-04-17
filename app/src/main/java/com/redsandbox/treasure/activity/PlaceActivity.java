package com.redsandbox.treasure.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.redsandbox.treasure.R;
import com.redsandbox.treasure.navigation.Compass;

public class PlaceActivity extends SherlockFragmentActivity implements ActionBar.TabListener {
    public static final String TITLE = "title";
    public static final String POSITION = "position";
    public static final String BUNDLE = "bundle";
    private static final int TAB_POSITION_COMPASS = 1;
    private LatLng mPosition;
    private String[] mTabs;
    private Compass mCompass;
    private GoogleMap mMap;
    private ViewGroup mCompassHolder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.tab_navigation);

        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        mTabs = getResources().getStringArray(R.array.place_tab);
        for (int i = 0; i < mTabs.length; i++) {
            ActionBar.Tab tab = getSupportActionBar().newTab();
            tab.setText(mTabs[i]);
            tab.setTabListener(this);
            getSupportActionBar().addTab(tab);
        }
        Bundle bundle = getIntent().getParcelableExtra(BUNDLE);
        mPosition = bundle.getParcelable(POSITION);
        mCompass = new Compass();
        mCompassHolder = (ViewGroup) findViewById(R.id.compass_holder);
        mCompass.onCreate(this, mCompassHolder);

        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCompass.onResume();

        setUpMapIfNeeded();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mCompass.onStop();
    }

    @Override
    public void onTabReselected(Tab tab, FragmentTransaction transaction) {
    }

    @Override
    public void onTabSelected(Tab tab, FragmentTransaction transaction) {
        if (tab.getPosition() == TAB_POSITION_COMPASS) {
            if (mCompassHolder != null) {
                mCompassHolder.setVisibility(View.VISIBLE);
            }
        } else {
            if (mCompassHolder != null) {
                mCompassHolder.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction transaction) {
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
        // Construct a CameraPosition focusing on Mountain View and animate the camera to that position.
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(mPosition)      // Sets the center of the map to Mountain View
                .zoom(11)                   // Sets the zoom
                .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        // Move the map so that it is centered on the initial circle
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }
}
