//  Created by 拓 小林
//  Copyright (c) 2012年 __CompanyName__. All rights reserved.

package com.taku.kobayashi.pngcamera;

import java.io.File;
import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;

import com.taku.kobayashi.pngcamera.CameraParameterExpandableAdapter.ParamsSelectListener;
import com.taku.kobayashi.pngcamera.SaveImageService.SaveCompleteListener;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback{

	public Bitmap PreviewImage;
	private static final String TAG = "PNGCamera_CameraPreView";
	private Context m_Context;
	private SurfaceHolder m_Holder;
	private int m_CameraDisplayOrientation = 0;
	private Camera m_Camera = null;
	private Size m_PreViewSize;
	private List<Size> m_PreviewList;
	private ImageView m_Thumbnail;
	private Size m_ThumbnailSize;
	private Thread m_Thread = null;
	private Handler m_Handler;

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public CameraPreview(Context context, AttributeSet attrs) {
		super(context, attrs);
		m_Context = context;
		m_Holder = getHolder();
		m_Holder.addCallback(this);
		if(Build.VERSION.SDK_INT < 11){
			m_Holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
		m_Handler = new Handler();
	}

	public void setThumbnailImageView(ImageView iv){
		m_Thumbnail = iv;
		//以下のフォルダの中から画像ファイルを読み込む
		String path = Tools.getSDCardFolderPath() + "/" + com.taku.kobayashi.pngcamera.Config.DIRECTORY_NAME_TO_SAVE;
		File dir = new File(path);
		String[] fileNames = dir.list();
		//多分昇順でほぞんされているので後ろから取ってくるといい
		for(int i = fileNames.length - 1;i >= 0; i--){
			if(fileNames[i].contains(".jpg") || fileNames[i].contains(".png")){
				String imagePath = path + "/" + fileNames[i];
				m_ThumbnailSize = m_Camera.getParameters().getJpegThumbnailSize();
				Log.d(TAG, "width:" + m_ThumbnailSize.width + " height:" + m_ThumbnailSize.height);
				File file = new File(imagePath);
				Bitmap image = Tools.getSelectSizeBitmap(m_Context, Uri.fromFile(file), m_ThumbnailSize.width, m_ThumbnailSize.height);
				//画像ファイルとしては正しいが、画像ではなかったり壊れているファイルなら消す
				if(image != null){
					m_Thumbnail.setImageBitmap(image);
					break;
				}else{
					file.delete();
				}
			}
		}
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public void surfaceCreated(SurfaceHolder holder) {
		try {
			m_Camera.setPreviewDisplay(holder);
		} catch (IOException exception) {
			releaseCamera();
		}
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public void surfaceDestroyed(SurfaceHolder holder) {
		this.releaseCamera();
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public void setCamera(int nCameraID, CameraParameterExpandableAdapter cpa){
		Tools.recordParam(m_Context, m_Context.getString(R.string.IntentCameraIDKey), String.valueOf(nCameraID));
		if(Build.VERSION.SDK_INT < 9){
			m_Camera = Camera.open();
		}else{
			m_Camera = Camera.open(nCameraID);
		}
		try {
			m_Camera.setPreviewDisplay(m_Holder);
		} catch (Exception e) {
			e.printStackTrace();
		}

		Camera.Parameters cp = m_Camera.getParameters();
		cpa.setParameters(m_Camera);
		//Log.d(TAG,"RateList:"+cp.getZoomRatios());
		//Log.d(TAG,"FlashMode:"+cp.getSupportedFlashModes());
		//Log.d(TAG,"Format:"+cp.getSupportedPictureFormats());
		//Log.d(TAG,"Effects:"+cp.getSupportedColorEffects());
		//Log.d(TAG,"AntiBanding:"+cp.getSupportedAntibanding());
		//Log.d(TAG,"WhiteBalance:"+cp.getSupportedWhiteBalance());
		//Log.d(TAG,"Scene:"+cp.getSupportedSceneModes());
		//Log.d(TAG,"Focus:"+cp.getSupportedFocusModes());

		m_PreviewList = cp.getSupportedPreviewSizes();
		/*
		List<Size> Picture = cp.getSupportedPictureSizes();
		for(int i = 0;i < Picture.size();i++){
			Log.d(TAG,"Picture width:"+Picture.get(i).width+" height:"+Picture.get(i).height);
		}
		List<Size> PreView = cp.getSupportedPreviewSizes();
		for(int i = 0;i < PreView.size();i++){
			Log.d(TAG,"PreView width:"+PreView.get(i).width+" height:"+PreView.get(i).height);
		}
		*/

		m_PreViewSize = cp.getPreviewSize();
		m_CameraDisplayOrientation = getCameraDisplayOrientation((Activity) m_Context, nCameraID);
		m_Camera.setDisplayOrientation(m_CameraDisplayOrientation);
		m_Camera.setParameters(cp);
		m_Camera.startPreview();
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------


	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	//YUV420 to BMP
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

	private void savePicture(Bitmap picture){
		// 画像のおいてある場所と保存する画像のファイル名の情報をとって来る
		String savedImagePathstr = Tools.getFilePath("." + Tools.getRecordingParam(m_Context,m_Context.getString(R.string.SaveFormatKey)));
		Tools.SaveImage(m_Context.getContentResolver(), picture, savedImagePathstr, (Activity) m_Context);
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------
	public void autoFocus(AutoFocusCallback cameraAutoFocusCallback){
		m_Camera.autoFocus(cameraAutoFocusCallback);
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public void takePreviewPicture() {
		Camera.Parameters cp = m_Camera.getParameters();
		//シャッター音
		String sound = Tools.getRecordingParam(m_Context, m_Context.getString(R.string.SutterSoundKey));
		if(Boolean.parseBoolean(sound)){
			MediaPlayer mp= MediaPlayer.create(m_Context, R.raw.camera_shutter);
			try {
				mp.prepare();
				mp.start();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		//画像の大きさ
		String size = Tools.getRecordingParam(m_Context, m_Context.getString(R.string.CameraPreviewSizeKey) + Tools.getRecordingParam(m_Context, m_Context.getString(R.string.IntentCameraIDKey)));
		String[] imageSize = size.split(m_Context.getString(R.string.ConnectSizeAndSize));
		cp.setPreviewSize(Integer.parseInt(imageSize[0]),Integer.parseInt(imageSize[1]));
		m_Camera.setParameters(cp);
		m_Camera.setOneShotPreviewCallback(null);
		m_Camera.stopPreview();
		m_Camera.setOneShotPreviewCallback(new PreviewCallback() {

			private byte[] m_Data;
			private Size m_Size;

			@Override
			public void onPreviewFrame(byte[] data, Camera camera) {

				m_Size = camera.getParameters().getPreviewSize();
				m_Data = data;
				//画像の保存が完了するまで次のスレッドは起動させない
				joinThread();
				m_Thread = new Thread(new Runnable() {
					private Bitmap m_ThumbnailImage;

					@Override
					public void run() {
						int[] rgb = new int[(m_Size.width * m_Size.height)];
						Tools.decodeYUV420SP(rgb, m_Data, m_Size.width, m_Size.height);
						Bitmap image = Bitmap.createBitmap(rgb, m_Size.width, m_Size.height, Bitmap.Config.ARGB_8888);
						rgb = null;

						if (m_CameraDisplayOrientation != 0) {
							//画像を回転させて取ってくる。
							Bitmap work = Tools.bitmapRotate(image, m_CameraDisplayOrientation);
							image.recycle();
							image = null;
							image = work;
						}

						String savedImagePathstr = Tools.getFilePath("." + Tools.getRecordingParam(m_Context, m_Context.getString(R.string.SaveFormatKey)));
						Tools.SaveImage(m_Context.getContentResolver(), image, savedImagePathstr, m_Context);
						m_ThumbnailImage = Tools.getSelectSizeBitmap(m_Context, Uri.fromFile(new File(savedImagePathstr)), m_ThumbnailSize.width, m_ThumbnailSize.height);
						m_Handler.post(new Runnable() {

							@Override
							public void run() {
								Tools.releaseImageView(m_Thumbnail);
								m_Thumbnail.setImageBitmap(m_ThumbnailImage);
							}
						});
					}
				});
				m_Thread.start();
				//decodeBitmapData(data, size.width, size.height);
				//savePicture(PreviewImage);
			}
		});
		Camera.Parameters acp = m_Camera.getParameters();
		cp.setPreviewSize(m_PreViewSize.width, m_PreViewSize.height);
		m_Camera.setParameters(acp);
		m_Camera.startPreview();
	}

	public void takePicture(ShutterCallback sc,PictureCallback raw,PictureCallback jpeg) {
		//TODO サイズ調整
		m_Camera.takePicture(sc, raw, jpeg);
		m_Camera.stopPreview();
		m_Camera.startPreview();
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public int getCameraDisplayOrientation(Activity act,int nCameraID){
		if(Build.VERSION.SDK_INT >= 9){
			Camera.CameraInfo info = new Camera.CameraInfo();
			Camera.getCameraInfo(nCameraID, info);
			int rotation = act.getWindowManager().getDefaultDisplay().getRotation();
			int degrees = 0;
			switch (rotation) {
				//portate:縦向き
				case Surface.ROTATION_0: degrees = 0; break;
				//landscape:横向き
				case Surface.ROTATION_90: degrees = 90; break;
				case Surface.ROTATION_180: degrees = 180; break;
				case Surface.ROTATION_270: degrees = 270; break;
			}
			int result;
			//Camera.CameraInfo.CAMERA_FACING_FRONT:アウトカメラ
			//Camera.CameraInfo.CAMERA_FACING_BACK:インカメラ

			if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
				result = (info.orientation + degrees) % 360;
				result = (360 - result) % 360;  // compensate the mirror
			} else {  // back-facing
				result = (info.orientation - degrees + 360) % 360;
			}
			return result;
		}
		return 90;
	}

	/*
	public int getCameraOrientation(int nCameraID){
		if(Build.VERSION.SDK_INT >= 9){
			Camera.CameraInfo info = new Camera.CameraInfo();
			Camera.getCameraInfo(nCameraID, info);
			return info.orientation;
		}else{
			if(nCameraID == 0){
				return 90;
			}else if(nCameraID == 1){
				return 270;
			}
		}
		return 90;
	}
	*/

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public boolean decodeBitmapData(byte[] data, int width, int height) {
		if(PreviewImage != null){
			PreviewImage.recycle();
			PreviewImage = null;
		}
		int[] rgb = new int[(width * height)];
		decodeYUV420SP(rgb, data, width, height);
		PreviewImage = Bitmap.createBitmap(rgb, width, height, Bitmap.Config.ARGB_8888);
		rgb = null;

		if (m_CameraDisplayOrientation != 0) {
			//画像を回転させて取ってくる。
			Bitmap work = Tools.bitmapRotate(PreviewImage, m_CameraDisplayOrientation);
			PreviewImage.recycle();
			PreviewImage = null;
			PreviewImage = work;
		}
		return true;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public void releaseCamera() {
		if (m_Camera != null){
			m_Camera.cancelAutoFocus();
			m_Camera.stopPreview();
			m_Camera.setPreviewCallback(null);
			m_Camera.release();
			m_Camera = null;
		};
		//アプリ起動時に画像を保存するスレッドが動いていて、終了するとファイルが壊れるので、保存が完了するまで待つ
		joinThread();
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	private void joinThread(){
		if(m_Thread != null && m_Thread.isAlive()){
			try {
				m_Thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
