package com.android.greentravel.util;


import com.android.greentravel.R;
import android.content.Context;


public class SWHttpError {
	
	
	
	public static String errorMessage(Context mContext, int statusCode){
		String ret = "";
		switch(statusCode){
		case 600:
			ret = mContext.getString(R.string.error_600);
			break;
		case 620:
			ret = mContext.getString(R.string.error_620);
			break;
		case 621:
			ret = mContext.getString(R.string.error_621);
			break;
		case 622:
			ret = mContext.getString(R.string.error_622);
			break;
		case 623:
			ret = mContext.getString(R.string.error_623);
			break;
		case 670:
			ret = mContext.getString(R.string.error_670);
			break;
		
		
		default:
			ret = mContext.getString(R.string.error_unknown);
			break;
		}
		return ret;
	}
	
	
}
