function OIS(){
	this.server = "http://172.16.15.101:5000";	
//	this.server = "http://182.92.171.190:5000";
	var ip   = JSBridge.get("ois_ip") ;
	var port = JSBridge.get("ois_port") ;
	if(ip && port){
		this.server = "http://" + ip + ":" + port;
		
	}
	//this.server = "http://172.16.11.147:5000";
	console.log("ois server set to " + this.server);
	
}

OIS.prototype.getinfoBycode = function(code){
	var info = '';
	if(code == 0)
		info = "网络异常，请检查!"
	else if (code == 600 )
		info = "用户不存在"
	else if (code == 604 || code == 605)
		info = "用户或口令错误，请检查!"
	else if(code == 601)
		info = "用户账号已被禁用!"
	else if(code == 602)
		info = "用户已过期禁用!"
	else if (code == 606)
		info = "验证码错误，请检查!"
	else if (code == 607)
		info = "验证码过期，请检查!"
	else if(code == 603)
		info = "用户已被占用，请检查"
	else if(code == 623)
		info = "摄像头离线"
	else if(code == 662) //江苏监看定制   订购产品包异常
		info = "token超时,请重新登录后购买"
	else if(code == 663) //江苏监看定制   订购产品包异常
		info = "产品包无效"	
	else if(code == 664) //江苏监看定制   订购产品包异常
		info = "价格无效"	
	else if(code == 666) //江苏监看定制   订购产品包异常
		info = "订单异常"	
	else if(code == 690) //江苏监看定制   通过和家庭token获取用户信息异常
		info = "token过期，请重新登录和家庭"	
	else
		info = "发生错误，请检查!"	
	
	return info;
}


/**
 * AJAX 请求封装
 * 其中data为json格式
 */
OIS.prototype._request = function(method, path, data, success, error){
	console.log("request" + this.server + path )
	var that = this;
	$.ajax({
			url       : path.indexOf("http")>=0?path:this.server + path, 
			type      : method,				
			timeout   : 10000,				
			dataType  : "json",				
			data      : method =='POST'?JSON.stringify(data):data,
			success   : function(data, textStatus, jqXHR){														
				console.log("request " + that.server + path + " ok code:" + jqXHR.status)				
				//TODO 考虑 error_code 非 0 的情况
				if(data && data.error_code && data.error_code != 0){ //业务层错误
					if(error) error(data.error_code)
					return;
				}
				if(success) success(jqXHR.status, data)
			},
			error : function( jqXHR, textStatus, errorThrown){
				//alert(jqXHR.status)
				console.log("request " + that.server + path + " error " + JSON.stringify(data) + "/" + jqXHR.status)
				if(error) error(jqXHR.status)
			}			
	})		
}

OIS.prototype.init = function(){
	
}

OIS.prototype.upgrade = function(callback){
	var request = 
	{
		terminal_type : JSBridge.terminal_type	
	}
	this._request('GET', '/ois/terminal/version', request, function(code, data){
		if(!data) return;
		if(data.version = JSBridge.get('version')){
			if(callback) 	callback(data.version, data.url)
		}
	}, function(){
		//
	});	
}
OIS.prototype.login = function(data, success, error){
	//增加mac信息
	
	if(JSBridge.get('mac') && JSBridge.get('mac') != ""){
		data.mac = JSBridge.get('mac');
		data.terminal_id = JSBridge.get('mac');
		data.terminal_type = JSBridge.terminal_type; 
	}
	
	if(JSBridge.get('app_version') && JSBridge.get('app_version') != "")
		data.soft_ver = JSBridge.get('app_version');
	
	
	//临时加入登陆发送消息
	this._request('POST', '/ois/user/login', data, function(code, data){
		//临时为IOS增加登陆成功事件
		JSBridge.set("ev_login_ok", "");
		success(code, data);
	}, function(code){
		error(code);
	});
	//this._request('POST', '/ois/user/login', data, success, error);
}

