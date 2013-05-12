//  Created by 拓 小林
//  Copyright (c) 2013年 TakuKobayashi All rights reserved.

package com.taku.kobayashi.pngcamera;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class CameraGalleryAdapter extends BaseAdapter{

	private static final String TAG = "PNGCamera_Parameter";
	private Activity m_Activity;
	private Bitmap[] m_Images;
	private List<File> m_ImgaeFileList;

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public CameraGalleryAdapter(Activity activity) {
		m_Activity = activity;
		String path = Tools.getSDCardFolderPath() + "/" + com.taku.kobayashi.pngcamera.Config.DIRECTORY_NAME_TO_SAVE;
		File dir = new File(path);
		File[] files = dir.listFiles();
		m_ImgaeFileList = Arrays.asList(files);
		//日付が新しい順
		Collections.reverse(m_ImgaeFileList);
		m_Images = new Bitmap[m_ImgaeFileList.size()];
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	@Override
	public int getCount() {
		return m_ImgaeFileList.size();
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
			convertView = m_Activity.getLayoutInflater().inflate(R.layout.cameragallerycellview, null);
		}
		Bitmap image = Tools.getBitmap(m_Activity, Uri.fromFile(m_ImgaeFileList.get(position)));
		ImageView imageView = (ImageView) convertView.findViewById(R.id.GalleryCellImage);
		imageView.setImageBitmap(image);
		return convertView;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------
	//使用していない画像は全てreleaseしてメモリを空ける
	public void NonUsingImageRelease(int nFirstVisible,int nLastVisible){
		for(int i = 0;i < nFirstVisible;i++){
			if(m_Images[i] != null){
				m_Images[i].recycle();
				m_Images[i] = null;
			}
		}
		int nLength = m_Images.length;
		for(int i = nLastVisible;i < nLength;i++){
			if(m_Images[i] != null){
				m_Images[i].recycle();
				m_Images[i] = null;
			}
		}
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public void FileDelete(int nFileNum){
		m_ImgaeFileList.get(nFileNum).delete();
		m_ImgaeFileList.remove(nFileNum);
		this.notifyDataSetChanged();
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public File getFile(int nFileNum){
		return m_ImgaeFileList.get(nFileNum);
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public void release(){
		int nLength = m_Images.length;
		for(int i = 0;i < nLength;i++){
			if(m_Images[i] != null){
				m_Images[i].recycle();
				m_Images[i] = null;
			}
		}
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

}