//  Created by 拓 小林
//  Copyright (c) 2012年 __CompanyName__. All rights reserved.

package com.taku.kobayashi.pngcamera;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback{

	public Bitmap PreviewImage;
	private static final String TAG = "AnotherWorld_CameraPreView";
	private Context m_Context;
	private SurfaceHolder m_Holder;
	private int m_CameraDisplayOrientation = 0;
	private Camera m_Camera = null;
	private Size m_ImageSize;
	private Size m_PreViewSize;
	private List<Size> m_PreviewList;
	private List<Size> m_ImageList;

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public CameraPreview(Context context, AttributeSet attrs) {
		super(context, attrs);
		m_Context = context;
		m_Holder = getHolder();
		m_Holder.addCallback(this);
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
		Log.d(TAG,"surfacechange");
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public void setCamera(int nCameraID){
		if(Build.VERSION.SDK_INT < 9){
			m_Camera = Camera.open();
		}else{
			m_Camera = Camera.open(nCameraID);
		}
		try {
			m_Camera.setPreviewDisplay(m_Holder);
		} catch (Exception e) {}

		Camera.Parameters cp = m_Camera.getParameters();
		//Log.d(TAG,"RateList:"+cp.getZoomRatios());
		//Log.d(TAG,"FlashMode:"+cp.getSupportedFlashModes());
		Log.d(TAG,"Format:"+cp.getSupportedPictureFormats());
		//Log.d(TAG,"Effects:"+cp.getSupportedColorEffects());
		//Log.d(TAG,"AntiBanding:"+cp.getSupportedAntibanding());
		//Log.d(TAG,"WhiteBalance:"+cp.getSupportedWhiteBalance());
		//Log.d(TAG,"Scene:"+cp.getSupportedSceneModes());
		//Log.d(TAG,"Focus:"+cp.getSupportedFocusModes());

		m_PreviewList = cp.getSupportedPreviewSizes();
		m_ImageList = cp.getSupportedPictureSizes();
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

		//RgbImageAndroid.toRgbImage(PreviewImage);
		m_PreViewSize = cp.getPreviewSize();
		m_ImageSize = cp.getPictureSize();
		Log.d(TAG,"Format:"+cp.getPreviewFormat());
		cp.setPictureSize(m_PreviewList.get(0).width, m_PreviewList.get(0).height);
		m_CameraDisplayOrientation = getCameraDisplayOrientation((Activity) m_Context, nCameraID);
		m_Camera.setDisplayOrientation(m_CameraDisplayOrientation);
		m_Camera.setParameters(cp);
		m_Camera.startPreview();
	}

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

	private PreviewCallback m_PreViewCallback = new PreviewCallback(){
		@Override
		public void onPreviewFrame(byte[] data, Camera camera) {
			Log.d(TAG,"byte length:"+data.length);
			//TODO 変更
			decodeBitmapData(data,m_PreviewList.get(0).width,m_PreviewList.get(0).height);
			savePicture(PreviewImage);
		}
	};

	private void savePicture(Bitmap picture){
		StringBuffer name = new StringBuffer();
		// 画像のおいてある場所と保存する画像のファイル名の情報をとって来る
		String savedImagePathstr = Tools.getFilePath(Config.SAVE_IMG_EXTENSION_PNG);
		Tools.SaveImage(m_Context.getContentResolver(), picture, savedImagePathstr, name.toString(), (Activity) m_Context);
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------
	public void autoFocus(AutoFocusCallback cameraAutoFocusCallback){
		m_Camera.autoFocus(cameraAutoFocusCallback);
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public void takuPreviewPicture() {

		//TODO サイズ調整
		Camera.Parameters cp = m_Camera.getParameters();
		cp.setPreviewSize(m_PreviewList.get(0).width, m_PreviewList.get(0).height);
		m_Camera.setParameters(cp);
		m_Camera.setOneShotPreviewCallback(null);
		m_Camera.stopPreview();
		m_Camera.setOneShotPreviewCallback(m_PreViewCallback);
		Camera.Parameters acp = m_Camera.getParameters();
		cp.setPreviewSize(m_PreViewSize.width, m_PreViewSize.height);
		m_Camera.setParameters(acp);
		m_Camera.startPreview();
	}

	public void takePicture(ShutterCallback shutter, PictureCallback raw, PictureCallback jpeg){
		//m_Camera.takePicture(shutter,raw,jpeg);
		//m_Camera.takePicture(shutter,raw,null);
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public void takePicture(ShutterCallback shutter, PictureCallback raw, PictureCallback post ,PictureCallback jpeg){
		Log.d(TAG,"post:"+shutter+" "+raw+" "+post+" "+jpeg);
		m_Camera.takePicture(shutter,raw,post,jpeg);
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public void stopPreview() {
		m_Camera.stopPreview();
	}

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
		Log.d(TAG,"RGB" +rgb);
		PreviewImage = Bitmap.createBitmap(rgb, width, height, Bitmap.Config.ARGB_8888);
		Log.d(TAG,"P:"+PreviewImage);
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
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------
}
