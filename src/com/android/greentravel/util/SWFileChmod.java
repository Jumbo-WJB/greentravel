package com.android.greentravel.util;

import java.io.IOException;

import android.util.Log;

public class SWFileChmod {

	public  static final String TAG = "SWFileChmod";
	
	public static String mpath=null;
	public  static  void changed(String path )
	{
		mpath=path;
		new Thread(new Runnable() {
			String path=mpath;
			@Override
			public void run() {
				// TODO Auto-generated method stub
				chmod(path);
			}
		}).start();
	}
	
	
	public static void chmod(String path)
	{
		try 
		{			
            String command = "chmod 777 " + path;
            Log.i(TAG, "command = " + command);
            Runtime runtime = Runtime.getRuntime(); 
            Process proc = runtime.exec(command);
		} 
		catch (IOException e) 
		{
			 Log.i(TAG,"chmod fail!!!!");
             e.printStackTrace();
		}
	}
}
