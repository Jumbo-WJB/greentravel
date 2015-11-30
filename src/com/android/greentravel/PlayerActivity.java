package com.android.greentravel;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

import com.android.greentravel.R;
import net.sunniwell.sunniplayer.Sunniplayer;
import net.sunniwell.sunniplayer.SunniplayerListener;
import com.android.greentravel.bean.CameraBean;
import com.android.greentravel.speex.Speex;
import com.android.greentravel.util.Constant;
import com.android.greentravel.util.EncG726;
import com.android.greentravel.util.Parameter;
import com.android.greentravel.util.SWInterface;
import com.android.greentravel.util.UtilBitmap;
import com.android.greentravel.view.MotionDetectionView;
import com.android.greentravel.view.VerticalSeekBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
/**
 * 直播界面
 *
 */
public class PlayerActivity extends Activity {
	
	private String TAG = PlayerActivity.class.getName();
	private final int CLARITY_HD= 0;  //高清
	private final int CLARITY_SD= 1;  //标清
	private final int CLARITY_LD= 2;  //流畅
	
	private final int MSG_SPEAK_DOWN = 2;
	private final int MSG_SPEAK_UP = 3;
	private final int MSG_SNATE_SHOW = 6;
	private final int MSG_ALARM_SHOW = 8;
	private final int MSG_ALARM_HIDE = 9;
	private final int MSG_HIDE_LOADING = 30;
	private final int MSG_SHOW_LOADING = 31;
	private final int MSG_SWITCHLIVESHOW= 40;
	private final int MSG_INITMONITONDATA= 50;
	private final int MSG_FLIP_SUCCESS= 37;
	private final int MSG_FLIP_FAIL= 38;
	private final int MSG_CLARITY_SUCCESS= 41;
	private final int MSG_CLARITY_FAIL= 42;
	private final int MSG_NETWORKTIP_SHOW= 51;
	private final int MSG_NETWORKTIP_HIDE= 52;
	
	

	
	private Vibrator vibrator;
	
	private String user= null;
	private String uid= null;
	private String ois_ip= null;  
	private int ois_port= 5000;  
	private String mac= null;
	
	private int mAlertShowNum= 0;
	private boolean mPlaying = false;
	private int mCurrentVolume = 0;
	private boolean bHide=false;
	private boolean mShowMotion = false;
	
	private int mWindth = 1280;
	private int mHeight = 720;
	private int mLocalHeight = 480;
	private int mLocalWindth = 640;
	private int mVDAWidth= 320;  //摄像头监测的布防区域宽度为320, 最后设置到摄像头的布防区域的x、width  需要  按照视频分辨率的宽和320的比例在计算 
	private int mVDAHeight= 240; //摄像头监测的布防区域高度为240，最后设置到摄像头的布防区域y、height  需要  按照视频分辨率的高和240的比例在计算 
	private double mZoom;  //视频分辨率和设备真实播放区域的比例
	private  double mZoomX;
	private  double mZoomY;
	private static int mEndgeTopY;
	private static int mEndgeBottomY;
	private static int mEndgeLeftX;
	private static int mEndgeRightX;
	private String mLocation= null;
	private Context mContext;
	
	private CameraBean mCameraBean = null;
	private Parameter parameter;

	
	private AudioManager mAudioManager = null;
	private Speex speex= null;
	private AudioRecord localAudioRecord = null;
	
	
	private SurfaceView mSurfaceView = null;
	private ProgressBar mLoadingLayout = null;
	private VerticalSeekBar miniVideoVolumnbar = null;
	private ImageView mPlayVoice = null;
	private ImageView mSpeakView = null;
    private ImageView mSpeakBigView = null;
    private ImageView mCameraView = null;
    private ImageView mClarityView = null;
    private LinearLayout mPlayBack = null;
    private LinearLayout mCameraController = null;
    private LinearLayout mVoiceController = null;
    private LinearLayout mHeadController = null;
    private LinearLayout mVdaStartController = null;
    private LinearLayout mVdaAppController = null;
    public LinearLayout mNetworkTip=null;
    private RelativeLayout malarmController = null;
    public RelativeLayout mMotionMain=null;
    public RelativeLayout mFramMain=null;
    private ImageView mMotionStart = null;
    private ImageView mMotionAPP = null;
    private TextView mCameraDes = null;
    private ImageView mFlip = null; //翻转
    
    private Sunniplayer player;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_player);
		
		//此处为特殊处理
		//由于 使用了 crosswalk浏览器，调用播放时横屏播放 界面上的元素都为乱码， （用普通浏览器没事儿），  临时解决办法： Manifest中定义PlayerActivity强制竖屏， onCreate时强制横屏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置成全屏模式  
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//强制为横屏  
		
		System.out.println("----------1111onCreate----");
		mContext=PlayerActivity.this;
		parameter= new Parameter(mContext);
		
		
		
		
		
		initData();
		initViews();
        
        initPlay();
