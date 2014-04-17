package com.redsandbox.treasure.network;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;

// Code from: http://lukencode.com/2010/04/27/calling-web-services-in-android-using-httpclient/
public class RestClient
{
    private ArrayList<NameValuePair> params;
    private ArrayList<NameValuePair> headers;
//    private ArrayList<ByteArrayBody> part_byte;
    private ArrayList<FileBody> part_byte;
    private ArrayList<String> part_name;
    private String body;
 
    private String url;
 
    private int responseCode;
    private String message;
 
    private String response;
 
    public String getResponse()
    {
        return response;
    }
 
    public String getErrorMessage()
    {
        return message;
    }
 
    public int getResponseCode()
    {
        return responseCode;
    }
 
    public RestClient(String url) {
        this.url = url;
        params = new ArrayList<NameValuePair>();
        headers = new ArrayList<NameValuePair>();
//        part_byte = new  ArrayList<ByteArrayBody>();
        part_byte = new  ArrayList<FileBody>();
        part_name = new  ArrayList<String>();
    }
    public String getUrl(){
    	return url;
    }
    public void AddParam(String name, String value)
    {
        params.add(new BasicNameValuePair(name, value));
    }
    
    public void AddParam(ArrayList<NameValuePair> pairs)
    {
        params.addAll(pairs);
    }
 
    public void AddHeader(String name, String value)
    {
        headers.add(new BasicNameValuePair(name, value));
    }
    
    public void AddBody(String s){
    	body = s;
    }
    
    public void AddPartImage(String filePath, String partName)
    {
        FileBody bin = new FileBody(new File(filePath));
        
        part_name.add(partName);
        part_byte.add(bin);
    }
 
    public void Execute(CommManager.RequestMethod method) throws Exception
    {
        switch (method)
        {
        case GET:
        {
            // add parameters
            String combinedParams = "";
            if (!params.isEmpty())
            {
                combinedParams += "?";
                for (NameValuePair p : params)
                {
                    String paramString = p.getName() + "=" + URLEncoder.encode(p.getValue(),"UTF-8");
                    if (combinedParams.length() > 1)
                    {
                        combinedParams += "&" + paramString;
                    }
                    else
                    {
                        combinedParams += paramString;
                    }
                }
            }
 
            System.out.println(url + combinedParams);
            HttpGet request = new HttpGet(url + combinedParams);
 
            // add headers
            for (NameValuePair h : headers)
            {
                request.addHeader(h.getName(), h.getValue());
            }
 
            executeRequest(request, url);
            break;
        }
        case POST:
        {
            HttpPost request = new HttpPost(url);
 
            // add headers
            for (NameValuePair h : headers)
            {
                request.addHeader(h.getName(), h.getValue());
            }
 
            if (!params.isEmpty())
            {
                request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
            }
            
            if (body != null && part_name.isEmpty())
            {
            	StringEntity se = new StringEntity(body, HTTP.UTF_8);
            	request.setEntity(se);
            }
            else if (!part_name.isEmpty() && body.isEmpty())
            {
            	MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            	for (int i = 0; i < part_name.size(); i++)
            		entity.addPart(part_name.get(i), part_byte.get(i));
            	request.setEntity(entity);
            }
            else if (!part_name.isEmpty() && !body.isEmpty())
            {
            	MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            	for (int i = 0; i < part_name.size(); i++)
            		entity.addPart(part_name.get(i), part_byte.get(i));
            	JSONObject object = new JSONObject(body);
            	Iterator<String> iterator = object.keys();
            	 
            	while (iterator.hasNext()) {
            		String name = iterator.next();
                	entity.addPart(name, new StringBody(String.valueOf(object.get(name))));
            	}
            	request.setEntity(entity);
            }
 
            executeRequest(request, url);
            break;
        }
        }
    }
 
    private void executeRequest(HttpUriRequest request, String url) throws Exception
    {
    	HttpParams httpParams = new BasicHttpParams();
    	HttpConnectionParams.setConnectionTimeout(httpParams, 20000);
    	HttpConnectionParams.setSoTimeout(httpParams, 20000);
    	
        HttpClient client = new DefaultHttpClient(httpParams);
 
        HttpResponse httpResponse;
 
        try
        {
            httpResponse = client.execute(request);
            responseCode = httpResponse.getStatusLine().getStatusCode();
            message = httpResponse.getStatusLine().getReasonPhrase();
 
            HttpEntity entity = httpResponse.getEntity();
 
            if (entity != null)
            {
 
                InputStream instream = entity.getContent();
                response = convertStreamToString(instream);
 
                // Closing the input stream will trigger connection release
                instream.close();
            }
 
        }
        /*
        catch (ClientProtocolException e)
        {
            client.getConnectionManager().shutdown();
            e.printStackTrace();
        }
        catch (IOException e)
        {
            client.getConnectionManager().shutdown();
            e.printStackTrace();
        }*/
        catch (Exception e)
        {
        	client.getConnectionManager().shutdown();
        	throw e;
        }
    }
 
    private static String convertStreamToString(InputStream is)
    {
 
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
 
        String line = null;
        try
        {
            while ((line = reader.readLine()) != null)
            {
                sb.append(line + "\n");
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                is.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}