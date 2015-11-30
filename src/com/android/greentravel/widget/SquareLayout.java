package com.android.greentravel.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class SquareLayout extends LinearLayout{


	 public SquareLayout(Context context) {
	         super(context);
	 }
	
	 public SquareLayout(Context context, int horizontalSpacing, int verticalSpacing) {
	         super(context);
	 }
	
	 public SquareLayout(Context context, AttributeSet attrs) {
	         super(context, attrs);
	 }
	

	
	 
	 @Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		 
		heightMeasureSpec= widthMeasureSpec;
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	

}
