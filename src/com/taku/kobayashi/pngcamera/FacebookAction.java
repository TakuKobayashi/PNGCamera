//  Created by Taku Kobayashi 小林 拓

package com.taku.kobayashi.pngcamera;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.EventListener;
import java.util.Iterator;
import java.util.Set;

import com.facebook.AccessToken;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.TokenCachingStrategy;
import com.facebook.internal.Utility;
import com.facebook.model.GraphObject;
import com.taku.kobayashi.pngcamera.TwitterAction.OAuthResultListener;
import com.taku.kobayashi.pngcamera.TwitterAction.UploadListener;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;


//Facebookへの認証処理
public class FacebookAction{

	private static final String TAG = "PNGCamera_FacebookAction";
	private static final String APP_ID = FacebookConfig.APP_ID;
	private static final String[] PERMISSIONS = {"offline_access","user_photos","photo_upload"};
	private static final int LOGIN_REQUESTCODE = 1;
	private Activity m_Activity;
	private Session m_Session;
	private UploadListener m_UploadListener = null;
	private LoginResultListener m_LoginResultListener = null;

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public FacebookAction(Activity act){
		m_Activity = act;
		Session.Builder sb = new Session.Builder(act);
		sb.setApplicationId(APP_ID);
		sb.setTokenCachingStrategy(m_TokenCachingStrategy);
		m_Session = sb.build();
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	private Session.OpenRequest settingSessionRequest(){
		Session.OpenRequest so = new Session.OpenRequest(m_Activity);
		so.setPermissions(Arrays.asList(PERMISSIONS));
		so.setCallback(m_SessionStatusCallback);
		so.setIsLegacy(true);
		return so;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public void startLogin(){
		m_Session.openForRead(this.settingSessionRequest());
		Log.d(TAG, "at:"+m_Session.getAccessToken()+" b:"+m_Session.getAuthorizationBundle());
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public void setLoginResult(int requestCode, int resultCode, Intent data){
		Log.d(TAG, "request:"+ requestCode +" result:"+ resultCode +"data:"+data.getExtras());
		Bundle extra = data.getExtras();
		Set<String> keySet = extra.keySet();
		Iterator<String> it = keySet.iterator();
		while (it.hasNext()) {
			String key = it.next();
			Log.d(TAG, "key:"+key +" value:"+extra.get(key));
		}
		if(resultCode == Activity.RESULT_OK){
			Log.d(TAG, "OK");
			m_Session.onActivityResult(m_Activity, requestCode, resultCode, data);
			Log.d(TAG, "AccessToken:"+m_Session.getAccessToken());
			if(m_LoginResultListener != null){
				if(m_Session.isOpened() && m_Session.getAccessToken() != null){
					Log.d(TAG,"open");
					m_LoginResultListener.success(m_Session.getAccessToken());
				}else{
					Log.d(TAG,"error");
					m_LoginResultListener.error();
				}
			}
		}else if(resultCode == Activity.RESULT_CANCELED){
			Log.d(TAG,"cancel");
			if(m_LoginResultListener != null){
				m_LoginResultListener.cancel();
			}
		}else{
			Log.d(TAG,"error2");
			if(m_LoginResultListener != null){
				m_LoginResultListener.error();
			}
		}
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public void uploadImage(File imageFile) {
		if(m_Session.isOpened()){
			Log.d(TAG,"open");
			Bundle parameters = new Bundle();
			parameters.putString("caption",m_Activity.getString(R.string.app_name));
			Request request = null;
			try {
				request = Request.newUploadPhotoRequest(m_Session, imageFile, m_RequestCallback);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return;
			}
			RequestAsyncTask requestAsynk = new RequestAsyncTask(request);
			requestAsynk.execute();
		}else{
			Log.d(TAG,"uploadError");
			if(m_UploadListener != null){
				m_UploadListener.error();
			}
		}
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	private Session.StatusCallback m_SessionStatusCallback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state, Exception exception) {
			Log.d(TAG, "session:"+session+"ex"+exception);
			Log.d(TAG, "ac:"+session.getAccessToken()+" ap:"+session.getAuthorizationBundle()+"st:"+state);
			Log.d(TAG, "open:"+session.isOpened()+" close:"+ session.isClosed() +"st:"+session.getState()+"as:"+session.getActiveSession());
			if(state == SessionState.OPENED){
				Log.d(TAG, "ac:"+session.getAccessToken()+" ac:"+ session.getActiveSession() + "as:"+session.getActiveSession()+"st"+state);
			}
		}
	};

	private Request.Callback m_RequestCallback = new Request.Callback() {

		@Override
		public void onCompleted(Response response) {
			Log.d(TAG, "response:"+response);
			if(m_UploadListener != null){
				if(response.getError() == null){
					Log.d(TAG, "success:");
					m_UploadListener.success();
				}else{
					Log.d(TAG, "error:"+response.getError());
					m_UploadListener.error();
				}
			}
		}
	};

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	private TokenCachingStrategy m_TokenCachingStrategy = new TokenCachingStrategy() {

		@Override
		public void save(Bundle bundle) {
			Set<String> keySet = bundle.keySet();
			Iterator<String> it = keySet.iterator();
			while (it.hasNext()) {
				String key = it.next();
				Log.d(TAG, "savekey:"+key +" savevalue:"+bundle.get(key));
			}
		}

		@Override
		public Bundle load() {
			Log.d(TAG, "load");
			return null;
		}

		@Override
		public void clear() {
			Log.d(TAG, "clear");
		}
	};

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * リスナーを追加する
	 */
	public void setOnLoginResultListener(LoginResultListener loginListener){
		m_LoginResultListener = loginListener;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * リスナーを削除する
	 */
	public void removeLoginResultListener(){
		m_LoginResultListener = null;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	//処理が終わったことを通知する独自のリスナーを作成
	public interface LoginResultListener extends EventListener {

		//Facebookの認証処理が完了し、AccessTokenが取得できる時に呼ばれる
		public void success(String accessToken);

		//Facebookの認証がキャンセルされた場合に呼ばれる
		public void cancel();

		//Facebookの認証時にエラーが発生した場合に呼ばれる
		public void error();

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

		//Facebookへの画像の投稿が完了した場合に呼ばれる
		public void success();

		//Facebookへの画像の投稿に失敗した場合に呼ばれる
		public void error();

	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------
}