OIS.prototype.regist = function(data, success, error){
	this._request('POST', '/ois/user/register', data, success, error);
}

/**
 * 获取用户的摄像头列表
 *
 */
OIS.prototype.getCamera = function(userid, success, error){
	
	var request = 
	{
		userid : userid,
		pagesize : 15,
		pageindex : 0,
		query     : "all",		
	}
	this._request('GET', '/ois/terminal/camera/info',request, success, error);
}

/**
 * 获取告警图片
 *
 */
OIS.prototype.getAlert = function(cameraid, success, error){	
	
	//this._request('GET', 'http://172.16.15.60:6006/C' + cameraid + '/alertCaptureList',null, success, error);		
	this._request('GET', this.server + '/C' + cameraid + '.alertCaptureList',null, success, error);		
}

/**
 * 获取回看信息
 *
 */
OIS.prototype.getRecordList = function(cameraid, request, success, error){	
	
	//this._request('GET', 'http://172.16.15.60:6006/C' + cameraid + '/alertCaptureList',null, success, error);		
	this._request('GET', this.server + '/C' + cameraid + '.json', request, success, error);		
}

/**
 * 获取告警图片
 * @files 数组
 */
OIS.prototype.delAlert = function(userid, cameraid, files, success, error){	
	var request = 
	{
		action     : "delfile", 		
		camera_id  : cameraid,
	    files      : []
	}
	files.forEach(function(val, idx, ary){
		request.files.push({filename:val});
	});	
	
	this._request('POST', '/ois/terminal/storage', request, success, error);		
}


/**
 * 获取注册验证码 
 */
OIS.prototype.getAuthcode = function(phone,success, error){	
	var request = {
		user_id : phone,
		phone   : phone
	}	
	this._request('GET', '/ois/user/getsms' , request, success, error);		
}
/**
 * 判断用户是否已存在
 */
OIS.prototype.checkUser = function(phone,success, error){	
	var request = {
		user_id : phone
	}	
	this._request('GET', '/ois/user/checkuser' , request, success, error);		
}
/**
 * 获取重置验证码
 */
OIS.prototype.getResetAuthcode = function(phone, success, error){	
	var request = {
		user             : phone,
		phone            : phone	
	}				
	this._request('POST', '/ois/user/reset', request, success, error);	
}

/**
 * 重置登录密码 
 */
OIS.prototype.resetLoginPassword = function(user, phone, sms, pwd,success, error){	
	var request = {
		user             : user,
		phone            : phone,
		sms_code         : sms,
		pswd_new         : pwd,
		pswd_new_confirm : pwd,
	}		
	
	this._request('POST', '/ois/user/resetconfirm', request, success, error);	
}


/*绑定摄像头*/
OIS.prototype.bind = function(cameraID, success, error){
	var request = {
			"terminal_id":JSBridge.get('mac'),
			"user_id":JSBridge.get('currentUser'), 
			"camera_id":cameraID
	}
	this._request('POST', '/ois/user/bind', request, success, error);
}
/*解绑摄像头*/
OIS.prototype.unbind = function(cameraID, success, error){
	var request = {
			"terminal_id":JSBridge.get('mac'),
			"user_id":JSBridge.get('currentUser'), 
			"camera_id":cameraID
	}
	this._request('POST', '/ois/user/unbind', request, success, error);
}
/*设置摄像头开关机,报警，录像属性*/
OIS.prototype.setSwitch = function(data, success, error){
	this._request('POST', '/ois/forward', data, success, error);
}
/*设置摄像头名称*/
OIS.prototype.setCameraName = function(data, success, error){
	this._request('POST', '/ois/terminal/camera/name', data, success, error);
}
/*获取摄像头/手机端的版本信息*/
OIS.prototype.getVersion = function(data, success, error){
	this._request('GET', '/ois/terminal/version', data, success, error);
}
/*升级摄像头版本*/
OIS.prototype.upgradeCamera = function(data, success, error){
	this._request('POST', '/ois/terminal/camera/upgrade', data, success, error);
}
/*重置WIFI*/
OIS.prototype.forgetWifi= function(data, success, error){
	this._request('POST', '/ois/forward', data, success, error);
}
/*恢复出厂设置*/
OIS.prototype.factoryReset= function(data,success,error){
	this._request('POST','/ois/forward',data,success,error);
}
/*重置密码*/
OIS.prototype.resetPassword = function(data, success, error){
	this._request('POST', '/ois/user/setpassword', data, success, error);
}
/*信息反馈*/
OIS.prototype.feedback = function(data, success, error){
	this._request('POST', '/ois/user/feedback', data, success, error);
}
/*获取摄像头订购明细*/
OIS.prototype.getOrderDetail = function(cameraid, userid, success, error){
	this._request('GET', "/ois/terminal/subscribe/info?camera_id="+cameraid+"&user_id="+userid, {}, success, error);
}


