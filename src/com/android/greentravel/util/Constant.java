package com.android.greentravel.util;

/**
 * 常量类
 * @author lijing
 *
 */
public class Constant {

	
	
	public static final int MSG_HIDE_LOADING = 30;
	public static final int MSG_SHOW_LOADING = 31;
	public static final int MSG_BACK = 32;
	

	

	public static final int QR_ACTIVITY = 301;  //二维码扫描
	public static final int MSG_CONNEC_RESULT = 302;  //零配置扫描结果
	
	
/*	public static final int ADDCAMERA_REQUESTCODE= 100;
	public static final int ADDCAMERA_RESULTCODE= 101;*/
	
	public static final int QRSCAN_REQUESTCODE= 110;  //二维码扫描
	public static final int QRSCAN_RESULTCODE= 111;
	public static final int PLAY_REQUESTCODE= 120;
	public static final int PLAY_RESULTCODE= 121;
	
	public static final int MSG_CHANGEMONITON= 201;
	
	
	/*public static final int MSG_VOLUMECHANGE= 300;
	public static final int MSG_CAMERAALERT= 301;
	public static final int MSG_USERDISABLE= 302;*/
	
	public static final String ACTION_CAMERA_ALERT= "android.sunniwell.CAMERA_ALERT";
	public static final String ACTION_USER_DISABLE= "android.sunniwell.USER_DISABLE";
	
	public static int preVolume= 0; //保存静音前音量 ，播放退出再进入后 再点静音键回复 此音量
}
