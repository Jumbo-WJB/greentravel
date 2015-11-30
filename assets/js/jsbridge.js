/**
 * IOS init
 */
var iosinit = function() {
	if (window.WebViewJavascriptBridge) { return }
	var messagingIframe
	var sendMessageQueue = []
	var receiveMessageQueue = []
	var messageHandlers = {}
	
	var CUSTOM_PROTOCOL_SCHEME = 'wvjbscheme'
	var QUEUE_HAS_MESSAGE = '__WVJB_QUEUE_MESSAGE__'
	
	var responseCallbacks = {}
	var uniqueId = 1
	
	function _createQueueReadyIframe(doc) {
		messagingIframe = doc.createElement('iframe')
		messagingIframe.style.display = 'none'
		messagingIframe.src = CUSTOM_PROTOCOL_SCHEME + '://' + QUEUE_HAS_MESSAGE
		doc.documentElement.appendChild(messagingIframe)
	}

	function init(messageHandler) {
		if (WebViewJavascriptBridge._messageHandler) { throw new Error('WebViewJavascriptBridge.init called twice') }
		WebViewJavascriptBridge._messageHandler = messageHandler
		var receivedMessages = receiveMessageQueue
		receiveMessageQueue = null
		for (var i=0; i<receivedMessages.length; i++) {
			_dispatchMessageFromObjC(receivedMessages[i])
		}
	}

	function send(data, responseCallback) {
		_doSend({ data:data }, responseCallback)
	}
	
	function registerHandler(handlerName, handler) {
		messageHandlers[handlerName] = handler
	}
	
	function callHandler(handlerName, data, responseCallback) {
		_doSend({ handlerName:handlerName, data:data }, responseCallback)
	}
	
	function _doSend(message, responseCallback) {
		if (responseCallback) {
			var callbackId = 'cb_'+(uniqueId++)+'_'+new Date().getTime()
			responseCallbacks[callbackId] = responseCallback
			message['callbackId'] = callbackId
		}
		sendMessageQueue.push(message)
		messagingIframe.src = CUSTOM_PROTOCOL_SCHEME + '://' + QUEUE_HAS_MESSAGE
	}

	function _fetchQueue() {
		var messageQueueString = JSON.stringify(sendMessageQueue)
		sendMessageQueue = []
		return messageQueueString
	}

	function _dispatchMessageFromObjC(messageJSON) {
		setTimeout(function _timeoutDispatchMessageFromObjC() {
			var message = JSON.parse(messageJSON)
			var messageHandler
			var responseCallback

			if (message.responseId) {
				responseCallback = responseCallbacks[message.responseId]
				if (!responseCallback) { return; }
				responseCallback(message.responseData)
				delete responseCallbacks[message.responseId]
			} else {
				if (message.callbackId) {
					var callbackResponseId = message.callbackId
					responseCallback = function(responseData) {
						_doSend({ responseId:callbackResponseId, responseData:responseData })
					}
				}
				
				var handler = WebViewJavascriptBridge._messageHandler
				if (message.handlerName) {
					handler = messageHandlers[message.handlerName]
				}
				
				try {
					handler(message.data, responseCallback)
				} catch(exception) {
					if (typeof console != 'undefined') {
						console.log("WebViewJavascriptBridge: WARNING: javascript handler threw.", message, exception)
					}
				}
			}
		})
	}
	
	function _handleMessageFromObjC(messageJSON) {
		if (receiveMessageQueue) {
			receiveMessageQueue.push(messageJSON)
		} else {
			_dispatchMessageFromObjC(messageJSON)
		}
	}

	window.WebViewJavascriptBridge = {
		init: init,
		send: send,
		registerHandler: registerHandler,
		callHandler: callHandler,
		_fetchQueue: _fetchQueue,
		_handleMessageFromObjC: _handleMessageFromObjC
	}

	var doc = document
	_createQueueReadyIframe(doc)
	var readyEvent = doc.createEvent('Events')
	readyEvent.initEvent('WebViewJavascriptBridgeReady')
	readyEvent.bridge = WebViewJavascriptBridge
	doc.dispatchEvent(readyEvent)
};



/**
 * HOST app 的回调
 * 
 */
function onevent(name, param){
	$(document).trigger(name, param);
}

// JSBridge 开始
var JSBridge;
function JSBridgeImpl(){	
	
}

//JSBridgeImpl.prototype.coreImpl;

JSBridgeImpl.prototype.get = function(key){
	var param = {
		key : key
	}	
	return this.coreImpl.ioctl('sysget', JSON.stringify(param));
	
}


