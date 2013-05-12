//  Created by 拓 小林
//  Copyright (c) 2013年 TakuKobayashi All rights reserved.

package com.taku.kobayashi.pngcamera;

public class Config {
	//APIレベル8の時、Camera.CameraInfoが使えず、CameraPreViewを回転させたい時の角度の設定
	public static final int CAMERA_DEFAULT_ROTATION = 90;
	//APIレベル8の時、Camera.CameraInfoが使えず、撮影させた画像の回転角度の設定
	public static final int CAMERA_FRONT_DEFAULT_ORIENTATION = 90;
	public static final int CAMERA_BACK_DEFAULT_ORIENTATION = 270;
	//APIレベル8以下の時でインカメラを使うときのカメラID
	public static final int CAMERA_FRONT_ID = 0;
	public static final int CAMERA_BACK_ID = 1;

	//SDCard内に保存するディレクトリ構成
	public static final String DIRECTORY_NAME_TO_SAVE = "PNGCamera/";
	//SDCardに保存する画像の拡張子
	public static final String SAVE_IMG_EXTENSION_PNG = ".png";
	public static final String SAVE_IMG_EXTENSION_JPEG = ".jpg";
	//画像を保存する場合の圧縮率
	public static final int COMPRESS_QUALITY = 100;

	//アプリを動作することが可能なSDカード内の最低空き容量(10MB)
	public static final long LIMIT_MINIMAM_SPACE = 10 * 1024;

	public static final float RENDER_IMAGE_RESIZE_RATE = 0.9f;
	//Twitterに投稿できるツイート文の最大文字数(写真のURL、23文字分減らす)
	public static final int TWITTER_MAX_TEXT_COUNT = 117;

}
