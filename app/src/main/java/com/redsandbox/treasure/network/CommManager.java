package com.redsandbox.treasure.network;

import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;

import com.redsandbox.treasure.network.RequestResult.ResultType;

import org.apache.http.NameValuePair;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class CommManager {
	public static final RELEASE_MODE RELEASE_TARGET = RELEASE_MODE.PROD;
	
	private static final String PROD_SERVER_URL = "http://space-env.elasticbeanstalk.com/";
	private static final String BETA_SERVER_URL = "http://localhost:8080/Space";
	private static CommManager instance = null;
	
	
	public enum RequestMethod {
		GET, POST
	}
	
	public enum RELEASE_MODE {
		PROD(0),
		BETA(1);

		private int version;

		private RELEASE_MODE(int val) {
			version = val;
		}

		public int getValue() {
			return version;
		}

	}

	public static final boolean FLAG_IS_PROD_VERSION = (RELEASE_TARGET == RELEASE_MODE.PROD) ? true : false;
	public static final boolean FLAG_IS_BETA_VERSION = (RELEASE_TARGET == RELEASE_MODE.BETA) ? true : false;
	
	
	public final static synchronized CommManager getInstance() {
		if (instance == null) {
			instance = new CommManager();
		}
		return instance;
	}
	
	public String getServerUrl(){
		if(FLAG_IS_PROD_VERSION){
			return PROD_SERVER_URL;
		}
		else if(FLAG_IS_BETA_VERSION){
			return BETA_SERVER_URL;
		}
		return PROD_SERVER_URL;
	}
	
	
	public void request(RequestMethod method, RequestResult.ResultType type, String api, ICommListener listner){
		CommRequest req = new CommRequest(method, type, api, null, null, null, listner);
		CommTask task = new CommTask();
		task.execute(req);
	}
	
	public void request(RequestMethod method, RequestResult.ResultType type, String api, String body, ICommListener listner){
		CommRequest req = new CommRequest(method, type, api, body, null, null, listner);
		CommTask task = new CommTask();
		task.execute(req);
	}
	
	public void request(RequestMethod method, RequestResult.ResultType type, String api, String body, String filePath, ICommListener listner){
		CommRequest req = new CommRequest(method, type, api, body, filePath, null, listner);
		CommTask task = new CommTask();
		task.execute(req);
	}
	
	public void request(RequestMethod method, RequestResult.ResultType type, String api, ArrayList<NameValuePair> params, ICommListener listner){
		CommRequest req = new CommRequest(method, type, api, null, null, params, listner);
		CommTask task = new CommTask();
		task.execute(req);
	}
	
	private RequestResult requestJSON(CommRequest request){
		try {
			request.getClient().Execute(request.getMethod());
			String response = request.getClient().getResponse();
			RequestResult result = new RequestResult(request.getResultType(), request.getApi(), response);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private RequestResult requestFile(CommRequest request){
		try {
			
			URL url = new URL(request.getClient().getUrl());

			// getting bitmap image
			HttpURLConnection connection = (HttpURLConnection)url.openConnection();
			connection.setDoInput(true);
			connection.setRequestMethod("GET");
			// save
			String path = Environment.getExternalStorageDirectory().getAbsolutePath();
			// TODO: save directory
			String fileName = "audiotemp.3gp";
			File file = new File(path, fileName);
			file.createNewFile();
			
			final FileOutputStream fileOutputStream = new FileOutputStream(file);
		    final byte buffer[] = new byte[16 * 1024];

		    final InputStream inputStream = connection.getInputStream();

		    int len1 = 0;
		    while ((len1 = inputStream.read(buffer)) > 0) {
		        fileOutputStream.write(buffer, 0, len1);
		    }
		    fileOutputStream.flush();
		    fileOutputStream.close();
		    
			RequestResult result = new RequestResult(request.getResultType(), request.getApi(), path+"/"+fileName);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

    public class CommRequest {
		private String mApi;
		private RequestMethod mMethod;
		private ICommListener mListener;
		private ResultType mType;
		private RestClient mClient;
		
		public CommRequest(RequestMethod method, ResultType type, String api, String body, String file, ArrayList<NameValuePair> params, ICommListener listner){
			mMethod = method;
			mApi = api;
			mListener = listner;
			mType = type;
			
			if(mApi.contains("http://") || mApi.contains("https://")){
				mClient = new RestClient(mApi);
			}
			else{
				mClient = new RestClient(getServerUrl()+mApi);
			}
				
			if(body != null && !TextUtils.isEmpty(body)){
				mClient.AddBody(body);
			}
			if(file != null && !TextUtils.isEmpty(file)){
				mClient.AddPartImage(file, "file");
			}
			if (params != null) {
				mClient.AddParam(params);
			}
		}
		
		public String getApi(){
			return mApi;
		}
		
		public ICommListener getListener(){
			return mListener;
		}
		
		public RequestMethod getMethod(){
			return mMethod;
		}
		
		public ResultType getResultType(){
			return mType;
		}
		
		public RestClient getClient(){
			return mClient;
		}
		
	}
	
	
	private class CommTask extends AsyncTask<CommRequest, Void, RequestResult>{
		private CommRequest mRequest;
		
		@Override
		protected RequestResult doInBackground(CommRequest... arg0) {
			mRequest = arg0[0];
			if(mRequest == null){
				return null;
			}
			switch(mRequest.getResultType()){
			case JSONOBJECT:
				return requestJSON(mRequest);
			
			case FILE_URL:
				return requestFile(mRequest);
			}
			return null;
		}
		
		@Override
        protected void onPostExecute(RequestResult result) {
			if(mRequest == null || result == null){
				mRequest.getListener().onRequestFinished(false, null);
				return;
			}
			
			if(mRequest.getListener() != null){
				mRequest.getListener().onRequestFinished(true, result);
			}
		}
	}
}