JSBridgeImpl.prototype.set = function(key, value){
	var param = {
		key   : key,
		value : value
	}
	this.coreImpl.ioctl('sysset', JSON.stringify(param));
	return null;
}



JSBridgeImpl.prototype.read = function(key){
	return localStorage[key];	
	
	
}
JSBridgeImpl.prototype.write = function(key, value){
	localStorage[key] = value;
}


JSBridgeImpl.prototype.timeupdate = function(time){
	var param = {		
		time : time
	}	

	return this.coreImpl.ioctl('timeupdate', JSON.stringify(param));	
}

JSBridgeImpl.prototype.play = function(user, cameraid, cameraname, type){
	var ois = new OIS();
	var link = document.createElement('a');
	link.setAttribute('href', ois.server);	
	if(!type) type = 'live'; //默认
	var param = {
		type     : type,
		cameraid : cameraid,
		cameraname : cameraname,
		user     : user,
		host     : link.hostname,
		port     : link.port
	}	
	
	
	//alert(JSON.stringify(param))
	
	return this.coreImpl.ioctl('play', JSON.stringify(param));	
}

JSBridgeImpl.prototype.scan = function(){
	var ois = new OIS();
	var link = document.createElement('a');
	link.setAttribute('href', ois.server);
	//alert("bc")
	//alert(JSON.stringify(param))
	return this.coreImpl.ioctl('scan', null);	
}

JSBridgeImpl.prototype.finish = function(){
	return this.coreImpl.ioctl('finish', null);	
}
//
JSBridgeImpl.prototype.upgrade = function(upgrade_url){
	var param = {
		upgrade_url     : upgrade_url
		
	}	
	//alert(JSON.stringify(param))
	
	return this.coreImpl.ioctl('upgrade', JSON.stringify(param));	
}

//扫描二维码
JSBridgeImpl.prototype.scanqr = function(){
		
	return this.coreImpl.ioctl('scanqr', null);	
}
//获取手机连接的wifi
JSBridgeImpl.prototype.getssid = function(){
		
	return this.coreImpl.ioctl('getssid', null);	
}
//开始无线扫描
JSBridgeImpl.prototype.startscanwifi = function(param){
	
	return this.coreImpl.ioctl('startscanwifi', param);	
}
//结束无线扫描
JSBridgeImpl.prototype.stopscanwifi = function(param){
	
	return this.coreImpl.ioctl('stopscanwifi', null);	
}

//江苏监看定制   获取 和家庭 token
JSBridgeImpl.prototype.getJiangSutoken = function(){
	
	return this.coreImpl.ioctl('getJiangSutoken', null);	
}

/**
 *
 * 接口模拟类
 */
function JSBridgeSim(){
	
	this.ioctl = function(cmd, param){		
		
		param = JSON.parse(param);
		
		if(cmd == 'sysset'){			
			localStorage[param.key] = param.value;
		}else if(cmd == 'sysget'){		
			return localStorage[param.key];
		}		
	}
}

/**
 *
 * IOS实现类
 */
function JSBridgeIOS(){
	iosinit();
	var bridge;
	var eventHandler = function(message){
		message = JSON.parse(message);
		//onevent(message.cmd, message.param);
		$(document).trigger(message.cmd, JSON.stringify(message.param));
	}
	
	WebViewJavascriptBridge.init(eventHandler);
	
	this.ioctl = function(cmd, param){		
		param = JSON.parse(param);
		var iossend = {
			cmd : cmd,
			param : param
		}
		WebViewJavascriptBridge.send(JSON.stringify(iossend))
		if(cmd == 'sysset')
			localStorage[param.key] = param.value;
		else if(cmd == 'sysget')		
			return localStorage[param.key];		
				
	}
}


var u = navigator.userAgent, app = navigator.appVersion;

//根据平台 替换实现类
// IOS ? --> Android ? --> Web
if(u.indexOf("iPhone")>=0){
	JSBridgeImpl.prototype.coreImpl = new JSBridgeIOS();
	JSBridgeImpl.prototype.terminal_type = 5;
}else if(typeof _jsbridge == 'undefined'){ //Android
	JSBridgeImpl.prototype.coreImpl = new JSBridgeSim();
	JSBridgeImpl.prototype.terminal_type = 3;
}else {
	JSBridgeImpl.prototype.coreImpl = _jsbridge;		
	JSBridgeImpl.prototype.terminal_type = 3;
}
	

JSBridge = new JSBridgeImpl();
/**
setTimeout(function(){
	$(document).trigger('devicechange');
},10000)
**/

document.onselectstart = function() { return false; } 