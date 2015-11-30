package com.android.greentravel.util;

import com.android.greentravel.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class SWDialogUtil {
	
	
    private static SWDialogUtil mInstance = null;
	
	public void initShow(Context mContext,String strMessage)
	{
		try {
			new AlertDialog.Builder(mContext)
			.setMessage(strMessage)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setTitle(mContext.getString(R.string.tips))
			.setCancelable(false)
			.setPositiveButton(mContext.getString(R.string.ok), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			}).show();	
		} catch (Exception e) {
			e.printStackTrace();
		}
		   
	}
	
	
    public static SWDialogUtil getInstance(){
        if(mInstance == null){
            mInstance = new SWDialogUtil();
        }

        return mInstance;
    }
	
    
    
    
    

    
    
	
}
