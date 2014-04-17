package com.redsandbox.treasure.network;

public class RequestResult {
	private String mResponse;
	private ResultType mType;
	private String mApi;
	public enum ResultType{
		JSONOBJECT, FILE_URL
	}
	
	public RequestResult(ResultType type, String api, String response){
		mResponse = response;
		mType = type;
		mApi = api;
	}
	
	public String getResponse(){
		return mResponse;
	}
	
	public ResultType getResultType(){
		return mType;
	}
	
	public String getApi(){
		return mApi;
	}
	
	
}
