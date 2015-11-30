package com.android.greentravel.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.android.greentravel.R;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

/**
 * 用于统一管理系统参数
 * @author guoziyun
 *
 */

public class Parameter {
	private final static String TAG = "Parameter";
	private Context mContext;
	private SharedPreferences preferences;
	private Editor editor;
	
	public Parameter(Context context){
		mContext= context;
		preferences= mContext.getSharedPreferences("config", mContext.MODE_PRIVATE);
		editor= preferences.edit();
		
		initParam(); 
	}
	
	private void initParam(){
		
		new Thread(){
    		
    		public void run() {
    				
    			String[] defaultParam= {"ois_ip", "ois_port", "mac", "app_type"};
	            for(String param: defaultParam){
	            	
	            	String value= get(param);
	            	if(value== null || value.trim().equals("")){
	            		
            			if(param.equals("mac")){
            				
            				value= getLocalMacAddress();
            				if(value== null || value.trim().equals("")){
            					value= getMacFromFile(mContext); 
            				}
    					}else if(param.equals("ois_ip")){
    						value= mContext.getString(R.string.test_server);
    					}else if(param.equals("ois_port")){
    						value= mContext.getString(R.string.server_port);
    					}else if(param.equals("app_type")){
    						value= mContext.getString(R.string.app_type);
    					}
            			Log.d(TAG, "----"+param+"-- init, value= ----"+value);
	            		set(param, value);
	            	}
	            	
	            }
	            
    		};
    	}.start();
    	
    	
    	//单独将 app_version 获取一遍，  (如果将 app_version保存到 config.txt中， 当 app升级覆盖后， config.txt 还存在 并保存 着旧版本)
		try {
			String app_version= mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName.trim();
			Log.d(TAG, "----app_version init, value= ----"+app_version);
			set("app_version", app_version);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// 获取参数
	public String get(String key){
		return preferences.getString(key, "");
	}
	
	// 设置参数
	public void set(String key, String value){
		
		if(key.equals("ois_ip") && !value.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")){ //如果ois是域名
			final String domain= value;
			new Thread(){
				public void run() {
					
					try {
						InetAddress inet = Inet4Address.getByName(domain);
						String ip = inet.getHostAddress();
						editor.putString("ois_ip", ip);
						Log.d(TAG, "---set param==ois_ip----value=="+ip);
					} catch (UnknownHostException e) {
						Log.e(TAG, "---parsing domain "+domain+" error--"+e);
						e.printStackTrace();
						editor.putString("ois_ip", domain);
					}finally{
						editor.commit(); 
					}
				};
			}.start();
		}else{
		
			editor.putString(key, value);
			editor.commit(); 
			Log.d(TAG, "---set param=="+key+"----value=="+value); 
		}
	}
	private String getLocalMacAddress() {

		WifiManager wifi = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = wifi.getConnectionInfo();
		return info.getMacAddress();
	}

	@SuppressLint("DefaultLocale")
	private String getMacFromFile(Context context) {
		List<String> mResult = readFileLines("/proc/net/arp");

		Log.d(TAG, "=======  /proc/net/arp  =========");
		for (int i = 0; i < mResult.size(); ++i)
			Log.d(TAG, "line  " + i + mResult.get(i));
		if (mResult != null && mResult.size() > 1) {// 两行以上才是有mac地址的数据
			for (int j = 1; j < mResult.size(); ++j) {// 第一行是抬头,第二行开始才是mac地址数据
				List<String> mList = new ArrayList<String>();
				String[] mType = mResult.get(j).split(" ");
				for (int i = 0; i < mType.length; ++i) {
					if (mType[i] != null && mType[i].length() > 0)
						mList.add(mType[i]);
				}

				if (mList != null && mList.size() > 4) {
					String result = "";
					String[] tmp = mList.get(3).split(":");// 列数:::0,1,2,3,4,5...//第三列才是mac地址
					for (int i = 0; i < tmp.length; ++i) {
						result += tmp[i];
					}
					result = mList.get(3).toUpperCase();
					Log.i(TAG, "Mac address(file): " + result);
					return result;
				}
			}
		}
		return null;
	}
	
	/**
     * 以行为单位读取文件，常用于读面向行的格式化文件
     */
    private List<String> readFileLines(String fileName) {
        File file = new File(fileName);
        BufferedReader reader = null;
        String tempString ="";
        List<String> mResult = new ArrayList<String>();
        try {
        	Log.i("result","以行为单位读取文件内容，一次读一整行：");
            reader = new BufferedReader(new FileReader(file));
            while((tempString = reader.readLine())!=null){
            	mResult.add(tempString);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        
        return mResult;
    }
	
	
//	private final static String TAG = "Parameter";
//	private String mConfigPath= null;
//	private Context mContext;
//	private JSONObject paramObject;
//	
//	public Parameter(Context context){
//		mContext= context;
//		mConfigPath= mContext.getFilesDir().getAbsolutePath() + "/config/config.txt";
//		
//		
//		initParam();
//	}
//	
//	private void initParam(){
//		
//		Log.d(TAG, "---mConfigPath--"+mConfigPath);
//		try {
//			//文件不存在则创建之
//	    	File file = new File(mConfigPath); 
//	        if(!file.exists()){
//	        	File dir = file.getParentFile();
//	        	if(!dir.exists()) {
//	        		dir.mkdirs();
//	        	}
//				file.createNewFile();
//	        }
//	        String content= FileUtils.readFileToString(file);
//	        Log.d(TAG, "---get config content--"+content); 
//	        paramObject= new JSONObject(content);
//		} catch (Exception e) {
//			e.printStackTrace();
//			paramObject= new JSONObject();
//		}
//		Iterator<String> iter= paramObject.keys();
//        while(iter.hasNext()){
//        	
//        	String key= iter.next();
//        	String value= get(key);
//        	if(value== null || value.trim().equals("")){
//        		try {
//					value= paramObject.getString(key);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//        		Log.d(TAG, "---"+key+" init from config.txt---value="+value);
//        		set(key, value); 
//        	}
//        }
//		
//		new Thread(){
//    		
//    		public void run() {
//    				
//    			String[] defaultParam= {"ois_ip", "ois_port", "mac", "app_type"};
//    			boolean needSave= false;
//	            for(String param: defaultParam){
//	            	
//	            	String value= get(param);
//	            	if(value== null || value.trim().equals("")){
//	            		
//	            		needSave= true;
//            			if(param.equals("mac")){
//            				
//            				value= getLocalMacAddress();
//            				if(value== null || value.trim().equals("")){
//            					value= getMacFromFile(mContext); 
//            				}
//    					}else if(param.equals("ois_ip")){
//    						value= mContext.getString(R.string.test_server);
//    					}else if(param.equals("ois_port")){
//    						value= mContext.getString(R.string.server_port);
//    					}else if(param.equals("app_type")){
//    						value= mContext.getString(R.string.app_type);
//    					}
//            			Log.d(TAG, "----"+param+"-- init, value= ----"+value);
//	            		set(param, value);
//	            	}
//	            	
//	            }
//	            if(needSave){
//	            	save();
//	            }
//    		};
//    	}.start();
//    	
//    	
//    	//单独将 app_version 获取一遍，  (如果将 app_version保存到 config.txt中， 当 app升级覆盖后， config.txt 还存在 并保存 着旧版本)
//		try {
//			String app_version= mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName.trim();
//			set("app_version", app_version);
//			Log.d(TAG, "----app_version init, value= ----"+app_version);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//    	
//		
//	}
//	
//	
//	// 获取参数
//	public String get(String key){
//		return System.getProperty(key, "");
//	}
//	
//	// 设置参数
//	public void set(String key, String value){
//		
//		
//		try {
//			if(key.equals("ois_ip") && !value.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")){ //如果ois是域名
//				final String domain= value;
//				new Thread(){
//					public void run() {
//						
//						try {
//							InetAddress inet = Inet4Address.getByName(domain);
//							String ip = inet.getHostAddress();
//							System.setProperty("ois_ip", ip);
//							Log.d(TAG, "---set param==ois_ip----value=="+ip);
//						} catch (UnknownHostException e) {
//							Log.e(TAG, "---parsing domain "+domain+" error--"+e);
//							e.printStackTrace();
//							System.setProperty("ois_ip", domain);
//						}
//					};
//				}.start();
//			}else{
//				System.setProperty(key, value);
//				Log.d(TAG, "---set param=="+key+"----value=="+value); 
//			}
//			paramObject.put(key, value);
//		} catch (Exception e) {
//			Log.d(TAG, "---set param=="+key+"----value=="+value +" error=="+e); 
//			e.printStackTrace(); 
//		}
//	}
//
//	public void save(){
//		try {
//			String content= paramObject.toString(); 
//			Log.d(TAG, "----save conifg content---"+content);
//			FileUtils.writeStringToFile(new File(mConfigPath), content);
//			
//		}catch (Exception e){
//			Log.e(TAG, "----save conifg error---"+e);
//			e.printStackTrace();
//		}
//	}
//	
//
//	
//	private String getLocalMacAddress() {
//
//		WifiManager wifi = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
//		WifiInfo info = wifi.getConnectionInfo();
//		return info.getMacAddress();
//	}
//
//	@SuppressLint("DefaultLocale")
//	private String getMacFromFile(Context context) {
//		List<String> mResult = readFileLines("/proc/net/arp");
//
//		Log.d(TAG, "=======  /proc/net/arp  =========");
//		for (int i = 0; i < mResult.size(); ++i)
//			Log.d(TAG, "line  " + i + mResult.get(i));
//		if (mResult != null && mResult.size() > 1) {// 两行以上才是有mac地址的数据
//			for (int j = 1; j < mResult.size(); ++j) {// 第一行是抬头,第二行开始才是mac地址数据
//				List<String> mList = new ArrayList<String>();
//				String[] mType = mResult.get(j).split(" ");
//				for (int i = 0; i < mType.length; ++i) {
//					if (mType[i] != null && mType[i].length() > 0)
//						mList.add(mType[i]);
//				}
//
//				if (mList != null && mList.size() > 4) {
//					String result = "";
//					String[] tmp = mList.get(3).split(":");// 列数:::0,1,2,3,4,5...//第三列才是mac地址
//					for (int i = 0; i < tmp.length; ++i) {
//						result += tmp[i];
//					}
//					result = mList.get(3).toUpperCase();
//					Log.i(TAG, "Mac address(file): " + result);
//					return result;
//				}
//			}
//		}
//		return null;
//	}
//	
//	/**
//     * 以行为单位读取文件，常用于读面向行的格式化文件
//     */
//    private List<String> readFileLines(String fileName) {
//        File file = new File(fileName);
//        BufferedReader reader = null;
//        String tempString ="";
//        List<String> mResult = new ArrayList<String>();
//        try {
//        	Log.i("result","以行为单位读取文件内容，一次读一整行：");
//            reader = new BufferedReader(new FileReader(file));
//            while((tempString = reader.readLine())!=null){
//            	mResult.add(tempString);
//            }
//            reader.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            if (reader != null) {
//                try {
//                    reader.close();
//                } catch (IOException e1) {
//                }
//            }
//        }
//        
//        return mResult;
//    }


}
