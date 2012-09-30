package com.taku.kobayashi.pngcamera;

import java.util.HashMap;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;

public class ImageFilter{

	private static final String TAG = "Lisa_MakeFilter";
	//private FilterCalcuration m_FilterCalcuration;
	private Bitmap m_OrgImage;

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public ImageFilter(){
		//m_OrgImage = org;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	//輝度（明るさ）調整
	public Bitmap BrightenFilter(Bitmap image,float value){
		ColorMatrix cm = new ColorMatrix();
		//CIExposureAdjustの演算処理は「s.rgb * pow(2.0, ev)」のため。処理時のevは露出パラメータ
		float scale = (float) Math.pow(2, value);
		//明度調整演算パラメータ
		cm.set(new float[] {
			scale, 0, 0, 0, 0,
			0, scale, 0, 0, 0,
			0, 0, scale, 0, 0,
			0, 0, 0, 1, 0 });
		return ImageColorFilter(image,new ColorMatrixColorFilter(cm));
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public Bitmap AlphaFilter(Bitmap image,float value){
		ColorMatrix cm = new ColorMatrix();
		cm.set(new float[] {
			1, 0, 0, 0, 0,
			0, 1, 0, 0, 0,
			0, 0, 1, 0, 0,
			0, 0, 0, value});
		return ImageColorFilter(image,new ColorMatrixColorFilter(cm));
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	//ネガ：色反転
	public Bitmap InvertFilter(Bitmap image){
		ColorMatrix cm = new ColorMatrix();
		cm.set(new float[]{
				-1	,0	,0	,0,255,
				0	,-1	,0	,0,255,
				0	,0	,-1	,0,255,
				0	,0	,0	,1,0
		});
		return ImageColorFilter(image,new ColorMatrixColorFilter(cm));
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	//ポスタリゼーション(階調)をかける(256段階の色の強さを指定した段階の値にあてはめた色にする)
	// #### Posterize [1, 255]
	public Bitmap PosterizeFilter(Bitmap image,float value){
		float p = (float) clamp(value, 1, 255);
		int step = (int) Math.floor(255 / p);
		int width = image.getWidth();
		int height = image.getHeight();
		int[] pixels = new int[width * height];
		image.getPixels(pixels, 0, width, 0, 0, width, height);
		//メモリから解放させる
		image.recycle();
		image = null;
		for(int y = 0;y < height;y++){
			for(int x = 0;x < width;x++){
				int color = pixels[y * width + x];
				pixels[y * width + x] = Color.rgb((int)(Math.floor(Color.red(color) / step) * step),(int)(Math.floor(Color.green(color) / step) * step),(int)(Math.floor(Color.blue(color) / step) * step));
			}
		}
		Bitmap newImage = Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888);
		//メモリから解放させる
		pixels = null;
		System.gc();
		return newImage;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	//ガンマ補正
	public Bitmap GammaFilter(Bitmap image,float value){
		int width = image.getWidth();
		int height = image.getHeight();
		int[] pixels = new int[width * height];
		image.getPixels(pixels, 0, width, 0, 0, width, height);
		//メモリから解放させる
		image.recycle();
		image = null;
		for(int y = 0;y < height;y++){
			for(int x = 0;x < width;x++){
				int color = pixels[y * width + x];
				pixels[y * width + x] = Color.rgb((int)(Math.pow(Color.red(color),value)),(int)(Math.pow(Color.green(color),value)),(int)(Math.pow(Color.blue(color),value)));
			}
		}
		Bitmap newImage = Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888);
		//メモリから解放させる
		pixels = null;
		System.gc();
		return newImage;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	//セピア:セピア色…黒色がかった茶色
	public Bitmap SepiaFilter(Bitmap image){
		ColorMatrix cm = new ColorMatrix();
		cm.set(new float[] {
				0.393f	,0.769f	,0.189f	,0	,0,
				0.349f	,0.686f	,0.168f	,0	,0,
				0.272f	,0.534f	,0.131f	,0	,0,
				0		,0		,0		,1	,0});
		return ImageColorFilter(image,new ColorMatrixColorFilter(cm));
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	//彩度調整
	public Bitmap SaturationFilter(Bitmap image,float value){
		ColorMatrix cm = new ColorMatrix();
		cm.setSaturation(value);
		return ImageColorFilter(image,new ColorMatrixColorFilter(cm));
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	//photoshopのシャープマスクをかける(輪郭がくっきりする処理)
	//※処理前と処理後では違いがわかりにくい
	public Bitmap SharpenFilter(Bitmap image){
		float[][] value = {
		{0.0f, -0.2f,  0.0f}
		,{-0.2f, 1.8f, -0.2f}
		,{0.0f, -0.2f,  0.0f}
		};
		return convolve(image, value);
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	/*
	public ColorMatrixColorFilter ContrastFilter(float contrast) {
		ColorMatrix cm = new ColorMatrix();
		float scale = contrast + 1.f;
		float translate = (-.5f * scale + .5f) * 255.f;
		cm.set(new float[] {
			scale	, 0		, 0		, 0, translate,
			0		, scale	, 0		, 0, translate,
			0		, 0		, scale	, 0, translate,
			0		, 0		, 0		, 1, 0 });
		ColorMatrixColorFilter filter = new ColorMatrixColorFilter(cm);
		return filter;
	}
	*/

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	/*
	// #### Constrast [-100, 100]
	Filtrr2.fx("contrast", function(p) {
		p = Filtrr2.Util.normalize(p, 0, 2, -100, 100);
		function c(f, c){
			return (f - 0.5) * c + 0.5;
		}
		this.process(function(rgba) {
			rgba.r = 255 * c(rgba.r / 255, p);
			rgba.g = 255 * c(rgba.g / 255, p);
			rgba.b = 255 * c(rgba.b / 255, p);
		});
	});
	*/

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public Bitmap ContrastFilter(Bitmap image,float contrast) {
		ColorMatrix cm = new ColorMatrix();
		//float scale = -.5f * contrast + .5f;
		float scale = contrast;
		float translate = (-.5f * scale + .5f) * 255.f;
		cm.set(new float[] {
			scale	, 0		, 0		, 0, 0,
			0		, scale	, 0		, 0, 0,
			0		, 0		, scale	, 0, 0,
			0		, 0		, 0		, 1, 0 });
		return ImageColorFilter(image,new ColorMatrixColorFilter(cm));
	}

	public Bitmap ContrastPixelsFilter(Bitmap image){
		ColorMatrix cm = new ColorMatrix();
		cm.set(new float[] {
			(1f/275)*255	, 0		, 0		, 0, ((120f /275) + 0.1f)*255,
			0		, (4f/675)*255	, 0		, 0, ((-140f/675)+0.1f)*255,
			0		, 0		, (1f/275)*255	, 0, ((120f /275) + 0.1f)*255,
			0		, 0		, 0		, 1, 0 });
		return ImageColorFilter(image,new ColorMatrixColorFilter(cm));
		/*
		int width = image.getWidth();
		int height = image.getHeight();
		int[] pixels = new int[width * height];
		image.getPixels(pixels, 0, width, 0, 0, width, height);
		//メモリから解放させる
		image.recycle();
		image = null;
		for(int y = 0;y < height;y++){
			for(int x = 0;x < width;x++){
				int color = pixels[y * width + x];
				int r = (int)(Contrast( ( Color.red(color) - 10 / 255 ) * ( 255 / ( 230 - 10 ) ) ) * 255);
				int g = (int)(Contrast( ( Color.green(color) - 35 / 255 ) * ( 255 / ( 170 - 35 ) ) ) * 255);
				int b = (int)(Contrast( ( Color.blue(color) - 10 / 255 ) * ( 255 / ( 230 - 10 ) ) ) * 255);
				pixels[y * width + x] = Color.rgb(r,g,b);
			}
		}
		Bitmap newImage = Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888);
		//メモリから解放させる
		pixels = null;
		System.gc();
		return newImage;
		*/
	}

	private float Contrast(float p){
		float con = ( p - 0.5f ) * 0.8f + 0.5f;
		return con;

	}

	public Bitmap TintFilter(Bitmap image){
		ColorMatrix cm = new ColorMatrix();
		cm.set(new float[] {
			255f / (150 - 20)	, 0		, 0		, 0, -20f * 255 / (150 - 20),
			0		, 255f / (170 - 35)	, 0		, 0, -35f * 255 / (170 - 35),
			0		, 0		, 255f / (230 - 10)	, 0, -10f * 255 / (230 - 10),
			0		, 0		, 0		, 1, 0 });
		return ImageColorFilter(image,new ColorMatrixColorFilter(cm));

	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public Bitmap PolaroidFilter(Bitmap image){
		ColorMatrix cm = new ColorMatrix();
		cm.set(new float[] {
				1.438f	,-0.122f,-0.016f,0,-0.03f * 255	,
				-0.062f	,1.378f	,-0.016f,0,0.05f * 255	,
				-0.062f	,-0.122f,1.483f	,0,-0.02f * 255	,
				0		,0		,0		,1,0});
		return ImageColorFilter(image,new ColorMatrixColorFilter(cm));
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	public Bitmap MonochromeFilter(Bitmap image){
		ColorMatrix cm = new ColorMatrix();
		cm.set(new float[] {
				1.5f	,1.5f	,1.5f	,0,-1 * 255	,
				1.5f	,1.5f	,1.5f	,0,-1 * 255	,
				1.5f	,1.5f	,1.5f	,0,-1 * 255	,
				0		,0		,0		,1,0});
		return ImageColorFilter(image,new ColorMatrixColorFilter(cm));
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	//露光調整
	// #### Expose [-100, 100]
	public Bitmap ExposeFilter(Bitmap image,float value){
		Point c1 = new Point(0,(int)(255 * value));
		Point c2 = new Point((int)(255 - (255 * value)),255);
		return CurvesFilter(image,new Point(0,0),c1,c2,new Point(255,255));
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	//トーンカーブ
	// #### Curves
	public Bitmap CurvesFilter(Bitmap image,Point s,Point c1,Point c2,Point e){
		HashMap<Integer, Integer> points = Bezier(s, c1, c2, e);
		int width = image.getWidth();
		int height = image.getHeight();
		int[] pixels = new int[width * height];
		image.getPixels(pixels, 0, width, 0, 0, width, height);
		//メモリから解放させる
		image.recycle();
		image = null;
		for(int y = 0;y < height;y++){
			for(int x = 0;x < width;x++){
				int color = pixels[y * width + x];
				Integer r = points.get(Color.red(color));
				Integer g = points.get(Color.green(color));
				Integer b = points.get(Color.blue(color));
				if(r != null && g != null && b != null){
					pixels[y * width + x] = Color.rgb(r, g, b);
				}
			}
		}
		Bitmap newImage = Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888);
		//メモリから解放させる
		pixels = null;
		points.clear();
		points = null;
		System.gc();
		return newImage;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	//ぼかし(全体を曇らせる)
	public Bitmap SimpleBlurFilter(Bitmap image){
		float[][] value = {
		{1f/9, 1f/9, 1f/9}
		,{1f/9, 1f/9, 1f/9}
		,{1f/9, 1f/9, 1f/9}
		};
		return convolve(image, value);
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	//ぼかし(全体を曇らせる)
	public Bitmap GausddianBlurFilter(Bitmap image){
		float[][] value = {
			{1f/273, 4f/273, 7f/273, 4f/273, 1f/273},
			{4f/273, 16f/273, 26f/273, 16f/273, 4f/273},
			{7f/273, 26f/273, 41f/273, 26f/273, 7f/273},
			{4f/273, 16f/273, 26f/273, 16f/273, 4f/273},
			{1f/273, 4f/273, 7f/273, 4f/273, 1f/273}
		};
		return convolve(image, value);
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	//輝度（明るさ）調整
	//#### 引数の範囲は -100～100
	public Bitmap BrightenPersentFilter(Bitmap image,float value){
		//float p = (float) m_FilterCalcuration.normalize(value, 0, 2, -100, 100);
		float p = (float) normalize(value, 0, 2, -100, 100);
		return BrightenFilter(image,p);
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	//透明度調整
	//#### 引数の範囲は -100～100
	public Bitmap AlphaPersentFilter(Bitmap image,float value){
		//float p = (float) m_FilterCalcuration.normalize(value, 0, 2, -100, 100);
		float p = (float) normalize(value, 0, 2, -100, 100);
		return AlphaFilter(image,p);
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	//彩度調整
	//#### 引数の範囲は -100～100
	public Bitmap SaturationPercentFilter(Bitmap image,float value){
		float p = (float) normalize(value, 0, 1, -100, 100);
		return SaturationFilter(image, p);
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	//ガンマ補正
	//#### 引数の範囲は -100, 100
	public Bitmap GammaParsentFilter(Bitmap image,float value){
		float p = (float) normalize(value, 0, 2, -100, 100);
		return GammaFilter(image, p);
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	//たたみ込み:kernel によって重みをかけて総和・積分する操作
	private Bitmap convolve(Bitmap image,float[][] value){
		int w = image.getWidth();
		int h = image.getHeight();
		int[] pixels = new int[w * h];
		int kh = value.length / 2;
		int kw = value[0].length / 2;
		image.getPixels(pixels, 0, w, 0, 0, w, h);
		//メモリから解放させる
		image.recycle();
		image = null;

		//TODO 処理速度向上
		for (int i = 0;i < h; i++) {
			for (int j = 0; j < w; j++) {
				int outIndex = i * w + j;
				int r = 0;
				int g = 0;
				int b = 0;
				for (int n = -kh; n <= kh; n++) {
					for (int m = -kw; m <= kw; m++) {
						if (i + n >= 0 && i + n < h) {
							if (j + m >= 0 && j + m < w) {
								float f = value[n + kh][m + kw];
								if (f == 0) {
									//とりあえず以降はスキップ
									continue;
								}
								int inIndex = (i+n) * w + (j+m);
								r += Color.red(pixels[inIndex]) * f;
								g += Color.green(pixels[inIndex]) * f;
								b += Color.blue(pixels[inIndex]) * f;
							}
						}
					}
				}
				//Log.d(TAG, "sr:"+(Color.red(pixels[outIndex]) - r)+"sg:"+(Color.green(pixels[outIndex]) - g)+"sb"+(Color.blue(pixels[outIndex]) - b));
				pixels[outIndex] = Color.rgb((int)clamp(r, 0, 255), (int)clamp(g, 0, 255), (int)clamp(b, 0, 255));
			}
		}
		Bitmap newImage = Bitmap.createBitmap(pixels,w,h,Bitmap.Config.ARGB_8888);
		//メモリから解放させる
		pixels = null;
		System.gc();
		return newImage;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	private double clamp(float val,int min,int max){
		int newMin = min;
		int newMax = max;
		return Math.min(newMax, Math.max(newMin, val));
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	//smin～smaxの範囲をdmin～dmax換算になるようにvalを変換
	private double normalize(float val, int dmin, int dmax,int smin,int smax){
		double sdist = Math.sqrt(Math.pow(smax - smin, 2));
		double ddist = Math.sqrt(Math.pow(dmax - dmin, 2));
		double ratio = ddist / sdist;
		double newVal = clamp(val,smin,smax);
		return dmin + (newVal - smin) * ratio;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	private HashMap<Integer, Integer> Bezier(Point C1,Point C2,Point C3,Point C4){
		PointF point = new PointF();
		int num = 1024;
		HashMap<Integer, Integer> points = new HashMap<Integer, Integer>();
		//f(x) = x^3 + 3x^2 + 3x + 1=(x+1)^3
		for (int i = 0; i < num; i++){
			float t = (float) i / num;
			float B1 = t * t * t;
			float B2 = 3 * t * t * (1-t);
			float B3 = 3 * t * (1-t) * (1-t);
			float B4 = (1-t) * (1-t) * (1-t);
			point.x = C1.x * B1 + C2.x * B2 + C3.x * B3 + C4.x * B4;
			point.y = C1.y * B1 + C2.y * B2 + C3.y * B3 + C4.y * B4;
			points.put((int)point.x, (int)point.y);
		}
		return points;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	private Bitmap ImageColorFilter(Bitmap image,ColorFilter filter){
		Paint paint = new Paint();
		paint.setColorFilter(filter);
		Bitmap copy = image.copy(Bitmap.Config.ARGB_8888, true);
		Canvas canvas = new Canvas(copy);
		canvas.drawBitmap(copy, null,new Rect(0,0,copy.getWidth(),copy.getHeight()), paint);
		canvas.save();
		image.recycle();
		image = null;
		return copy;
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------
}