//        initSpeak();
//        initMoniton();
        
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		System.out.println("----------1111onResume----");
		super.onResume();
		if(!mPlaying){
			mHandler.sendEmptyMessage(MSG_SHOW_LOADING);
			mHandler.postDelayed(new Runnable() {  //如果不延时播放 当手机处于直播界面锁屏后，有些手机密码解锁后 直播界面缩小一倍 
				@Override
				public void run() {
					player.startPlay(mLocation, true);  
				}
			}, 500);  
			
		}
		initSpeak();  
	}
	
	/*@Override
	protected void onRestart() {
		super.onRestart();
		Log.i(TAG, "onRestart ");
		
		if(!mPlaying){
			mHandler.sendEmptyMessage(MSG_SHOW_LOADING);
			MediaPlayerManager.getManager().startPlay(mLocation, mSurfaceView, mLiveSunniplayerListener, -1);
		}
		initSpeak();
	}*/
	
	private void initData(){
		
		
		user= getIntent().getStringExtra("user");
		uid= getIntent().getStringExtra("cameraid");
		String cameraname= getIntent().getStringExtra("cameraname"); 
		/*user= "18600366235";
		uid= "8140-01D4-13141858";
		String cameraname= "";*/
		ois_ip= parameter.get("ois_ip");
		ois_port= Integer.parseInt(parameter.get("ois_port")); 
		mac= parameter.get("mac");
		mCameraBean = new CameraBean(uid);
		mCameraBean.setDescripter(cameraname);
		
		mLocalWindth = getWindowManager().getDefaultDisplay().getWidth();
		mLocalHeight = getWindowManager().getDefaultDisplay().getHeight();
		
		
		new Thread(){
			@Override
			public void run() {
				//获取布防数据
				int resolution= SWInterface.getResolution(parameter, uid);
				mCameraBean.setClarity(resolution);
				int[] monitioninfo= SWInterface.getDetection(parameter, uid);
				Log.d(TAG, "--resolution--"+resolution +"--monitioninfo--"+monitioninfo);
				if(monitioninfo== null){
					mCameraBean.setMotionDetection(false);
				}else{
					mCameraBean.setMotionDetection(true);
					if(monitioninfo.length== 0){
						monitioninfo= null;
					}
					
				}
				mCameraBean.setMonitioninfo(monitioninfo);  
				mHandler.sendEmptyMessage(MSG_INITMONITONDATA);
			
				
				
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
		
		vibrator = (Vibrator) getSystemService(this.VIBRATOR_SERVICE);
		mCameraController = (LinearLayout) findViewById(R.id.camera_controller);
		mVoiceController = (LinearLayout) findViewById(R.id.voice_controller);
		malarmController = (RelativeLayout) findViewById(R.id.alarm_controller);
		mVdaStartController = (LinearLayout) findViewById(R.id.vda_start_controller);
		mVdaAppController = (LinearLayout) findViewById(R.id.vda_app_controller);
		mHeadController = (LinearLayout) findViewById(R.id.head_controller);
		mFramMain=(RelativeLayout)findViewById(R.id.playmian);
		mLoadingLayout = (ProgressBar)findViewById(R.id.video_content_loading);
        mSurfaceView = (SurfaceView)findViewById(R.id.player_surface);
        mSurfaceView.setOnTouchListener(mSurfaceTouchListener);
        mMotionMain=(RelativeLayout)findViewById(R.id.motionmian);
        
        
        
        
        miniVideoVolumnbar = (VerticalSeekBar) findViewById(R.id.mini_video_volumnbar);
		miniVideoVolumnbar.setOnSeekBarChangeListener(mVolumeBarChangeListener);
		mPlayVoice = (ImageView) findViewById(R.id.play_vioce);
		mPlayVoice.setOnTouchListener(mOnTouchListener);
		mAudioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
		registVolumeReceiver(); //注册当音量发生变化时接收的广播, 一定要放到 536行setVolume(volume, true)前面
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		int mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		miniVideoVolumnbar.setMax(mMaxVolume);
		
		mCurrentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		miniVideoVolumnbar.setProgress(mCurrentVolume);
		if(mCurrentVolume== 0){
			mPlayVoice.setImageResource(R.drawable.player_none_mute_bg);
		}
			
		
		mSpeakView = (ImageView)findViewById(R.id.speak);
        mSpeakView.setOnTouchListener(mOnTouchListener); 
        mSpeakBigView = (ImageView)findViewById(R.id.speak_big);
        
        
        mCameraView = (ImageView)findViewById(R.id.camera);
        mCameraView.setOnTouchListener(mOnTouchListener);
        
        
        mClarityView = (ImageView)findViewById(R.id.clarity);
        mClarityView.setOnTouchListener(mOnTouchListener);
        
        mPlayBack = (LinearLayout) findViewById(R.id.back);
		mPlayBack.setOnTouchListener(mOnTouchListener);
        
		mFlip = (ImageView) findViewById(R.id.flip);
		mFlip.setOnTouchListener(mOnTouchListener);
    	
		
		
		mMotionStart = (ImageView) findViewById(R.id.vda_start);
		mMotionStart.setOnLongClickListener(mOnLongClickListener);
		mMotionStart.setOnClickListener(mOnClickListener);
		mMotionAPP = (ImageView) findViewById(R.id.vda_app);
		mMotionAPP.setOnTouchListener(mOnTouchListener);
		
		mCameraDes=(TextView) findViewById(R.id.camera_des);
		if(mCameraBean!=null)
		{
			mCameraDes.setText(mCameraBean.getDescripter());
			
		}
		
	}
	
	
	
	
	
	
	
	
	
	
	private void initPlay() {
		
		player= new Sunniplayer(mContext, mSurfaceView, mLiveSunniplayerListener, false);
		player.setDisplayMode(mLocalWindth, mLocalHeight, Sunniplayer.VIDEO_BEST_FIT);
		
//		String longindex = SWSystemUtil.generateRandomCharAndNumber(10);	
		
		
		/*mLocation= "http://" + ois_ip + ":" + ois_port + "/C" + uid
				+ ".ts?drm=true&ois=" + ois_ip + "&port="
				+ ois_port + "&stbid=" + longindex +  "&user=" + user + "&cid=C" + uid;*/
		mLocation= "http://" + ois_ip + ":" + ois_port + "/C" + uid
				+ ".ts"; 
		
//		mLocation= "http://172.16.15.101:5000/C8140-01D4-13142234.ts?drm=true&ois=172.16.15.101&port=5000&stbid=3167950737&user=18600366235&cid=C8140-01D4-13142234";
		Log.i(TAG, mLocation);
//		mHandler.sendEmptyMessage(MSG_SHOW_LOADING);	
//		MediaPlayerManager.getManager().startPlay(mLocation, mSurfaceView, mLiveSunniplayerListener, -1);
		
			
	}
	private void initMonitonData(){
		
		/*if(mCameraBean.getClarity()== CameraBean.CLARITY_HD){ //高清
			mWindth= 1280;
			mHeight= 720;
		}else if(mCameraBean.getClarity()== CameraBean.CLARITY_LD){ //流畅
			mWindth= 320;
			mHeight= 240;
		}else{ //标清
			mWindth= 640;
			mHeight= 480;
		}*/
		
		Log.d(TAG, "-------------#####---"+mLocalWindth + "---"+mLocalHeight+"---"+mWindth+"---"+mHeight);
		
		
		int _height= mHeight*mLocalWindth/mWindth;
		if(_height> mLocalHeight){ //视频播放高度与设备高度一致
			mZoom= ((double) mHeight) / ((double) mLocalHeight);
			mZoomX = ((double) mVDAWidth) / ((double) mWindth);
			mZoomY = ((double) mVDAHeight) / ((double) mHeight);
			mEndgeTopY= 0;
			mEndgeBottomY= 0;
			mEndgeLeftX = (int) ((mLocalWindth - ((double) mLocalHeight) / ((double) mHeight) * mWindth) / 2);
			mEndgeRightX = mLocalWindth - (int) ((mLocalWindth - ((double) mLocalHeight) / ((double) mHeight) * mWindth) / 2);
		}else{ //视频播放宽度与设备宽度一致
			mZoom= ((double) mWindth) / ((double) (mLocalWindth));
			mZoomX = ((double) mVDAWidth) / ((double) mWindth);
			mZoomY = ((double) mVDAHeight) / ((double) mHeight);
			mEndgeLeftX= 0;
			mEndgeRightX= 0;
			mEndgeTopY = (int) ((mLocalHeight - ((double) mLocalWindth) / ((double) mWindth) * mHeight) / 2);
			mEndgeBottomY = mLocalHeight - (int) ((mLocalHeight - ((double) mLocalWindth) / ((double) mWindth) * mHeight) / 2);
			
		}
		
		Log.i(TAG, "mZoom="+mZoom+"--mZoomX="+mZoomX+"--mZoomY="+mZoomY+
				"--mEndgeLeftX="+mEndgeLeftX+"--mEndgeRightX="+mEndgeRightX+
				"--mEndgeTopY="+mEndgeTopY+"--mEndgeBottomY="+mEndgeBottomY);
		
		
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		lp.setMargins(mLocalWindth -mEndgeLeftX - 70, 40,  mEndgeLeftX + 10, mLocalHeight - 110);
		malarmController.setLayoutParams(lp);
		
		
        RelativeLayout.LayoutParams lp1 = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
		lp1.setMargins(mEndgeLeftX, mEndgeTopY, mEndgeLeftX,mEndgeTopY);
		mMotionMain.setLayoutParams(lp1);
		
		if(mCameraBean.getMotionDetection()){
			mMotionStart.setImageResource(R.drawable.vda_focus);//如果开启布防，则布防按钮显示高亮
		}
	}
	
	
	public void initMoniton() {
		Log.i(TAG, "initMoniton");
		
		mMotionMain.removeAllViews(); 
		if (mCameraBean!=null&&mCameraBean.getMotionDetection() && mShowMotion) {
			MotionDetectionView motion = new MotionDetectionView(mContext, mHandler);
			
			int[] monitioninfo= mCameraBean.getMonitioninfo();
			if(monitioninfo== null){
//				int[] monitioninfo= new int[]{0, 0, ((int) (((double) (mLocalWindth - mEndgeLeftX * 2)) / (double) 2)), ((int) (((double) (mLocalHeight - mEndgeTopY * 2)) / (double) 2))};
				monitioninfo= new int[]{0, 0, 160, 120}; 
				mCameraBean.setMonitioninfo(monitioninfo);
			}
			int[] monitioninfoS= new int[4];
			monitioninfoS[0]=(int)(((double)monitioninfo[0]/(double)mZoomX)/(double)mZoom);
			monitioninfoS[1]=(int)(((double)monitioninfo[1]/(double)mZoomY)/(double)mZoom);
			monitioninfoS[2]=(int)(((double)monitioninfo[2]/(double)mZoomX)/(double)mZoom);     
			monitioninfoS[3]=(int)(((double)monitioninfo[3]/(double)mZoomY)/(double)mZoom);    
			
			motion.initMoiton(monitioninfoS, (mLocalWindth - 2*mEndgeLeftX), (mLocalHeight - 2*mEndgeTopY), mZoomY);

			ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
			mMotionMain.addView(motion, layoutParams);
			motion.bringToFront();
			motion.setVisibility(View.VISIBLE);
			motion.setUid(uid);
		}

		malarmController.bringToFront();
		mFramMain.invalidate();

	}
	
	
	/*@Override
	protected void onStop() {
		super.onStop();
		Log.i(TAG, "onStop ");
		System.out.println("----------1111onStop----");
		MediaPlayerManager.getManager().stop();
		releaseSpeak();
		mPlaying=false;
		
	}*/
	
	@Override
	protected void onPause() { 
		// TODO Auto-generated method stub
		super.onPause();
		Log.i(TAG, "onPause ");
		System.out.println("----------1111onPause----");
		player.stop();
		releaseSpeak();
		mPlaying=false;
	}
	
	@Override
	protected void onDestroy() {
		System.out.println("----------1111onDestroy----");
		super.onDestroy();		
		Log.i(TAG, "onDestroy ");
		
		kill();
	}
	
	public void kill() {
		Log.i(TAG, " on kill");
		player.stop();
		player.release();
		unregistVolumeReceiver();
		releaseSpeak();
		if(vibrator!=null){
			vibrator.cancel();
		}
		
	}
	
	private void onBack(){
		
		System.out.println("----------1111back----");
		kill();
		setResult(Constant.PLAY_RESULTCODE);
		PlayerActivity.this.finish(); 
	}
	
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {	
			onBack();
		} 

		return super.onKeyDown(keyCode, event);
	}
	
	private View.OnLongClickListener mOnLongClickListener= new View.OnLongClickListener(){

		@Override
		public boolean onLongClick(View view) {
			if (view.getId() == R.id.vda_start) {
				
				if (mCameraBean != null) {
					
					if(!mCameraBean.getMotionDetection()){
						mCameraBean.setMotionDetection(true);
						new Thread(){
							public void run() {
								SWInterface.setDetection(parameter, uid, 1);
							};
						}.start();
					}
					
					
					mShowMotion = true;
					
//					mMotionAPP.setImageResource(R.drawable.vda_ok);
					mVdaAppController.setVisibility(View.VISIBLE);
					mVdaStartController.setVisibility(View.GONE);
					mHeadController.setVisibility(View.INVISIBLE);
					
					mSurfaceView.setOnTouchListener(null);   //调整布防区域大小时  关闭mSurfaceView上的touch监听 （如果不关闭，则调整布防区域大小时也会触发 mSurfaceView的touch事件）
					initMoniton();
				}
				
				
				
			}
			return true;
		}
		
	};
	
	
	private AlertDialog alertDialog= null;
	private View.OnClickListener mOnClickListener= new View.OnClickListener(){

		@Override
		public void onClick(View view) {
			if (view.getId() == R.id.vda_start) {
				if (mCameraBean != null) {
					if(mCameraBean.getMotionDetection()){
						
						if(alertDialog== null){
							alertDialog= new AlertDialog.Builder(mContext)
							   .setCancelable(false)
							   .setTitle(getString(R.string.tips))
							   .setMessage(getString(R.string.ask_motionDetection))
							   .setPositiveButton(getString(R.string.close), new DialogInterface.OnClickListener() {

									@Override
									public void onClick( DialogInterface dialog, int which) {
										dialog.dismiss();
										alertDialog= null;
										mMotionStart.setImageResource(R.drawable.vda);
										mCameraBean.setMotionDetection(false);
										new Thread(){
											public void run() {
												SWInterface.setDetection(parameter, uid, 2);
											};
										}.start();
										
										
									}
								})
								.setNegativeButton(getString(R.string.cancle), new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										dialog.dismiss();
										alertDialog= null;
									}
								}).create();
							alertDialog.show();
						}
							
					}else{
						mShowMotion = true;
						mCameraBean.setMotionDetection(true);
						new Thread(){
							public void run() {
								SWInterface.setDetection(parameter, uid, 1);
							};
						}.start();
//						mMotionAPP.setImageResource(R.drawable.vda_ok);
						mVdaAppController.setVisibility(View.VISIBLE);
						mVdaStartController.setVisibility(View.GONE);
						mHeadController.setVisibility(View.INVISIBLE);
						mSurfaceView.setOnTouchListener(null);   //调整布防区域大小时  关闭mSurfaceView上的touch监听 （如果不关闭，则调整布防区域大小时也会触发 mSurfaceView的touch事件）
						initMoniton();
					}
				}
			}
		}
		
	};
	
	private View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			switch (motionEvent.getAction()) {
			case MotionEvent.ACTION_DOWN:
				
				if (view.getId() == R.id.speak) {
					mSpeakView.setImageResource(R.drawable.speak_focus);
					mSpeakBigView.setVisibility(View.VISIBLE);
					mHandler.sendEmptyMessageDelayed(MSG_SPEAK_DOWN, 200);
				}else if (view.getId() == R.id.camera) {
					mCameraView.setImageResource(R.drawable.camera_focus);
				}else if (view.getId() == R.id.clarity) {
					mClarityView.setImageResource(R.drawable.cloud_focus);
				}else if (view.getId() == R.id.vda_app) {
					mMotionAPP.setImageResource(R.drawable.vda_ok_focus);
				}else if(view.getId()== R.id.flip){
					mFlip.setImageResource(R.drawable.flip_focus);
				}else if (view.getId() == R.id.back) {
					onBack();
				} 
				
				
				break;
			case MotionEvent.ACTION_UP:
				if (view.getId() == R.id.speak) {
					mSpeakView.setImageResource(R.drawable.speak);
					mSpeakBigView.setVisibility(View.INVISIBLE);
					mHandler.removeMessages(MSG_SPEAK_DOWN);
					mHandler.sendEmptyMessage(MSG_SPEAK_UP);
				}else if (view.getId() == R.id.play_vioce) {
					SwicthVoice();
				}else if (view.getId() == R.id.camera) {
					mCameraView.setImageResource(R.drawable.camera);
					if (mPlaying) {
						mHandler.sendEmptyMessage(MSG_SWITCHLIVESHOW);
						SnapShort(); 
					}
					

				}else if (view.getId() == R.id.clarity) {
					mClarityView.setImageResource(R.drawable.cloud);
					
					showClarityMenu(view);
					
					
				}else if (view.getId() == R.id.vda_app) {
					mShowMotion = false;
					initMoniton();
					mMotionAPP.setImageResource(R.drawable.vda_ok);
					
					if(mCameraBean.getMotionDetection()){ //如果开启侦测，则按钮显示高亮
						mMotionStart.setImageResource(R.drawable.vda_focus);
					}else{
						mMotionStart.setImageResource(R.drawable.vda);
					}
					mVdaAppController.setVisibility(View.GONE);
					mVdaStartController.setVisibility(View.VISIBLE);
					mHeadController.setVisibility(View.VISIBLE);
					mSurfaceView.setOnTouchListener(mSurfaceTouchListener);   //布防区域调整完后  重新开启mSurfaceView上的touch监听
				}else if(view.getId()== R.id.flip){
					mFlip.setImageResource(R.drawable.flip);
					
					
					if(alertDialog== null){
						alertDialog= new AlertDialog.Builder(mContext)
						   .setCancelable(false)
						   .setTitle(getString(R.string.tips))
						   .setMessage(getString(R.string.ask_flip))
						   .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {

								@Override
								public void onClick( DialogInterface dialog, int which) {
									dialog.dismiss();
									alertDialog= null;
									
									new Thread(){
										public void run() {
											boolean success= SWInterface.setFlip(parameter, uid);
											if(success){
												mHandler.sendEmptyMessage(MSG_FLIP_SUCCESS);
											}else{
												mHandler.sendEmptyMessage(MSG_FLIP_FAIL);
											}
											
										};
									}.start();
									
									
								}
							})
							.setNegativeButton(getString(R.string.cancle), new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();
									alertDialog= null;
								}
							}).create();
						alertDialog.show();
					}
				} 
				
				break;
			}
			return true;
		}
	};
	
	
	/*---------------------------清晰度设置相关代码--------------*/
	private PopupWindow clarityMenu= null;
	private TextView mClarity_LD, mClarity_SD, mClarity_HD;
	public void showClarityMenu(View view){
		if(clarityMenu== null){
			View menuView = LayoutInflater.from(mContext).inflate(R.layout.clarity_menu, null);
			menuView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);  //强制绘制menuView，并且马上初始化menuView的尺寸 ， 否则 clarityMenu.getContentView().getMeasuredHeight()获取不到值或者值为-2
			clarityMenu = new PopupWindow(menuView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
			
			mClarity_LD= (TextView) menuView.findViewById(R.id.clarity_LD);
			mClarity_LD.setOnTouchListener(mClarityMenuTouchListener);
			mClarity_SD= (TextView) menuView.findViewById(R.id.clarity_SD);
			mClarity_SD.setOnTouchListener(mClarityMenuTouchListener);
			mClarity_HD= (TextView) menuView.findViewById(R.id.clarity_HD);
			mClarity_HD.setOnTouchListener(mClarityMenuTouchListener);
			
			
			clarityMenu.setTouchable(true);
			clarityMenu.setTouchInterceptor(new OnTouchListener() {
	            @Override
	            public boolean onTouch(View v, MotionEvent event) {
	            	return false;
	            }
	        });

			clarityMenu.setBackgroundDrawable(new ColorDrawable(R.color.btn_gray));
		}
		
        if(!clarityMenu.isShowing()){
        	mClarity_LD.setTextColor(getResources().getColor(R.color.white));
			mClarity_SD.setTextColor(getResources().getColor(R.color.white));
			mClarity_HD.setTextColor(getResources().getColor(R.color.white));
			if(mCameraBean.getClarity()== CLARITY_LD){
				mClarity_LD.setTextColor(getResources().getColor(R.color.text_blue));
			}else if(mCameraBean.getClarity()== CLARITY_SD){
				mClarity_SD.setTextColor(getResources().getColor(R.color.text_blue));
			}else if(mCameraBean.getClarity()== CLARITY_HD){
				mClarity_HD.setTextColor(getResources().getColor(R.color.text_blue));
			}
        	int[] location = new int[2];
            view.getLocationOnScreen(location);
        	clarityMenu.showAtLocation(view, Gravity.NO_GRAVITY, location[0], location[1]-clarityMenu.getContentView().getMeasuredHeight());    
        }
		
	}
	
	private View.OnTouchListener mClarityMenuTouchListener = new View.OnTouchListener() {

		@Override
		public boolean onTouch(View view, MotionEvent event) {
			
			switch (event.getAction()) {
				case MotionEvent.ACTION_UP:
					
					mClarity_LD.setTextColor(getResources().getColor(R.color.white));
					mClarity_SD.setTextColor(getResources().getColor(R.color.white));
					mClarity_HD.setTextColor(getResources().getColor(R.color.white));
					((TextView)view).setTextColor(getResources().getColor(R.color.text_blue));
					
					int value= 0;
					if (view.getId() == R.id.clarity_LD) { 
						value= CLARITY_LD;
					}else if (view.getId() == R.id.clarity_SD) { 
						value= CLARITY_SD;
					}else if (view.getId() == R.id.clarity_HD) { 
						value= CLARITY_HD;
					}
					mHandler.postDelayed(new Runnable() {
						@Override
						public void run() {
							clarityMenu.dismiss();
						}
					}, 200);
					if(mCameraBean.getClarity()== value){ //要设置的清晰度和当前相同,不重复设置
						return true;
					}
					
					player.stop();
					mHandler.sendEmptyMessage(MSG_SHOW_LOADING); 
					System.out.println("-----1111停止----------");
					final int clarity= value;
					new Thread(){
						public void run() {
							boolean success= SWInterface.setResolution(parameter, uid, clarity);
							if(success){
								mCameraBean.setClarity(clarity);
								mHandler.sendEmptyMessage(MSG_INITMONITONDATA); 
								mHandler.sendEmptyMessage(MSG_CLARITY_SUCCESS);
							}else{
								mHandler.sendEmptyMessage(MSG_CLARITY_FAIL);
							}
						};
					}.start();
					
					
					break;
			
			}
			
			return true;
		}
		
	};
	
	
	public void SwitchLiveShow() {

		if (bHide) {
			bHide = false;
			Log.i(TAG, "changed visible bHide:" + bHide);
			if(mPlaying){
				if (mCameraController.getVisibility() == View.INVISIBLE) {
					mCameraController.setVisibility(View.VISIBLE);
				}
			}
			

			if (mVoiceController.getVisibility() == View.INVISIBLE) {
				mVoiceController.setVisibility(View.VISIBLE);
			}
			if (mHeadController.getVisibility() == View.INVISIBLE) {
				mHeadController.setVisibility(View.VISIBLE);
			}
		} else {
			bHide = true;
			Log.i(TAG, "changed visible bHide:" + bHide);
			if(mPlaying){
				if (mCameraController.getVisibility() == View.VISIBLE) {
					mCameraController.setVisibility(View.INVISIBLE);
				}
			}
			


			if (mVoiceController.getVisibility() == View.VISIBLE) {
				mVoiceController.setVisibility(View.INVISIBLE);
			}
			if (mHeadController.getVisibility() == View.VISIBLE) {
				mHeadController.setVisibility(View.INVISIBLE);
			}

		}
	}
	
	
	/*-------------------------------抓拍相关代码---------------------*/
	public void SnapShort() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					
					String name= System.currentTimeMillis() + ".jpg";
					//抓拍保存至图库   app_name目录下
					File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), mContext.getString(R.string.app_name));
					if (!file.exists()) {
						file.mkdirs();
					}
					File file1 = new File(file, name);
					if (player.takeSnapShot(file1.getAbsolutePath(), 0, 0)) {

						if (file1.exists()) {
							Bitmap mBitmap = null;
							mBitmap = UtilBitmap.getBitmap(file1.getPath());
							if (mBitmap != null) {
								mHandler.sendEmptyMessage(MSG_SNATE_SHOW);
							}
						    mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file1)));  
						}
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
				mHandler.sendEmptyMessage(MSG_SWITCHLIVESHOW);

			}
		}).start();

	}
	
	
	/*-------------------------------音量调节相关代码---------------------*/
	private SeekBar.OnSeekBarChangeListener mVolumeBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			Log.i(TAG, "seek changed ");
			setVolume(progress);
			
		}

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
		}
	};
	
	private void setVolume(int volume) {
		try {
			Log.i(TAG, "volume: " + volume + " mCurrentVolume:" + mCurrentVolume + " mMuteFlag:" );
			if (volume == mCurrentVolume)
				return;
			Log.i(TAG, "start setVolume ok");
			mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
			mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
			if (volume == 0) {
				mPlayVoice.setImageResource(R.drawable.player_none_mute_bg);
			} else {
				mPlayVoice.setImageResource(R.drawable.player_mute_bg);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	//private int preVolume= 0;
	public void SwicthVoice(){
		if(mCurrentVolume!= 0){
			Constant.preVolume= mCurrentVolume;
			setVolume(0);
		}else{
			setVolume(Constant.preVolume); 
		}
		
	}
	
	//接收音量变化广播、 摄像头报警广播
	private class MyReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
            String action= intent.getAction();
            Log.d(TAG, "----receive action---"+action); 
            if(action.equals("android.media.VOLUME_CHANGED_ACTION")){
            	mCurrentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) ;// 当前的媒体音量
                int volume= miniVideoVolumnbar.getProgress();
                Log.d(TAG, "----mCurrentVolume="+mCurrentVolume+"---volume="+volume);
                if(mCurrentVolume!= volume){ //如果音量发生变化则更改seekbar的位置
                	Log.d(TAG, "--------set miniVideoVolumnbar volume="+mCurrentVolume);
                	miniVideoVolumnbar.setProgress(mCurrentVolume);
                	if(mCurrentVolume== 0){
                		mPlayVoice.setImageResource(R.drawable.player_none_mute_bg);
                	}else{
                		mPlayVoice.setImageResource(R.drawable.player_mute_bg);
                	}
                }
            }else if(action.equals(Constant.ACTION_CAMERA_ALERT)){
            	
            	String camera_id= intent.getStringExtra("camera_id");
            	Log.d(TAG, "---alert camera_id="+camera_id+"--currentUid="+uid);
            	if(uid.equals(camera_id)){
            		mAlertShowNum= 0; 
            		mHandler.sendEmptyMessage(MSG_ALARM_SHOW);  
            	}
            }else if(action.equals(Constant.ACTION_USER_DISABLE)){ //用户禁用
            	
            	
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
            filter.addAction("android.media.VOLUME_CHANGED_ACTION") ;   //系统音量变化
            filter.addAction(Constant.ACTION_CAMERA_ALERT);  //摄像头报警
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
	
	
	
	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			
			
			case MSG_INITMONITONDATA:
				initMonitonData();
				break;
			
			case MSG_HIDE_LOADING:
			    if(mLoadingLayout.getVisibility()==View.VISIBLE){
		     		mLoadingLayout.setVisibility(View.INVISIBLE);        		
		     	}
				break;
			case MSG_SHOW_LOADING:
			    if(mLoadingLayout.getVisibility()==View.INVISIBLE){
		     		mLoadingLayout.setVisibility(View.VISIBLE);        		
		     	}
				break;
				
				
			case MSG_SPEAK_DOWN:
				
				
				miniVideoVolumnbar.setEnabled(false); //对讲时不允许调节音量
				mPlayVoice.setEnabled(false); //对讲时不允许调节音量
				startSpeaking();
				
				break;
			case MSG_SPEAK_UP:
				stopSpeaking();
				miniVideoVolumnbar.setEnabled(true); 
				mPlayVoice.setEnabled(true); 
				
				break;
			case MSG_SWITCHLIVESHOW:
				SwitchLiveShow();
				break;
				
			case MSG_SNATE_SHOW:
				Toast.makeText(mContext, "已保存至手机相册", Toast.LENGTH_LONG).show(); 
				break;
				
			case Constant.MSG_CHANGEMONITON:
				
				final int[] moitoninfo= (int[]) msg.obj;
				final int[] moitoninfoS= new int[4];
						
				moitoninfoS[0]=(int)(moitoninfo[0]*mZoom*mZoomX);
		        moitoninfoS[1]=(int)(moitoninfo[1]*mZoom*mZoomY);
		        moitoninfoS[2]=(int)(moitoninfo[2]*mZoom*mZoomX);
		        moitoninfoS[3]=(int)(moitoninfo[3]*mZoom*mZoomY);
				mCameraBean.setMonitioninfo(moitoninfoS);
				
				new Thread(){
					public void run() {
						SWInterface.delAllDetection(parameter, uid);
						SWInterface.addDetection(parameter, uid, moitoninfoS); 
					};
				}.start();
				
				break;
			case MSG_FLIP_SUCCESS:
				Toast.makeText(mContext, mContext.getString(R.string.flip_success), Toast.LENGTH_LONG).show(); 
				break;
			case MSG_FLIP_FAIL:
				Toast.makeText(mContext, mContext.getString(R.string.flip_fail), Toast.LENGTH_LONG).show(); 
				break;
			case MSG_CLARITY_SUCCESS:
				player.startPlay(mLocation, true); 
				System.out.println("-----1111222播放----------");
				Toast.makeText(mContext, "清晰度设置成功", Toast.LENGTH_LONG).show(); 
				break;
			case MSG_CLARITY_FAIL:
				player.startPlay(mLocation, true);  
				System.out.println("-----1111333播放----------"); 
				Toast.makeText(mContext, "清晰度设置失败，请稍后再试", Toast.LENGTH_LONG).show(); 
				break;
			case MSG_ALARM_SHOW:
				
				malarmController.setVisibility(View.VISIBLE);
				mHandler.sendEmptyMessageDelayed(MSG_ALARM_HIDE, 500);
				
				
				break;
			case MSG_ALARM_HIDE:
				malarmController.setVisibility(View.GONE);
				if(mAlertShowNum>= 10){
					mAlertShowNum= 0;
				}else{
					mHandler.sendEmptyMessageDelayed(MSG_ALARM_SHOW, 500); 
					mAlertShowNum++;
				}
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
	
	private View.OnTouchListener mSurfaceTouchListener = new View.OnTouchListener() {
		private float mLastX = 0;
		private float mLastY = 0;

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction() & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_DOWN:
					Log.i(TAG, "ACTION_DOWN ");
					mLastX = event.getX();
					mLastY = event.getY();
					break;
					
			
				case MotionEvent.ACTION_UP:
					Log.i(TAG, "ACTION_UP ");
					float distanceX = event.getX() - mLastX;
					float distanceY = event.getY() - mLastY;
					if (mPlaying && Math.abs(distanceX) > 10 && Math.abs(distanceX) > Math.abs(distanceY)) {
						MoveCameraLeftRight(distanceX);
					}else if (mPlaying && Math.abs(distanceY) > 10) {
						MoveCameraUpDown(distanceY);
					}else{
						SwitchLiveShow();
					}
					break;
				case MotionEvent.ACTION_MOVE:
					break;
				default:
					break;
			}
			return true;
		}
	};
	
	
	public void MoveCameraLeftRight(float distanceX) {
		if (!mPlaying) {
			return;
		}
		
		if (distanceX < 0) {
			// right
			Log.i(TAG, "scroll right ");
			new Thread(){
        		public void run() {
        			SWInterface.move(parameter, uid, "right"); 
        		};
        	}.start();

			long[] pattern = { 100, 200 }; // 停止 开启 停止 开启
			vibrator.vibrate(pattern, -1); // 重复两次上面的pattern
											// 如果只想震动一次，index设为-1
		} else {
			// left
			Log.i(TAG, "scroll left ");
			new Thread(){
        		public void run() {
        			SWInterface.move(parameter, uid, "left"); 
        		};
        	}.start();

			long[] pattern = { 100, 200 }; // 停止 开启 停止 开启
			vibrator.vibrate(pattern, -1); // 重复两次上面的pattern 如果只想震动一次，index设为-1
		}
	}

	public void MoveCameraUpDown(float distanceY) {

		if (!mPlaying) {
			return;
		}
		

		if (distanceY > 0) {// down
			Log.i(TAG, "scroll up ");
			new Thread(){
        		public void run() {
        			SWInterface.move(parameter, uid, "up"); 
        		};
        	}.start();

			long[] pattern = { 100, 200 }; // 停止 开启 停止 开启
			vibrator.vibrate(pattern, -1);
		} else {// up
			Log.i(TAG, "scroll down ");
			new Thread(){
        		public void run() {
        			SWInterface.move(parameter, uid, "down"); 
        		};
        	}.start();

			long[] pattern = { 100, 200 }; // 停止 开启 停止 开启
			vibrator.vibrate(pattern, -1);
		}
	}
	
	
	private SunniplayerListener mLiveSunniplayerListener = new SunniplayerListener() {

		@Override
		public void onPlayerEncounteredError() {
			mPlaying=false;
			mCameraController.setVisibility(View.INVISIBLE);
			mHandler.sendEmptyMessage(MSG_HIDE_LOADING);
        	Log.i(TAG, "onPlayerEncounteredError");
        	
        	new AlertDialog.Builder(mContext)
				.setMessage(mContext.getString(R.string.player_error))
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

		@Override
		public void onPlayerEndReached() {
			 mPlaying=false;
			 mCameraController.setVisibility(View.INVISIBLE);
			 mHandler.sendEmptyMessage(MSG_HIDE_LOADING);
			 Log.i(TAG, "onPlayerEndReached");
		
    		 new AlertDialog.Builder(mContext)
				.setMessage(mContext.getString(R.string.network_error_ask))
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

		@Override
		public void onPlayerPaused() {
			
			 Log.i(TAG, "onPlayerPaused");
			 mPlaying=false;
		}

		@Override
		public void onPlayerPlaying() {
			Log.i(TAG, "onPlayerPlaying");
			mPlaying=true;
			mCameraController.setVisibility(View.VISIBLE);
		}

		@Override
		public void onPlayerPositionChanged() {
			 Log.i(TAG, "onPlayerPositionChanged");
			 mPlaying=true;
			 mHandler.sendEmptyMessage(MSG_HIDE_LOADING);
		}

		@Override
		public void onPlayerStoped() {
			 mPlaying=false;
			 Log.i(TAG, "onPlayerStoped");
		}
    	
    };
    
    
   
    
    
    
    
    
    
    
    
    /*--------------------------------对讲相关代码--------------------------*/
    /**
     * 初始化录音
     */
    public void initSpeak(){
        
        new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					if(speex== null){
			    		speex = new Speex();
			    		speex.init();
			    	}
			    	EncG726.getEnc().native_g726_init(8000 * 4);
					int i = AudioRecord.getMinBufferSize(8000, 16, 2);
					Log.d(TAG, "---speak init, buffer-- "+i);
					if(localAudioRecord== null){
			        	localAudioRecord = new AudioRecord(1, 8000, 16, 2, i);
			            localAudioRecord.startRecording();
			            Log.d(TAG, "---speak localAudioRecord init-- ");
			        }
				} catch (Exception e) {
					Log.e(TAG, "-- speak init error--"+e);
					e.printStackTrace();
				}
			}
		}).start();
    }
    /**
     * 停止录音
     */
    public void releaseSpeak(){
    	stopSpeaking();
    	try {
    		EncG726.getEnc().native_g726_deinit();
			if (speex != null) {
				speex.exit();
			}
			if (localAudioRecord != null) {
				localAudioRecord.stop();
				localAudioRecord.release();
			}
			Log.d(TAG, "---speak localAudioRecord release-- ");
		} catch (Exception e) {
			Log.d(TAG, "---speak localAudioRecord release error-- "+e);
			e.printStackTrace();
		}finally{
			localAudioRecord = null; 
			speex= null;
		}
    	try {
    		mMessageStack.put(new AudioMessage());   //为了停止发送语音线程mThreadSend
		} catch (Exception e) {
			Log.e(TAG, "--send audio thread stop error--"+e);
			e.printStackTrace();
		}
    	
    	disConnect();
       
    }
    
    
    
    private ThreadCreateAudio mThreadCreateAudio = null;
   
    private int tmpVolume= 0;
    public void startSpeaking(){
    	
        if (mThreadCreateAudio == null){
        	
        	tmpVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        	mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 2, 0);
        	
        	mThreadCreateAudio = new ThreadCreateAudio();
        	mThreadCreateAudio.start();
           
        }
    }

    public void stopSpeaking(){
    	
        if (mThreadCreateAudio != null){
        	mThreadCreateAudio.stopThread();
            try{
            	mThreadCreateAudio.interrupt();
            }catch (Exception e){
                e.printStackTrace();
            }finally{
            	mThreadCreateAudio = null;
            }
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, tmpVolume, 0);
        }
        
    }
    
    
    private class ThreadCreateAudio extends Thread {
    	private boolean mRunning = false;
    	
        public void run() {
        	long threadId= this.currentThread().getId();
    		Log.d(TAG, "----create audio thread start--"+threadId);
        	
        	mRunning = true;
        	if(mThreadSend== null || !mThreadSend.isAlive()){
        		mThreadSend= new ThreadSend();
        		mThreadSend.start();
			}
        	
            int ret= 0;
            byte[] arrayOfByte1 = new byte[Speex.DEFAULT_FRAME_SIZE * 2];
    		byte[] arrayOfByte2 = new byte[Speex.DEFAULT_FRAME_SIZE * 2];
    		
            

            while (true){
            	
            	byte[] arrayOfByte6 = new byte[160];
                if (!mRunning){
                	Log.d(TAG, "----create audio thread stop--"+threadId);
                    break;
                }
                if(localAudioRecord== null){
                	continue;
                }
                ret = localAudioRecord.read(arrayOfByte1, 0, arrayOfByte1.length);
                Log.d(TAG, "---create audio ---"+threadId + "--ret--"+ret);
                if (ret > 0) {
                	speex.denoise(arrayOfByte1, arrayOfByte2, ret);
                    final int len = EncG726.getEnc().native_g726_encode(arrayOfByte2, ret, arrayOfByte6);
                    
                    AudioMessage msg= new AudioMessage();
                    msg.pts= (int) System.currentTimeMillis();
                    msg.len= len;
                    msg.data= arrayOfByte6; 
                    try {
                    	
						mMessageStack.put(msg); 
						
					} catch (Exception e) {
						Log.e(TAG, "---send audio error---"+e);
						e.printStackTrace();
					}
                
                }
            	
            
            }
        }

        public void stopThread() {
            mRunning = false;
            Log.d(TAG, "----create audio thread set stop--");
        }
        
        
    }
    
    
    
    class AudioMessage{
    	public int pts;
    	public int len;
    	public byte[] data;
    }
    private LinkedBlockingQueue<AudioMessage> mMessageStack = new LinkedBlockingQueue<AudioMessage>();
    
    
    
    
	private Socket mWorkSocket = null;
	private DataOutputStream dos = null;
	private InputStream dis = null;

	public void connect() {
		try {
			if(mWorkSocket!= null && mWorkSocket.isClosed()){
				disConnect();
			}
			if(mWorkSocket== null){
				mWorkSocket = new Socket(ois_ip, ois_port);
				Log.i(TAG, "--socket connect--");

				mWorkSocket.setSoTimeout(8000);
				mWorkSocket.setKeepAlive(true);
				dos = new DataOutputStream(new BufferedOutputStream(mWorkSocket.getOutputStream()));
				dis = mWorkSocket.getInputStream(); 
				
				if(mThreadReceive== null || !mThreadReceive.isAlive()){
					mThreadReceive= new ThreadReceive();
					mThreadReceive.start();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void disConnect() {
		try {
			if (mWorkSocket != null) {
				mWorkSocket.close();
				Log.i(TAG, "--socket close--");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			mWorkSocket = null;
		}

	}
	
	private ThreadReceive mThreadReceive= null;
	private class ThreadReceive extends Thread{
		
		public void run() {
			byte[] data= new byte[1024];
			long threadId= this.currentThread().getId();
    		Log.d(TAG, "----recv socket thread start--"+threadId);
			while(true){
				try {
					
					int len= dis.read(data);
					
					Log.d(TAG, "----recv socket thread len=="+len +"----"+threadId);    
					if(len== -1){
						if(mWorkSocket== null || mWorkSocket.isClosed()){
							Log.d(TAG, "----recv socket thread stop--"+threadId);
							break;
						}
						Thread.sleep(500);  
					}
					
				} catch (Exception e) {
					if(mWorkSocket== null || mWorkSocket.isClosed()){
						Log.d(TAG, "----recv socket thread stop--"+threadId+"---"+e);
						break;
					}
					e.printStackTrace();   
				}
			}
		};
	}
	
	private ThreadSend mThreadSend= null;
    private class ThreadSend extends Thread{
    	
    	public void run() {
    		long threadId= this.currentThread().getId();
    		Log.d(TAG, "----send socket thread start--"+threadId);
    		
            while (true){
            	try {
					AudioMessage msg= mMessageStack.take();
					if(msg.pts== 0){
						Log.d(TAG, "----send socket thread stop--"+threadId);
            			break;
					}
					
					
					// 转发头
					dos.writeByte(0x71);
					dos.writeByte(0);
					
					dos.writeBytes(mac);
					for (int i = mac.length(); i < 64; ++i) {
						dos.writeByte(0);
					}

					dos.writeBytes(uid);
					for (int i = uid.length(); i < 64; ++i) {
						dos.writeByte(0);
					}
					int len = 70 + 8 + msg.len;
					dos.writeInt(len);

					// 指令
					dos.writeByte(0x22);
					dos.writeByte(0);
					dos.writeBytes(mac);
					for (int i = mac.length(); i < 64; ++i) {
						dos.writeByte(0);
					}

					dos.writeInt(8 + (int) msg.len);
					dos.writeInt(msg.pts);

					// length
					dos.writeInt(msg.len);

					// 音频信息
					dos.write(msg.data, 0, msg.len);

					dos.flush();
					Log.d(TAG, "--send socket---"+threadId);
					
				} catch (Exception e) {
					Log.i(TAG, "--send socket error--"+e);
					e.printStackTrace();
					disConnect();
					connect();
				}
            	
            }
        }
    }
    
    
    
}
