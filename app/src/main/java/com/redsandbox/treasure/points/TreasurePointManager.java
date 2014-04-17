package com.redsandbox.treasure.points;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.redsandbox.treasure.core.Constants;
import com.redsandbox.treasure.db.DataProviderContract;
import com.redsandbox.treasure.network.CommManager;
import com.redsandbox.treasure.network.ICommListener;
import com.redsandbox.treasure.network.RequestResult;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kmh4500@gmail.com
 */
public class TreasurePointManager {

    private static TreasurePointManager instance;
    private final Context mContext;

    public TreasurePointManager(Context context) {
        this.mContext = context;
    }

    public static void init(Context context) {
        instance = new TreasurePointManager(context);
    }
    public static TreasurePointManager getInstance() {
        return instance;
    }

    public List<TreasurePoint> loadPoints() {
        Cursor cursor = mContext.getContentResolver().query(
                DataProviderContract.POINT_TABLE_CONTENTURI, null,
                "SpaceID = ?", new String[] { Constants.TREASURE_SPACE_ID }, null);
        List<TreasurePoint> points = new ArrayList<TreasurePoint>();
        while (cursor.moveToNext()) {
            points.add(TreasurePoint.fromCursor(cursor));
        }
        return points;
    }

    public void fetchPoints() {
        ICommListener listener = new ICommListener() {

            public void onRequestFinished(boolean isSuccess,
                                          RequestResult result) {
                if (isSuccess && result != null && result.getResponse() != null) {
                    List<TreasurePoint> points = TreasurePoint
                            .parseString(result.getResponse());
                    ContentValues[] values = TreasurePoint
                            .convertToContentValues(points);
                    mContext.getContentResolver()
                            .bulkInsert(
                                    DataProviderContract.POINT_TABLE_CONTENTURI,
                                    values);
                    mContext.getContentResolver().notifyChange(
                            DataProviderContract.POINT_TABLE_CONTENTURI, null);
                }
            }
        };
        CommManager.getInstance().request(CommManager.RequestMethod.GET,
                RequestResult.ResultType.JSONOBJECT,
                "/api?action=getPost&SpaceID=" + Constants.TREASURE_SPACE_ID, listener);
    }
}
