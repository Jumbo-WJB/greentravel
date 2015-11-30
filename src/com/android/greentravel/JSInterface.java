package com.android.greentravel;

import java.util.Date;
import java.util.logging.LogRecord;

import com.android.greentravel.qr.CaptureActivity;
import com.android.greentravel.util.CameraWifiConfig;
import com.android.greentravel.util.Constant;
import com.android.greentravel.util.MyPluginClient;
import com.android.greentravel.util.Parameter;

import org.json.JSONObject;
import org.xwalk.core.JavascriptInterface;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Created by luodong on 2015-8-27.
 */
public class JSInterface{
//    private SharedPreferences sp;
    private static final String TAG = JSInterface.class.getName();
    private Context mContext;
    private WalkWebViewWrapper mWvWrapper;
    private Parameter parameter;
    private MyPluginClient pluginClient;
    
    
    private CameraWifiConfig wifiConfig;  //零配置扫描工具类
    public void init(Context context, WalkWebViewWrapper wvWrapper){
    	mContext= context;
    	mWvWrapper= wvWrapper;
//        sp = context.getSharedPreferences("SP", context.MODE_PRIVATE);
        parameter= new Parameter(mContext);
        
    }

    
   
    /*private void write(String key, String value) {
        // 存入数据
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, value);
        editor.commit();
    }


    private String read(String key) {
        return sp.getString(key, "");
    }*/


