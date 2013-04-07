package com.taku.kobayashi.pngcamera;

import java.io.File;

import com.taku.kobayashi.pngcamera.TwitterAction.OAuthResultListener;
import com.taku.kobayashi.pngcamera.TwitterAction.UploadListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageButton;

public class CameraGalleryActivity extends Activity{

	private final static String TAG = "PNGCamera_CameraGalleryActivity";
	private CameraGalleryAdapter m_CameraGalleryAdapter;
	private int m_nSelectImageNumber = 0;
	private ImageButton m_TwitterButton;
	private WebView m_TwitterWebView;
	private TwitterAction m_TwitterAction;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cameragalleryview);
		getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_HIDDEN);

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
		m_TwitterButton =  (ImageButton) findViewById(R.id.TwitterButton);
		m_TwitterButton.setImageResource(R.drawable.twitter_icon);
		m_TwitterButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setupTweet();
			}
		});
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

		m_TwitterWebView = (WebView) findViewById(R.id.TwitterWebView);
		m_TwitterWebView.getSettings().setJavaScriptEnabled(true);
		WebSettings webSettings = m_TwitterWebView.getSettings();
		webSettings.setSavePassword(false);

		m_TwitterWebView.setWebChromeClient(new WebChromeClient(){
			//WebViewがURLを読み込んでいる最中の処理の設定
			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				super.onProgressChanged(view, newProgress);
				setProgress(newProgress * 100);
			}
		});
		m_TwitterWebView.setWebViewClient(m_TwitterOAuthCallbackClient);
		m_TwitterWebView.setVisibility(View.INVISIBLE);

		m_TwitterAction = new TwitterAction(this);
		m_TwitterAction.setOnUploadListener(m_TwitterImageUploadListener);
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

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

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	private void showMailerList(){
		Intent mailIntent = new Intent(Intent.ACTION_SEND);
		//画像をメールに添付するために必要
		mailIntent.setType("image/*");
		//画像へのパス
		mailIntent.putExtra(Intent.EXTRA_STREAM,Uri.fromFile(m_CameraGalleryAdapter.getFile(m_nSelectImageNumber)));
		startActivity(mailIntent);
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	private void setupTweet(){
		SharedPreferences setting = PreferenceManager.getDefaultSharedPreferences(this);
		String AccessToken = setting.getString(this.getString(R.string.TwitterAccessTokenKey), null);
		String AccessTokenSecret = setting.getString(this.getString(R.string.TwitterAccessTokenSecretKey), null);
		Log.d(TAG,"AT:"+ AccessToken+ " ATS:"+AccessTokenSecret);
		if(AccessToken != null && AccessTokenSecret != null){
			m_TwitterAction.setAccessToken(AccessToken, AccessTokenSecret);
			sendTwitterAction("test");
		}else{
			Log.d(TAG, "oauth");
			m_TwitterAction.setOnOAuthResultListener(new OAuthResultListener() {
				//認証ページのURLを取得した時に呼ばれる
				@Override
				public void requestOAuthUrl(String url) {
					Log.d(TAG, "url:"+url);
					if(url != null){
						m_TwitterWebView.loadUrl(url);
						m_TwitterWebView.setVisibility(View.VISIBLE);
						//WebView上で入力時にキーボードを出現させるためにフォーカスをあてる。
						m_TwitterWebView.requestFocus();
					}
				}

				//認証完了後AccessToken取得完了した時に呼ばれる
				@Override
				public void oAuthResult(String token, String tokenSecret) {
					m_TwitterButton.setClickable(true);
					m_TwitterWebView.setVisibility(View.INVISIBLE);
					m_TwitterAction.setAccessToken(token, tokenSecret);
					sendTwitterAction("test");
				}

				//認証エラーが発生した時に呼ばれる
				@Override
				public void oAuthError(int StatusCode) {
					m_TwitterButton.setClickable(true);
					m_TwitterWebView.setVisibility(View.INVISIBLE);
				}
			});
			m_TwitterButton.setClickable(false);
			m_TwitterAction.startOAuth();
		}
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	private void sendTwitterAction(String tweet){
		//Fileは投稿する画像のファイル、第二引数(String)はツイート文
		m_TwitterAction.sendImageWithTweetToTwitter(m_CameraGalleryAdapter.getFile(m_nSelectImageNumber), tweet);
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	private WebViewClient m_TwitterOAuthCallbackClient = new WebViewClient(){

		//※Android2.*系でも常時呼ばれる
		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			if ((url != null) && (url.startsWith(TwitterConfig.CallbackUrl))) {
				m_TwitterWebView.stopLoading();
				m_TwitterWebView.setVisibility(View.INVISIBLE);
				//認証完了後にCallbackするURLをフックし、AccessTokenを取得する処理を行う
				m_TwitterAction.returnOAuth(url);
			}
		};
	};

	private UploadListener m_TwitterImageUploadListener = new UploadListener() {

		@Override
		public void success(File UploadFile, String Tweet) {
			Tools.showToast(CameraGalleryActivity.this, CameraGalleryActivity.this.getString(R.string.ContributeSucessMessage));
		}

		@Override
		public void error(int StatusCode) {
			Tools.showToast(CameraGalleryActivity.this, CameraGalleryActivity.this.getString(R.string.ContributeFailedMessage));
		}
	};

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Tools.releaseImageView((ImageButton) findViewById(R.id.FaceBookButton));
		Tools.releaseImageView((ImageButton) findViewById(R.id.TwitterButton));
		Tools.releaseImageView((ImageButton) findViewById(R.id.MailButton));
		Tools.releaseImageView((ImageButton) findViewById(R.id.TrashButton));
		m_CameraGalleryAdapter.release();
		Tools.releaseWebView(m_TwitterWebView);
	}
}