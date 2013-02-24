package com.taku.kobayashi.pngcamera;

import java.util.EventListener;

import com.taku.kobayashi.pngcamera.CameraParameterExpandableAdapter.ParamsSelectListener;

import android.app.Activity;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioRecord.OnRecordPositionUpdateListener;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnInfoListener;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

public class SaveImageService extends IntentService {

	private final static String TAG = "PNGCamera_SaveImageService";
	private SaveCompleteListener CompleteListener = null;
	//private boolean m_bRecording;

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public SaveImageService(String name) {
		super(name);
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	@Override
	protected void onHandleIntent(Intent intent) {
		byte[] data = intent.getByteArrayExtra(this.getString(R.string.IntentPictureByteDateKey));
		int width = intent.getIntExtra(this.getString(R.string.IntentPictureWidthKey), 0);
		int height = intent.getIntExtra(this.getString(R.string.IntentPictureHeightKey), 0);
		int orientation = intent.getIntExtra(this.getString(R.string.IntentCameraOrientationKey), 0);
		Bitmap image = decodeBitmapData(data, width, height, orientation);
		String savedImagePathstr = Tools.getFilePath("." + Tools.getRecordingParam(this,this.getString(R.string.SaveFormatKey)));
		Tools.SaveImage(this.getContentResolver(), image, savedImagePathstr, this);
		if(CompleteListener != null){
			CompleteListener.complete(savedImagePathstr);
		}
	}

	private Bitmap decodeBitmapData(byte[] data, int width, int height, int orientation) {
		int[] rgb = new int[(width * height)];
		Tools.decodeYUV420SP(rgb, data, width, height);
		Bitmap image = Bitmap.createBitmap(rgb, width, height, Bitmap.Config.ARGB_8888);
		rgb = null;

		if (orientation != 0) {
			//画像を回転させて取ってくる。
			Bitmap work = Tools.bitmapRotate(image, orientation);
			image.recycle();
			image = null;
			image = work;
		}
		return image;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	/**
	 * リスナーを追加する
	 */
	public void setOnSaveCompleteLister(SaveCompleteListener listener){
		this.CompleteListener = listener;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * リスナーを削除する
	 */
	public void removeListener(){
		this.CompleteListener = null;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------


	public interface SaveCompleteListener extends EventListener {

		public void complete(String filePath);

	}

}