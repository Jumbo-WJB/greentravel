package com.android.greentravel.speex;


public class Speex  {
	/* quality
	 * 1 : 4kbps (very noticeable artifacts, usually intelligible)
	 * 2 : 6kbps (very noticeable artifacts, good intelligibility)
	 * 4 : 8kbps (noticeable artifacts sometimes)
	 * 6 : 11kpbs (artifacts usually only noticeable with headphones)
	 * 8 : 15kbps (artifacts not usually noticeable)
	 */
	public static final int DEFAULT_FRAME_SIZE = 320;
	public static final int DEFAULT_SAMPLE_RATE = 8000;
	
	private int mFrameSize;
	private int mSampleRate;

	public void init() {
		mFrameSize = DEFAULT_FRAME_SIZE;
		mSampleRate  = DEFAULT_SAMPLE_RATE;
		open(mFrameSize, mSampleRate);
	}
	
	public void init(int frame_size, int sample_rate) {
		mFrameSize = frame_size;
		mSampleRate  = sample_rate;
		open(mFrameSize, mSampleRate);
	}
	
	public void exit(){
		close();
	}
	
	static {
		try {
			System.loadLibrary("speex_jni");
		} catch (Throwable e) {
			e.printStackTrace();
		}

	};

	public native int open(int frame_size, int sample_rate);
	public native int denoise(byte src[], byte dst[], int size);
	public native void close();
}