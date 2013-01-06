//  Created by 拓 小林
//  Copyright (c) 2012年 __CompanyName__. All rights reserved.

package com.taku.kobayashi.pngcamera;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.http.util.ByteArrayBuffer;

import android.app.Activity;
import android.app.ActivityManager;
import android.view.WindowManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.Toast;

public class ExtraLayout {

	//define
	private static final String TAG = "PNGCamera_ExtraLayout";
	private static final float LIST_BASE_CELL_HEIGHT = 42f;
	private static final float LIST_CELL_HEIGHT_PADDING = 10f;

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	private static DisplayMetrics getDisplayMetrics(Context context){
		DisplayMetrics displayMetrics = new DisplayMetrics();
		((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(displayMetrics);
		return displayMetrics;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	//端末の解像度を取得
	public static CGSize getDisplaySize(Context context){
		DisplayMetrics displayMetrics = getDisplayMetrics(context);
		return new CGSize(displayMetrics.widthPixels,displayMetrics.heightPixels);
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public static int getListCellMinHeight(Context context){
		DisplayMetrics displayMetrics = getDisplayMetrics(context);
		int minHeight = (int)(LIST_BASE_CELL_HEIGHT * displayMetrics.density);
		return minHeight;
	}

	public static int getListCellHeightPadding(Context context){
		DisplayMetrics displayMetrics = getDisplayMetrics(context);
		int paddingHeight = (int)(LIST_CELL_HEIGHT_PADDING * displayMetrics.density);
		return paddingHeight;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------
/*
	//ベース(iPhone)となる縦横比に調整した画面の幅と高さの計算
	private static CGSize getDisplayResize(Context context) {

		float width;
		float height;

		CGSize displaySize = getDisplaySize(context);

		// 縦長の解像度端末
		if (BASE_ASPECT_RATIO > displaySize.aspectRatio) {
			width = displaySize.width;
			height = (width * BASE_DISPLAY_HEIGHT / BASE_DISPLAY_WIDTH);
		} else if (BASE_ASPECT_RATIO < displaySize.aspectRatio) {
			height = displaySize.height;
			width = height * BASE_DISPLAY_WIDTH / BASE_DISPLAY_HEIGHT;
		} else {
			width = displaySize.width;
			height = displaySize.height;
		}

		return new CGSize(width,height);
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	//ベース(iPhone)となる縦横比に調整した場合の拡縮率の計算
	public static float getResizeRatio(Context context) {
		//ipone版に合わせたサイズに計算する
		float sizeRatio;

		CGSize displaySize = getDisplaySize(context);

		// 縦長の解像度端末
		if (BASE_ASPECT_RATIO >= displaySize.aspectRatio) {
			sizeRatio = (float) displaySize.width / BASE_DISPLAY_WIDTH;
		} else {
			sizeRatio = (float) displaySize.height / BASE_DISPLAY_HEIGHT;
		}

		return sizeRatio;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	//画像の幅、高さを取得する
	public static CGSize getImageSize(Context context, Integer resId) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		options.inScaled = false;
		BitmapFactory.decodeResource(context.getResources(), resId, options);
		return new CGSize(options.outWidth, options.outHeight);
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	//ベース(iPhone)となる縦横比に調整した場合の画像の画像の幅、高さを拡縮率を反映させた値にする
	public static CGSize getImageResize(Context context, Integer resId) {
		CGSize size = getImageSize(context,resId);
		//iphoneの解像度で使用しているしている画像をAndroidの解像度に合わせたサイズで表示させるための計算
		return new CGSize(size.width * getResizeRatio(context),size.height * getResizeRatio(context));
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	//ベース(iPhone)となる縦横比にViewを調整する
	public static View getParenetView(Activity act,Integer layoutID){
		//レイアウトを作って返す
		LinearLayout outSideLayout = new LinearLayout(act);
		outSideLayout.setGravity(Gravity.CENTER);
		View view = act.getLayoutInflater().inflate(layoutID, null);
		view.setLayoutParams(new LayoutParams((int)getDisplayResize(act).width,(int)getDisplayResize(act).height));
		outSideLayout.addView(view);
		return outSideLayout;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	//Resource(drawableフォルダ)から画像を取ってくる場合
	public static Bitmap getBitmap(Context con, Integer rsc) {

		BitmapFactory.Options options = new BitmapFactory.Options();

		int nInSampleSize = 1;
		nInSampleSize = (int)Math.floor(Math.max(getImageSize(con, rsc).width / getDisplaySize(con).width,getImageSize(con, rsc).height / getDisplaySize(con).height));
		options.inSampleSize = nInSampleSize;
		return BitmapFactory.decodeResource(con.getResources(), rsc, options);

	}
*/
	//---------------------------------------------------------------------------------------------------------------------------------------------------------------
/*
	//画面をベース(iPhone)となる縦横比に調整した場合、画像の幅、高さを拡縮率を反映させた値に調整する
	public static void setBaseImageView(Activity act,ImageView imageView,Integer res){
		CGSize imageSize = Tools.getImageResize(act, res);
		imageView.getLayoutParams().width = (int)imageSize.width;
		imageView.getLayoutParams().height = (int)imageSize.height;
		imageView.setImageResource(res);
	}
*/
	//---------------------------------------------------------------------------------------------------------------------------------------------------------------
/*
	//画面をベース(iPhone)となる縦横比に調整した場合、画像の幅、高さを拡縮率を反映させた値に調整する
	public static void setBaseImageView(Activity act,ImageView imageView,Bitmap image){
		CGSize imageSize = new CGSize(image.getWidth() * getResizeRatio(act), image.getHeight() * getResizeRatio(act));
		imageView.getLayoutParams().width = (int)imageSize.width;
		imageView.getLayoutParams().height = (int)imageSize.height;
		imageView.setImageBitmap(image);
	}
*/
	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	/*
	public static Bitmap getBitmapRGB565(Context con, Integer rsc,int nSampleSize) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		//int nInSampleSize = 1;
		//nInSampleSize = (int)Math.floor(Math.max(getImageSize(con, rsc).width / getDisplaySize(con).width,getImageSize(con, rsc).height / getDisplaySize(con).height));
		options.inSampleSize = nSampleSize;
		options.inPreferredConfig = Bitmap.Config.RGB_565;
		return BitmapFactory.decodeResource(con.getResources(), rsc, options);
	}
	*/

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	/*
	public static Bitmap getBitmapRGB565(Context context,byte[] data,int nSampleSize){
		BitmapFactory.Options bitmapFactoryOptions = new BitmapFactory.Options();
		bitmapFactoryOptions.inSampleSize = nSampleSize;
		bitmapFactoryOptions.inPreferredConfig = Bitmap.Config.RGB_565;
		Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, bitmapFactoryOptions);
		return bitmap;
	}
	*/

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	/*
	public static Bitmap getBitmapRGB565(String url,int nSampleSize) {
		Bitmap bm = null;
		try {
			BitmapFactory.Options bitmapFactoryOptions = new BitmapFactory.Options();
			bitmapFactoryOptions.inSampleSize = nSampleSize;
			bitmapFactoryOptions.inPreferredConfig = Bitmap.Config.RGB_565;
			URL aURL = new URL(url);
			URLConnection conn = aURL.openConnection();
			conn.connect();
			InputStream is = conn.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is);
			bm = BitmapFactory.decodeStream(new FilterInputStream(is){
				@Override
				public long skip(long n) throws IOException {
					long totalBytesSkipped = 0L;
					while (totalBytesSkipped < n) {
						long bytesSkipped = in.skip(n - totalBytesSkipped);
						if (bytesSkipped == 0L) {
							int b = read();
							if (b < 0) {
								break; // we reached EOF
							} else {
								bytesSkipped = 1; // we read one byte
							}
						}
						totalBytesSkipped += bytesSkipped;
					}
					return totalBytesSkipped;
				}
			},null,bitmapFactoryOptions);
			bis.close();
			is.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bm;
	}
	*/

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	/*
	public static void setResultImageView(Activity act,ImageView imageView,Bitmap image,int nScale){
		CGSize imageSize = new CGSize(image.getWidth() * getResizeRatio(act) * nScale, image.getHeight() * getResizeRatio(act) * nScale);
		imageView.getLayoutParams().width = (int)imageSize.width;
		imageView.getLayoutParams().height = (int)imageSize.height;
		imageView.setImageBitmap(image);
	}
	*/

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	//画像を引き延ばす際の画像の粗さを除去
	public static Bitmap makeFilterImage(Bitmap image){
		Bitmap FilteredImage = Bitmap.createScaledBitmap(image, image.getWidth(), image.getHeight(), true);
		image.recycle();
		image = null;
		return FilteredImage;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	//画像を合成して新しい画像を作る(メモリ使用量削減のための実装)
	public static Bitmap makeComposeingImage(Bitmap base,Bitmap add,RectF dst){
		Paint paint = new Paint();
		paint.setFilterBitmap(true);
		paint.setAntiAlias(true);
		//Bitmap edit = base.copy(Bitmap.Config.RGB_565, true);
		Bitmap edit = base.copy(Bitmap.Config.ARGB_8888, true);
		Canvas canvas = new Canvas(edit);
		canvas.drawBitmap(add, new Rect(0,0,add.getWidth(),add.getHeight()), dst, paint);
		canvas.save();
		base.recycle();
		base = null;
		add.recycle();
		add = null;
		return edit;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

}
