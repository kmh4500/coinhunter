package com.redsandbox.treasure.activity;

import android.app.Activity;
import android.content.ContentValues;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;

import com.redsandbox.treasure.R;
import com.redsandbox.treasure.core.Constants;
import com.redsandbox.treasure.db.DataProviderContract;
import com.redsandbox.treasure.dialog.LoadingDialog;
import com.redsandbox.treasure.network.CommManager;
import com.redsandbox.treasure.network.ICommListener;
import com.redsandbox.treasure.network.RequestResult;
import com.redsandbox.treasure.points.TreasurePoint;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class InputActivity extends Activity  {

	enum PointType {
		POST
	}

	public static final String POINT_X = "X";
	public static final String POINT_Y = "Y";

	private EditText contentView;

	private TreasurePoint mPoint;
	private LoadingDialog mLoadingDialog;
	private PointType mPointType = PointType.POST;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.input);
		mPoint = new TreasurePoint();
		mPoint.x = getIntent().getDoubleExtra(POINT_X, 0f);
		mPoint.y = getIntent().getDoubleExtra(POINT_Y, 0f);
		mPoint.spaceID = Constants.TREASURE_SPACE_ID;
        mPoint.color = getResources().getColor(R.color.star_color);

		contentView = (EditText) findViewById(R.id.content);

		mLoadingDialog = new LoadingDialog(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.input_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_send:
			send();
			break;

		default:
			break;
		}
		return true;
	}

	private void send() {
		mPoint.text = contentView.getText().toString();
		String apiAction = "addPost";
		try {
			if (mPointType == PointType.POST) {
				apiAction = "addPost";
				JSONObject object = new JSONObject();
				object.put(Constants.TEXT, contentView.getText().toString());
				mPoint.text = object.toString();
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("X", String.valueOf(mPoint.x)));
		params.add(new BasicNameValuePair("Y", String.valueOf(mPoint.y)));
		params.add(new BasicNameValuePair("Text", String.valueOf(mPoint.text)));
		params.add(new BasicNameValuePair("SpaceID", String
				.valueOf(mPoint.spaceID)));
        params.add(new BasicNameValuePair("Color", String
                .valueOf(mPoint.color)));
		mLoadingDialog.show();
		CommManager.getInstance().request(CommManager.RequestMethod.POST,
				RequestResult.ResultType.JSONOBJECT, "/api?action=" + apiAction, params,
				new ICommListener() {

					public void onRequestFinished(boolean isSuccess,
							RequestResult result) {
						ContentValues values = TreasurePoint
								.convertToContentValues(mPoint);
						getContentResolver().insert(
								DataProviderContract.POINT_TABLE_CONTENTURI,
								values);
						getContentResolver().notifyChange(
								DataProviderContract.POINT_TABLE_CONTENTURI,
								null);
						mLoadingDialog.dismiss();
						finish();
					}
				});
	}
}
