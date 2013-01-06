package com.taku.kobayashi.pngcamera;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventListener;
import java.util.List;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import android.widget.TextView;

public class CameraParameterAdapter extends BaseAdapter{

	private static final String TAG = "PNGCamera_Parameter";
	//Listやbundleをbooleanの型で扱おうとすると何かとめんどくさくなるので定義する
	private static final int BOOLEAN_FALSE = 0;
	private static final int BOOLEAN_TRUE = 1;
	private static final int UNSELECTING = -1;
	private Camera.Parameters m_Parameter;
	private Activity m_Activity;
	private ArrayList<String> m_CameraParamsList;
	private Bundle m_CameraParamsValues;
	private Bundle m_CameraParamsCurrentValues;
	//private HashMap<String,List<String>> m_CameraParamsValues;
	//private HashMap<String,Boolean> m_bSelected;
	private ParamsSelectListener SelectListener = null;

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public CameraParameterAdapter(Activity act){
		m_Activity = act;
		m_CameraParamsList = new ArrayList<String>();
		//m_CameraParamsValues = new HashMap<String, List<String>>();
		m_CameraParamsCurrentValues = new Bundle();
		m_CameraParamsValues = new Bundle();
		//m_bSelected = new HashMap<String, Boolean>();
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	//TODO PNG.JPEG シャッター音 オートフォーカス サイズ
	public void setParameters(Camera.Parameters cp){
		m_Parameter = cp;

		//PNG,JPEG
		setParams(R.string.SaveFormatKey,getArraysFromXml(R.array.SaveFormatValues));
		//シャッター音
		setParams(R.string.SutterSoundKey,getArraysFromXml(R.array.SutterSoundValues));
		//保存画像のサイズ
		//setParams(R.string.SavePreviewSizeKey, conversionSizeToString(cp.getSupportedPreviewSizes()));
		//カメラのカラーエフェクト
		setParams(R.string.CameraColorEffectKey,cp.getSupportedColorEffects());
		//カメラのフラッシュ
		setParams(R.string.CameraFlashModeKey,cp.getSupportedFlashModes());
		//カメラのフォーカス
		setParams(R.string.CameraFlashModeKey,cp.getSupportedFlashModes());
	}

	public void releaseParameters(){
		m_CameraParamsValues.clear();
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
			String cross = m_Activity.getString(R.string.ConnectSizeAndSize);
			String height = String.valueOf(sizeList.get(i).height);
			converted.add(width + cross + height);
		}
		return converted;
	}

	private void setParams(int keyRes, List<String> paramsList){
		String key = m_Activity.getString(keyRes);
		if(paramsList.isEmpty() == false){
			setArrayListAndSize(R.string.KeyListAccessKey, key);
			setArrayListAndSize(R.string.ShowKeyListAccessKey, m_Activity.getString(m_Activity.getResources().getIdentifier(key, "string", m_Activity.getPackageName())));
			//keyに該当する日本語のリスト保存する
			ArrayList<String> valueList = new ArrayList<String>();
			for(int i = 0;i < paramsList.size();i++){
				int value_res = m_Activity.getResources().getIdentifier(key + paramsList.get(i), "string", m_Activity.getPackageName());
				Log.d(TAG,"key:"+key+" values:"+value_res);
				if(value_res != 0){
					valueList.add(m_Activity.getString(value_res));
				}else{
					valueList.add(paramsList.get(i));
				}
			}
			m_CameraParamsValues.putStringArrayList(m_Activity.getResources().getString(R.string.SupportListPrefixKey) + key, valueList);

			//keyに該当するCameraParamasを記録
			m_CameraParamsValues.putStringArray(m_Activity.getResources().getString(R.string.ValueListPrefixKey) + key, paramsList.toArray(new String[0]));

			//m_CameraParamsValues.put(key, valueList);
			//TODO Bundleにする
			//m_bSelected.put(key, false);
		}
	}

