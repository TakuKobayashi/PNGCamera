package com.taku.kobayashi.pngcamera;

import java.io.File;

import com.taku.kobayashi.pngcamera.TwitterAction.OAuthResultListener;
import com.taku.kobayashi.pngcamera.TwitterAction.UploadListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.TextView;

public class CameraGalleryActivity extends Activity{

	private final static String TAG = "PNGCamera_CameraGalleryActivity";
	private CameraGalleryAdapter m_CameraGalleryAdapter;
	private int m_nSelectImageNumber = 0;
	private ImageButton m_TwitterButton;
	private WebView m_TwitterWebView;
	private TwitterAction m_TwitterAction;
	private FacebookAction m_FacebookAction;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
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
		FacebookButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setupFacebook();
			}
		});
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

		m_FacebookAction = new FacebookAction(this);
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

	private void showTweetDialog(){
		AlertDialog.Builder tweetDialogBuilder = new AlertDialog.Builder(this);
		tweetDialogBuilder.setCancelable(true);
		final AlertDialog tweetDialog = tweetDialogBuilder.create();
		tweetDialog.show();
		tweetDialog.setContentView(R.layout.tweetdialog);
		final TextView tweetCountText = (TextView) tweetDialog.findViewById(R.id.TweetCountText);
		tweetCountText.setText(String.valueOf(Config.TWITTER_MAX_TEXT_COUNT - 2));
		tweetCountText.setTextColor(Color.BLACK);
		final EditText tweetText = (EditText) tweetDialog.findViewById(R.id.TweetText);
		tweetText.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				int nEditableLength = Config.TWITTER_MAX_TEXT_COUNT - s.length() - 2;
				if(nEditableLength >= 0){
					tweetCountText.setTextColor(Color.BLACK);
				}else{
					tweetCountText.setTextColor(Color.RED);
				}
				tweetCountText.setText(String.valueOf(nEditableLength));
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});
		ImageButton sendTweetButton = (ImageButton) tweetDialog.findViewById(R.id.SendTweetButton);
		sendTweetButton.setImageResource(R.drawable.tweetbutton);
		sendTweetButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sendTwitterAction(tweetText.getText().toString());
				tweetDialog.cancel();
			}
		});
		sendTweetButton.setOnTouchListener(ExtraLayout.ImageTouchListener);
		tweetDialog.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				Tools.releaseImageView((ImageButton) tweetDialog.findViewById(R.id.SendTweetButton));
			}
		});
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	private void setupTweet(){
		SharedPreferences setting = PreferenceManager.getDefaultSharedPreferences(this);
		String AccessToken = setting.getString(this.getString(R.string.TwitterAccessTokenKey), null);
		String AccessTokenSecret = setting.getString(this.getString(R.string.TwitterAccessTokenSecretKey), null);
		if(AccessToken != null && AccessTokenSecret != null){
			m_TwitterAction.setAccessToken(AccessToken, AccessTokenSecret);
			showTweetDialog();
		}else{
			m_TwitterAction.setOnOAuthResultListener(new OAuthResultListener() {
				//認証ページのURLを取得した時に呼ばれる
				@Override
				public void requestOAuthUrl(String url) {
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
					showTweetDialog();
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

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(m_TwitterWebView.getVisibility() == View.VISIBLE && keyCode == KeyEvent.KEYCODE_BACK){
			m_TwitterWebView.setVisibility(View.INVISIBLE);
			m_TwitterButton.setClickable(true);
			return true;
		}
		return super.onKeyDown(keyCode, event);
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

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

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

	private void setupFacebook(){
		m_FacebookAction.startLogin();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		m_FacebookAction.setLoginResult(requestCode, resultCode, data);
	}

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