    @JavascriptInterface
    public String ioctl(String cmd, String param){
        Log.d(TAG,"ioctl-->cmd:" + cmd + " param:" + param);
        String result = "";
        if(cmd.equals("sysset")){
            try {
                JSONObject obj = new JSONObject(param);
                String key = obj.getString("key");
                String value = obj.getString("value");
//                System.setProperty(key,value);
                parameter.set(key, value);
                /*if("ois_ip".equals(key)){
                	parameter.save();
                }*/
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(cmd.equals("sysget")){
            try {
                JSONObject obj = new JSONObject(param);
                String key = obj.getString("key");
                
                result= parameter.get(key);
//                result = System.getProperty(key);
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /*if(cmd.equals("read")){
            try {
                JSONObject obj = new JSONObject(param);
                String key = obj.getString("key");
                result = read(key);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if(cmd.equals("write")){
            try {
                JSONObject obj = new JSONObject(param);
                String key = obj.getString("key");
                String value = obj.getString("value");
                write(key, value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }*/
        
        if(cmd.equals("play")){
        	try {
                JSONObject obj = new JSONObject(param);
                String type= obj.getString("type");
                String user= obj.getString("user");
                String cameraid= obj.getString("cameraid");
                String cameraname= obj.getString("cameraname");
                Log.d(TAG, "--user--"+obj.getString("user")+"--uid--"+obj.getString("cameraid"));
                Intent intent= null;
                if("vod".equals(type)){ //回看
                	intent= new Intent(mContext, RecordActivity.class);
                	intent.putExtra("user", user);
    				intent.putExtra("cameraid", cameraid);
    				intent.putExtra("cameraname", cameraname);
    				mContext.startActivity(intent);
                }else{ //直播
                	intent= new Intent(mContext, PlayerActivity.class);
                	intent.putExtra("user", user);
    				intent.putExtra("cameraid", cameraid);
    				intent.putExtra("cameraname", cameraname);
                	((Activity)mContext).startActivityForResult(intent, Constant.PLAY_REQUESTCODE);  //mContext为MainActivity
                }
                
				
            } catch (Exception e) {
                e.printStackTrace();
            }
        	
        }
        
       /* if(cmd.equals("scan")){
        	try {
                Intent intent= new Intent(mContext, AddCameraActivity.class); 
				((Activity)mContext).startActivityForResult(intent, Constant.ADDCAMERA_REQUESTCODE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        	
        }*/
        
        if(cmd.equals("scanqr")){ //二维码扫描
        	try {
        		Intent intent = new Intent(mContext, CaptureActivity.class);
        		((Activity)mContext).startActivityForResult(intent, Constant.QRSCAN_REQUESTCODE);  //mContext为MainActivity
        		
            } catch (Exception e) {
                e.printStackTrace();
            }
        	
        }
        
        
        if(cmd.equals("getssid")){
        	
        	new Thread(new Runnable() {
    			@Override
    			public void run() {
    				WifiManager wifi = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
    				String ssid= "", localIp= "";
    				if (wifi.isWifiEnabled()) {
    					WifiInfo info = wifi.getConnectionInfo();
    			    	
    			    	if(info!= null){
    			    		ssid = info.getSSID();  //获取当前连接wifi名称, 4.0的版本获取的ssid 前后带""号，例如："sunniwell1715"
    			    		if(ssid.startsWith("\"")){
    			    			ssid= ssid.substring(1);
    			    		}
    			    		if(ssid.endsWith("\"")){
    			    			ssid= ssid.substring(0, ssid.length()-1);
    			    		}
    			    		
    			    		
    			    		int ip = info.getIpAddress();
    		                localIp= (ip & 0xFF) + "." + ((ip >> 8) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "." + ((ip >> 24) & 0xFF);
    		                Log.d(TAG, "----wifiSSID=="+ssid+"----localIp="+localIp);
    		                
    		                Message msg= new Message();
    		                msg.what= MSG_SENDSSID;
    		                Bundle bundle= new Bundle();
    		                bundle.putString("ssid", ssid);
    		                bundle.putString("ip", localIp);
    		                msg.setData(bundle);
    		                mHandler.sendMessage(msg);
    			    	}
    				}
    		        
    			}
    		}).start();
        	
        }
        
        if(cmd.equals("startscanwifi")){
        	
        	if(wifiConfig== null){
        		wifiConfig= new CameraWifiConfig(mHandler);
        	}
        	
        	try {
        		JSONObject obj = new JSONObject(param);
                
            	String ssid= obj.getString("ssid");
            	String pwd= obj.getString("pwd");
    			String ip= obj.getString("ip");
            	wifiConfig.setIp(ip);
            	wifiConfig.setPwd(pwd);
            	wifiConfig.setSsid(ssid);
            	wifiConfig.startConfig();
            	
            	
            	/*Message msg= new Message();
				msg.what= Constant.MSG_CONNEC_RESULT;
				msg.obj= "8140-01D4-13141858";
				mHandler.sendMessageDelayed(msg, 3000);*/
            	
            
			} catch (Exception e) {
				Log.e(TAG, "---start wifi scan error---"+e);
				e.printStackTrace();
			}
        	
        }
        if(cmd.equals("stopscanwifi")){
        	
        	if(wifiConfig!= null){
        		wifiConfig.stopConfig();
        	}
        }
        
        
        
        if(cmd.equals("timeupdate")){
        	try {
                JSONObject obj = new JSONObject(param);
                
				String time= obj.getString("time");
				
				if (mContext instanceof RecordActivity) {
					((RecordActivity)mContext).timeUpdate(time);
					
				}
				
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        if(cmd.equals("upgrade")){
        	try {
                JSONObject obj = new JSONObject(param);
                
				String upgrade_url= obj.getString("upgrade_url");
				
				Uri uri = Uri.parse(upgrade_url);

				Intent intent = new Intent(Intent.ACTION_VIEW,uri);

				mContext.startActivity(intent); 
				
            } catch (Exception e) {
                e.printStackTrace();  
            }
        }
        
        if(cmd.equals("finish")){
        	((Activity)mContext).finish(); 
        }
        if(cmd.equals("getJiangSutoken")){ //江苏移动定制，获取和家庭token
        	
        	try {
        		if(pluginClient== null){
             		pluginClient= new MyPluginClient(mContext);
             	}
//        		System.out.println("-----time111---"+new Date().getTime()); 
        		pluginClient.startConnect();
        		
        		new Thread(){
        			@Override
					public void run() {
						String token= "";
						for(int i= 0; i< 5; i++){
							token= pluginClient.getToken();
							if(token!= null && !"".equals(token.trim())){
								break;
							}
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						Log.i(TAG, "----get 和家庭 token ---"+token); 
						
						Message msg= new Message();
		                msg.what= MSG_JIANGSU_TOKEN_RESUTL;
		                msg.obj= token;
		                mHandler.sendMessage(msg);
					}
        		}.start();
        		
        		
        		
            	
//            	System.out.println("-----time111222---"+new Date().getTime());
            	
			} catch (Exception e) {
				Log.e(TAG, "----get 和家庭 token error---"+e);
				e.printStackTrace();
			}
        	
        }
        
        return result;
    }
    
    
    public final int MSG_SENDSSID= 1;
    public final int MSG_JIANGSU_TOKEN_RESUTL= 2;
    private Handler mHandler= new Handler(){

		public void dispatchMessage(android.os.Message msg) {
			
			switch (msg.what) {
			case MSG_SENDSSID:
				
				Bundle bundle= msg.getData();
				String ssid= bundle.getString("ssid");
				String ip= bundle.getString("ip");
				String param= "{ \"ssid\": \""+ssid+"\", \"ip\":\""+ip+"\" }";
				mWvWrapper.raiseEvent("ssidresult", param);
				Log.d(TAG, "-----ssidresult---"+param);
				break;
				
			case Constant.MSG_CONNEC_RESULT:
				
				String cameraId= (String) msg.obj;
				String result= "{ \"cameraid\": \""+cameraId+"\"}";
				mWvWrapper.raiseEvent("scanwifiresult", result); 
				Log.d(TAG, "-----scanwifiresult---"+result); 
				
				break;
			case MSG_JIANGSU_TOKEN_RESUTL:
				String token= (String) msg.obj;
				String token_result= "{ \"token\": \""+token+"\"}";
				mWvWrapper.raiseEvent("jiangsutokenresult", token_result); 
				Log.d(TAG, "-----jiangsutokenresult---"+token_result); 
				break;
				
			default:
				break;
			}
			
		};
    	
    	
    };
}