	private void setArrayListAndSize(int res, String value){
		//keyListを保存する
		ArrayList<String> keyList = m_CameraParamsValues.getStringArrayList(m_Activity.getResources().getString(res));
		if(keyList == null){
			keyList = new ArrayList<String>();
		}
		//keyのpositionの情報を記録する
		keyList.add(value);
		m_CameraParamsValues.putStringArrayList(m_Activity.getResources().getString(res), keyList);
		//表示させるList数を保存する
		m_CameraParamsValues.putInt(m_Activity.getResources().getString(R.string.DefaultAdapterSizeKey), keyList.size());
		m_CameraParamsCurrentValues.putInt(m_Activity.getResources().getString(R.string.CurrentAdapterSizeKey), keyList.size());
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	private void recordParams(String key,String value){
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(m_Activity);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(key, value);
		editor.commit();
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public void switchSelected(int position){
		int currentPosition = m_CameraParamsCurrentValues.getInt(m_Activity.getResources().getString(R.string.CurrentSelectPositionKey), UNSELECTING);
		int defaultAdapterSize = m_CameraParamsValues.getInt(m_Activity.getResources().getString(R.string.DefaultAdapterSizeKey), 0);
		ArrayList<String> currentList = m_CameraParamsCurrentValues.getStringArrayList(m_Activity.getResources().getString(R.string.CurrentSelectSupportListKey));
		Log.d(TAG,"position:"+position+" defaultAdapterSize:"+defaultAdapterSize+" currentList:"+currentList);
		if(currentPosition == position){
			//CameraParamsのリスト
			m_CameraParamsCurrentValues.putStringArrayList(m_Activity.getResources().getString(R.string.CurrentSelectSupportListKey), new ArrayList<String>());
			//表示する項目の件数
			m_CameraParamsCurrentValues.putInt(m_Activity.getResources().getString(R.string.CurrentAdapterSizeKey), defaultAdapterSize);
			//選択されている項目の場所
			m_CameraParamsCurrentValues.putInt(m_Activity.getResources().getString(R.string.CurrentSelectPositionKey), UNSELECTING);
		}else if(currentPosition < 0 || position < currentPosition || currentPosition + currentList.size() <= position){
			ArrayList<String> keyList = m_CameraParamsValues.getStringArrayList(m_Activity.getResources().getString(R.string.KeyListAccessKey));
			String key = keyList.get(position);
			Log.d(TAG,"key:"+key);
			ArrayList<String> selectList = m_CameraParamsValues.getStringArrayList(m_Activity.getResources().getString(R.string.SupportListPrefixKey) + key);
			//CameraParamsのリスト
			Log.d(TAG, "selectList:"+selectList);
			m_CameraParamsCurrentValues.putStringArrayList(m_Activity.getResources().getString(R.string.CurrentSelectSupportListKey), selectList);
			//表示する項目の件数
			m_CameraParamsCurrentValues.putInt(m_Activity.getResources().getString(R.string.CurrentAdapterSizeKey), defaultAdapterSize + selectList.size());
			//選択されている項目の場所
			m_CameraParamsCurrentValues.putInt(m_Activity.getResources().getString(R.string.CurrentSelectPositionKey), position);
		}else{
			//TODO CameraParamsをカメラにセット
			ArrayList<String> keyList = m_CameraParamsValues.getStringArrayList(m_Activity.getResources().getString(R.string.KeyListAccessKey));
			String key = keyList.get(currentPosition);
			String[] Params = m_CameraParamsValues.getStringArray(m_Activity.getResources().getString(R.string.ValueListPrefixKey) + key);
			setParamstoCamera(key, Params[position - currentPosition]);
		}
		this.notifyDataSetChanged();
	}

	private void setParamstoCamera(String key, String Param){
		if(key == ""){
			//TODO Stringから直す(条件の設定)
		}else{
			//TODO そのままカメラにセット
		}
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	@Override
	public int getCount(){
		return m_CameraParamsCurrentValues.getInt(m_Activity.getResources().getString(R.string.CurrentAdapterSizeKey));
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	@Override
	public Object getItem(int position){
		return position;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	@Override
	public long getItemId(int position){
		return position;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		if(convertView == null){
			convertView = m_Activity.getLayoutInflater().inflate(R.layout.cameraparamsadapterview, null);
		}
		TextView paramtext = (TextView) convertView.findViewById(R.id.CameraParamsText);
		int currentPosition = m_CameraParamsCurrentValues.getInt(m_Activity.getResources().getString(R.string.CurrentSelectPositionKey), UNSELECTING);
		ArrayList<String> keyList = m_CameraParamsValues.getStringArrayList(m_Activity.getResources().getString(R.string.ShowKeyListAccessKey));
		if(currentPosition == UNSELECTING){
			convertView.setBackgroundColor(Color.argb(218, 29, 29, 29));
			paramtext.setText(keyList.get(position));
		}else{
			ArrayList<String> ShowList = m_CameraParamsValues.getStringArrayList(m_Activity.getResources().getString(R.string.SupportListPrefixKey) + keyList.get(currentPosition));
			ArrayList<String> currentList = m_CameraParamsCurrentValues.getStringArrayList(m_Activity.getResources().getString(R.string.CurrentSelectSupportListKey));
			if(currentPosition < position && position < currentPosition + currentList.size()){
				convertView.setBackgroundColor(Color.argb(0, 64, 65, 65));
				paramtext.setText(currentList.get(position - currentPosition - 1));
			}else if(currentPosition + currentList.size() <= position){
				convertView.setBackgroundColor(Color.argb(218, 29, 29, 29));
				paramtext.setText(keyList.get(position - currentList.size()));
			}else{
				convertView.setBackgroundColor(Color.argb(218, 29, 29, 29));
				paramtext.setText(keyList.get(position));
			}
		}
		return convertView;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	//TODO クリックするとCameraにParameterが入る
	/*
	private OnItemClickListener m_ListValuesListener = new OnItemClickListener(){
		@Override
		public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
			Log.d(TAG,"click"+SelectListener);
			if(SelectListener != null){
				SelectListener.selected(m_Parameter);
			}
		}

	};
	*/

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
