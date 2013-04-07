package com.taku.kobayashi.pngcamera;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
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
		FacebookButton.setImageResource(R.drawable.facebook_icon);
		ImageButton  TwitterButton =  (ImageButton) findViewById(R.id.TwitterButton);
		TwitterButton.setImageResource(R.drawable.twitter_icon);
		ImageButton MailButton = (ImageButton) findViewById(R.id.MailButton);
		MailButton.setImageResource(R.drawable.mail_icon);
		MailButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showMailerList();
			}
		});
		ImageButton TrashButton = (ImageButton) findViewById(R.id.TrashButton);
		TrashButton.setImageResource(R.drawable.trash_icon);
		TrashButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDeleteDialog();
			}
		});
	}

	private void showDeleteDialog(){
		AlertDialog.Builder deleteImgDialog = new AlertDialog.Builder(this);
		deleteImgDialog.setMessage(this.getString(R.string.GalleryDeleteDialogMessage));
		deleteImgDialog.setCancelable(true);
		deleteImgDialog.setPositiveButton(this.getString(R.string.SelectYesText), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				m_CameraGalleryAdapter.FileDelete(m_nSelectImageNumber);
				if(m_CameraGalleryAdapter.isEmpty() == true){
					CameraGalleryActivity.this.finish();
				}
				Tools.showToast(CameraGalleryActivity.this, CameraGalleryActivity.this.getString(R.string.GalleryDeleteDialogMessage));
			}
		});
		deleteImgDialog.setNegativeButton(this.getString(R.string.SelectNoText), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {

			}
		});
		deleteImgDialog.create().show();
	}

	private void showMailerList(){
		Intent mailIntent = new Intent(Intent.ACTION_SEND);
		//画像をメールに添付するために必要
		mailIntent.setType("image/*");
		//画像へのパス
		mailIntent.putExtra(Intent.EXTRA_STREAM,Uri.fromFile(m_CameraGalleryAdapter.getFile(m_nSelectImageNumber)));
		startActivity(mailIntent);
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