Date.prototype.Format = function(fmt) {// author: meizz
	var o = {
		"M+" : this.getMonth() + 1,
		// 月份
		"d+" : this.getDate(),
		// 日
		"h+" : this.getHours(),
		// 小时
		"m+" : this.getMinutes(),
		// 分
		"s+" : this.getSeconds(),
		// 秒
		"q+" : Math.floor((this.getMonth() + 3) / 3),
		// 季度
		"S" : this.getMilliseconds() // 毫秒
	};
	if (/(y+)/.test(fmt))
		fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
	for (var k in o)
	if (new RegExp("(" + k + ")").test(fmt))
		fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
	return fmt;
}
function getQueryStr(str){  
	var LocString=String(window.document.location.href);  
	var rs = new RegExp("(^|)"+str+"=([^\&]*)(\&|$)","gi").exec(LocString), tmp;  
  
	if(tmp=rs){  
		return tmp[2];  
	}  
  
	// parameter cannot be found  
	return "";  
}  

function log(content){	
	//$("#msg").text(content + "<br>" + $("#msg").text())
	$("#msg").text(content)
	
}
function logView(){
	
	$(document.body).append('<div id="msg" style="opacity:0.8;z-index:99;background-color:;black;width:100%;color:white;position:absolute;top:0px;height:120px;overflow-y:scroll"></div>')
	
}