/*获取消费明细*/
OIS.prototype.getConsumeDetail = function(userid, success, error){
	this._request('GET', "/ois/terminal/consumption?user_id="+userid, {}, success, error);
}

/*获取产品包*/
OIS.prototype.getService = function(success, error){
	this._request('GET', "/ois/terminal/service/info", {}, success, error);
}

/*江苏监看定制   获取财付通付费地址*/
OIS.prototype.getPayUrl = function(userid, sid, quantity, cameraid, token, success, error){
	//var url= "/ois/waptenpay/buy?user="+userid+"&sid="+sid+"&quantity="+quantity+"&cameraid="+cameraid+"&token="+token;
	var request= {
		user: userid,
		sid: sid,
		quantity: quantity,
		cameraid: cameraid,
		token: token
	}
	this._request('GET', '/ois/waptenpay/buy', request, success, error);
}
/*江苏监看定制   根据和家庭tokan获取用户名 密码*/
OIS.prototype.getUserFromJSToken = function(jstoken, success, error){
	var request= {
		cmtokenid: jstoken
	}
	this._request('GET', '/ois/terminal/userinfo', request, success, error);
}

function userDisableListener(){
	dialog.toast("当前账户已被禁用,将退出登录");
	JSBridge.set("currentUser", "");
	JSBridge.write("userinfo", "");
	
	setTimeout(function(){
		window.location = "login2.html"
	},2000)
}
$(document).on('userdisable', userDisableListener)
/*获取某一个摄像头的信息*/
OIS.prototype.getOneCamera = function(userid, cameraid,success, error){
	
	var request = 
	{
		"userid" : userid,
		"camera_id" : cameraid,
		"query"   : "all",		
	}
	this._request('GET', '/ois/terminal/camera/info',request, success, error);
}
/*添加分享*/
OIS.prototype.addShare = function(userid,cameraid,success, error){
	
	var request = 
	{
		"action" : "add",
		"camera_id" : cameraid, 
		"user_id" : userid

	}
	this._request('POST', '/ois/camera/share',request, success, error);
}
/*删除分享*/
OIS.prototype.delShare = function(userid,cameraid,success, error){
	
	var request = 
	{
		"action" : "del",
		"camera_id" : cameraid, 
		"user_id" : userid

	}
	this._request('POST', '/ois/camera/share',request, success, error);
}
/*关闭分享*/
OIS.prototype.disableShare = function(cameraid,success, error){
	
	var request = 
	{
			"action":"disable",
			"camera_id":cameraid
	}
	this._request('POST', '/ois/camera/share',request, success, error);
}
/*获取用户被分享的摄像头列表*/
OIS.prototype.getSharedCamera = function(userid,success, error){
	
	var request = 
	{
		user_id : userid	
	}
	this._request('GET', '/ois/shared/camera',request, success, error);
}
/*获取摄像头被分享的列表*/
OIS.prototype.getSharedUser = function(cameraid,success, error){
	
	var request = 
	{
		camera_id : cameraid	
	}
	this._request('GET', '/ois/shared/user',request, success, error);
}
 