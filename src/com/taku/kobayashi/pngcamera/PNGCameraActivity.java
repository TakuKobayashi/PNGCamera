//  Created by 拓 小林
//  Copyright (c) 2013年 TakuKobayashi All rights reserved.

package com.taku.kobayashi.pngcamera;

import java.io.File;

import com.taku.kobayashi.pngcamera.CameraPreview.SaveCompleteListener;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class PNGCameraActivity extends Activity {

	private static final String TAG = "PNGCamera_AnotherWorldActivity";
	private CameraPreview m_CameraPreview = null;
	private int m_nCameraID = 0;
	private ExpandableListView m_CameraParamsList;
	private CameraParameterExpandableAdapter m_CameraParameterAdapter;
	private SensorManager m_SensorManager = null;
	private OrientationEventListener m_OrientationListener;
	private boolean m_bMoveSurFace = false;
	private boolean m_bAutoFocus = false;

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.cameraview);

		m_nCameraID = getIntent().getIntExtra(getResources().getString(R.string.IntentCameraIDKey), 0);

		ImageButton shatterButton = (ImageButton) findViewById(R.id.ShutterButton);
		shatterButton.setImageResource(R.drawable.device_access_camera);
		shatterButton.setOnClickListener(m_ShatterListener);

		ImageButton InOutButton = (ImageButton) findViewById(R.id.InOutButton);
		InOutButton.setImageResource(R.drawable.device_access_switch_camera);
		if(Build.VERSION.SDK_INT >= 9){
			if (Camera.getNumberOfCameras() > 1) {
				InOutButton.setOnClickListener(m_InOutButtonListener);
			}else{
				InOutButton.setVisibility(View.INVISIBLE);
			}
		}else{
			InOutButton.setVisibility(View.INVISIBLE);
		}

		RelativeLayout CameraOptionLayout = (RelativeLayout) findViewById(R.id.CameraParamsTextLayout);
		CameraOptionLayout.setOnClickListener(m_CameraOptionListener);
		m_CameraParameterAdapter = new CameraParameterExpandableAdapter(this);
		m_CameraParameterAdapter.setCameraId(m_nCameraID);

		//OrientationListenerを測定した結果、画面の向きに該当する値を割り当てた
		m_OrientationListener = new OrientationEventListener(this,SensorManager.SENSOR_DELAY_UI) {

			@Override
			public void onOrientationChanged(int orientation) {
				if(m_CameraPreview != null){
					if((68 <= orientation && orientation < 113) || (248 <= orientation && orientation < 270)){
						m_CameraPreview.m_CameraDisplayOrientation = 90;
					}else if(113 <= orientation && orientation < 158){
						m_CameraPreview.m_CameraDisplayOrientation = 180;
					}else if(158 <= orientation && orientation < 203){
						m_CameraPreview.m_CameraDisplayOrientation = 270;
					}else if(203 <= orientation && orientation < 248){
						m_CameraPreview.m_CameraDisplayOrientation = 0;
					}else if(orientation == OrientationEventListener.ORIENTATION_UNKNOWN){

					}else{
						m_CameraPreview.m_CameraDisplayOrientation = 90;
					}
				}
			}
		};
		m_SensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
		if(checkAllSDcardStatus() == false){
			finish();
		}

	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(m_CameraParamsList.getVisibility() == View.VISIBLE){
			if(keyCode == KeyEvent.KEYCODE_BACK){
				m_CameraParamsList.setVisibility(View.GONE);
				return true;
			}
		}
		if (keyCode == KeyEvent.KEYCODE_CAMERA) {
			if (m_CameraPreview != null && checkAllSDcardStatus()) {
				m_CameraPreview.takePreviewPicture(m_nCameraID);
				return true;
			}
		} else if (keyCode == KeyEvent.KEYCODE_MENU && event.isLongPress()) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	private OnClickListener m_ShatterListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (m_CameraPreview != null && checkAllSDcardStatus()) {
				m_CameraPreview.takePreviewPicture(m_nCameraID);
			}
		}
	};

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	private OnClickListener m_InOutButtonListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			m_CameraParamsList.setVisibility(View.GONE);
			if (m_nCameraID == 0) {
				m_nCameraID = 1;
			} else {
				m_nCameraID = 0;
			}
			Intent intent = getIntent();
			intent.putExtra(getResources().getString(R.string.IntentCameraIDKey), m_nCameraID);
			finish();
			startActivity(intent);
		}
	};

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	private OnClickListener m_CameraOptionListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if(m_CameraParamsList.getVisibility() == View.GONE){
				m_CameraParamsList.setVisibility(View.VISIBLE);
			}else{
				m_CameraParamsList.setVisibility(View.GONE);
			}
		}

	};

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	private OnChildClickListener m_CameraParameterListener = new OnChildClickListener() {

		@Override
		public boolean onChildClick(ExpandableListView parent, View v,int groupPosition, int childPosition, long id) {
			m_CameraParameterAdapter.setSelectParam(groupPosition, childPosition);
			return true;
		}

	};

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	private Camera.AutoFocusCallback CameraAutoFocusCallback = new Camera.AutoFocusCallback() {
		public void onAutoFocus(boolean success, Camera camera) {
			m_bAutoFocus = false;
		}
	};

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	private SensorEventListener m_SensorEventListener = new SensorEventListener() {

		private float before = 0f;

		@Override
		public void onSensorChanged(SensorEvent event) {
			if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
				float[] gravity = event.values;
				float sum = Math.abs(gravity[0])+Math.abs(gravity[1])+Math.abs(gravity[2]);
				if(Math.abs(before - sum) < 0.8){
					if(m_bMoveSurFace == true && m_bAutoFocus == false){
						m_bMoveSurFace = false;
						m_bAutoFocus = true;
						m_CameraPreview.autoFocus(CameraAutoFocusCallback);
					}
				}else{
					m_bMoveSurFace = true;
				}
				before = sum;
			}
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {

		}
	};

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	@Override
	protected void onResume(){
		super.onResume();
		m_CameraPreview = (CameraPreview) findViewById(R.id.CameraPreview);
		ImageView im = (ImageView) findViewById(R.id.ThumbnailImageview);
		im.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(PNGCameraActivity.this, CameraGalleryActivity.class);
				startActivity(intent);
			}
		});
		m_CameraPreview.setCamera(m_nCameraID, m_CameraParameterAdapter);
		m_CameraPreview.setThumbnailImageView(im);
		m_SensorManager.registerListener(m_SensorEventListener, m_SensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_UI);
		m_OrientationListener.enable();
		CGSize displaySize = ExtraLayout.getDisplaySize(this);

		m_CameraParamsList = (ExpandableListView) findViewById(R.id.CameraParamsList);
		m_CameraParamsList.getLayoutParams().width = (int)(displaySize.width * 3 / 4);
		int height = m_CameraParameterAdapter.getGroupCount() * ExtraLayout.getListCellMinHeight(this);
		if(height > (displaySize.height / 2)){
			height = (int)(displaySize.height / 2);
		}
		m_CameraParamsList.getLayoutParams().height = height;
		m_CameraParamsList.setAdapter(m_CameraParameterAdapter);
		m_CameraParamsList.setOnChildClickListener(m_CameraParameterListener);
		m_CameraParamsList.setVisibility(View.GONE);
		m_CameraPreview.setOnSaveCompleteListener(new SaveCompleteListener() {
			@Override
			public void complete(File imageFile) {
				//Intentでカメラを呼び出したとき、撮影した画像を送って返す。
				Intent intent = getIntent();
				String action = intent.getAction();
				if(MediaStore.ACTION_IMAGE_CAPTURE.equals(action)){
					intent.setData(Uri.fromFile(imageFile));
					setResult(Activity.RESULT_OK, intent);
					finish();
				}
			}
		});
	};

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	@Override
	protected void onPause(){
		super.onPause();
		//最初にfalseにしておかないとたまにオートフォーカスが走って落ちることがある
		m_bMoveSurFace = false;
		//まずはとりあえず見えなくさしとく
		m_CameraParamsList.setVisibility(View.GONE);
		//センサーを切る
		if(m_SensorManager != null){
			m_SensorManager.unregisterListener(m_SensorEventListener);
		}
		m_OrientationListener.disable();
		//カメラを切る
		m_CameraPreview.releaseCamera();
		m_CameraPreview.releaseCamera();
		m_CameraParamsList = null;
		m_CameraParameterAdapter.releaseParameters();
		Tools.releaseImageView((ImageView) findViewById(R.id.ThumbnailImageview));
	};

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Tools.releaseImageView((ImageButton) findViewById(R.id.ShutterButton));
		Tools.releaseImageView((ImageButton) findViewById(R.id.InOutButton));
	}

	//--------------------------------------------------------------------------------------------------------------------------------------------------------------

	private boolean checkAllSDcardStatus(){
		if(Tools.checkSDcardMount() == false){
			Tools.showToast(this, this.getString(R.string.AlertSDcardMountMessage));
			return false;
		}else if(Tools.checkSDcardAvailableSpace() == false){
			Tools.showToast(this, this.getString(R.string.AlertSDcardLackSpace).replace("*", String.valueOf(Config.LIMIT_MINIMAM_SPACE / 1024)));
			return false;
		}
		return true;
	}

	//--------------------------------------------------------------------------------------------------------------------------------------------------------------
}
