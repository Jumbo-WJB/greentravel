package com.android.greentravel.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class UtilBitmap {
	static final String TAG = "UtilBitmap";
	private static Context mContext = null;

	public static void init(Context context) {
		mContext = context;
	}

	public static Bitmap getBitmapScaled(Bitmap bitmap, int width, int height) {
		if (bitmap == null) {
			return null;
		}
		if (width < 0 || height < 0)
			return bitmap;

		float scaleW = 1.0f * width / bitmap.getWidth();
		float scaleH = 1.0f * height / bitmap.getHeight();

		if (scaleW == 1.0f && scaleH == 1.0f) {
			return bitmap;
		}

		Matrix matrix = new Matrix();
		matrix.postScale(scaleW, scaleH);
		Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
				bitmap.getHeight(), matrix, true);
		return newBitmap;
	}

	public static Bitmap getBitmap(Resources resouce, int resId) {
		Bitmap bitmap = BitmapFactory.decodeResource(resouce, resId);
		return bitmap;
	}

	public static Bitmap getBitmap(int resId) {
		// Options options = new Options();
		// options.inJustDecodeBounds = true;
		// BitmapFactory.decodeResource(mContext.getResources(), resId,
		// options);
		// options.inJustDecodeBounds = false;
		// if(options.outWidth > 200 || options.outHeight > 200){
		// options.inSampleSize = options.outHeight / 200 > options.outWidth /
		// 200 ?
		// options.outHeight / 200 : options.outWidth / 200;
		// }
		//
		return BitmapFactory.decodeResource(mContext.getResources(), resId);
	}

	public static Bitmap getBitmap(String url, int timeout) {
		URL myFileURL;
		Bitmap bitmap = null;
		try {
			myFileURL = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) myFileURL
					.openConnection();
			conn.setConnectTimeout(timeout);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			InputStream is = conn.getInputStream();
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			bitmap = BitmapFactory.decodeStream(is);
			is.close();
			return bitmap;
		} catch (Exception e) {
		}

		return null;
	}

	public static Bitmap getBitmap(String fileName) {
		try {
			File f = new File(fileName);
			if (!f.exists())
				return null;
			InputStream is = new FileInputStream(f);
			Bitmap bm = BitmapFactory.decodeStream(is);
			is.close();
			return bm;
		} catch (Exception e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Bitmap getSmallBitmap(String fileName, int width, int height) {
		try {
			File f = new File(fileName);
			if (!f.exists())
				return null;
			InputStream is = new FileInputStream(f);
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = false;
			int widthscaled = options.outWidth / width;
			int heightscaled = options.outHeight / height;
			options.inSampleSize = widthscaled > heightscaled ? widthscaled
					: heightscaled;
			Bitmap btp = BitmapFactory.decodeStream(is, null, options);
			is.close();
			return btp;
		} catch (Exception e) {

		} catch (OutOfMemoryError e) {

		}

		return null;
	}

	public static Bitmap getBitmap(AssetManager assetManager, String name) {
		try {
			InputStream is = assetManager.open(name);
			Bitmap bm = BitmapFactory.decodeStream(is);
			is.close();
			return bm;
		} catch (IOException e) {
		}

		return null;
	}

	

	public static Bitmap drawableToBitmap(Drawable drawable) {
		int width = drawable.getIntrinsicWidth();
		int height = drawable.getIntrinsicHeight();
		Bitmap bitmap = Bitmap.createBitmap(width, height, drawable
				.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
				: Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, width, height);
		drawable.draw(canvas);
		return bitmap;
	}

	public static Bitmap createMirrorBitmap2(Bitmap originalImage,
			int reflectionHeight) {
		int reflectionGap = 0;

		int width = originalImage.getWidth();
		int height = originalImage.getHeight();

		Matrix matrix = new Matrix();
		matrix.preScale(1, -1);

		Bitmap bitmap = Bitmap.createBitmap(originalImage, 0, height
				- reflectionHeight, width, reflectionHeight, matrix, false);

		Bitmap reflectionImage = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(reflectionImage);
		canvas.drawBitmap(bitmap, 0, 0, null);
		bitmap.recycle();
		Paint paint = new Paint();
		LinearGradient shader = new LinearGradient(0,
				originalImage.getHeight(), 0, reflectionImage.getHeight()
						+ reflectionGap, 0xf0ffffff, 0x00ffffff,
				Shader.TileMode.MIRROR);

		paint.setShader(shader);

		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));

		canvas.drawRect(0, 0, width, reflectionImage.getHeight()
				+ reflectionGap, paint);

		return reflectionImage;
	}

	public static Bitmap createMirrorBitmap(Bitmap originalImage,
			int reflectionHeight) {
		int reflectionGap = 2;

		int width = originalImage.getWidth();
		int height = originalImage.getHeight();

		Matrix matrix = new Matrix();
		matrix.preScale(1, -1);

		Bitmap bitmap = Bitmap.createBitmap(originalImage, 0, height
				- reflectionHeight, width, reflectionHeight, matrix, false);

		Bitmap reflectionImage = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight() + reflectionGap, Config.ARGB_8888);
		Canvas canvas = new Canvas(reflectionImage);
		canvas.drawBitmap(bitmap, 0, reflectionGap, null);
		bitmap.recycle();
		Paint paint = new Paint();
		// 创建LinearGradient并设置渐变颜色数组
		// 第一个,第二个参数表示渐变起点 可以设置起点终点在对角等任意位置
		// 第三个,第四个参数表示渐变终点
		// 第五个参数表示渐变颜色
		// 第六个参数可以为空,表示坐标,值为0-1 new float[] {0.25f, 0.5f, 0.75f, 1 }
		// 如果这是空的，颜色均匀分布，沿梯度线。
		// 第七个表示平铺方式
		// CLAMP重复最后一个颜色至最后
		// MIRROR重复着色的图像水平或垂直方向已镜像方式填充会有翻转效果
		// REPEAT重复着色的图像水平或垂直方向
		// LinearGradient shader = new LinearGradient(0,
		// originalImage.getHeight(), 0, reflectionImage.getHeight()
		// + reflectionGap, 0xf0ffffff, 0x00ffffff,
		// Shader.TileMode.MIRROR);

		LinearGradient shader = new LinearGradient(0, 0, 0,
				reflectionImage.getHeight() + reflectionGap, 0x80ffffff,
				0x00ffffff, Shader.TileMode.MIRROR);

		paint.setShader(shader);

		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));

		canvas.drawRect(0, 0, width, reflectionImage.getHeight()
				+ reflectionGap, paint);

		return reflectionImage;
	}
}
