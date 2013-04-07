//  Created by Taku Kobayashi 小林 拓

package com.taku.kobayashi.pngcamera;

import java.io.File;
import java.util.EventListener;

import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.OAuthAuthorization;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.media.MediaProvider;

import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.app.Activity;
import android.content.SharedPreferences;

//Twitterへの認証処理
public class TwitterAction{

	private static final String TAG = "PNGCamera_TwitterAction";
	private Activity m_Activity;
	private Handler m_Handler;
	private Twitter m_Twitter;
	private OAuthResultListener m_OAuthResultListener = null;
	private OAuthAuthorization m_OAuthAuthorization;
	private int m_ErrorStatusCode = 0;
	private File m_UploadFile;
	private String m_Tweet;
	private UploadListener m_UploadListener = null;

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public TwitterAction(Activity act){
		m_Activity = act;
		m_Handler = new Handler();
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	private Configuration settingConsumerKey(){
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true);
		cb.setOAuthConsumerKey(TwitterConfig.ConsumerKey);
		cb.setOAuthConsumerSecret(TwitterConfig.ConsumerSecret);
		return cb.build();
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	private Configuration settingAccessToken(String AccessToken,String AccessTokenSecret){
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true);
		cb.setOAuthConsumerKey(TwitterConfig.ConsumerKey);
		cb.setOAuthConsumerSecret(TwitterConfig.ConsumerSecret);
		cb.setOAuthAccessToken(AccessToken);
		cb.setOAuthAccessTokenSecret(AccessTokenSecret);
		cb.setMediaProvider(MediaProvider.TWITTER.toString());
		return cb.build();
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	//AccessTokenの設定
	public void setAccessToken(String AccessToken,String AccessTokenSecret){
		m_Twitter = new TwitterFactory(settingAccessToken(AccessToken, AccessTokenSecret)).getInstance();
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	//認証ページのURLを取ってくる
	public void startOAuth(){
		m_OAuthAuthorization = new OAuthAuthorization(settingConsumerKey());
		// これが無いとgetOAuthRequestToken()で例外が発生するらしい
		m_OAuthAuthorization.setOAuthAccessToken(null);

		//マルチスレッドにしないとAndroid3.0以降の端末では例外処理が発生する
		new Thread(new Runnable() {

			private String url = null;

			@Override
			public void run() {
				//アプリの認証オブジェクト作成
				RequestToken req = null;
				try {
					req = m_OAuthAuthorization.getOAuthRequestToken();
				} catch (TwitterException e) {
					e.printStackTrace();
					oAuthErrorHandler(e.getStatusCode());
				}
				if(req != null){
					url = req.getAuthorizationURL();
				}else{
					//エラー原因が不明
					oAuthErrorHandler(TwitterConfig.UNKOWN_ERROR_STATUS_CODE);
				}
				//メインスレッドに処理を投げる
				m_Handler.post(new Runnable() {
					@Override
					public void run() {
						if(m_OAuthResultListener != null){
							m_OAuthResultListener.requestOAuthUrl(url);
						}
					}
				});
			}
		}).start();
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	//認証完了後AccessTokenを取得する処理
	//(CallbackURLにCallBackされればAccessTokeが取得することが可能になる)
	public void returnOAuth(final String url){

		//マルチスレッドにしないとAndroid3.0以降の端末では例外処理が発生する
		new Thread(new Runnable() {

			private AccessToken accessToken = null;

			@Override
			public void run() {
				try {
					//AccessTokenを取得する
					accessToken = m_OAuthAuthorization.getOAuthAccessToken();
					Log.d(TAG, "AT:"+accessToken.getToken()+"ATS:"+accessToken.getTokenSecret());
					//AccessTokenを記録する
					recordAccessToken(accessToken.getToken(),accessToken.getTokenSecret());
					//メインスレッドに処理を投げる
					m_Handler.post(new Runnable() {
						@Override
						public void run() {
							if(m_OAuthResultListener != null){
								m_OAuthResultListener.oAuthResult(accessToken.getToken(), accessToken.getTokenSecret());
							}
						}
					});
				} catch (TwitterException e) {
					e.printStackTrace();
					oAuthErrorHandler(e.getStatusCode());
				}
			}
		}).start();
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	//取得したAccessTokenをローカルに保存する
	private void recordAccessToken(String AccessToken,String AccessTokenSecret){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(m_Activity);
		SharedPreferences.Editor editor = sp.edit();
		editor.putString(m_Activity.getString(R.string.TwitterAccessTokenKey), AccessToken);
		editor.putString(m_Activity.getString(R.string.TwitterAccessTokenSecretKey), AccessTokenSecret);
		editor.commit();
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	//認証処理中にエラーが出たときに行う処理
	private void oAuthErrorHandler(int StatusCode){
		m_ErrorStatusCode = StatusCode;
		//メインスレッドに処理を投げる
		m_Handler.post(new Runnable() {
			@Override
			public void run() {
				if(m_OAuthResultListener != null){
					m_OAuthResultListener.oAuthError(m_ErrorStatusCode);
				}
			}
		});
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * リスナーを追加する
	 */
	public void setOnOAuthResultListener(OAuthResultListener OAuthListener){
		m_OAuthResultListener = OAuthListener;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * リスナーを削除する
	 */
	public void removeOAuthResulListener(){
		m_OAuthResultListener = null;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	//処理が終わったことを通知する独自のリスナーを作成
	public interface OAuthResultListener extends EventListener {

		//Twitterの認証ページのURLを取得した場合に呼ばれる
		public void requestOAuthUrl(String url);

		//Twitterの認証時にエラーが発生した場合に呼ばれる
		public void oAuthError(int StatusCode);

		//Twitterの認証処理が完了し、AccessTokenが取得できる時に呼ばれる
		public void oAuthResult(String token,String tokenSecret);

	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public void sendImageWithTweetToTwitter(File imageFile,String Tweet){
		m_UploadFile = imageFile;
		m_Tweet = Tweet;

		//マルチスレッドにしないとAndroid3.0以降の端末では例外処理が発生する
		new Thread(new Runnable() {

			private int StatusCode = 0;

			@Override
			public void run() {
				try {
					//ツイートする内容の設定
					StatusUpdate status = new StatusUpdate(m_Tweet);
					//投稿する画像ファイルの設定
					status.media(m_UploadFile);
					//twitterと通信し、ツイートと画像を投稿する
					m_Twitter.updateStatus(status);

					//メインスレッドに処理を投げる
					m_Handler.post(new Runnable() {
						@Override
						public void run() {
							if(m_UploadListener != null){
								m_UploadListener.success(m_UploadFile, m_Tweet);
							}
						}
					});
				} catch (TwitterException e) {
					e.printStackTrace();
					StatusCode = e.getStatusCode();
					//メインスレッドに処理を投げる
					m_Handler.post(new Runnable() {
						@Override
						public void run() {
							if(m_UploadListener != null){
								m_UploadListener.error(StatusCode);
							}
						}
					});
				}
			}
		}).start();
	}



	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * リスナーを追加する
	 */
	public void setOnUploadListener(UploadListener uploadListener){
		m_UploadListener = uploadListener;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * リスナーを削除する
	 */
	public void removeUploadListener(){
		m_UploadListener = null;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	//処理が終わったことを通知する独自のリスナーを作成
	public interface UploadListener extends EventListener {

		//Twitterへの画像の投稿が完了した場合に呼ばれる
		public void success(File UploadFile,String Tweet);

		//Twitterへの画像の投稿に失敗した場合に呼ばれる
		public void error(int StatusCode);

	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------


}
