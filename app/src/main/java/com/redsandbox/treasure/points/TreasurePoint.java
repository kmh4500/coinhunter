package com.redsandbox.treasure.points;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.redsandbox.treasure.core.Constants;
import com.redsandbox.treasure.db.DataProviderContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TreasurePoint {
	private static final char TAB = '\t';
	private static final String X = "X";
	private static final String Y = "Y";
	private static final String TEXT = "Text";
	private static final String POST_ID = "PostID";
	private static final String SPACE_ID = "SpaceID";
	private static final String COLOR = "Color";
	public double x;
	public double y;
	public String text;
	public String postID;
	public boolean focused;
	public String spaceID;
	public int color;

	public TreasurePoint(double x, double y, String text, int color, String postID, String spaceID) {
		this.x = x;
		this.y = y;
		this.text = text;
		this.color = color;
		this.postID = postID;
		this.spaceID = spaceID;
	}

	public TreasurePoint() {
	}

	public TreasurePoint copy() {
		return new TreasurePoint(x, y, text, color, postID, spaceID);
	}

	public static List<TreasurePoint> parseString(String jsonString) {
		JSONArray jsonArray;
		List<TreasurePoint> points = new ArrayList<TreasurePoint>();
		try {
			jsonArray = new JSONArray(jsonString);
			for (int i = 0; i < jsonArray.length(); ++i) {
				JSONObject jsonItem = jsonArray.getJSONObject(i);
				if (jsonItem.has(X) && jsonItem.has(Y) && jsonItem.has(TEXT) && jsonItem.has(POST_ID) && jsonItem.has(SPACE_ID) &&  jsonItem.has(COLOR)) {
					TreasurePoint point = new TreasurePoint(jsonItem.getDouble(X),
							jsonItem.getDouble(Y),
							jsonItem.getString(TEXT),
							jsonItem.getInt(COLOR),
							jsonItem.getString(POST_ID),
							jsonItem.getString(SPACE_ID));
					points.add(point);
					
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		Log.d("DEBUG", "parse string : " + jsonString + "," + points.size());
		return points;
	}
	
	public static ContentValues[] convertToContentValues(List<TreasurePoint> points) {
		ContentValues[] values = new ContentValues[points.size()];
		for (int i = 0; i < points.size(); ++i) {
			values[i] = convertToContentValues(points.get(i));
		}
		return values;
	}

	public static ContentValues convertToContentValues(TreasurePoint point) {
		ContentValues values = new ContentValues();
		values.put(DataProviderContract.COLUMN_POST_ID, point.postID);
		values.put(DataProviderContract.COLUMN_X, point.x);
		values.put(DataProviderContract.COLUMN_Y, point.y);
		values.put(DataProviderContract.COLUMN_COLOR, point.color);
		values.put(DataProviderContract.COLUMN_TEXT, point.text);
		values.put(DataProviderContract.COLUMN_SPACE_ID, point.spaceID);
		return values;
	}

	public void setFocused(boolean focused) {
		this.focused = focused;
	}

	public static TreasurePoint fromCursor(Cursor cursor) {
		return new TreasurePoint(
			cursor.getDouble(cursor.getColumnIndex(DataProviderContract.COLUMN_X)),
			cursor.getDouble(cursor.getColumnIndex(DataProviderContract.COLUMN_Y)),
			cursor.getString(cursor.getColumnIndex(DataProviderContract.COLUMN_TEXT)),
			cursor.getInt(cursor.getColumnIndex(DataProviderContract.COLUMN_COLOR)),
			cursor.getString(cursor.getColumnIndex(DataProviderContract.COLUMN_POST_ID)),
			cursor.getString(cursor.getColumnIndex(DataProviderContract.COLUMN_SPACE_ID)));
	}

	public String getLabel() {
		JSONObject object = null;
		try {
			object = new JSONObject(text);
			if (object.has(Constants.LABEL)) {
				return object.getString(Constants.LABEL);
			}
		} catch (JSONException e) {
		}
		return null;
	}
}
