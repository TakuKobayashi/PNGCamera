package com.taku.kobayashi.pngcamera;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class CameraParameterAdapter extends BaseAdapter{

	private static final String TAG = "PNGCamera_Parameter";
	private Activity m_Activity;
	private ArrayList<String> m_CameraParamsList;
	private HashMap<String,ParameterValueAdapter> m_CameraParamsValues;
	private Bundle m_CameraOptionValues;
	private Bundle m_ShowingSize;

	private List<String> m_SupportedAntibandingList;
	private List<String> m_SupportedColorEffectsList;
	private List<String> m_SupportedFlashModesList;
	private List<String> m_SupportedFocusModesList;

	public CameraParameterAdapter(Activity act){
		m_Activity = act;
		m_CameraParamsList = new ArrayList();
		m_CameraParamsValues = new HashMap<String, ParameterValueAdapter>();

		m_CameraOptionValues = new Bundle();
		m_ShowingSize = new Bundle();
		m_CameraOptionValues.
	}

	public void setParameters(Camera.Parameters cp){
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

	private void setOptionValue(int keyRes, List<String> valueList){
		String key = m_Activity.getString(keyRes);
		if(valueList.isEmpty() == false){
			for(int i = 0;i < valueList.size();i++){

			}
			ParameterValueAdapter pva = new ParameterValueAdapter(valueList);
			m_CameraParamsValues.put(key, pva);
			int res = m_Activity.getResources().getIdentifier(key, "string", m_Activity.getPackageName());
			//CameraParamsListのポジション番号を記録

			String optionsPosition = m_Activity.getString(R.string.PrefixSupportOption) + String.valueOf(m_CameraParamsList.size());
			m_CameraOptionValues.putString(optionsPosition, key);
			m_CameraParamsList.add(m_Activity.getString(res));
			//要素を記録
			m_CameraOptionValues.putStringArrayList(key, (ArrayList<String>) valueList);
		}
	}
	
	private void setKeyValues(int position){
		
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
