package com.android.greentravel.view;

import com.android.greentravel.R;
import com.android.greentravel.util.Constant;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

public class MotionDetectionView extends ImageView {
    private final String TAG = "MotionDetectionView";
    private final String TAG1 = "MotionDraw";


	public final int LEFTTOP=0;
	public final int RIGHTTOP=1;
	public final int LEFTBOTTOM=2;
	public final int RIGHTBOTTOM=3;
	public final int CENTERER=4;
	
	public int location=CENTERER;
	

    private Context mContext = null;
    private Handler mCallback= null;

    private String mTitle = null;
    private String mMotionID = null;
    private String mUid = null;
    private int mLineWidth = 15;
    private int mCorner= 25;   //左上角、右下角 区域宽度
    public int mWidth = 280;
    public int mHeight = 200; 
    public int mLeft = 0;
    public int mTop = 0;  
    private int mRight = 0;
    private int mBottom = 0;
    private double mZoomY;
    
    private int mStartX = 0;
    private int mStartY = 0;
    private int mStartL = 0;
    private int mStartT = 0;
    private int mStartR = 0;
    private int mStartB = 0;
    
    private int mMaxWidth = 0;    
    private int mMaxHeight = 0;

    private boolean mCanMove = false;
    private boolean mMoved = false;
    private boolean mChangedSize = false;
    
    private Bitmap rbStretch= null;  //右下角拖拽缩放提示图标
    private Bitmap ltStretch= null;  //左上角拖拽缩放提示图标
    

