package com.taku.kobayashi.pngcamera;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class CameraParameterAdapter extends BaseAdapter{

	private static final String TAG = "PNGCamera_Parameter";
	private Context m_Context;
	private ArrayList<String> m_CameraOptionList;
	private Bundle m_CameraOptionValues;
	private Bundle m_ShowingSize;

	private List<String> m_SupportedAntibandingList;
	private List<String> m_SupportedColorEffectsList;
	private List<String> m_SupportedFlashModesList;
	private List<String> m_SupportedFocusModesList;


	public CameraParameterAdapter(Context con){
		m_Context = con;
		m_CameraOptionList = new ArrayList();
		m_CameraOptionValues = new Bundle();
		m_ShowingSize = new Bundle();
	}

	public void setParameters(Camera.Parameters cp){
		String supportParam = cp.flatten();
		Log.d(TAG,supportParam);
		String[] params = supportParam.split(",");
		for(int i = 0;i < params.length;i++){
			Log.d(TAG,params[i]);
		}
		cp.getSupportedAntibanding();


		//カメラのカラーエフェクト
		setOptionValue(R.string.CameraColorEffectKey,cp.getSupportedColorEffects());
		//カメラのフラッシュ
		setOptionValue(R.string.CameraFlashModeKey,cp.getSupportedFlashModes());
		//カメラのフォーカス
		setOptionValue(R.string.CameraFlashModeKey,cp.getSupportedFlashModes());
		cp.getSupportedFocusModes();

		cp.getSupportedFocusModes();
		cp.getSupportedJpegThumbnailSizes();
	}

	private void setOptionValue(int keyRes, List valueList){
		String key = m_Context.getString(keyRes);
		if(valueList.isEmpty() == false){
			int res = m_Context.getResources().getIdentifier(key, "string", m_Context.getPackageName());
			//CameraParamsListのポジション番号を記録
			String optionsPosition = m_Context.getString(R.string.PrefixSupportOption) + String.valueOf(m_CameraOptionList.size());
			m_CameraOptionValues.putString(optionsPosition, key);
			m_CameraOptionList.add(m_Context.getString(res));
			//要素を記録
			m_CameraOptionValues.putStringArrayList(key, (ArrayList<String>) valueList);
		}
	}

	public void showSupportOptions(int position){

		int currentPosition = position;
		Set<String> keys = m_ShowingSize.keySet();
		for(String key : keys){
			currentPosition = currentPosition - m_ShowingSize.getInt(key);
		}
		ArrayList supportList = m_CameraOptionValues.getStringArrayList(key);

	}

	public void setCameraOption(int position){

	}

	//選択された場所を正確に取り出すために計算する
	private void controllPostion(int position){

	}

	private void recordParams(String key,String value){
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(m_Context);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(key, value);
		editor.commit();
	}

	private void recordSupportedParams(String key,Set<String> params){
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(m_Context);
		SharedPreferences.Editor editor = settings.edit();
		editor.putStringSet(key, params);
		editor.commit();
	}

	private Set<String> getRecordSupportedParams(String key){
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(m_Context);
		return settings.getStringSet(key, null);
	}

	private String getRecordValue(String key){
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(m_Context);
		return settings.getString(key, null);
	}

	@Override
	public int getCount() {
		return 0;
	}

	@Override
	public Object getItem(int arg0) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO 自動生成されたメソッド・スタブ
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}
}
