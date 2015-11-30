package com.android.greentravel.bean;


/**
 * Created with IntelliJ IDEA.
 * User: husl
 * Date: 13-11-16
 * Time: 上午9:47
 * To change this template use File | Settings | File Templates.
 */
public class CameraBean {
    private static final String TAG = "CameraBean";
    public static final int PERSONAL_TYPE= 0;
	public static final int INDUSTRY_TYPE= 1;
	public static final int CLARITY_HD= 0;  //高清
	public static final int CLARITY_SD= 1;  //标清
	public static final int CLARITY_LD= 2;  //流畅
	
	// uid sid avindex
    private String mUid = null;
    private String mDescripter = null;
    private String mDefinition = null;
    private int clarity= -1;  //清晰度  高清：1280*720  标清：640*480  流畅：320*240
    private boolean mMotionDetection = false;
    private boolean mIsContented = false;
    private boolean encrypt= false;  //摄像头视频 是否加密
    
    private String cdsIp= null; //cds的ip
    
    private int dataPort= 0; //视频监听端口
    
    
    private String cdsDomain= null; //cds域名地址， 访问cds时首选dns进行访问，如果dns不存再通过ip访问
    
    private int httpxPort= 0;
    
    private int type= 0; //摄像头类型 1：行业摄像头 0：普通摄像头
    private String userId= ""; //摄像头对应的用户id（如果是普通摄像头，user_id=登录用户名  如果是行业摄像头 user_id= 行业账号）
    private String version=""; //摄像头当前版本
    private String hardwareType="";  //摄像头型号
    
    private int videoRemain= -1;  //产品包中回看天数   如果为0代表 无法在观看该摄像头的回看 需要订购产品包
    private int alertCapRemain= -1;  //产品包中报警图片数   如果为0代表 无法在观看该摄像头的报警图片 需要订购产品包
    
    private int[] monitioninfo = null;
    private int[] Orgmonitioninfo = null;
    


    public CameraBean(String uid){
        if(uid == null || uid.equals("")){
            return;
        }

        mUid = uid;
        start(mUid);
    }

    public void start(String uid) {
        mUid = uid;
    }



    public void setContented(boolean contented){
        mIsContented = contented;
    }

    public boolean isContented(){
        return mIsContented;
    }
    
   

    public String getUid(){
        return mUid;
    }

    public void setDescripter(String descripter){
        mDescripter = descripter;
    }

    public int getClarity() {
		return clarity;
	}

	public void setClarity(int clarity) {
		this.clarity = clarity;
	}

	public String getDescripter(){
        return mDescripter;
    }

    public void setDefinition(String definition){
        mDefinition = definition;
    }

    public String getDefinition(){
        return mDefinition;
    }

    public void setMotionDetection(boolean motionDetection){
        mMotionDetection = motionDetection;
    }

    public boolean getMotionDetection(){
        return mMotionDetection;
    }

  


	public boolean isEncrypt() {
		return encrypt;
	}

	public void setEncrypt(boolean encrypt) {
		this.encrypt = encrypt;
	}

	public String getCdsHost() {
		if(cdsDomain== null || cdsDomain.trim().equals("")){
			return cdsIp;
		}else{
			return cdsDomain;
		}
	}

	public String getHardwareType() {
		return hardwareType;
	}

	public int getVideoRemain() {
		return videoRemain;
	}

	public void setVideoRemain(int videoRemain) {
		this.videoRemain = videoRemain;
	}

	public int getAlertCapRemain() {
		return alertCapRemain;
	}

	public void setAlertCapRemain(int alertCapRemain) {
		this.alertCapRemain = alertCapRemain;
	}

	public void setHardwareType(String hardwareType) {
		this.hardwareType = hardwareType;
	}

	public void setCdsIp(String cdsIp) {
		this.cdsIp = cdsIp;
	}

	public int getDataPort() {
		return dataPort;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getUserId() {
		return userId;
	}

	public int[] getMonitioninfo() {
		return monitioninfo;
	}

	public void setMonitioninfo(int[] monitioninfo) {
		this.monitioninfo = monitioninfo;
	}

	public int[] getOrgmonitioninfo() {
		return Orgmonitioninfo;
	}

	public void setOrgmonitioninfo(int[] orgmonitioninfo) {
		Orgmonitioninfo = orgmonitioninfo;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public void setDataPort(int dataPort) {
		this.dataPort = dataPort;
	}

	
	public void setCdsDomain(String cdsDomain) {
		this.cdsDomain = cdsDomain;
	}

	public int getHttpxPort() {
		return httpxPort;
	}

	public void setHttpxPort(int httpxPort) {
		this.httpxPort = httpxPort;
	}

	public String getCdsIp() {
		return cdsIp;
	}

	public String getCdsDomain() {
		return cdsDomain;
	}

}


