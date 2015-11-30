package com.android.greentravel;

import com.android.greentravel.R;
import com.android.greentravel.util.Constant;

import org.xwalk.core.XWalkView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;


public class MainActivity extends Activity {
	private String TAG = MainActivity.class.getName();
	private int splashTime= 3000;
	private final int MSG_STOPSPLASH= 1;
	
//    private WebViewWrapper mWvWrapper;
    private WalkWebViewWrapper mWvWrapper;
    private Context mContext;
   
    private View splash;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);  //全屏
        setContentView(R.layout.activity_main);
        mContext= MainActivity.this;
        registVolumeReceiver();
        
        splash= findViewById(R.id.splash);
        
        /*WebView wv = (WebView)findViewById(R.id.webview);
        mWvWrapper= new WebViewWrapper(mContext, wv);
        wv.loadUrl("file:///android_asset/index.html");*/
        
        
        
        
        XWalkView wv=(XWalkView)findViewById(R.id.webView);
        mWvWrapper= new WalkWebViewWrapper(mContext, wv);
        wv.load("file:///android_asset/index.html", null);

        mHanlder.sendEmptyMessageDelayed(MSG_STOPSPLASH, splashTime);
        
        
        Intent intent= new Intent(this, CameraService.class);
        startService(intent);
        

    }
    
    private Handler mHanlder= new Handler(){
    	
    	public void dispatchMessage(android.os.Message msg) {
    		switch (msg.what) {
			case MSG_STOPSPLASH:
				splash.setVisibility(View.GONE); 
//				getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);  //退出全屏
				break;

			default:
				break;
			}
    		
    		
    	}
    };
    
  
    
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
    	 if (event.getKeyCode() == KeyEvent.KEYCODE_BACK ) {
    		 
    		 if(event.getAction() == KeyEvent.ACTION_DOWN){
    			 mWvWrapper.raiseEvent("back", "");
    		 }
    		 return true;
    	 }
    	
    	 return super.dispatchKeyEvent(event);
    }
    
    
    
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	// TODO Auto-generated method stub
    	super.onActivityResult(requestCode, resultCode, data);
    	
    	Log.d(TAG, "--requestCode--"+requestCode+"--resultCode--"+resultCode);
    	/*if(requestCode== Constant.ADDCAMERA_REQUESTCODE){
    		if(resultCode== Constant.ADDCAMERA_RESULTCODE){
    			mWvWrapper.raiseEvent("devicechange", "");
    		}
    		
    		
    	}*/
    	
    	if(requestCode== Constant.QRSCAN_REQUESTCODE && resultCode== Constant.QRSCAN_RESULTCODE){ //二维码扫描
    		
//    		if(resultCode== Constant.QRSCAN_RESULTCODE){
			String cameraid = data.getStringExtra("QRString");
			String result= "{\"cameraid\": \""+cameraid+"\"}";
			mWvWrapper.raiseEvent("scanqrresult", result);
			Log.d(TAG, "-----scan qr result---"+result);
//    		}
    		
    	}else if(requestCode== Constant.PLAY_REQUESTCODE && resultCode== Constant.PLAY_RESULTCODE){ // 退出播放，刷新界面数据
    		
    		mWvWrapper.raiseEvent("livequit", "");
    		Log.d(TAG, "---livequit refresh camera list on webpage----");
    	}
    }
    
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	unregistVolumeReceiver();
    	Log.d(TAG, "----onDestroy---"); 
    }
   
    
  //接收用户禁用广播
  	private class MyReceiver extends BroadcastReceiver{

  		@Override
  		public void onReceive(Context context, Intent intent) {
              String action= intent.getAction();
              Log.d(TAG, "----receive action---"+action); 
              if(action.equals(Constant.ACTION_USER_DISABLE)){ //用户禁用
            	  mWvWrapper.raiseEvent("userdisable", "");
              }
          }
      };
      
      
      private MyReceiver mReceiver= null;
      
      private void registVolumeReceiver(){
      	if(mReceiver== null){
      		mReceiver = new MyReceiver() ;
      		IntentFilter filter = new IntentFilter() ;
      		filter.addAction(Constant.ACTION_USER_DISABLE);  //用户禁用
            getApplicationContext().registerReceiver(mReceiver, filter) ;
      	}
      }
      
      private void unregistVolumeReceiver(){
      	if(mReceiver!= null){
      		getApplicationContext().unregisterReceiver(mReceiver);
      		mReceiver= null;
      	}
      }
    
  
   

}
