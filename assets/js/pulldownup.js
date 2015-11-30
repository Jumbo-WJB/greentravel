/**
 * 上拉下拉刷新
 */
function Pulldownup(downprocessor, upprocessor){
	var myScroll;
	var pullDownEl, pullDownL;  
	var pullUpEl, pullUpL;  
	var Downcount = 0 ,Upcount = 0;  
	var loadingStep = 0;//加载状态0默认，1显示加载状态，2执行加载数据，只有当为0时才能再次加载，这是防止过快拉动刷新  
	
	var downprocessor, upprocessor;
	
	this.refresh = function(){
		pullDownAction();
	}
	//是为了刷新滚动区域
	this.simpleRefresh = function(){
		myScroll.refresh();  
	}
	
	function init () {
		
		pullDownEl = $('#pullDown');  
		pullDownL = pullDownEl.find('.pullDownLabel');  
		pullDownEl['class'] = pullDownEl.attr('class');  
		pullDownEl.attr('class','').hide();  
		  
		pullUpEl = $('#pullUp');  
		pullUpL = pullUpEl.find('.pullUpLabel');  
		pullUpEl['class'] = pullUpEl.attr('class');  
		pullUpEl.attr('class','').hide();  
		
		myScroll = new IScroll('#wrapper', { mouseWheel: true ,click:true, probeType:1, bounce:true});
		myScroll.on('scroll', onScroll);
		myScroll.on('scrollEnd', onScrollEnd);		

	}
	
	function pullDownAction() {//下拉事件  
		if(!downprocessor) return;
		
		downprocessor(function(){
			var el, li, i;  
		
			pullDownEl.removeClass('loading');  
			pullDownL.html('下拉显示更多...');  
			pullDownEl['class'] = pullDownEl.attr('class');  
			pullDownEl.attr('class','').hide();  
			myScroll.refresh();  
			loadingStep = 0;  
		});
	}  
	
	
	function pullUpAction() {//上拉事件  
		if(!upprocessor) return;
		upprocessor(function() {  
			var el, li, i;  
			
			pullUpEl.removeClass('loading');  
			pullUpL.html('上拉显示更多...');  
			pullUpEl['class'] = pullUpEl.attr('class');  
			pullUpEl.attr('class','').hide();  
			myScroll.refresh();  
			loadingStep = 0;  
		});  
	}  
	function onScroll() {
		  if(loadingStep == 0 && !pullDownEl.attr('class').match('flip|loading') && !pullUpEl.attr('class').match('flip|loading')){  
			if (this.y > 5) {  
				if(!downprocessor)  return;
				//下拉刷新效果  
				pullDownEl.attr('class',pullUpEl['class'])  
				pullDownEl.show();  
				myScroll.refresh();  
				pullDownEl.addClass('flip');  
				pullDownL.html('刷新');  
				loadingStep = 1;  
			}else if (this.y < (this.maxScrollY - 5)) {  
				if(!uprocessor)  return;
				//上拉刷新效果  
				pullUpEl.attr('class',pullUpEl['class'])  
				pullUpEl.show();  
				myScroll.refresh();  
				pullUpEl.addClass('flip');  
				pullUpL.html('准备刷新...');  
				loadingStep = 1;  
			}  
			}  
		
	}
	function onScrollEnd(){
		 if(loadingStep == 1){  
			if (pullUpEl.attr('class').match('flip|loading')) {  
				if(!uprocessor)  return;
				pullUpEl.removeClass('flip').addClass('loading');  
				pullUpL.html('Loading...');  
				loadingStep = 2;  
				pullUpAction();  
			}else if(pullDownEl.attr('class').match('flip|loading')){  
				if(!downprocessor)  return;
				pullDownEl.removeClass('flip').addClass('loading');  
				pullDownL.html('Loading...');  
				loadingStep = 2;  
				pullDownAction();  
			}  
		}  
	}
	

	init();
	
}




