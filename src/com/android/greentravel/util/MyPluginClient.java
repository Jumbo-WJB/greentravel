package com.android.greentravel.util;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.chinamobile.js.pluginsupport.apk.PluginClient;
/**
 *
 * 启动江苏和家庭服务，获取token
 *
 */
public class MyPluginClient extends PluginClient{
	private static final String TAG= "MyPluginClient";
	private String token= "";
	
	public MyPluginClient(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected String getAppId() {
		// TODO Auto-generated method stub
		return "APP_ID";
	}

	@Override
	protected String getAppKey() {
		// TODO Auto-generated method stub
		return "APP_KEY";
	}

	public void startConnect(){
		
		Intent pluginActivity = new Intent("com.jscmcc.cmplugin.service.plugin");
		
		boolean isOk = bindService(pluginActivity);
		Log.d(TAG, "--------bindService isOk-------"+isOk);
		
	}
	
	
	@Override
	protected void onServiceConnected() {
		// TODO Auto-generated method stub
		Log.d(TAG, "和家庭客户端已连接...");
		//handler.sendEmptyMessage(0);  
		new Thread(GetTokenRun).start();
	}

	@Override
	protected void onServiceDisconnected() {
		// TODO Auto-generated method stub
		Log.d(TAG, "和家庭客户端已断开...");
	}
	
	private Runnable GetTokenRun = new Runnable() {
		@Override
		public void run() {
			{
				try {
					Log.d(TAG, "--------start get token--------");
					token = getUserSSOTokenAsync();
					Log.d(TAG, "--------get token success, token="+token);
				} catch (Exception e) {
					e.printStackTrace();
					Log.d(TAG, "--------get token exception, e="+e);
				}finally{
					unbindService();
				}
			};
		}
	};

	public String getToken() {
		return token;
	}
}
