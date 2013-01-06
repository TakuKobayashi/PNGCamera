package com.taku.kobayashi.pngcamera;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AbsListView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

public class PNGCameraActivity extends Activity {

	private static final String TAG = "PNGCamera_AnotherWorldActivity";
	private CameraPreview m_CameraPreview = null;
	private int m_nCameraID = 0;
	private ListView m_CameraParamsList;
	private CameraParameterAdapter m_CameraParameterAdapter;

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

		TextView CameraOptionButton = (TextView) findViewById(R.id.CameraParamsButton);
		//ImageButton CameraOptionButton = (ImageButton) findViewById(R.id.CameraParamsButton);
		//CameraOptionButton.setImageResource(R.drawable.setting_icon);
		CameraOptionButton.setOnClickListener(m_CameraOptionListener);

		m_CameraParameterAdapter = new CameraParameterAdapter(this);

	}

	//----------------------------------------------------------------------------------

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_CAMERA) {
			if (m_CameraPreview != null) {
				m_CameraPreview.autoFocus(CameraAutoFocusCallback);
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
			if (m_CameraPreview != null) {
				m_CameraPreview.autoFocus(CameraAutoFocusCallback);
			}
		}
	};

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	private OnClickListener m_InOutButtonListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
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

	private OnItemClickListener m_CameraParameterListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> adapterview, View view, int position,long id) {
			m_CameraParameterAdapter.switchSelected(position);
		}

	};

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	private Camera.AutoFocusCallback CameraAutoFocusCallback = new Camera.AutoFocusCallback() {
		public void onAutoFocus(boolean success, Camera camera) {
			m_CameraPreview.takuPreviewPicture();
		}
	};

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	private Camera.ShutterCallback CameraShutterCallback = new Camera.ShutterCallback() {
		@Override
		public void onShutter() {

		}
	};

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	@Override
	protected void onResume(){
		super.onResume();
		m_CameraPreview = (CameraPreview) findViewById(R.id.CameraPreview);
		m_CameraPreview.setCamera(m_nCameraID);
		m_CameraPreview.setCameraParams(m_CameraParameterAdapter);
		CGSize displaySize = ExtraLayout.getDisplaySize(this);

		m_CameraParamsList = (ListView) findViewById(R.id.CameraParamsList);
		m_CameraParamsList.getLayoutParams().width = (int)(displaySize.width * 2 / 3);
		int height = (int)(displaySize.height / 2);
		if(height > m_CameraParameterAdapter.getCount() * ExtraLayout.getListCellMinHeight(this)){
			height = m_CameraParameterAdapter.getCount() * ExtraLayout.getListCellMinHeight(this) + 12;
		}
		m_CameraParamsList.getLayoutParams().height = height;
		m_CameraParamsList.setAdapter(m_CameraParameterAdapter);
		m_CameraParamsList.setOnItemClickListener(m_CameraParameterListener);
		m_CameraParamsList.setVisibility(View.GONE);
	};

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	@Override
	protected void onPause(){
		super.onPause();
		m_CameraPreview.releaseCamera();
		m_CameraParamsList = null;
		m_CameraParameterAdapter.releaseParameters();
	};

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Tools.releaseImageView((ImageButton) findViewById(R.id.ShutterButton));
		Tools.releaseImageView((ImageButton) findViewById(R.id.InOutButton));
		//Tools.releaseImageView((ImageButton) findViewById(R.id.CameraParamsButton));
	}

	//--------------------------------------------------------------------------------------------------------------------------------------------------------------

}
