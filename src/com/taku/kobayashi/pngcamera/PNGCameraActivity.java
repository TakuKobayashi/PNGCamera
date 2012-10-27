package com.taku.kobayashi.pngcamera;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;

public class PNGCameraActivity extends Activity {

	private static final String TAG = "AnotherWorld_AnotherWorldActivity";
	private CameraPreview m_CameraPreview = null;
	private int m_nCameraID = 0;

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
	};

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	@Override
	protected void onPause(){
		super.onPause();
		m_CameraPreview.releaseCamera();
	};

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Tools.releaseImageView((ImageButton) findViewById(R.id.ShutterButton));
		Tools.releaseImageView((ImageButton) findViewById(R.id.InOutButton));
	}

	//--------------------------------------------------------------------------------------------------------------------------------------------------------------

}
