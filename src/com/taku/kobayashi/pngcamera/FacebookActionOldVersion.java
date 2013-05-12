//  Created by Taku Kobayashi 小林 拓

package com.taku.kobayashi.pngcamera;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.EventListener;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.internal.Utility;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

//Facebookへの認証処理
public class FacebookActionOldVersion{

	private static final String TAG = "PNGCamera_FacebookAction";
	private static final String APP_ID = FacebookConfig.APP_ID;
	private static final String[] PERMISSIONS = FacebookConfig.PERMISSIONS;
	private static final int AUTHORIZE_ACTIVITY_RESULT_CODE = 0;
	private static final int LOGIN_REQUESTCODE = 1;
	private static final String SAVE_KEY = "facebook-session";
	private static final String ACCESSTOKEN_KEY = "access_token";
	private static final String EXPIRES_KEY = "expires_in";
	private ProgressDialog m_WaitingDialog;
	private Activity m_Activity;
	private Handler m_Handler;
	private Facebook m_Facebook;
	private AsyncFacebookRunner m_AsyncFacebookRunner;

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public FacebookActionOldVersion(Activity act){
		m_Activity = act;
		m_Facebook = new Facebook(APP_ID);
		m_AsyncFacebookRunner = new AsyncFacebookRunner(m_Facebook);
		m_Handler = new Handler();
		setRecordSession();
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	private void saveSession(){
		Editor editor = m_Activity.getSharedPreferences(SAVE_KEY, Context.MODE_PRIVATE).edit();
		editor.putString(ACCESSTOKEN_KEY, m_Facebook.getAccessToken());
		editor.putLong(EXPIRES_KEY, m_Facebook.getAccessExpires());
		editor.commit();
	}

	private void setRecordSession(){
		SharedPreferences savedSession = m_Activity.getSharedPreferences(SAVE_KEY, Context.MODE_PRIVATE);
		m_Facebook.setAccessToken(savedSession.getString(ACCESSTOKEN_KEY, null));
		m_Facebook.setAccessExpires(savedSession.getLong(EXPIRES_KEY, 0));
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public void startLogin(){
		if(m_Facebook.isSessionValid() == false){
			m_Facebook.authorize(m_Activity ,PERMISSIONS ,AUTHORIZE_ACTIVITY_RESULT_CODE ,new LoginDialogListener());
		}
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public boolean isLogin(){
		return m_Facebook.isSessionValid();
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public void setLoginResult(int requestCode, int resultCode, Intent data){
		if(requestCode == AUTHORIZE_ACTIVITY_RESULT_CODE){
			m_Facebook.authorizeCallback(requestCode, resultCode, data);
		}
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public void uploadImage(File imageFile){
		if(m_Facebook.isSessionValid()){
			byte[] imageData = Tools.getImageFileToByte(imageFile, m_Activity);
			if(imageData == null){
				return;
			}
			m_WaitingDialog = ProgressDialog.show(m_Activity, "", m_Activity.getString(R.string.UploadingImageMessage), true,true);
			Bundle params = new Bundle();
			//String,byte[](とってきた写真データをbyte[]に変換する)
			params.putByteArray("photo",imageData);
			params.putString("caption",m_Activity.getPackageName());
			//写真のアップロードのリクエスト送信
			m_AsyncFacebookRunner.request("me/photos", params, "POST",new PhotoUploadListener(), null);
		}
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	private final class LoginDialogListener implements DialogListener {
		@Override
		public void onComplete(Bundle values) {
			saveSession();
		}

		@Override
		public void onFacebookError(FacebookError error) {
			Tools.showToast(m_Activity, m_Activity.getString(R.string.AuthorizationFailedMessage));
			error.printStackTrace();
		}

		@Override
		public void onError(DialogError error) {
			Tools.showToast(m_Activity, m_Activity.getString(R.string.AuthorizationFailedMessage));
			error.printStackTrace();
		}

		@Override
		public void onCancel() {
			Tools.showToast(m_Activity, m_Activity.getString(R.string.AuthorizationCancelMessage));
		}
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	private class PhotoUploadListener extends BaseRequestListener {

		@Override
		public void onComplete(final String response, final Object state) {
			m_WaitingDialog.dismiss();
			m_Handler.post(new Runnable() {
				@Override
				public void run() {
					Tools.showToast(m_Activity, m_Activity.getString(R.string.ContributeSucessMessage));
				}
			});
		}
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	private abstract class BaseRequestListener implements RequestListener{
		@Override
		public void onIOException(IOException e, Object state) {
			e.printStackTrace();
		}

		@Override
		public void onFileNotFoundException(FileNotFoundException e,Object state) {
			e.printStackTrace();
		}

		@Override
		public void onMalformedURLException(MalformedURLException e,Object state) {
			e.printStackTrace();
		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
			e.printStackTrace();
		}
	}


	//---------------------------------------------------------------------------------------------------------------------------------------------------------------
}
