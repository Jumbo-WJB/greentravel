package com.android.greentravel.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.util.Log;


public class SWHttp {
	
	private final String TAG = "SWHttp";  
	private String urlString;
    private String body;
    
    private Map<String, String> params;
    private Map<String, String> headers;
	
	
	public SWHttp(String urlString, Map<String, String> params, Map<String, String> headers) {
        this.urlString = urlString;
        this.params = params;
        this.headers = headers;
    }
    public SWHttp(String urlString, String body, Map<String, String> headers) {
        this.urlString = urlString;
        this.body = body;
        this.headers = headers;
    }
    

    
    public SWHttpResponse executePost(){
    	
    	SWHttpResponse retBean= new SWHttpResponse();
    	BasicHttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
		HttpConnectionParams.setSoTimeout(httpParams, 5000);
		DefaultHttpClient client = new DefaultHttpClient(httpParams);
    	try {
    		HttpPost httpPost = new HttpPost(this.getUrlString());
    		if (this.getHeaders() != null) {
    			for (Entry<String, String> header : this.getHeaders().entrySet()) {
    				httpPost.setHeader(header.getKey(), header.getValue());
    			}
    		}
    		
    		if (this.getParams() != null) {
    			List<BasicNameValuePair> formparams = new ArrayList<BasicNameValuePair>();
    			for (Entry<String, String> param : this.getParams().entrySet()) {
    				formparams.add(new BasicNameValuePair(param.getKey(), param.getValue()));
    			}
    			HttpEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
    			httpPost.setEntity(entity);
    		}
    		
    		if(this.getBody()!= null){
    			
    			HttpEntity entity = new StringEntity(this.getBody(), "UTF-8");
    			httpPost.setEntity(entity);
    		}
    		Log.d(TAG, "--post url--"+this.getUrlString()+ "--post body--"+this.getBody());
    		HttpResponse response= client.execute(httpPost);
    		retBean.statusCode= response.getStatusLine().getStatusCode();
    		
    		if(retBean.statusCode== 200 || retBean.statusCode== 400){
    			String body= EntityUtils.toString(response.getEntity());
    			Log.d(TAG, "--post url--"+this.getUrlString() + "--statusCode--"+ retBean.statusCode +"--response body--"+body);
    			retBean.bodyJson= new JSONObject(body); 
    			retBean.errorCode= retBean.bodyJson.has("error_code") ? retBean.bodyJson.getInt("error_code"): -1;
    			
    		}else{
    			Log.d(TAG, "--post url--"+this.getUrlString() + "--statusCode--"+ retBean.statusCode);
    		}
		} catch (Exception e) {
			Log.e(TAG, "--post url--"+this.getUrlString()+ "--error--"+e);
			e.printStackTrace();
		}finally{
			client.getConnectionManager().shutdown();
		}
		return retBean;
    }
    
    
    public SWHttpResponse executeGet(){
    	
    	SWHttpResponse retBean= new SWHttpResponse();
    	BasicHttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
		HttpConnectionParams.setSoTimeout(httpParams, 5000);
		DefaultHttpClient client = new DefaultHttpClient(httpParams);
    	try {
    		HttpGet httpGet = new HttpGet(this.getUrlString());
    		if (this.getHeaders() != null) {
    			for (Entry<String, String> header : this.getHeaders().entrySet()) {
    				httpGet.setHeader(header.getKey(), header.getValue());
    			}
    		}
    		HttpResponse response= client.execute(httpGet);
    		retBean.statusCode= response.getStatusLine().getStatusCode();
    		if(retBean.statusCode== 200 || retBean.statusCode== 400){
    			String body= EntityUtils.toString(response.getEntity());
    			retBean.bodyJson= new JSONObject(body);
    			retBean.errorCode= retBean.bodyJson.has("error_code") ? retBean.bodyJson.getInt("error_code"): -1;
    		}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			client.getConnectionManager().shutdown();
		}
		return retBean;
    }
    
    
    
    
	public String getUrlString() {
		return urlString;
	}
	public String getBody() {
		return body;
	}
	public Map<String, String> getParams() {
		return params;
	}
	public Map<String, String> getHeaders() {
		return headers;
	}
}
