package com.android.greentravel;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.logging.Handler;

/**
 * Created by luodong on 2015-8-27.
 */
public class WebViewWrapper {
    private WebView mWv;
    private Context mContext;
    private JSInterface mJse;


    /**
     * 注意需要在UI线程调用此方法
     * @param event
     * @param param
     */
    public void raiseEvent(final String event,final String param) {
        mWv.loadUrl("javascript:onEvent('"+event+"','" + param + "')");
    }

    public WebViewWrapper(Context context,WebView wv){
        mWv = wv;
        this.mContext = context;
        initWebView(wv);
    }


    private void addJavaScriptInterface(WebView wv){
        mJse = new JSInterface();
        mJse.init(this.mContext, null);
        wv.addJavascriptInterface(mJse, "_jsbridge");
    }

    private void initWebView(WebView wv){
        wv.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // 重写此方法表明点击网页里面的链接还是在当前的webview里跳转，不跳到浏览器那边
                view.loadUrl(url);
                return true;
            }
        });
        wv.getSettings().setJavaScriptEnabled(true);
//        wv.setBackgroundColor(Color.TRANSPARENT);

        //enablecrossdomain();
        wv.getSettings().setDomStorageEnabled(true);
        wv.getSettings().setDatabaseEnabled(true);
        wv.getSettings().setAllowFileAccessFromFileURLs(true);
        wv.getSettings().setAllowUniversalAccessFromFileURLs(true);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            wv.getSettings().setDatabasePath("/data/data/" + wv.getContext().getPackageName() + "/databases/");
        }
        addJavaScriptInterface(mWv);
    }

    /**
     * 可用跨域访问
     */
    private void enablecrossdomain() {

        try {
            Field webviewclassic_field = WebView.class
                    .getDeclaredField("mProvider");
            webviewclassic_field.setAccessible(true);
            Object webviewclassic = webviewclassic_field.get(this);
            Field webviewcore_field = webviewclassic.getClass()
                    .getDeclaredField("mWebViewCore");
            webviewcore_field.setAccessible(true);
            Object mWebViewCore = webviewcore_field.get(webviewclassic);
            Field nativeclass_field = webviewclassic.getClass()
                    .getDeclaredField("mNativeClass");
            nativeclass_field.setAccessible(true);
            Object mNativeClass = nativeclass_field.get(webviewclassic);

            Method method = mWebViewCore.getClass().getDeclaredMethod(
                    "nativeRegisterURLSchemeAsLocal",
                    new Class[] { int.class, String.class });
            method.setAccessible(true);
            method.invoke(mWebViewCore, mNativeClass, "http");
            method.invoke(mWebViewCore, mNativeClass, "https");
        } catch (Exception e) {
            Log.e("webview", "enablecrossdomain error");
            // e.printStackTrace();
        }

    }
}
