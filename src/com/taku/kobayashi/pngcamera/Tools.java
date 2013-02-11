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
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.hardware.Camera;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Log;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.Toast;

public class Tools {

	//define
	private static final String TAG = "PNGCamera_Tools";

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------
/*
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

	//SDカード等から画像をとってくる場合
	public static Bitmap getBitmap(Context con, Uri uri) {

		int orientation = Tools.getImageOrientation(uri, con);

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = getSampleSize(con,uri,orientation);
		//options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		options.inScaled = true;

		//Log.d(TAG,"SampleSize:"+getSampleSize(con,uri,orientation) + "orientation:"+orientation);
		Bitmap tmp = null;
		try {
			InputStream is = con.getContentResolver().openInputStream(uri);
			tmp = BitmapFactory.decodeStream(is, null, options);
			is.close();
		} catch (FileNotFoundException e) {
			Log.d(TAG, "openInputStream failed  ***ERROR***");
		} catch (IOException e) {
			Log.d(TAG, "closeInputStream failed  ***ERROR***");
		}

		Bitmap bitmap = tmp.copy(Config.ARGB_8888, true);

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
	//サイトから画像をとってくる場合
	public static Bitmap getBitmap(String url) {
		Bitmap bm = null;
		try {
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
			});
			bis.close();
			is.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bm;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	//byte[]データを画像データに変換する。
	public static Bitmap getBitmap(Context context,byte[] data,int orientation){
		BitmapFactory.Options bitmapFactoryOptions = new BitmapFactory.Options();
		bitmapFactoryOptions.inSampleSize = getSampleSize(context,data,orientation);
		bitmapFactoryOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
		Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, bitmapFactoryOptions);

		if (orientation != 0) {
			//画像を回転させて取ってくる。
			Bitmap work = Tools.bitmapRotate(bitmap, orientation);
			bitmap.recycle();
			bitmap = null;
			bitmap = work;
		}
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

	//文字列をUri化
	public static Uri getConversionUri(String str) {
		if (str.contains("content://") == false && str.contains("file://") == false) {
			str = "file://" + str;
		}
		return Uri.parse(str);
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	//SDカードからの画像を使用する場合の計算
	private static int getSampleSize(Context con,Uri uri , int orientation) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		int nImageSize = 1;

		// 画像のサイズを分ける
		try {
			InputStream is = con.getContentResolver().openInputStream(uri);
			BitmapFactory.decodeStream(is, null, options);
			is.close();
			//nImageSize = calculateSampleSize(con,options.outWidth,options.outHeight,orientation);
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}

		return nImageSize;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	//byteデータからの画像を使用する場合の計算
	private static int getSampleSize(Context con,byte[] data , int orientation) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(data, 0, data.length, options);
		int nImageSize = 1;
		//nImageSize = calculateSampleSize(con,options.outWidth,options.outHeight,orientation);
		return nImageSize;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

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

/*
	//縮小させる画像の縮小値(自然数)の計算
	//例：return = 2の場合、画像は縦:1/2、横：1/2のサイズになる
	private static int calculateSampleSize(Context con,int ImgWidth,int ImgHeight,int orientation){
		int width = 0;
		int height = 0;
		//Log.d(TAG,"orientation:" + orientation);

		//90度回転させるとき
		if(Math.abs(orientation % 180) == 90){
			width = ImgHeight;
			height = ImgWidth;
		}else{
			width = ImgWidth;
			height = ImgHeight;
		}
		int num = 1;
		// 画面サイズにできるだけ近づけた大きさに縮小させる
		if (width > getDisplayResize(con).width || height > getDisplayResize(con).height) {
			//切り上げ(画面サイズより大きい値)
			num = (int) Math.max(Math.max(Math.ceil(width / getDisplayResize(con).width), Math.ceil((height / getDisplayResize(con).height))),1);
			//切り捨て(下面サイズより小さい値)
			//num = (int) Math.max(Math.max(Math.floor(width / getDisplayResize(con).width), Math.floor((height / getDisplayResize(con).height))),1);
		}

		return num;
	}
*/
	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	//画像の縮小値を直接指定
	public static Bitmap getSmallBitmap(Context con, Integer rsc,int nInSampleSize) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = nInSampleSize;
		return BitmapFactory.decodeResource(con.getResources(), rsc, options);
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	//入力されたテキストデータ(InputStream,これはbyteデータ)を文字列(String)に変換
	private static String Is2String(InputStream in) throws IOException {
		StringBuffer out = new StringBuffer();
		byte[] b = new byte[4096];
		//保持しているStringデータ全てをStringBufferに入れる
		for (int n; (n = in.read(b)) != -1;) {
			out.append(new String(b, 0, n));
		}
		return out.toString();
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
			//要注意 cursorをcloseする必要はない Android4.0以降では例外処理が発生する
			/*
			if (cursor != null) {
				cursor.close();
			}
			*/
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

	//メモリの使用量についての情報を取得
	public static void memoryInfo(Context con) {
		ActivityManager activityManager = (ActivityManager) con.getSystemService(Activity.ACTIVITY_SERVICE);
		ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
		activityManager.getMemoryInfo(memoryInfo);

		/*
		//使えるメモリ容量全体
		Log.d(TAG, "memoryInfo.availMem[MB] = " + (int)(memoryInfo.availMem/1024/1024));
		//backgroundで実行しているものを次々とkillしていくしきい値
		Log.d(TAG, "memoryInfo.threshold[MB] = " + (int)(memoryInfo.threshold/1024/1024));
		//
		Log.d(TAG, "memoryInfo.lowMemory = " + memoryInfo.lowMemory);
		*/
		/*
		// 確保しているヒープサイズ
		Log.d(TAG, "NativeHeapSize = " + android.os.Debug.getNativeHeapSize()/1024);
		// 空きヒープサイズ
		Log.d(TAG, "NativeHeapFreeSize = " + android.os.Debug.getNativeHeapFreeSize()/1024);
		// 使用中ピープサイズ
		Log.d(TAG, "NativeHeapAllocatedSize = " + android.os.Debug.getNativeHeapAllocatedSize()/1024);
		*/

		/*
		// 確保しているヒープサイズ
		Log.d(TAG, mes + ":NativeHeapSize = " + android.os.Debug.getNativeHeapSize());
		// 空きヒープサイズ
		Log.d(TAG, mes + ":NativeHeapFreeSize = " + android.os.Debug.getNativeHeapFreeSize());
		// 使用中ピープサイズ
		Log.d(TAG, mes + ":NativeHeapAllocatedSize = " + android.os.Debug.getNativeHeapAllocatedSize());
		*/

		// アプリのメモリ情報を取得
		Runtime runtime = Runtime.getRuntime();
		// トータルメモリ
		Log.v(TAG, "totalMemory[KB] = " + (int)(runtime.totalMemory()/1024));
		// 空きメモリ
		Log.v(TAG, "freeMemory[KB] = " + (int)(runtime.freeMemory()/1024));
		//現在使用しているメモリ
		Log.v(TAG, "usedMemory[KB] = " + (int)( (runtime.totalMemory() - runtime.freeMemory())/1024) );
		// Dalvikで使用できる最大メモリ
		Log.v(TAG, "maxMemory[KB] = " + (int)(runtime.maxMemory()/1024));

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

	//画像データをbyte[]データに変換する。(ただしJPEG圧縮される)
	public static byte[] getImagebyte(Bitmap bitmap){
		byte[] bMapArray = null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
			bMapArray = baos.toByteArray();
			baos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bMapArray;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	//画像をSDカードに保存する
	public static boolean SaveImage(ContentResolver contentResolver, Bitmap bitmap, String strFilePath, String strFileName, Activity act) {
		Uri imageUri = null;
		// 保存する画像の情報をDBに登録してギャラリーなどで検索してデータが出てくるようにする。
		if (act != null) {
			// 画像が保存されている場所の情報をとって来る
			Uri mediaUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
			ContentValues contentValues = new ContentValues();
			contentValues.put(MediaStore.Images.Media.DATA, strFilePath);
			contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, strFileName);
			contentValues.put(MediaStore.Images.Media.TITLE, strFileName);
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
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
			outputStream.close();
			if (act != null) {
				File file = new File(strFilePath);
				// 画像を保存する(UriにあるデータをスキャンしmediaDBに登録する)
				act.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
			}
		} catch (Exception e) {
			Log.d(TAG, "Savefalse");
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
			String strExtDir = Environment.getExternalStorageDirectory().toString()+"/"+com.taku.kobayashi.pngcamera.Config.DIRECTORY_NAME_TO_SAVE;
			File files = new File(strExtDir);
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

	//SDカード内からFileが保存されているパスからテキストファイルを読み込む場合
	public static String loadTextFile(String strFilePath){
		File file = new File(strFilePath);
		if(file.exists()){
			String str;
			try {
				FileInputStream fis = new FileInputStream(file);
				str = Tools.Is2String(fis);
			} catch (IOException e) {
				str = "";
			}
			return str;
		}else{
			return null;
		}
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	//リソースからテキストファイルを読み込む場合
	public static String loadTextFile(Context con,Integer resId){
		InputStream is = con.getResources().openRawResource(resId);
		String str;
		try {
			str = Tools.Is2String(is);
		} catch (IOException e) {
			str = "";
		}
		return str;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	/*
	public static String LoadFileFromServer(String urlStr){
		//TODO StringをUTF8に変化する処理の追加
		URL url = null;
		try {
			url = new URL(jp.co.aitia.hairstylenavi.Config.UpdateInfoURL + urlStr);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}
		String strlist = null;
		InputStream is;
		//URLからデータを読み込む
		try {
			is = url.openStream();
			strlist = Tools.Is2String(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return strlist;
	}
	*/

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	/*
	//フォルダ内にあるファイルの内、StartFilterStr:ファイル名前方一致、EndFilterStr:ファイル名後方一致、でファイルを検索し、該当ファイルをリストで返す
	public static ArrayList<File> FileStringFilter(String FileDirPath,String StartFilterStr,String EndFilterStr){
		File file = new File(FileDirPath);
		ArrayList<File> FileList = new ArrayList<File>();
		//String 引数がnullだった場合は制限をかけない。
		if(file.exists()){
			//listFiles()はSDカード内の該当ディレクトリを検索しているため処理に時間がかかる
			File[] files = file.listFiles();
			//処理回数削減(処理速度向上)のため
			int nLength = files.length;
			for(int i=0;i < nLength;i++){
				boolean bFilter = true;
				if(StartFilterStr != null){
					if(!files[i].getName().startsWith(StartFilterStr)){
						bFilter = false;
					}
				}
				if(EndFilterStr != null){
					if(!files[i].getName().endsWith(EndFilterStr)){
						bFilter = false;
					}
				}
				if(bFilter){
					FileList.add(files[i]);
				}
			}
		}
		return FileList;
	}
	*/

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	//サーバーからデータをダウンロードし保存する処理
	//string_url:url上にあるファイル filename:保存先のファイルパス＋保存するファイル名
	public static long DownloadFromUrl(String string_url, String filename) {
		try {
			URL url = new URL(string_url);
			File file = new File(filename);
			file.createNewFile();

			URLConnection ucon = url.openConnection();
			InputStream is = ucon.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is);

			ByteArrayBuffer baf = new ByteArrayBuffer(50);
			int current = 0;
			long total = 0;
			while ((current = bis.read()) != -1) {
				baf.append((byte) current);
				total += current;
			}

			FileOutputStream fos = new FileOutputStream(file);
			fos.write(baf.toByteArray());
			fos.close();

			return total;
		} catch (IOException e) {
			return -1;
		}
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	/*
	//CSVデータを最新のバージョンの物に上書く
	public static void OverwriteTextFile(String strFilePath,String strData){
		File file = new File(strFilePath);
		//すでにファイルが存在した場合そのファイルを消去する。
		if(file.exists()){
			file.delete();
		}
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(file,true);
			OutputStreamWriter osw = new OutputStreamWriter(fos,"UTF-8");
			//バイト符号化されているものを文字列に変換し、バッファに書き込む
			osw.write(strData);
			//保存されているデータを実際に書き込む
			osw.flush();
			osw.close();
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	*/

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

	/*
	//二つのリストをAND型で結合して返す。
	public static ArrayList ListUnionTypeAND(ArrayList ListA , ArrayList ListB){
		ArrayList result = new ArrayList();
		ArrayList ListFew = new ArrayList();
		ArrayList ListMany = new ArrayList();
		//片方が空なら空のまま返す
		if(ListA.isEmpty()){
			return ListA;
		}
		if(ListB.isEmpty()){
			return ListB;
		}
		//処理回数を少なくするためにsizeの小さい方の回数分処理を行うようにする
		if(ListA.size() < ListB.size()){
			ListFew = ListA;
			ListMany = ListB;
		}else{
			ListFew = ListB;
			ListMany = ListA;
		}
		//処理回数削減(処理速度向上)のため
		int nLength = ListFew.size();
		for(int i = 0;i < nLength;i++){
			if(ListMany.contains(ListFew.get(i)) == true){
				result.add(ListFew.get(i));
			}
		}
		return result;
	}
	*/

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	/*
	//二つのリストをOR型で結合して返す。
	public static ArrayList ListUnionTypeOR(ArrayList ListA , ArrayList ListB){
		ArrayList result = new ArrayList();
		ArrayList ListFew = new ArrayList();
		ArrayList ListMany = new ArrayList();
		//片方が空なら結合しない
		if(ListA.isEmpty()){
			return ListB;
		}
		if(ListB.isEmpty()){
			return ListA;
		}
		//処理回数を少なくするためにsizeの小さい方の回数分処理を行うようにする
		if(ListA.size() < ListB.size()){
			ListFew = ListA;
			ListMany = ListB;
		}else{
			ListFew = ListB;
			ListMany = ListA;
		}
		result = ListMany;
		//処理回数削減(処理速度向上)のため
		int nLength = ListFew.size();
		for(int i = 0;i < nLength;i++){
			//ダブらないようにする
			if(result.contains(ListFew.get(i)) == false){
				result.add(ListFew.get(i));
			}
		}
		return result;
	}
	*/

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	/*
	//命名規約に基づき、保持しているCSVファイルからVolume番号のリストを取得する
	public static ArrayList<Integer> GetAvailableVolumeNumberFromCSV(String strTarget){
		ArrayList<File> havingVolumeFiles = FileStringFilter(SDCardCtrl.CSVDir, strTarget, jp.co.aitia.hairstylenavi.Config.DATA_FILE_EXTENSION);
		ArrayList<Integer> VolumeNumberList = new ArrayList<Integer>();
		//処理回数削減(処理速度向上)のため
		int nLength = havingVolumeFiles.size();
		for(int i = 0;i < nLength;i++){
			String fileName = havingVolumeFiles.get(i).getName();
			String str = fileName.replaceAll(strTarget, "");
			String strVolume = str.replaceAll(jp.co.aitia.hairstylenavi.Config.DATA_FILE_EXTENSION, "");

			try {
				int num = Integer.parseInt(strVolume);
				VolumeNumberList.add(num);
			} catch (NumberFormatException e) {

			}
		}
		return VolumeNumberList;
	}
	*/

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	/*
	//命名規約に基づき、保持している画像ファイルからUpdateが必要なVolume番号のリストを取得する
	public static ArrayList<Integer> GetUpdateVolumeNumberFromImage(String strTarget,ArrayList<String> OnServerFileNameList){
		//Log.d(TAG, "OnServerFileNameList:"+OnServerFileNameList);
		ArrayList<Integer> nVolumeList = new ArrayList<Integer>();
		String[] ImgFileNames = null;
		if(strTarget.equals(jp.co.aitia.hairstylenavi.Config.HAIRSTYLE_FILE_NAME)){
			ImgFileNames = new File(SDCardCtrl.HairStyleImgsDir).list();
		}else if(strTarget.equals(jp.co.aitia.hairstylenavi.Config.STAMP_FILE_NAME)){
			ImgFileNames = new File(SDCardCtrl.StampImgsDir).list();
		}else if(strTarget.equals(jp.co.aitia.hairstylenavi.Config.FRAME_FILE_NAME)){
			ImgFileNames = new File(SDCardCtrl.FrameImgsDir).list();
		}
		//File[] files = ImgFiles.listFiles();
		if(ImgFileNames != null){
			//処理回数削減(処理速度向上)のため
			int nImgFileNamesLength = ImgFileNames.length;
			for(int i = 0;i < nImgFileNamesLength;i++){
				OnServerFileNameList.remove(ImgFileNames[i]);
			}
			//処理回数削減(処理速度向上)のため
			int nOnServerFileNameListLength = OnServerFileNameList.size();
			for(int i = 0;i < nOnServerFileNameListLength;i++){
				String strName = OnServerFileNameList.get(i);
				String strReplace0 = strName.replaceAll(jp.co.aitia.hairstylenavi.Config.USE_IMAGE_FILE_EXTENTION, "");
				String strReplace1 = strReplace0.replaceAll(jp.co.aitia.hairstylenavi.Config.HAIRSTYLE_THUMBNAIL_INITIAL, "");
				String strReplace2 = strReplace1.replaceAll(jp.co.aitia.hairstylenavi.Config.FRAMESTAMP_SELECT_IMG_NAME, "");
				int num = -1;
				//Log.d(TAG, "strReplace2:"+strReplace2);
				try {
					num = Integer.parseInt(strReplace2);
				} catch (NumberFormatException e) {

				}
				if(num != -1){
					int nVolume = 0;
					//stamp,frame,selectviewのみVolume番号が違うので例外として扱う
					if(strName.startsWith(jp.co.aitia.hairstylenavi.Config.FRAMESTAMP_SELECT_IMG_NAME) == true){
						nVolume = num;
					}else{
						nVolume = (int) Math.floor(num / jp.co.aitia.hairstylenavi.Config.IMAGEFILE_NAME_RULE_IN_VOLUME_NUMBER);
					}
					if(nVolumeList.contains(nVolume) == false){
						nVolumeList.add(nVolume);
					}
				}
			}
		}
		//Log.d(TAG, "nVolumeList:"+nVolumeList);
		return nVolumeList;
	}
	*/


	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	/*
	public static String LanguageURLArgument(){
		String local = Locale.getDefault().toString();
		if(local.equals("zh_CN")){
			//Chinese
			return "ch";
		}else if(local.equals("ja_JP")){
			//Japanese
			return "ja";
		}else{
			//English
			return "en";
		}
	}
	*/

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

	/*
	public static Bitmap getSentImageIntent(Activity act){
		int width = act.getIntent().getIntExtra(act.getResources().getString(R.string.IntentPictureWidthKey), 0);
		int height = act.getIntent().getIntExtra(act.getResources().getString(R.string.IntentPictureHeightKey), 0);
		int[] pixels = act.getIntent().getIntArrayExtra(act.getResources().getString(R.string.IntentPictureDataKey));
		Bitmap image = Bitmap.createBitmap(pixels, 0, width, width, height, Config.ARGB_8888);
		pixels = null;
		System.gc();
		return image;
	}
	*/

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	/*
	public static void SendImageIntent(Activity act,Intent intent,Bitmap Image){
		int width = Image.getWidth();
		int height = Image.getHeight();
		int[] pixels = new int[width * height];
		Log.d(TAG,"w:"+width+" h:"+height);
		Image.getPixels(pixels, 0, width, 0, 0, width, height);

		intent.putExtra(act.getResources().getString(R.string.IntentPictureDataKey), pixels);
		intent.putExtra(act.getResources().getString(R.string.IntentPictureWidthKey), width);
		intent.putExtra(act.getResources().getString(R.string.IntentPictureHeightKey), height);
		act.startActivity(intent);
	}
	*/

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------
/*
	//画像を一時的にローカルフォルダに保存する
	public static boolean SaveLocalTemporaryImageFile(Context con,String FileName,Bitmap bitmap){
		boolean Success = false;
		//String[] strFileList = con.fileList();
		//FileName = FileName + jp.co.tpnconpany.lisa.no.kagami.Config.SAVE_TEMPORARY_IMAGE_FILEEXTENTION;
		try {
			//FileOutputStream out = con.openFileOutput(jp.co.tpnconpany.lisa.no.kagami.Config.SAVE_TEMPORARY_IMAGE_FILENAME,Context.MODE_PRIVATE);
			FileOutputStream out = con.openFileOutput(FileName,Context.MODE_PRIVATE);
			//画質向上のため可逆圧縮であるPNGに保存する
			Success = bitmap.compress(Bitmap.CompressFormat.PNG,jp.co.tpnconpany.lisa.no.kagami.Config.COMPRESS_QUALITY ,out);
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.d(TAG, FileName+": "+Success);
		return Success;
	}
*/
	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	//一時的にローカルフォルダに保存した画像を取りだす
	public static Bitmap getLocalTemporaryImageFile(Context con,String FileName){
		//FileName = FileName + jp.co.tpnconpany.lisa.no.kagami.Config.SAVE_TEMPORARY_IMAGE_FILEEXTENTION;
		Bitmap image = null;
		try {
			InputStream in = con.openFileInput(FileName);
			BitmapFactory.Options opt = new BitmapFactory.Options();
			opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
			image = BitmapFactory.decodeStream(in, null, opt);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			Log.d(TAG,"NotFound");
		}
		Log.d(TAG, FileName+": "+image);
		return image;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	//一時的にローカルフォルダに保存した画像を全て削除する
	public static boolean DeleteLocalTemporaryImageFile(Context con){
		//FileName = FileName + jp.co.tpnconpany.lisa.no.kagami.Config.SAVE_TEMPORARY_IMAGE_FILEEXTENTION;
		String[] strFileList = con.fileList();
		int num = strFileList.length;
		for(int i = 0;i < num;i++){
			boolean success = con.deleteFile(strFileList[i]);
			Log.d(TAG, strFileList[i]+" delette:"+success);
		}
		return true;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	//AssetsフォルダにあるZipで圧縮されたファイルの解凍
	public static void OpenZIPAssetsFile(Context con,String ZIPName){
		try {
			AssetManager am = con.getResources().getAssets();
			InputStream is  = am.open(ZIPName, AssetManager.ACCESS_STREAMING);
			ZipInputStream zis = new ZipInputStream(is);
			ZipEntry ze = zis.getNextEntry();
			while(ze != null){
				if (ze != null) {
					String path = con.getFilesDir().toString() + "/" + ze.getName();
					FileOutputStream fos = new FileOutputStream(path, false);
					byte[] buf = new byte[1024];
					int size = 0;
					while ((size = zis.read(buf, 0, buf.length)) > -1) {
						fos.write(buf, 0, size);
					}
					fos.close();
					zis.closeEntry();
					ze = zis.getNextEntry();
				}
			}
			zis.close();
		} catch (Exception e) {

		}
	}

	public static String getSDCardFolderPath(){
		String path = "/sdcard2/";
		File file = new File(path);
		file.getTotalSpace();
		if(file.exists() && file.isDirectory()){
			return path;
		}else{
			return Environment.getExternalStorageState().toString() + "/";
		}
	}

}
