package com.redsandbox.treasure.dialog;

import android.app.Activity;
import android.os.AsyncTask;

public class LoadingAsyncTask extends AsyncTask {
	
	private Activity act;
	private Runnable job;
	private LoadingDialog dialog;
	
	public LoadingAsyncTask(Activity act, Runnable job) {
		this.act = act;
		this.job = job;
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();

		act.runOnUiThread(new Runnable() {

			public void run() {
				dialog = new LoadingDialog(act);
				dialog.show();
				
			}
		});
	}
	
	@Override
	protected void onPostExecute(Object result) {
		super.onPostExecute(result);

		act.runOnUiThread(new Runnable() {
			public void run() {
				dialog.dismiss();
			}
		});
	}
	
	@Override
	protected Object doInBackground(Object... arg0) {
		job.run();
		return null;
	}

}
