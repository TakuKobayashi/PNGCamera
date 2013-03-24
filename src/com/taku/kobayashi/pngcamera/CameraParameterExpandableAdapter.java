//  Created by 拓 小林
//  Copyright (c) 2013年 TakuKobayashi All rights reserved.

package com.taku.kobayashi.pngcamera;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventListener;
import java.util.List;

import android.app.Activity;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class CameraParameterExpandableAdapter extends BaseExpandableListAdapter{

	private static final String TAG = "PNGCamera_Parameter";
	//Listやbundleをbooleanの型で扱おうとすると何かとめんどくさくなるので定義する
	private static final int UNSELECTING = -1;
	private Activity m_Activity;
	private Bundle m_CameraParamsValues;
	private Bundle m_CameraParamsCurrentValues;
	private ParamsSelectListener SelectListener = null;
	private int m_nCameraId = 0;

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public CameraParameterExpandableAdapter(Activity act){
		m_Activity = act;
		m_CameraParamsCurrentValues = new Bundle();
		m_CameraParamsValues = new Bundle();
	}

	public void setCameraId(int cameraId){
		m_nCameraId = cameraId;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public void setParameters(Camera camera){
		Camera.Parameters cp = camera.getParameters();
		//PNG,JPEG
		setParams(camera, commonIdParamsKey(m_Activity.getString(R.string.SaveFormatKey)), getArraysFromXml(R.array.SaveFormatValues));
		//シャッター音
		setParams(camera, commonIdParamsKey(m_Activity.getString(R.string.SutterSoundKey)), getArraysFromXml(R.array.SutterSoundValues));
		//カメラのカラーエフェクト
		setParams(camera, m_Activity.getString(R.string.CameraColorEffectKey), cp.getSupportedColorEffects());
		//カメラのフラッシュ
		setParams(camera, m_Activity.getString(R.string.CameraFlashModeKey), cp.getSupportedFlashModes());
		//カメラのホワイトバランス
		setParams(camera, m_Activity.getString(R.string.CameraWhiteBalanceKey), cp.getSupportedWhiteBalance());
		//カメラの撮影シーン
		List<String> SupportedSceneList = cp.getSupportedSceneModes();
		List<String> RemoveList =  getArraysFromXml(R.array.SceneExceptValues);
		SupportedSceneList.removeAll(RemoveList);
		setParams(camera, m_Activity.getString(R.string.CameraSceneKey), cp.getSupportedSceneModes());

		setParams(camera, m_Activity.getString(R.string.CameraPreviewSizeKey), convertSizeToString(cp.getSupportedPreviewSizes()));
		setParams(camera, m_Activity.getString(R.string.CameraFocusModeKey), cp.getSupportedFocusModes());
	}

	public void releaseParameters(){
		m_CameraParamsCurrentValues.clear();
		m_CameraParamsValues.clear();
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	//カメラIdに関係なく使用するKeyはあらかじめ登録しておく
	private String commonIdParamsKey(String key){
		ArrayList<String> keyList = m_CameraParamsValues.getStringArrayList(m_Activity.getString(R.string.ExceptSwitchIdKeyListKey));
		if(keyList == null){
			keyList = new ArrayList<String>();
		}
		//keyのpositionの情報を記録する
		keyList.add(key);
		m_CameraParamsValues.putStringArrayList(m_Activity.getString(R.string.ExceptSwitchIdKeyListKey), keyList);
		return key;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	private List<String> getArraysFromXml(int res){
		String[] strArray = m_Activity.getResources().getStringArray(res);
		return Arrays.asList(strArray);
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	private List<String> convertSizeToString(List<Size> sizeList){
		String[] result = new String[sizeList.size()];
		for(int i = 0;i < sizeList.size();i++){
			String convert = sizeList.get(i).width + m_Activity.getString(R.string.SizeConnectionWord) + sizeList.get(i).height;
			result[i] = convert;
		}
		return Arrays.asList(result);
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	private void setParams(Camera camera,String key, List<String> paramsList){
		if(paramsList != null && paramsList.isEmpty() == false){
			setArrayListAndSize(R.string.KeyListAccessKey, key);
			setArrayListAndSize(R.string.ShowKeyListAccessKey, m_Activity.getString(m_Activity.getResources().getIdentifier(key, "string", m_Activity.getPackageName())));
			//keyに該当する日本語のリスト保存する
			ArrayList<String> showValueList = new ArrayList<String>();
			ArrayList<String> valueList = new ArrayList<String>();
			for(int i = 0;i < paramsList.size();i++){
				//リソース名に-は使えない
				String processingResouceWord = paramsList.get(i).replaceAll("-", "");
				int value_res = m_Activity.getResources().getIdentifier(key + processingResouceWord, "string", m_Activity.getPackageName());
				if(value_res != 0){
					showValueList.add(m_Activity.getString(value_res));
				}else{
					showValueList.add(paramsList.get(i));
				}
				valueList.add(paramsList.get(i));
			}
			m_CameraParamsValues.putStringArrayList(m_Activity.getString(R.string.SupportListPrefixKey) + key, showValueList);

			//keyに該当するCameraParamasを記録
			m_CameraParamsValues.putStringArrayList(m_Activity.getString(R.string.ValueListPrefixKey) + key, valueList);
			ArrayList<String> commonKeyList = m_CameraParamsValues.getStringArrayList(m_Activity.getString(R.string.ExceptSwitchIdKeyListKey));
			String currentKey = key;
			if(commonKeyList.contains(key) == false){
				currentKey = currentKey + m_nCameraId;
			}
			String defaultParam = Tools.getRecordingParam(m_Activity, currentKey);
			if(defaultParam.length() > 0){
				Log.d(TAG,"default:"+defaultParam+" length:"+defaultParam.length()+" key:"+key);
				Tools.setCameraParams(m_Activity, camera, key, defaultParam);
			}else{
				Tools.recordParam(m_Activity, currentKey, paramsList.get(0));
				Tools.setCameraParams(m_Activity, camera, key, paramsList.get(0));
			}
		}
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

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

	public void switchSelected(int position){
		int currentPosition = m_CameraParamsCurrentValues.getInt(m_Activity.getResources().getString(R.string.CurrentSelectPositionKey), UNSELECTING);
		int defaultAdapterSize = m_CameraParamsValues.getInt(m_Activity.getResources().getString(R.string.DefaultAdapterSizeKey), 0);
		ArrayList<String> currentList = m_CameraParamsCurrentValues.getStringArrayList(m_Activity.getResources().getString(R.string.CurrentSelectSupportListKey));
		if(currentPosition == position){
			setCameraParamsCurrentValue(new ArrayList<String>(), new ArrayList<String>(),defaultAdapterSize, UNSELECTING);
		}else if(currentPosition < 0 || position < currentPosition){
			ArrayList<String> keyList = m_CameraParamsValues.getStringArrayList(m_Activity.getResources().getString(R.string.KeyListAccessKey));
			String key = keyList.get(position);
			ArrayList<String> paramsList = m_CameraParamsValues.getStringArrayList(m_Activity.getResources().getString(R.string.ValueListPrefixKey) + key);
			ArrayList<String> selectList = m_CameraParamsValues.getStringArrayList(m_Activity.getResources().getString(R.string.SupportListPrefixKey) + key);
			setCameraParamsCurrentValue(paramsList, selectList, defaultAdapterSize + selectList.size(), position);
		}else if(currentPosition + currentList.size() < position){
			int keyPosition = position - currentList.size();
			ArrayList<String> keyList = m_CameraParamsValues.getStringArrayList(m_Activity.getResources().getString(R.string.KeyListAccessKey));
			String key = keyList.get(keyPosition);
			ArrayList<String> paramsList = m_CameraParamsValues.getStringArrayList(m_Activity.getResources().getString(R.string.ValueListPrefixKey) + key);
			ArrayList<String> selectList = m_CameraParamsValues.getStringArrayList(m_Activity.getResources().getString(R.string.SupportListPrefixKey) + key);
			setCameraParamsCurrentValue(paramsList,selectList, defaultAdapterSize + selectList.size(), keyPosition);
		}else{
			ArrayList<String> keyList = m_CameraParamsValues.getStringArrayList(m_Activity.getResources().getString(R.string.KeyListAccessKey));
			String key = keyList.get(currentPosition);
			ArrayList<String> paramsList = m_CameraParamsValues.getStringArrayList(m_Activity.getResources().getString(R.string.ValueListPrefixKey) + key);
			if(SelectListener != null){
				SelectListener.selected(key, paramsList.get(position - currentPosition - 1));
			}
		}
		this.notifyDataSetChanged();
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	private void setCameraParamsCurrentValue(ArrayList<String> selectSupportList,ArrayList<String> selectShowList,int adapaterSize, int selectPosition){
		m_CameraParamsCurrentValues.putStringArrayList(m_Activity.getResources().getString(R.string.CurrentSelectParamListKey), selectSupportList);
		//CameraParamsのリスト
		m_CameraParamsCurrentValues.putStringArrayList(m_Activity.getResources().getString(R.string.CurrentSelectSupportListKey), selectShowList);
		//表示する項目の件数
		m_CameraParamsCurrentValues.putInt(m_Activity.getString(R.string.CurrentAdapterSizeKey), adapaterSize);
		//選択されている項目の場所
		m_CameraParamsCurrentValues.putInt(m_Activity.getString(R.string.CurrentSelectPositionKey), selectPosition);
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public void setSelectParam(int groupPosition, int childPosition){
		ArrayList<String> keyList = m_CameraParamsValues.getStringArrayList(m_Activity.getResources().getString(R.string.KeyListAccessKey));
		String key = keyList.get(groupPosition);
		ArrayList<String> paramsList = m_CameraParamsValues.getStringArrayList(m_Activity.getResources().getString(R.string.ValueListPrefixKey) + key);
		ArrayList<String> commonKeyList = m_CameraParamsValues.getStringArrayList(m_Activity.getString(R.string.ExceptSwitchIdKeyListKey));
		String currentKey = key;
		if(commonKeyList.contains(key) == false){
			currentKey = currentKey + m_nCameraId;
		}
		Tools.recordParam(m_Activity, currentKey, paramsList.get(childPosition));
		if(SelectListener != null && key != null){
			SelectListener.selected(key, paramsList.get(childPosition));
		}
		this.notifyDataSetChanged();
	}

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

	public interface ParamsSelectListener extends EventListener {

		public void selected(String key,String value);

	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		ArrayList<String> keyList = m_CameraParamsValues.getStringArrayList(m_Activity.getResources().getString(R.string.KeyListAccessKey));
		String key = keyList.get(groupPosition);
		ArrayList<String> paramsList = m_CameraParamsValues.getStringArrayList(m_Activity.getResources().getString(R.string.ValueListPrefixKey) + key);
		return paramsList.get(childPosition);
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	@Override
	public View getChildView(int groupPosition, int childPosition,boolean isLastChild, View convertView, ViewGroup parent) {
		ArrayList<String> keyList = m_CameraParamsValues.getStringArrayList(m_Activity.getString(R.string.KeyListAccessKey));
		String key = keyList.get(groupPosition);
		ArrayList<String> selectList = m_CameraParamsValues.getStringArrayList(m_Activity.getString(R.string.SupportListPrefixKey) + key);
		ArrayList<String> paramsList = m_CameraParamsValues.getStringArrayList(m_Activity.getString(R.string.ValueListPrefixKey) + key);
		ArrayList<String> exceptIdList = m_CameraParamsValues.getStringArrayList(m_Activity.getString(R.string.ExceptSwitchIdKeyListKey));
		String currentKey = key;
		if(exceptIdList.contains(key) == false){
			currentKey = currentKey + m_nCameraId;
		}
		String record = Tools.getRecordingParam(m_Activity, currentKey);
		convertView = m_Activity.getLayoutInflater().inflate(R.layout.cameraparamsvaluecellview, null);
		convertView.setMinimumHeight(ExtraLayout.getListCellMinHeight(m_Activity));
		TextView paramtext = (TextView) convertView.findViewById(R.id.CameraParamsText);
		paramtext.setText(selectList.get(childPosition));
		if(paramsList.get(childPosition).equals(record)){
			convertView.setBackgroundColor(Color.argb(218, 243, 153, 32));
		}else{
			convertView.setBackgroundColor(Color.argb(0, 0, 0, 0));
		}
		return convertView;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	@Override
	public boolean areAllItemsEnabled(){
		return true;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	@Override
	public int getChildrenCount(int groupPosition) {
		ArrayList<String> keyList = m_CameraParamsValues.getStringArrayList(m_Activity.getResources().getString(R.string.KeyListAccessKey));
		String key = keyList.get(groupPosition);
		ArrayList<String> selectList = m_CameraParamsValues.getStringArrayList(m_Activity.getResources().getString(R.string.SupportListPrefixKey) + key);
		return selectList.size();
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	@Override
	public Object getGroup(int groupPosition) {
		//CameraParamsのKey
		ArrayList<String> keyList = m_CameraParamsValues.getStringArrayList(m_Activity.getResources().getString(R.string.KeyListAccessKey));
		return keyList.get(groupPosition);
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	@Override
	public int getGroupCount() {
		return m_CameraParamsValues.getStringArrayList(m_Activity.getString(R.string.KeyListAccessKey)).size();
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		//Groupの日本語のString
		ArrayList<String> showList = m_CameraParamsValues.getStringArrayList(m_Activity.getResources().getString(R.string.ShowKeyListAccessKey));
		convertView = m_Activity.getLayoutInflater().inflate(R.layout.cameraparamskeylistcellview, null);
		convertView.setMinimumHeight(ExtraLayout.getListCellMinHeight(m_Activity));
		convertView.setBackgroundColor(Color.argb(218, 29, 29, 29));
		TextView paramtext = (TextView) convertView.findViewById(R.id.CameraParamsText);
		paramtext.setText(showList.get(groupPosition));
		return convertView;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	@Override
	public boolean hasStableIds() {
		return true;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

}
