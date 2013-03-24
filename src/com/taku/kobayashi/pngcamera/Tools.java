//  Created by 拓 小林
//  Copyright (c) 2013年 TakuKobayashi All rights reserved.

package com.taku.kobayashi.pngcamera;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.Toast;

public class Tools {

	//define
	private static final String TAG = "PNGCamera_Tools";

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	//SDカードから幅と高さを指定したサイズを基準にした値になるように縮小した画像を取得する
	public static Bitmap getSelectSizeBitmap(Context con, Uri uri,int width, int height) {
		//分割させる値の計算
		int orientation = Tools.getImageOrientation(uri, con);
		BitmapFactory.Options calcOptions = new BitmapFactory.Options();
		calcOptions.inJustDecodeBounds = true;
		int imageWidth = 0;
		int imageHeight = 0;
		int currentWidth = 0;
		int currentHeight = 0;
		try {
			InputStream is = con.getContentResolver().openInputStream(uri);
			BitmapFactory.decodeStream(is, null, calcOptions);
			is.close();
			imageWidth = calcOptions.outWidth;
			imageHeight = calcOptions.outHeight;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		//90度回転させるとき
		if(Math.abs(orientation % 180) == 90){
			currentWidth = imageHeight;
			currentHeight = imageWidth;
		}else{
			currentWidth = imageWidth;
			currentHeight = imageHeight;
		}
		int nSampleSize = 1;
		if (currentWidth > width || currentHeight > height) {
			nSampleSize = (int) Math.max(Math.max(Math.ceil(currentWidth / width), Math.ceil((currentHeight / height))), 1);
		}

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = nSampleSize;
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;

		Bitmap tmp = null;
		try {
			InputStream is = con.getContentResolver().openInputStream(uri);
			tmp = BitmapFactory.decodeStream(is, null, options);
			is.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		Bitmap bitmap = null;
		if(tmp == null){
			return null;
		}else{
			bitmap = tmp.copy(Config.ARGB_8888, true);
		}

		if (orientation != 0) {
			//画像を回転させて取ってくる。
			Bitmap work = Tools.bitmapRotate(tmp, orientation);
			bitmap.recycle();
			bitmap = null;
			bitmap = work;
		}
		tmp.recycle();
		tmp = null;

		return bitmap;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	//画像を回転させる
	public static Bitmap bitmapRotate(Bitmap bmp, int orientation) {
		Matrix matrix = new Matrix();
		matrix.postRotate(orientation);
		return Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public static final void setCameraParams(Context con, Camera camera, String key, String value){
		Camera.Parameters cp = camera.getParameters();
		if(key.equals(con.getString(R.string.CameraColorEffectKey))){
			cp.setColorEffect(value);
		}else if(key.equals(con.getString(R.string.CameraWhiteBalanceKey))){
			cp.setWhiteBalance(value);
		}else if(key.equals(con.getString(R.string.CameraSceneKey))){
			cp.setSceneMode(value);
		}else if(key.equals(con.getString(R.string.CameraFocusModeKey))){
			cp.setFocusMode(value);
		}
		camera.setParameters(cp);
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	//例えば、本来縦長の画像であったとしても横向きで保存されていることがある。そのような画像には回転角度の情報もデータベースに登録されているのでその情報をとってくる
	private static int getImageOrientation(Uri uri, Context con) {
		//CursorでSQLite(DB)を操作
		Cursor cursor = null;
		try {
			cursor = ((Activity) con).managedQuery(uri, null, null, null, null);

			int orientation_ColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.ORIENTATION);
			if (cursor.moveToFirst()) {
				return cursor.getInt(orientation_ColumnIndex);
			}
		} catch (Exception e) {

		} finally {

		}
		return 0;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	//Toastの表示
	public static void showToast(Context con, String message) {
		Toast toast = Toast.makeText(con, message, Toast.LENGTH_LONG);
		toast.show();
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	//ImageViewを使用したときのメモリリーク対策
	public static void releaseImageView(ImageView imageView){
		if (imageView != null) {
			BitmapDrawable bitmapDrawable = (BitmapDrawable)(imageView.getDrawable());
			if (bitmapDrawable != null) {
				bitmapDrawable.setCallback(null);
			}
			imageView.setImageBitmap(null);
		}
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	//画像をSDカードに保存する
	public static boolean SaveImage(ContentResolver contentResolver, Bitmap bitmap, String strFilePath, Context con) {
		String name = new StringBuffer().toString();
		Uri imageUri = null;
		// 保存する画像の情報をDBに登録してギャラリーなどで検索してデータが出てくるようにする。
		if (con != null) {
			// 画像が保存されている場所の情報をとって来る
			Uri mediaUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
			ContentValues contentValues = new ContentValues();
			contentValues.put(MediaStore.Images.Media.DATA, strFilePath);
			contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, name);
			contentValues.put(MediaStore.Images.Media.TITLE, name);
			contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
			// DBに新しくとってきたコンテンツの情報を挿入する(contentResolver.insert)
			imageUri = contentResolver.insert(mediaUri, contentValues);
		} else {
			imageUri = Uri.fromFile(new File(strFilePath));
		}
		try {
			// imageUriにあるファイルを開く(openOutputStream)
			OutputStream outputStream = contentResolver.openOutputStream(imageUri);
			// bitmap画像を圧縮する(圧縮後の拡張子,圧縮率,画像)
			String value = Tools.getRecordingParam(con, con.getString(R.string.SaveFormatKey));
			if(value.equals(con.getString(R.string.SaveFormatpng))){
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
			}else{
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
			}
			outputStream.close();
			if (con != null) {
				File file = new File(strFilePath);
				// 画像を保存する(UriにあるデータをスキャンしmediaDBに登録する)
				con.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	//保存するファイルのパスをとって来る
	public static String getFilePath(String file_extention) {
		String strFilePath = new String();
		String strTempName = new String();
		int i = 0;
		while (true) {
			String strExtDir = Tools.getSDCardFolderPath() + "/" + com.taku.kobayashi.pngcamera.Config.DIRECTORY_NAME_TO_SAVE;
			// 今の時間をミリ秒で返す(PHPのstrtotimeと同じ)
			long dateTaken = System.currentTimeMillis();
			if (i == 0) {
				// 年-月-日_時.分.秒 + 拡張子 または 年-月-日_時.分.秒 +_番号 + 拡張子
				strTempName = DateFormat.format("yyyy-MM-dd_kk.mm.ss", dateTaken).toString() + file_extention;
			} else {
				strTempName = DateFormat.format("yyyy-MM-dd_kk.mm.ss", dateTaken).toString() + "_" + i + file_extention;
			}
			strFilePath = strExtDir + strTempName;
			File file = new File(strFilePath);
			// 同じファイル名がないか調べる
			if (file.exists() == false){
				break;
			}
			i++;
		}
		return strFilePath;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	//ネットワークに接続されているかどうかの判別
	public static boolean CheckNetWork(Context context){
		ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		if(networkInfo != null){
			return connectivityManager.getActiveNetworkInfo().isConnected();
		}
		return false;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	//WebViewを使用したときのメモリリーク対策
	public static void releaseWebView(WebView webview){
		webview.stopLoading();
		webview.setWebChromeClient(null);
		webview.setWebViewClient(null);
		webview.destroy();
		webview = null;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public static void recordParam(Context con,String key, String value){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(con);
		SharedPreferences.Editor editor = sp.edit();
		editor.putString(key, value);
		editor.commit();
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public static String getRecordingParam(Context con, String key){
		SharedPreferences setting = PreferenceManager.getDefaultSharedPreferences(con);
		return setting.getString(key, "");
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	//Android2.3以前のSDカードはAndroid4.0以降では第二メモリを指すので、SDカードに保存できる時はそちらに保存するようにパスを指定する
	public static String getSDCardFolderPath(){
		String path = "/sdcard2/";
		File file = new File(path);
		if(file.exists() && file.isDirectory()){
			return path;
		}else{
			return Environment.getExternalStorageDirectory().toString() + "/";
		}
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------
	//SDカードがマウント中かどうか調べる
	public static boolean checkSDcardMount() {
		return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------
	//SDカードに十分な空き容量があるか調べる
	public static boolean checkSDcardAvailableSpace() {
		String strSDcardPath = getSDCardFolderPath();
		StatFs statFs = new StatFs(strSDcardPath);

		double SDcardAvailableSpace = Math.abs((double)statFs.getAvailableBlocks() * (double)statFs.getBlockSize() / 1024.0);
		if (SDcardAvailableSpace >= com.taku.kobayashi.pngcamera.Config.LIMIT_MINIMAM_SPACE) {
			return true;
		} else {
			return false;
		}
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------
	//YUV420SP(Androidのカメラで撮影された生データのbyte配列)をビットマップに変換できるようにRGBのint配列に変換
	public static final void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width, int height) {
		final int frameSize = width * height;

		for (int j = 0, yp = 0; j < height; j++) {
			int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
			for (int i = 0; i < width; i++, yp++) {
				int y = (0xff & ((int) yuv420sp[yp])) - 16;
				if (y < 0) y = 0;
				if ((i & 1) == 0) {
						v = (0xff & yuv420sp[uvp++]) - 128;
						u = (0xff & yuv420sp[uvp++]) - 128;
				}

				int y1192 = 1192 * y;
				int r = (y1192 + 1634 * v);
				int g = (y1192 - 833 * v - 400 * u);
				int b = (y1192 + 2066 * u);

				if (r < 0) r = 0; else if (r > 262143) r = 262143;
				if (g < 0) g = 0; else if (g > 262143) g = 262143;
				if (b < 0) b = 0; else if (b > 262143) b = 262143;

				rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
			}
		}
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------
}
