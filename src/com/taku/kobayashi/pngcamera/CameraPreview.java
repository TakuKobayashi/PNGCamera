//  Created by 拓 小林
//  Copyright (c) 2013年 TakuKobayashi rights reserved.

package com.taku.kobayashi.pngcamera;

import java.io.File;
import java.io.IOException;
import java.util.EventListener;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;

import com.taku.kobayashi.pngcamera.CameraParameterExpandableAdapter.ParamsSelectListener;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback{

	public Bitmap PreviewImage;
	private static final String TAG = "PNGCamera_CameraPreView";
	private Context m_Context;
	private SurfaceHolder m_Holder;
	public int m_CameraDisplayOrientation = 0;
	private Camera m_Camera = null;
	private ImageView m_Thumbnail;
	private Size m_ThumbnailSize;
	private Thread m_Thread = null;
	private Handler m_Handler;
	private SaveCompleteListener SaveListener = null;

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
		m_ThumbnailSize = m_Camera.getParameters().getJpegThumbnailSize();
		//多分昇順で保存されているので後ろから取ってくるといい
		for(int i = fileNames.length - 1;i >= 0; i--){
			if(fileNames[i].contains(".jpg") || fileNames[i].contains(".png")){
				String imagePath = path + "/" + fileNames[i];
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

		cpa.setParameters(m_Camera);
		cpa.setOnParamsSelectListener(new ParamsSelectListener() {
			@Override
			public void selected(String key, String value) {
				Tools.setCameraParams(m_Context, m_Camera, key, value);
			}
		});
		m_CameraDisplayOrientation = getCameraDisplayOrientation((Activity) m_Context, nCameraID);
		m_Camera.setDisplayOrientation(m_CameraDisplayOrientation);
		m_Camera.startPreview();
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public void autoFocus(AutoFocusCallback cameraAutoFocusCallback){
		m_Camera.autoFocus(cameraAutoFocusCallback);
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public void takePreviewPicture(int nCameraId) {
		Camera.Parameters cp = m_Camera.getParameters();
		Size previewSize = cp.getPreviewSize();
		//シャッター音
		String sound = Tools.getRecordingParam(m_Context, m_Context.getString(R.string.SutterSoundKey));
		if(Boolean.parseBoolean(sound)){
			MediaPlayer mp = MediaPlayer.create(m_Context, R.raw.camera_shutter);
			try {
				mp.prepare();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			mp.start();
			mp.setOnCompletionListener(new OnCompletionListener(){
				@Override
				public void onCompletion(MediaPlayer mp) {
					mp.stop();
					mp.release();
					mp = null;
				}
			});
		}
		//画像の大きさ
		String size = Tools.getRecordingParam(m_Context, m_Context.getString(R.string.CameraPreviewSizeKey) + nCameraId);
		String[] imageSize = size.split(m_Context.getString(R.string.SizeConnectionWord));
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

						//画像を保存
						String savedImagePathstr = Tools.getFilePath("." + Tools.getRecordingParam(m_Context, m_Context.getString(R.string.SaveFormatKey)));
						Tools.SaveImage(m_Context, image, savedImagePathstr);
						image.recycle();
						image = null;
						File file = new File(savedImagePathstr);
						if(SaveListener != null){
							SaveListener.complete(file);
						}
						m_ThumbnailImage = Tools.getSelectSizeBitmap(m_Context, Uri.fromFile(file), m_ThumbnailSize.height, m_ThumbnailSize.width);
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
			}
		});
		Camera.Parameters acp = m_Camera.getParameters();
		acp.setPreviewSize(previewSize.width, previewSize.height);
		m_Camera.setParameters(acp);
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

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * リスナーを追加する
	 */
	public void setOnSaveCompleteListener(SaveCompleteListener listener){
		this.SaveListener = listener;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * リスナーを削除する
	 */
	public void removeListener(){
		this.SaveListener = null;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public interface SaveCompleteListener extends EventListener {

		public void complete(File imageFile);

	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------
}