    public MotionDetectionView(Context context, Handler callback) {
        super(context);
        mContext = context;
        mCallback= callback;
    }
    
    
    public void initMoiton(int[] monitoninfo,int maxwindth,int maxheight, double zoomY)
    {
    	mLeft=monitoninfo[0];
    	mTop=monitoninfo[1];
    	mWidth=monitoninfo[2];
    	mHeight=monitoninfo[3];
    	mMaxWidth=maxwindth;
    	mMaxHeight=maxheight;
    	mZoomY= zoomY;
    	mCorner= (int) (25/mZoomY);
    	Bitmap bitmap= BitmapFactory.decodeResource(mContext.getResources(), R.drawable.rbstretch);
        rbStretch= Bitmap.createScaledBitmap(bitmap, mCorner, mCorner, true);
        
        Bitmap bitmap1= BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ltstretch);
        ltStretch= Bitmap.createScaledBitmap(bitmap1, mCorner, mCorner, true); 
    }

   
    int[] oldLoction;
    int[] NewLoction;
    public boolean onTouchEvent(MotionEvent event){
        int x = (int)event.getX();
        int y = (int)event.getY();
        int action = event.getAction();
        switch (action){
            case MotionEvent.ACTION_DOWN:
            	Log.i(TAG1, "ACTION_DOWN ");
            	oldLoction=new int[]{getLeft(),getTop(),mWidth,mHeight};
                mCanMove = true;
                int result=bChangeSizLoaction(x,y);
                switch (result) {
				case LEFTTOP:
					mChangedSize=true;
					location=LEFTTOP;
					break;
                case RIGHTTOP:
                	mChangedSize=true;
                	location=RIGHTTOP;
					break;
				case LEFTBOTTOM:
					mChangedSize=true;
					location=LEFTBOTTOM;
					break;
				case RIGHTBOTTOM:
					mChangedSize=true;
					location=RIGHTBOTTOM;
					break;
				case CENTERER:
					mChangedSize=false;
					location=CENTERER;
					break;

				default:
					break;
				}
                break;
            case MotionEvent.ACTION_MOVE:
            	Log.i(TAG, "ACTION_MOVE ");
                mMoved = true;
                break;
            case MotionEvent.ACTION_UP:
            	Log.i(TAG, "ACTION_UP ");
                mCanMove = false;
                mMoved = false;
                mChangedSize=false; 
                
                break;
        }
        
		if(mChangedSize)
		{
			ChangedSize(x,y);	
			return true;
		}
		else
		{
			if (mCanMove) {

				if (mMoved) {
					mLeft = getLeft() + (x - mStartX);
					mTop = getTop() + (y - mStartY);

					Log.i(TAG1, "----###MOVE----mMaxWidth="+mMaxWidth+"---mMaxHeight="+mMaxHeight+"---mRight="+mRight+"---mBottom="+mBottom+"---mLeft="+mLeft+"---mTop="+mTop);
					
					mLeft = mLeft < 0 ? 0 : mLeft;
					mTop = mTop < 0 ? 0 : mTop;
					
					if((mLeft + mWidth)>= mMaxWidth){
						mLeft= mMaxWidth-mWidth;
					}
					
					if((mTop + mHeight)>= mMaxHeight){
						mTop= mMaxHeight- mHeight;
					}
					invalidate();
				}
				return true;
			} else {
				TheFinish();
				return true;
			}
		}
	} 
    
    
    public void ChangedSize(int x, int y)
    {
    	Log.i(TAG1, "changeSize");
    	if(mMoved)
		{
			switch (location) {
			case LEFTTOP:
				 
				 mLeft = getLeft() + (x - mStartX);
				 mTop = getTop() + (y - mStartY) - mCorner; //有些设备不容易拖拽到顶部，需要 - mCorner
				 
				 if(mLeft<= 0){
					 mLeft= 0;
				 }
				 if(mTop<= 0){
					 mTop= 0;
				 }
				 
				 if((mStartR - mLeft) <= mCorner){
					 mLeft= mStartR - mCorner;
				 }
				 if((mStartB - mTop)<= mCorner){
					 mTop= mStartB - mCorner;
				 }
				 mWidth = mStartR - mLeft;
				 mHeight= mStartB - mTop;
				 
				
                 Log.i(TAG, "result:LEFTTOP");
             	 Log.i(TAG, "left:"+mLeft+" right:"+mRight);
            	 Log.i(TAG, "top:"+mTop+" bottom:"+mBottom);
            	 setFrame(mLeft, mTop, mStartR, mStartB);
				break;
				
			case RIGHTBOTTOM:
				 mWidth=x;
				 mHeight=y + mCorner; //有些设备不容易拖拽到底部，需要 + mCorner
    			 mLeft = mStartL;
                 mTop = mStartT;
                 
                 if((mLeft + mWidth)>= mMaxWidth){
                	 mWidth= mMaxWidth - mLeft;
                 }
                 if((mTop + mHeight)>= mMaxHeight){
                	 mHeight= mMaxHeight - mTop;
                 }
                 
                 if(mWidth<= mCorner){
                	 mWidth= mCorner;
                 }
                 if(mHeight<= mCorner){
                	 mHeight=  mCorner;
                 }
                 
                 
                 
                 
                 Log.i(TAG, "result:RIGHTBOTTOM");
                 Log.i(TAG, "left:"+mLeft+" right:"+mRight);
                 Log.i(TAG, "top:"+mTop+" bottom:"+mBottom);
                 setFrame(mLeft, mTop, (mLeft+mWidth), (mTop+mHeight));
				break;

			default:
				break;
			}                 
            invalidate();
		}
    }

    
    public void TheFinish(){
    	Log.i(TAG1, "Finish");
    	
    	NewLoction=new int[]{getLeft(),getTop(),mWidth,mHeight};
    	Message msg= new Message();
    	msg.what= Constant.MSG_CHANGEMONITON;
    	msg.obj= NewLoction;
    	mCallback.sendMessage(msg);
//    	MotionManager.getMotionManager().changemotion(mUid,mMotionID,NewLoction);
    	
    	
    	
		invalidate();
  
    }
    

    
    public int bChangeSizLoaction(int x,int y)
    {
    	Log.i(TAG, "*************");
    	Log.i(TAG, "mStartX:"+x+"----mStartY:"+y);
    	Log.i(TAG, "left:"+getLeft()+" right:"+getRight());
    	Log.i(TAG, "top:"+getTop()+" bottom:"+getBottom());
    	mStartX= x;
    	mStartY= y;
    	mStartL= getLeft();
    	mStartT= getTop();
    	mStartR= getLeft() + mWidth;
    	mStartB= getTop() + mHeight;
    	Log.i(TAG, "R:"+mStartR);
        if(Math.abs(getRight()-(x+getLeft()))<mCorner*2&&Math.abs(getBottom()-(y+getTop()))<mCorner*2)
        {
        	return RIGHTBOTTOM;
        }
        else if(Math.abs(x)<mCorner&&Math.abs(getBottom()-(y+getTop()))<mCorner)
        {
        	return LEFTBOTTOM;
        }
        else if(Math.abs(x)<mCorner*2&&Math.abs(y)<mCorner*2)
        {
        	return LEFTTOP;
        }
        else if(Math.abs(getRight()-(x+getLeft()))<mCorner&&Math.abs(y)<mCorner)
        {
        	return RIGHTTOP;
        }
        else
        {
        	return CENTERER;
        }
    }

   
    
    

    public String getMotionID(){
        return mMotionID;
    }
    
    public void setMotionID(String motionID){
    	mMotionID=motionID;
    }
    
    public String getUid(){
        return mUid;
    }
    
    public void setUid(String uid){
        mUid=uid;
    }
    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG );
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.i(TAG, "onDraw ");
        
        paint.setARGB(255, 0, 255, 0);
        paint.setStrokeWidth(mLineWidth);
       
        canvas.drawBitmap(rbStretch, mWidth-mCorner, mHeight-mCorner, paint);  //右下角拖拽缩放提示图标
        canvas.drawBitmap(ltStretch, 0, 0, paint);  //左上角拖拽缩放提示图标
        
        canvas.drawLine(0, 0, 0, mHeight, paint);
        canvas.drawLine(0, mHeight, mWidth, mHeight, paint);
        canvas.drawLine(0, 0, mWidth, 0, paint);
        canvas.drawLine(mWidth, 0, mWidth, mHeight, paint);
         
        if(mTitle != null){
            paint.setARGB(80, 255, 0, 0);
            paint.setTextSize(26);
            canvas.drawText(mTitle, mLineWidth + 5, 30, paint);
        }
        setFrame(mLeft, mTop, mLeft + mWidth, mTop + mHeight);
        
    }
}
