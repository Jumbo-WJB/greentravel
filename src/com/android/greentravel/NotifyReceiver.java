package com.android.greentravel;

import com.android.greentravel.util.Constant;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


//监听 用户禁用的告警，暂时没用
public class NotifyReceiver {
	
	/*private final String TAG= NotifyReceiver.class.getName();
	
	private MyReceiver mReceiver= null;
	private Context mContext;
	private Handler mHandler;
	
	public NotifyReceiver(Context context, Handler handler){
		mContext= context;
		mHandler= handler;
		
	}
	
	
	//接收音量变化广播、 摄像头报警广播
	private class MyReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
            String action= intent.getAction();
            Log.d(TAG, "----receive action---"+action); 
            if(action.equals("android.media.VOLUME_CHANGED_ACTION")){
            	
            	mHandler.sendEmptyMessage(Constant.MSG_VOLUMECHANGE);
            	
            }else if(action.equals(Constant.ACTION_CAMERA_ALERT)){
            	
            	String camera_id= intent.getStringExtra("camera_id");
            	Message msg= new Message();
            	msg.what= Constant.MSG_CAMERAALERT;
            	msg.obj= camera_id;
            	mHandler.sendMessage(msg);
            	
            	
            }else if(action.equals(Constant.ACTION_USER_DISABLE)){
            	
            	mHandler.sendEmptyMessage(Constant.MSG_USERDISABLE); 
            }
        }
    };
    
    
    
    
    public void registVolumeReceiver(){
    	if(mReceiver== null){
    		mReceiver = new MyReceiver() ;
    		IntentFilter filter = new IntentFilter() ;
            filter.addAction("android.media.VOLUME_CHANGED_ACTION") ;   //系统音量变化
            filter.addAction(Constant.ACTION_CAMERA_ALERT);  //摄像头报警
            filter.addAction(Constant.ACTION_USER_DISABLE);  //用户禁用
            mContext.registerReceiver(mReceiver, filter) ;
    	}
    }
    
    public void unregistVolumeReceiver(){
    	if(mReceiver!= null){
    		mContext.unregisterReceiver(mReceiver);
    		mReceiver= null;
    	}
    }*/
	
	
	
	
	
	
	
	
	
	

}
