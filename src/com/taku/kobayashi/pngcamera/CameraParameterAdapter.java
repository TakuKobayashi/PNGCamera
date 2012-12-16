package com.taku.kobayashi.pngcamera;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class CameraParameterAdapter extends BaseAdapter{

	private static final String TAG = "PNGCamera_Parameter";
	private Camera.Parameters m_Parameter;
	private Activity m_Activity;
	private ArrayList<String> m_CameraParamsList;
	private Bundle m_CameraParamsValues;
	//private HashMap<String,List<String>> m_CameraParamsValues;
	private HashMap<String,Boolean> m_bSelected;
	private ParamsSelectListener SelectListener = null;

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public CameraParameterAdapter(Activity act){
		m_Activity = act;
		m_CameraParamsList = new ArrayList<String>();
		//m_CameraParamsValues = new HashMap<String, List<String>>();
		m_CameraParamsValues = new Bundle();
		m_bSelected = new HashMap<String, Boolean>();
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	//TODO PNG.JPEG シャッター音 オートフォーカス サイズ
	public void setParameters(Camera.Parameters cp){
		m_Parameter = cp;

		//PNG,JPEG
		setOptionValue(R.string.SaveFormatKey,getArraysFromXml(R.array.SaveFormatValues));
		//シャッター音
		setOptionValue(R.string.SutterSoundKey,getArraysFromXml(R.array.SutterSoundValues));
		//保存画像のサイズ
		setOptionValue(R.string.SutterSoundKey,getArraysFromXml(R.array.SutterSoundValues));
		//カメラのカラーエフェクト
		setOptionValue(R.string.CameraColorEffectKey,cp.getSupportedColorEffects());
		//カメラのフラッシュ
		setOptionValue(R.string.CameraFlashModeKey,cp.getSupportedFlashModes());
		//カメラのフォーカス
		setOptionValue(R.string.CameraFlashModeKey,cp.getSupportedFlashModes());
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	private List<String> getArraysFromXml(int res){
		String[] strArray = m_Activity.getResources().getStringArray(res);
		return Arrays.asList(strArray);
	}

	private List<String> conversionSizeToString(List<Size> sizeList){
		ArrayList<String> converted = new ArrayList<String>();
		for(int i = 0;i < sizeList.size();i++){
			//TODO 縦横によってサイズを絞り込む
			String width = String.valueOf(sizeList.get(i).width);
			//converted.add(object);
		}
		return null;
	}


	private void setOptionValue(int keyRes, List<String> paramsList){
		String key = m_Activity.getString(keyRes);
		if(paramsList.isEmpty() == false){
			Log.d(TAG, "params:"+paramsList);
			ArrayList<String> keyList = m_CameraParamsValues.getStringArrayList(m_Activity.getResources().getString(R.string.CameraParameterKeyListKey));
			keyList.add(key);
			m_CameraParamsValues.putStringArrayList(key, keyList);

			//m_CameraParamsList.add(key);
			m_CameraParamsValues.putInt(m_Activity.getResources().getString(R.string.AdapterCurrentListSizeKey), m_CameraParamsList.size());
			//keyに該当する日本語のリスト
			ArrayList<String> valueList = new ArrayList<String>();
			for(int i = 0;i < paramsList.size();i++){
				int value_res = m_Activity.getResources().getIdentifier(key + paramsList.get(i), "string", m_Activity.getPackageName());
				if(value_res != 0){
					valueList.add(m_Activity.getString(value_res));
				}else{
					valueList.add(paramsList.get(i));
				}
			}
			Log.d(TAG, "cameraParams:"+valueList);
			m_CameraParamsValues.putStringArrayList(key, valueList);
			//m_CameraParamsValues.put(key, valueList);
			//TODO Bundleにする
			m_bSelected.put(key, false);
		}
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	private void recordParams(String key,String value){
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(m_Activity);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(key, value);
		editor.commit();
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	private void recordSupportedParams(String key,Set<String> params){
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(m_Activity);
		SharedPreferences.Editor editor = settings.edit();
		editor.commit();
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public void switchSelected(int position){
		String key = m_CameraParamsList.get(position);
		ArrayList<String> setList = m_CameraParamsValues.getStringArrayList(key);
		ArrayList<String> currentList = m_CameraParamsValues.getStringArrayList(m_Activity.getResources().getString(R.string.AdapterCurrentSelectListKey));
		if(setList.equals(currentList) == true){

		}else{

		}
		m_CameraParamsValues.putStringArrayList(key, value);
		if(m_bSelected.get(key) == false){
			m_bSelected.put(key, true);
		}else{
			m_bSelected.put(key, false);
		}
		this.notifyDataSetChanged();
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	@Override
	public int getCount() {
		return m_CameraParamsValues.getInt(m_Activity.getResources().getString(R.string.AdapterCurrentListSizeKey));
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	@Override
	public Object getItem(int position) {
		return position;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	@Override
	public long getItemId(int position) {
		return position;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null){
			convertView = m_Activity.getLayoutInflater().inflate(R.layout.cameraparamsadapterview, null);
		}
		String key = m_CameraParamsList.get(position);
		int res = m_Activity.getResources().getIdentifier(key, "string", m_Activity.getPackageName());
		//valueはxml内で定義された該当する日本語
		String value = null;
		if(res != 0){
			value = m_Activity.getString(res);
		}else{
			value = key;
		}
		TextView paramtext = (TextView) convertView.findViewById(R.id.CameraParamsText);
		paramtext.setText(value);
		ListView valueListView = (ListView) convertView.findViewById(R.id.ParamsValueList);
		valueListView.setAdapter(m_CameraParamsValues.get(key));
		valueListView.setOnItemClickListener(m_ListValuesListener);
		valueListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

		if(m_bSelected.get(key) == false){
			valueListView.setVisibility(View.GONE);
		}else{
			valueListView.setVisibility(View.VISIBLE);
		}

		return convertView;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	//TODO クリックするとCameraにParameterが入る
	private OnItemClickListener m_ListValuesListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
			Log.d(TAG,"click"+SelectListener);
			if(SelectListener != null){
				SelectListener.selected(m_Parameter);
			}
		}

	};

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * リスナーを追加する
	 */
	public void setOnParamsSelectListener(ParamsSelectListener listener){
		this.SelectListener = listener;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * リスナーを削除する
	 */
	public void removeListener(){
		this.SelectListener = null;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------


	//検出処理が終わったことを通知する独自のリスナーを作成
	public interface ParamsSelectListener extends EventListener {

		//検出処理が終了したことを通知する
		public void selected(Camera.Parameters params);

	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------


}
