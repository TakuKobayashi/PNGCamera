//  Created by 拓 小林
//  Copyright (c) 2013年 TakuKobayashi All rights reserved.

package com.taku.kobayashi.pngcamera;

public class CGSize {

	public float width;
	public float height;
	public float aspectRatio;

	public CGSize(float w, float h) {
		width = w;
		height = h;
		//アスペクト比の計算
		aspectRatio = w / h;
	}

}
