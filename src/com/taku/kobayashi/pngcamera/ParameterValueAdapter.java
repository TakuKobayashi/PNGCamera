package com.taku.kobayashi.pngcamera;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ParameterValueAdapter extends BaseAdapter{

	private static final String TAG = "PNGCamera_ParameterValueAdapter";
	private Activity m_Activity;
	private ArrayList<String> m_Values;

	public ParameterValueAdapter(Activity act,ArrayList<String> Values){
		m_Activity = act;
		m_Values = Values;
		Log.d(TAG,"values:"+Values+" size:"+m_Values.size());
	}

	@Override
	public int getCount() {
		return m_Values.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null){
			convertView = m_Activity.getLayoutInflater().inflate(R.layout.parametervalueadapterview, null);
		}
		TextView parameterValueText= (TextView) convertView.findViewById(R.id.ParameterValueText);
		parameterValueText.setText(m_Values.get(position));
		Log.d(TAG,"position:"+position+" value:"+m_Values.get(position));
		return convertView;
	}

	public String getValue(int position){
		return m_Values.get(position);
	}
}
