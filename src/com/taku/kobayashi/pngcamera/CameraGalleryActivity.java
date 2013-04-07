package com.taku.kobayashi.pngcamera;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Gallery;
import android.widget.ImageButton;

public class CameraGalleryActivity extends Activity{

	private final static String TAG = "PNGCamera_CameraGalleryActivity";
	private CameraGalleryAdapter m_CameraGalleryAdapter;
	private int m_nSelectImageNumber = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cameragalleryview);

		m_CameraGalleryAdapter = new CameraGalleryAdapter(this);
		Gallery cameraGallery = (Gallery) findViewById(R.id.CameraGallery);
		cameraGallery.setAdapter(m_CameraGalleryAdapter);
		cameraGallery.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position,long id) {
				m_nSelectImageNumber = position;
				//メモリリーク対策
				m_CameraGalleryAdapter.NonUsingImageRelease(parent.getFirstVisiblePosition(), parent.getLastVisiblePosition() + 1);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				Log.d(TAG, "onNothingSelected");
			}
		});
		ImageButton FacebookButton = (ImageButton) findViewById(R.id.FaceBookButton);
		FacebookButton.setImageResource(R.id.FaceBookButton);
		ImageButton  TwitterButton =  (ImageButton) findViewById(R.id.TwitterButton);
		TwitterButton.setImageResource(R.drawable.twitter_icon);
		ImageButton MailButton = (ImageButton) findViewById(R.id.MailButton);
		MailButton.setImageResource(R.drawable.mail_icon);
		ImageButton TrashButton = (ImageButton) findViewById(R.id.TrashButton);
		TrashButton.setImageResource(R.drawable.trash_icon);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Tools.releaseImageView((ImageButton) findViewById(R.id.FaceBookButton));
		Tools.releaseImageView((ImageButton) findViewById(R.id.TwitterButton));
		Tools.releaseImageView((ImageButton) findViewById(R.id.MailButton));
		Tools.releaseImageView((ImageButton) findViewById(R.id.TrashButton));
		m_CameraGalleryAdapter.release();
	}
}