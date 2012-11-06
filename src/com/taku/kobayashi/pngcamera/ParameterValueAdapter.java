package com.taku.kobayashi.pngcamera;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ParameterValueAdapter extends BaseAdapter{

	Activity m_Activity;
	ArrayList<String> m_Values;

	public ParameterValueAdapter(Activity act,ArrayList<String> Values){
		m_Activity = act;
		m_Values = Values;
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

		return convertView;
	}

	public String getValue(int position){
		return m_Values.get(position);
	}
}
