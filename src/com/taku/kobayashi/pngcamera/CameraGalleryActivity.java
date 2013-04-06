package com.taku.kobayashi.pngcamera;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Gallery;

public class CameraGalleryActivity extends Activity{

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cameragalleryview);
		Gallery cameraGallery = (Gallery) findViewById(R.id.CameraGallery);
	}
}