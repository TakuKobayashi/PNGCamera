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
import android.widget.BaseAdapter;
import android.widget.RadioButton;
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
	public void setParameters(Camera camera){
		Camera.Parameters cp = camera.getParameters();
		String key = null;
		//PNG,JPEG
		setParams(camera, m_Activity.getString(R.string.SaveFormatKey), getArraysFromXml(R.array.SaveFormatValues));
		//シャッター音
		setParams(camera, m_Activity.getString(R.string.SutterSoundKey), getArraysFromXml(R.array.SutterSoundValues));
		//保存画像のサイズ
		//setParams(R.string.SavePreviewSizeKey, conversionSizeToString(cp.getSupportedPreviewSizes()));
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
	}

	private void setDefaultValue(String key,String defaultValue){
		String recordValue = Tools.getRecordParam(m_Activity, key);
		if(recordValue == null){
			Tools.recordParams(m_Activity, key, defaultValue);
		}
	}

	public void releaseParameters(){
		m_CameraParamsCurrentValues.clear();
		m_CameraParamsValues.clear();
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	private List<String> getArraysFromXml(int res){
		String[] strArray = m_Activity.getResources().getStringArray(res);
		return Arrays.asList(strArray);
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

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

	private void setParamsWithCameraParams(String key, Camera.Parameters cp, List<String> paramsList){

	}

	private void setParams(Camera camera,String key, List<String> paramsList){
		if(paramsList.isEmpty() == false){
			setArrayListAndSize(R.string.KeyListAccessKey, key);
			setArrayListAndSize(R.string.ShowKeyListAccessKey, m_Activity.getString(m_Activity.getResources().getIdentifier(key, "string", m_Activity.getPackageName())));
			//keyに該当する日本語のリスト保存する
			ArrayList<String> showValueList = new ArrayList<String>();
			ArrayList<String> valueList = new ArrayList<String>();
			for(int i = 0;i < paramsList.size();i++){
				//リソース名に-は使えない
				String processingResouceWord = paramsList.get(i).replaceAll("-", "");
				int value_res = m_Activity.getResources().getIdentifier(key + processingResouceWord, "string", m_Activity.getPackageName());
				Log.d(TAG,"key:"+key+" values:"+value_res);
				if(value_res != 0){
					showValueList.add(m_Activity.getString(value_res));
				}else{
					showValueList.add(paramsList.get(i));
				}
				valueList.add(paramsList.get(i));
			}
			m_CameraParamsValues.putStringArrayList(m_Activity.getResources().getString(R.string.SupportListPrefixKey) + key, showValueList);

			//keyに該当するCameraParamasを記録
			m_CameraParamsValues.putStringArrayList(m_Activity.getResources().getString(R.string.ValueListPrefixKey) + key, valueList);
			String defaultParam = Tools.getRecordParam(m_Activity, key);
			if(defaultParam != null){
				Tools.setCameraParams(m_Activity, camera, key, defaultParam);
			}
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
			//TODO CameraParamsをカメラにセット
			ArrayList<String> keyList = m_CameraParamsValues.getStringArrayList(m_Activity.getResources().getString(R.string.KeyListAccessKey));
			String key = keyList.get(currentPosition);
			ArrayList<String> paramsList = m_CameraParamsValues.getStringArrayList(m_Activity.getResources().getString(R.string.ValueListPrefixKey) + key);
			ArrayList<String> selectList = m_CameraParamsValues.getStringArrayList(m_Activity.getResources().getString(R.string.SupportListPrefixKey) + key);
			Tools.recordParams(m_Activity, key, paramsList.get(position - currentPosition - 1));
			Tools.recordParams(m_Activity, m_Activity.getString(R.string.RecordShowPrefixKey) + key, selectList.get(position - currentPosition - 1));
			if(SelectListener != null){
				SelectListener.selected(key, paramsList.get(position - currentPosition - 1));
			}
		}
		this.notifyDataSetChanged();
	}

	private void setCameraParamsCurrentValue(ArrayList<String> selectSupportList,ArrayList<String> selectShowList,int adapaterSize, int selectPosition){
		m_CameraParamsCurrentValues.putStringArrayList(m_Activity.getResources().getString(R.string.CurrentSelectParamListKey), selectSupportList);
		//CameraParamsのリスト
		m_CameraParamsCurrentValues.putStringArrayList(m_Activity.getResources().getString(R.string.CurrentSelectSupportListKey), selectShowList);
		//表示する項目の件数
		m_CameraParamsCurrentValues.putInt(m_Activity.getString(R.string.CurrentAdapterSizeKey), adapaterSize);
		//選択されている項目の場所
		m_CameraParamsCurrentValues.putInt(m_Activity.getString(R.string.CurrentSelectPositionKey), selectPosition);
	}

	private void setParamstoCamera(String key, String Param){
		Log.d(TAG, "selectParam:"+Param);
		if(key == ""){
			//TODO Stringから直す(条件の設定)
		}else{
			//TODO そのままカメラにセット
			if(SelectListener != null){
			}
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
		int currentPosition = m_CameraParamsCurrentValues.getInt(m_Activity.getResources().getString(R.string.CurrentSelectPositionKey), UNSELECTING);
		ArrayList<String> showList = m_CameraParamsValues.getStringArrayList(m_Activity.getResources().getString(R.string.ShowKeyListAccessKey));
		if(currentPosition == UNSELECTING){
			convertView = m_Activity.getLayoutInflater().inflate(R.layout.cameraparamskeylistcellview, null);
			convertView.setMinimumHeight(ExtraLayout.getListCellMinHeight(m_Activity));
			convertView.setBackgroundColor(Color.argb(218, 29, 29, 29));
			TextView paramtext = (TextView) convertView.findViewById(R.id.CameraParamsText);
			paramtext.setText(showList.get(position));
		}else{
			ArrayList<String> currentList = m_CameraParamsCurrentValues.getStringArrayList(m_Activity.getResources().getString(R.string.CurrentSelectSupportListKey));
			if(currentPosition < position && position <= currentPosition + currentList.size()){
				convertView = m_Activity.getLayoutInflater().inflate(R.layout.cameraparamsvaluecellview, null);
				convertView.setMinimumHeight(ExtraLayout.getListCellMinHeight(m_Activity));
				TextView paramtext = (TextView) convertView.findViewById(R.id.CameraParamsText);
				convertView.setBackgroundColor(Color.argb(0, 0, 0, 0));
				String word = currentList.get(position - currentPosition - 1);
				paramtext.setText(word);
				ArrayList<String> keyList = m_CameraParamsValues.getStringArrayList(m_Activity.getResources().getString(R.string.KeyListAccessKey));
				String recordValue = Tools.getRecordParam(m_Activity, m_Activity.getString(R.string.RecordShowPrefixKey) + keyList.get(currentPosition));
				RadioButton selectButton = (RadioButton) convertView.findViewById(R.id.CameraRadioButton);
				if(word.equals(recordValue) == true){
					selectButton.setChecked(true);
				}else{
					selectButton.setChecked(false);
				}
			}else if(currentPosition + currentList.size() < position){
				String word = showList.get(position - currentList.size());
				convertView = m_Activity.getLayoutInflater().inflate(R.layout.cameraparamskeylistcellview, null);
				convertView.setMinimumHeight(ExtraLayout.getListCellMinHeight(m_Activity));
				convertView.setBackgroundColor(Color.argb(218, 29, 29, 29));
				TextView paramtext = (TextView) convertView.findViewById(R.id.CameraParamsText);
				paramtext.setText(word);
			}else{
				String word = showList.get(position);
				convertView = m_Activity.getLayoutInflater().inflate(R.layout.cameraparamskeylistcellview, null);
				convertView.setMinimumHeight(ExtraLayout.getListCellMinHeight(m_Activity));
				convertView.setBackgroundColor(Color.argb(218, 29, 29, 29));
				TextView paramtext = (TextView) convertView.findViewById(R.id.CameraParamsText);
				paramtext.setText(word);
			}
		}
		return convertView;
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


}
