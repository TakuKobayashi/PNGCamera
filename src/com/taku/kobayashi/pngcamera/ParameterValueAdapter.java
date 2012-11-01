package com.taku.kobayashi.pngcamera;

import java.util.ArrayList;
import java.util.List;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class ParameterValueAdapter extends BaseAdapter{

	List<String> m_Values;

	public ParameterValueAdapter(List<String> Values){
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
			convertView = (TextView);
		}else{

		}
		return convertView;
	}
}
