//  Created by 拓 小林
//  Copyright (c) 2013年 TakuKobayashi All rights reserved.

package com.taku.kobayashi.pngcamera;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class CameraGalleryAdapter extends BaseAdapter{

	private static final String TAG = "PNGCamera_Parameter";
	private Context m_Context;
	private Bitmap[] m_Images;
	private List<File> m_ImgaeFileList;

	public CameraGalleryAdapter(Context context) {
		m_Context = context;
		String path = Tools.getSDCardFolderPath() + "/" + com.taku.kobayashi.pngcamera.Config.DIRECTORY_NAME_TO_SAVE;
		File dir = new File(path);
		File[] files = dir.listFiles();
		m_ImgaeFileList = Arrays.asList(files);
		m_Images = new Bitmap[m_ImgaeFileList.size()];
	}

	@Override
	public int getCount() {
		return m_ImgaeFileList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO 自動生成されたメソッド・スタブ
		return 0;
	}

	@Override
	public View getView(int position, View arg1, ViewGroup arg2) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}


}