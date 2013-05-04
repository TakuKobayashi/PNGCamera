//  Created by Taku Kobayashi 小林 拓

package com.taku.kobayashi.pngcamera;

import java.io.File;
import java.util.Arrays;
import java.util.EventListener;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.internal.Utility;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;


//Facebookへの認証処理
public class FacebookAction{

	private static final String TAG = "PNGCamera_FacebookAction";
	private static final String APP_ID = FacebookConfig.APP_ID;
	private static final String[] PERMISSIONS = {"offline_access","user_photos","photo_upload"};
	private static final int LOGIN_REQUESTCODE = 1;
	private Activity m_Activity;
	private Session m_Session;

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public FacebookAction(Activity act){
		m_Activity = act;
		Session.Builder sb = new Session.Builder(act);
		sb.setApplicationId(APP_ID);
		m_Session = sb.build();
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	private Session.OpenRequest settingSessionRequest(){
		Session.OpenRequest so = new Session.OpenRequest(m_Activity);
		so.setPermissions(Arrays.asList(PERMISSIONS));
		so.setRequestCode(LOGIN_REQUESTCODE);
		so.setCallback(m_SessionStatusCallback);
		so.setIsLegacy(true);
		return so;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public void startLogin(){
		Utility.clearFacebookCookies(m_Activity);
		m_Session.openForRead(this.settingSessionRequest());
		Log.d(TAG, "at:"+m_Session.getAccessToken()+" b:"+m_Session.getAuthorizationBundle());
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public void setLoginResult(int requestCode, int resultCode, Intent data){
		Log.d(TAG, "request:"+ requestCode +" result:"+ resultCode +"data:"+data.getExtras());
		//Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	private Session.StatusCallback m_SessionStatusCallback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state, Exception exception) {
			Log.d(TAG, "session:"+session);
			Log.d(TAG, "ac:"+session.getAccessToken()+" ap:"+session.getAuthorizationBundle()+"st:"+state);
			Log.d(TAG, "open:"+session.isOpened()+" close:"+ session.isClosed() +"st:"+session.getState()+"as:"+session.getActiveSession());
		}
	};

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------
}
