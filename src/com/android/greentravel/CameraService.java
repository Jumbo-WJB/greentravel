package com.android.greentravel;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.android.greentravel.R;
import com.android.greentravel.util.Constant;
import com.android.greentravel.util.Parameter;
import com.android.greentravel.util.SWHttpResponse;
import com.android.greentravel.util.SWInterface;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

public class CameraService extends Service{
	
	private final String TAG= CameraService.class.getName();
	private final int MSG_ALERT= 1;
	private final int MSG_DISABLEUSER= 2;
	
	private Parameter parameter;
	private Context mContext;
	private NotificationManager nm;   
//	private Map<String, Integer> alertMap= new HashMap<String, Integer>();
	
	private ScheduledThreadPoolExecutor executor;
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "---cameraService onCreate----");
		mContext= CameraService.this;
		parameter= new Parameter(mContext);
		nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE); 
		
		executor= (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(1);
		executor.scheduleWithFixedDelay(new HeartRunnable(), 0, 15, TimeUnit.SECONDS);
	} 
	
	
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "---cameraService onDestroy----");
		executor.shutdown();
		
	}
	
	private class HeartRunnable implements Runnable{
		
		@Override
		public void run() {
			Log.d(TAG, "----heart---");
			try {
				SWHttpResponse response= SWInterface.heart(parameter);
				if(response.statusCode== 200 && response.errorCode== 0){
					
					/*JSONArray array= new JSONArray();
					JSONObject obj= new JSONObject();
					obj.put("action", "disable_user");
					obj.put("user_id", "18611223405");
					array.put(obj);
					response.bodyJson.put("notifies", array); */  
					
					if(!response.bodyJson.has("notifies")){
						return;
					}
					JSONArray notifies= response.bodyJson.getJSONArray("notifies");
					for(int i= 0, n= notifies.length(); i< n; i++){
						
						JSONObject notify= notifies.getJSONObject(i);
						
						String action= notify.getString("action");
						
						if("alert".equals(action)){
							String camera_id= notify.has("camera_id") ? notify.getString("camera_id") : "";
							
							if(camera_id.equals("")){
								continue;
							}
							
							Message msg= new Message();
							msg.what= MSG_ALERT;
							msg.obj= camera_id;
							mHandler.sendMessage(msg);
						}else if("disable_user".equals(action)){
							
							
							String user_id= notify.has("user_id") ? notify.getString("user_id") : "";
							Log.d(TAG, "----get disable user notify--------"+user_id); 
							if(user_id.equals("")){
								continue;
							}
							if(user_id.equals(parameter.get("currentUser"))){
								
								mHandler.sendEmptyMessage(MSG_DISABLEUSER);
								
								
							}
						}
					}
				}
			
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
	
	
	private Handler mHandler= new Handler(){
		@Override
		public void dispatchMessage(Message msg) {
			super.dispatchMessage(msg);
			switch (msg.what) {
			case MSG_ALERT:  //处理告警
				
				String camera_id= (String) msg.obj;
				Log.d(TAG, "----get alert notify, camera_id="+camera_id);
				
				
				//发送告警广播，  直播界面会有红灯闪烁
				Intent intent1= new Intent();
				intent1.setAction(Constant.ACTION_CAMERA_ALERT);
				intent1.putExtra("camera_id", camera_id);
				sendBroadcast(intent1);   
				
				
				
				//以下为 在消息栏中  添加告警通知
				String camera_name= "";
				try {
					JSONArray array= new JSONArray(parameter.get("cameraList"));
					
					for(int i= 0, n= array.length(); i< n; i++){
						JSONObject cameraObj= array.getJSONObject(i);
						JSONObject baseObj= cameraObj.getJSONObject("base");
						if(baseObj.getString("camera_id").equals(camera_id)){
							camera_name= baseObj.getString("name");  
							break;
						}
					}
					Log.d(TAG, "----get alert notify, camera_name="+camera_name); 
					if(camera_name.equals("")){
						break;
					}
					
					
					int notificationId= camera_id.hashCode();
					nm.cancel(camera_id, notificationId);
					/*if(alertMap.containsKey(camera_id)){
						alertMap.put(camera_id, (alertMap.get(camera_id)+1));
					}else{
						alertMap.put(camera_id, 1);
					}*/
					
					
					
					Intent intent = new Intent(mContext, MainActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
					Notification n = new Notification(R.drawable.notify_icon, mContext.getString(R.string.app_name), System.currentTimeMillis());             
//					n.setLatestEventInfo(mContext, mContext.getString(R.string.app_name), "您的摄像头["+camera_name+"]第"+alertMap.get(camera_id)+"次检测到异常!", contentIntent);
					n.setLatestEventInfo(mContext, mContext.getString(R.string.app_name), "您的摄像头["+camera_name+"]检测到异常!", contentIntent);
					n.flags = Notification.FLAG_AUTO_CANCEL; 
					
					nm.notify(camera_id, notificationId, n);  
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				break;
				
				
			case MSG_DISABLEUSER:  //处理用户禁用
				
				Intent intent3= new Intent();
				intent3.setAction(Constant.ACTION_USER_DISABLE);
				sendBroadcast(intent3);   
				
				break;

			default:
				break;
			}
		}
	};
	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
//		return START_NOT_STICKY;
	}
	
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	

}
