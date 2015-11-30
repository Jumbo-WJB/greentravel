function Dialog(){
	var dialog = []
	dialog.push('<div id="waitdialog" style="display:none;position: absolute;left: 0px;top: 0px;bottom:0;width: 100%;height: 100%;z-index:300">');
	dialog.push('<div style="position: absolute;left: 0px;top: 0px;height: 100%;background: #000000;opacity:0.0">')
	dialog.push('</div>')
	dialog.push('<div style="position:relative;text-align:center;border-radius:5px;background: #000000;opacity:0.9;color:white;margin:auto;font-size:24px;top:40%;padding:10px;width:100px">')
	dialog.push('<div><i class="fa fa-spinner fa-spin"></i></div>')
	dialog.push('<div style="font-size:12px" class="txt"></div>')
	dialog.push('</div>')
	dialog.push('</div>')
	
	var toast = [];
	toast.push('<div id="toast" style="z-index:300;display:none;position:absolute;left:0;right:0;top:0;text-align:center">')
	toast.push('<div class="txt" style="position:relative;display:inline;text-align:center;border-radius:2px;background-color:#333333;opacity:0.8;color:white;margin:auto;font-size:16px;top:300px;padding:10px">')
	toast.push('</div>')
	toast.push('</div>')	
	
	
	
	this.init = function(){		
		if($("#waitdialog").length > 0) return;	
		$("body").append(dialog.join(""));
		$("body").append(toast.join(""));
	
	}
	
	this.show = function(txt){
		this.init();
		if(txt)
			$('#waitdialog .txt').html(txt);	
		$('#waitdialog').show();
	}
	this.hide = function(){
		$('#waitdialog').hide();
	}
	
	this.toast = function(info){
		this.init();
		$('#toast .txt').html(info);
		$('#toast').show();
		setTimeout(function(){
			$('#toast').hide();
		},2500);
	}
}