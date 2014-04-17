package com.redsandbox.treasure;

import android.app.Application;

import com.redsandbox.treasure.points.TreasurePointManager;

/**
 * Created by 민현 on 2014-04-16.
 */
public class TreasureApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        TreasurePointManager.init(getApplicationContext());
    }
}
