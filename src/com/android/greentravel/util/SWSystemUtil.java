package com.android.greentravel.util;

public class SWSystemUtil {

	public static void threadDelay(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static String generateRandomCharAndNumber(Integer len) {
		StringBuffer sb = new StringBuffer();
		for (Integer i = 0; i < len; i++) {

			int numValue = (int) (Math.random() * 10);
			sb.append(numValue);
		}
		return sb.toString();
	}

}
