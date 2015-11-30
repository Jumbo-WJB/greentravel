package com.android.greentravel.util;

public class EncG726 {
	public static final int API_ER_ANDROID_NULL = -10000;
	public static final byte FORMAT_ALAW = 1;
	public static final byte FORMAT_LINEAR = 2;
	public static final byte FORMAT_ULAW = 0;
	public static final int G726_16 = 0;
	public static final int G726_24 = 1;
	public static final int G726_32 = 2;
	public static final int G726_40 = 3;
	private static EncG726 mG726Enc = new EncG726();

	static {
		try {
			System.loadLibrary("g726jni");
		} catch (UnsatisfiedLinkError localUnsatisfiedLinkError) {
			System.out.println("loadLibrary(g726jni),"
					+ localUnsatisfiedLinkError.getMessage());
		}
	}

	public static EncG726 getEnc() {
		return mG726Enc;
	}

	public native void native_g726_init(int bitrate);

	public native void native_g726_deinit();

	public native int native_g726_decode(byte[] src, int len, byte[] dst);

	public native int native_g726_encode(byte[] src, int len, byte[] dst);
}
