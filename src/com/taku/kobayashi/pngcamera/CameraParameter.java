package com.taku.kobayashi.pngcamera;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.support.v4.app.NavUtils;

public class CameraParameter {

	private static final String TAG = "PNGCamera_Parameter";
	private Activity m_Activity;

	private List<String> m_SupportedAntibandingList;
	private List<String> m_SupportedColorEffectsList;
	private List<String> m_SupportedFlashModesList;
	private List<String> m_SupportedFocusModesList;

	public CameraParameter(Activity act){
		m_Activity = act;
	}

	public void setCameraParams(Camera.Parameters cp){
		String supportParam = cp.flatten();
		Log.d(TAG,supportParam);
		String[] params = supportParam.split(",");
		for(int i = 0;i < params.length;i++){
			Log.d(TAG,params[i]);
		}
		cp.getSupportedAntibanding();
		cp.getSupportedColorEffects();
		cp.getSupportedFlashModes();
		cp.getSupportedFocusModes();
		cp.getSupportedJpegThumbnailSizes();
	}

	private void recordParams(String key,String value){
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(m_Activity);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(key, value);
		editor.commit();
	}

	private void recordSupportedParams(String key,Set<String> params){
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(m_Activity);
		SharedPreferences.Editor editor = settings.edit();
		editor.putStringSet(key, params);
		editor.commit();
	}

	private Set<String> getRecordSupportedParams(String key){
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(m_Activity);
		return settings.getStringSet(key, null);
	}

	private String getRecordValue(String key){
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(m_Activity);
		return settings.getString(key, null);
	}
}
