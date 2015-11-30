package com.android.greentravel;

import org.xwalk.core.XWalkResourceClient;
import org.xwalk.core.XWalkView;

import android.content.Context;
import android.webkit.WebResourceResponse;

/**
 * Created by luodong on 2015-8-27.
 */
public class WalkWebViewWrapper {
    private XWalkView mWv;
    private Context mContext;
    private JSInterface mJse;


    /**
     * 注意需要在UI线程调用此方法
     * @param event
     * @param param
     */
    public void raiseEvent(final String event,final String param) {
        mWv.load("javascript:onevent('"+event+"','" + param + "')", null); 
    }

    public WalkWebViewWrapper(Context context,XWalkView wv){
        mWv = wv;
        this.mContext = context;
        initWebView(wv);
    }


    private void addJavaScriptInterface(XWalkView wv){
        mJse = new JSInterface();
        mJse.init(this.mContext, this);
        wv.addJavascriptInterface(mJse, "_jsbridge");
    }

    private void initWebView(XWalkView wv){
    	mWv.setResourceClient(new MyResourceClient(mWv));
        addJavaScriptInterface(mWv);
    }
    
    private class MyResourceClient extends XWalkResourceClient {
        MyResourceClient(XWalkView view) {
            super(view);
        }

        @Override
        public WebResourceResponse shouldInterceptLoadRequest(XWalkView view, String url) {
            // Handle it here.
            return super.shouldInterceptLoadRequest(view,url);
        }

        @Override
        public void onReceivedLoadError(XWalkView view, int errorCode, String description, String failingUrl) {
            return;
            //super.onReceivedLoadError(view, errorCode, description, failingUrl);
        }
    }

   
}
