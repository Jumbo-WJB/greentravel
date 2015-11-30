package com.android.greentravel;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.android.greentravel.R;
import net.sunniwell.sunniplayer.Sunniplayer;
import net.sunniwell.sunniplayer.SunniplayerListener;
import com.android.greentravel.bean.CameraBean;
import com.android.greentravel.util.Constant;
import com.android.greentravel.util.Parameter;
import com.android.greentravel.util.SWInterface;
import com.android.greentravel.util.SWSystemUtil;

import org.xwalk.core.XWalkView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
/**
 * 回看
 *
 */
public class RecordActivity extends Activity {

	
	private String TAG = RecordActivity.class.getName();
	private final int MSG_HIDE_LOADING = 30;
	private final int MSG_SHOW_LOADING = 31;
	private final int MSG_HIDE_ERROR = 40;
	private final int MSG_SHOW_ERROR = 41;
	private final int MSG_NETWORKTIP_SHOW= 51;
	private final int MSG_NETWORKTIP_HIDE= 52;
	
	private final int PLAY= 1;
	private final int PAUSE= 0;
	private final int STOP= -1;
	
	private int mLocalHeight = 480;
	private int mLocalWindth = 640;
	private int mHeadHeight = 0;
	
	private CameraBean mCameraBean = null;
	private Parameter parameter;
	private String user= null;
	private String uid= null;
	private String ois_ip= null;  
	private int ois_port= 5000; 
	
	private int playState= STOP;
	
//	private boolean mPlaying = false;
	
	private String mLocation= null;
	private Context mContext;
	
//	private ProgressBar mLoadingLayout = null;
	private View mLoadingLayout= null;
	private View mErrorLayout= null;
	
	private SurfaceView mSurfaceView = null;
	private LinearLayout mPlayBack = null;
	private LinearLayout mPlayController = null; 
	private LinearLayout mHeadController = null; 
	private RelativeLayout mPlayerLayout = null; 
	private RelativeLayout mWebviewLayout = null; 
	public LinearLayout mNetworkTip=null;
	
	
	private ImageButton mPlayPause = null;
	private TextView mCameraDes = null;
	private WalkWebViewWrapper mWvWrapper;
	
	private Sunniplayer player;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_record);
		mContext=RecordActivity.this;
		parameter= new Parameter(mContext);
		
		registVolumeReceiver();
		initData();
		
		initViews();
		initPlay();
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mHandler.sendEmptyMessage(MSG_SHOW_LOADING);
		if(playState== STOP){
			player.startPlay(mLocation, true);
			Log.d(TAG, "----onResume play url----"+mLocation);
		}else if(playState== PAUSE){
			player.play();
		}
	}
	
	private void initData(){
			
			
		user= getIntent().getStringExtra("user");
		uid= getIntent().getStringExtra("cameraid");
		String cameraname= getIntent().getStringExtra("cameraname"); 
		ois_ip= parameter.get("ois_ip");
		ois_port= Integer.parseInt(parameter.get("ois_port")); 
		mCameraBean = new CameraBean(uid);
		mCameraBean.setDescripter(cameraname);
		
		new Thread(){
			@Override
			public void run() {
				
				//获取当前网络模式 （wifi 还是 流量）
				ConnectivityManager connectMgr = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo info = connectMgr.getActiveNetworkInfo();
				if (info != null) {
					Log.d(TAG, "---current network----"+info.getType() + "---" +ConnectivityManager.TYPE_MOBILE+ "--"+ConnectivityManager.TYPE_WIFI);
					if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
						mHandler.sendEmptyMessage(MSG_NETWORKTIP_SHOW);
					}
				}
			}
		}.start();
		
		
	}

	private void initViews(){
		mLoadingLayout = findViewById(R.id.video_loading);
		mErrorLayout = findViewById(R.id.video_error);
		
		
		mPlayerLayout= (RelativeLayout) findViewById(R.id.player_layout);
		mWebviewLayout= (RelativeLayout) findViewById(R.id.webview_layout);
		mHeadController= (LinearLayout) findViewById(R.id.head_controller);
		mHeadHeight= mHeadController.getLayoutParams().height;
		
		mSurfaceView = (SurfaceView)findViewById(R.id.player_surface);
		mSurfaceView.setOnTouchListener(mSurfaceTouchListener);
		
		mPlayBack = (LinearLayout) findViewById(R.id.back);
		mPlayBack.setOnTouchListener(mOnTouchListener);
		
		
		mPlayController = (LinearLayout) findViewById(R.id.play_controller);
		mPlayPause = (ImageButton) findViewById(R.id.play_pause);
		mPlayPause.setOnTouchListener(mOnTouchListener);
		
		mCameraDes=(TextView) findViewById(R.id.camera_des);
		mCameraDes.setText(mCameraBean.getDescripter());
		
		
		XWalkView wv=(XWalkView)findViewById(R.id.webView);
        mWvWrapper= new WalkWebViewWrapper(mContext, wv);
        wv.load("file:///android_asset/timeline2.html?cameraid="+uid, null);
			
        
        switchPortrait();
	}
	
	
	
	
	
	
	
	
	private void initPlay() {
		
		player= new Sunniplayer(mContext, mSurfaceView, mSunniplayerListener, false);
//		player.setDisplayMode(mLocalWindth, (mLocalHeight-mHeadHeight)/2, Sunniplayer.VIDEO_FIT_VERTICAL);
		player.setDisplayMode(mLocalWindth, mLocalHeight, Sunniplayer.VIDEO_BEST_FIT);
		
		String longindex = SWSystemUtil.generateRandomCharAndNumber(10); 
//		mLocation="http://"+ois_ip+":"+ois_port+"/"+uid+".m3u8";  
		/*mLocation= "http://"+ois_ip+":"+ois_port + "/C" + uid+ ".m3u8?" 
				+ "drm=true&ois=" + ois_ip + "&port="
				+ ois_port + "&stbid=" + longindex + "&user="
				+  user + "&cid=C" + uid;  */
		/*mLocation= "http://"+ois_ip+":"+ois_port + "/C" + uid+ ".m3u8"  
				+ "?stbid=" + longindex;*/  
		mLocation= "http://"+ois_ip+":"+ois_port + "/C" + uid+ ".ts"; 
		
		Log.i(TAG, "---play url-----"+mLocation); 
		
			
	}
	
	public void timeUpdate(String time){
		
		Log.d(TAG, "---------update time-----"+time); 
		try {
			SimpleDateFormat fomat= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date date= fomat.parse(time);
			long start= date.getTime()/1000;  
			long end= start + 3600;
			String longindex = SWSystemUtil.generateRandomCharAndNumber(10);
			/*mLocation= "http://"+ois_ip+":"+ois_port + "/C" + uid
					+ ".m3u8?playseek=" + start+"-"+end
					+ "&drm=true&ois=" + ois_ip + "&port="
					+ ois_port + "&stbid=" + longindex + "&user="
					+  user + "&cid=C" + uid;*/
			mLocation= "http://"+ois_ip+":"+ois_port + "/C" + uid
					+ ".m3u8?playseek=" + start+"-"+end
					+  "&stbid=" + longindex;
			Log.d(TAG, "---------play url-----"+mLocation); 
			mHandler.sendEmptyMessage(MSG_SHOW_LOADING);
//			player.stop();  
			player.startPlay(mLocation, true);    
			
			
		} catch (Exception e) {
			Log.e(TAG, "---time=="+time+"-- format error---"+e );
			e.printStackTrace();
		}
		
		
		
		
	}
	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			
			
			case MSG_HIDE_LOADING:
				mErrorLayout.setVisibility(View.GONE);
	     		mLoadingLayout.setVisibility(View.GONE);        		
				break;
			case MSG_SHOW_LOADING:
				mErrorLayout.setVisibility(View.GONE);
	     		mLoadingLayout.setVisibility(View.VISIBLE); 
				break;
			case MSG_HIDE_ERROR:
				mErrorLayout.setVisibility(View.GONE);
				mLoadingLayout.setVisibility(View.GONE);        		
				break;
			case MSG_SHOW_ERROR:
				mLoadingLayout.setVisibility(View.GONE); 
				mErrorLayout.setVisibility(View.VISIBLE); 
				break;
			case MSG_NETWORKTIP_SHOW:
				if(mNetworkTip== null){
					mNetworkTip= (LinearLayout) findViewById(R.id.network_tip);
				}
				mNetworkTip.setVisibility(View.VISIBLE);
				mHandler.sendEmptyMessageDelayed(MSG_NETWORKTIP_HIDE, 5000);
				break;
			case MSG_NETWORKTIP_HIDE:
				mNetworkTip.setVisibility(View.GONE); 
				break; 
			
			default:
				break;
			}
			
		}
	};
	
	@Override
	protected void onPause() { 
		// TODO Auto-generated method stub
		super.onPause();
		Log.d(TAG, "----onPause------- ");
		if(playState== PLAY){
			player.pause();
		}else if(playState== STOP){
			player.stop();
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "----onDestroy------- ");
		kill(); 
	}
	public void kill() {
		player.stop();
		player.release();
		unregistVolumeReceiver();
		Log.i(TAG, "------- kill------");
	}
	
	private void onBack(){
		
		Log.i(TAG, "------- onBack ------");
		kill();
		RecordActivity.this.finish(); 
	}
	
	public void onConfigurationChanged(Configuration config) {
		
		super.onConfigurationChanged(config);
		Log.d(TAG, "----------onConfigurationChanged---" + config.orientation);
		
		if(config.orientation==Configuration.ORIENTATION_LANDSCAPE){
			switchLandscape();
		}
		if(config.orientation==Configuration.ORIENTATION_PORTRAIT){
			switchPortrait();
		}
		
	};
	private void switchPortrait(){
		
		mLocalWindth = getWindowManager().getDefaultDisplay().getWidth();
		mLocalHeight = getWindowManager().getDefaultDisplay().getHeight();
		
		
		RelativeLayout.LayoutParams lp1= (LayoutParams) mPlayerLayout.getLayoutParams();
		lp1.height= (mLocalHeight- mHeadHeight)/2;
		lp1.topMargin= mHeadHeight;
		mPlayerLayout.setLayoutParams(lp1);
		if(player!= null){
//			player.setDisplayMode(mLocalWindth, (mLocalHeight- mHeadHeight)/2, Sunniplayer.VIDEO_FIT_VERTICAL);
			player.setDisplayMode(mLocalWindth, mLocalHeight, Sunniplayer.VIDEO_BEST_FIT);
			Log.d(TAG, "---switchPortrait setDisplayMode---"+mLocalWindth+"--"+(mLocalHeight- mHeadHeight)/2);
		}
		
		RelativeLayout.LayoutParams lp2= (LayoutParams)mWebviewLayout.getLayoutParams();
		lp2.height= (mLocalHeight- mHeadHeight)/2;
		mWebviewLayout.setLayoutParams(lp2);
		mWebviewLayout.setVisibility(View.VISIBLE);
		mHeadController.setVisibility(View.VISIBLE);
		if(playState!= STOP){
			mPlayController.setVisibility(View.VISIBLE);
		}
		
		
		Log.d(TAG, "---switchPortrait---"+mLocalWindth+"--"+mLocalHeight+"--"+mHeadHeight);
		
	}
	
	
	private void switchLandscape(){
		
		mLocalWindth = getWindowManager().getDefaultDisplay().getWidth();
		mLocalHeight = getWindowManager().getDefaultDisplay().getHeight();
		
		
		RelativeLayout.LayoutParams lp1= (LayoutParams) mPlayerLayout.getLayoutParams();
		lp1.height= mLocalHeight;
		lp1.topMargin= 0;
		mPlayerLayout.setLayoutParams(lp1);
		
		if(player!= null){
//			player.setDisplayMode(mLocalWindth, mLocalHeight, Sunniplayer.VIDEO_4_3);   
			mHandler.postDelayed(new Runnable() {
				
				@Override
				public void run() { //需要延时200毫秒后 再设置 displaymode, 否则视频横屏时可能会出现 半屏画面的现象
					player.setDisplayMode(mLocalWindth, mLocalHeight, Sunniplayer.VIDEO_BEST_FIT); 
				}
			}, 200);    
			
			Log.d(TAG, "---switchLandscape setDisplayMode---"+mLocalWindth+"--"+mLocalHeight);
		}
		
		mWebviewLayout.setVisibility(View.GONE);
		mHeadController.setVisibility(View.VISIBLE);
		if(playState!= STOP){
			mPlayController.setVisibility(View.VISIBLE);
		}
		Log.d(TAG, "---switchLandscape---"+mLocalWindth+"--"+mLocalHeight+"--"+mHeadHeight); 
	}
	
	private View.OnTouchListener mSurfaceTouchListener = new View.OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction() & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_UP:
					
					if(getResources().getConfiguration().orientation== Configuration.ORIENTATION_LANDSCAPE){
						if(mHeadController.getVisibility()== View.VISIBLE){
							mHeadController.setVisibility(View.GONE);
						}else{
							mHeadController.setVisibility(View.VISIBLE);
						}
						
					}
					
					
					if(playState== STOP){
						return true;
					}
					if(mPlayController.getVisibility()== View.VISIBLE){
						mPlayController.setVisibility(View.GONE);
					}else{
						mPlayController.setVisibility(View.VISIBLE);
					}
					break;
			}
			return true;
		}
	};
	
	private View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			switch (motionEvent.getAction()) {
			case MotionEvent.ACTION_DOWN:
				
				if (view.getId() == R.id.back) {
					onBack();
				} 
				
				
				break;
			case MotionEvent.ACTION_UP:
				if(view.getId()== R.id.play_pause){
					if (playState== PLAY){
						player.pause();
						mPlayPause.setBackgroundResource(R.drawable.ctrl_pause);
					}else if(playState== PAUSE){
						player.play();
						mPlayPause.setBackgroundResource(R.drawable.ctrl_play); 
					}
				}
				
				break;
			}
			return true;
		}
	};
	
	
	private SunniplayerListener mSunniplayerListener = new SunniplayerListener(){

		@Override
        public void onPlayerPlaying() {
			Log.i(TAG, "onPlayerPlaying");
			
			mSurfaceView.setVisibility(View.VISIBLE);
			mPlayPause.setBackgroundResource(R.drawable.ctrl_play);
			mPlayController.setVisibility(View.VISIBLE);
			mHandler.sendEmptyMessage(MSG_HIDE_LOADING);
        }

        @Override
        public void onPlayerPaused() {
        	playState= PAUSE;
            Log.i(TAG, "onPlayerPaused");
        }

		@Override
		public void onPlayerStoped() {
			Log.i(TAG, " on onPlayerStoped");
			playState= STOP;
			
        }

		@Override
		public void onPlayerEndReached() {
			Log.i(TAG, " onPlayerEndReached"); 
			playState= STOP;
			mSurfaceView.setVisibility(View.GONE);
			mPlayController.setVisibility(View.INVISIBLE);
			mHandler.sendEmptyMessage(MSG_SHOW_ERROR);
			
			

        }

        @Override
        public void onPlayerPositionChanged() {
        	Log.i(TAG, "onPlayerPositionChanged");
        	playState= PLAY;
        }

		@Override
		public void onPlayerEncounteredError() {
			Log.i(TAG, "onPlayerEncounteredError");
			playState= STOP;
			mSurfaceView.setVisibility(View.GONE);
			mPlayController.setVisibility(View.INVISIBLE); 
			mHandler.sendEmptyMessage(MSG_SHOW_ERROR);
			
		}

	       
	};
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {	
			onBack();
			
		} 
	
		return super.onKeyDown(keyCode, event);
	}
	
	
	//接收用户禁用广播
	private class MyReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
            String action= intent.getAction();
            Log.d(TAG, "----receive action---"+action); 
            if(action.equals(Constant.ACTION_USER_DISABLE)){ //用户禁用
            	
            	
            	new AlertDialog.Builder(mContext)
				.setMessage(mContext.getString(R.string.user_disable))
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(mContext.getString(R.string.tips))
				.setCancelable(false)
				.setPositiveButton(mContext.getString(R.string.ok), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						onBack();
					}
				}).show();
            	
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
