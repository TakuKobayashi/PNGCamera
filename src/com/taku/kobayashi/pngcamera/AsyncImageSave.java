package com.taku.kobayashi.pngcamera;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.widget.Toast;

public class AsyncImageSave extends AsyncTask<String, Integer, Long>{

	private static final String TAG = "Noz_AsyncImgDownLoader";
	private ProgressDialog m_ProgressDialog = null;
	private Activity m_Activity;

	private boolean m_bIsError; //ダウンロード失敗フラグ.(非同期保存の解除用).
	//ダウンロードに失敗した場合ダウンロードしたファイルはすべて削除する。そのために必要。
	private ArrayList<String> m_DownLoadList;

	public AsyncImageSave(Activity activity){
		m_Activity = activity;
	}

	public void initParam(){
		m_DownLoadList = new ArrayList<String>();
		m_bIsError = false;
	}

	//AsynkTask実行前のSetup
	//処理実行中にUIに表示させること
	@Override
	protected void onPreExecute() {
		m_ProgressDialog = new ProgressDialog(m_Activity);
		m_ProgressDialog.setTitle(m_Activity.getResources().getString(R.string.UpdatingDialogTitle));
		//false == 横長のバーで進捗状況を表示する。true だと進捗状況は表示しない
		m_ProgressDialog.setIndeterminate(false);
		m_ProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		m_ProgressDialog.setCancelable(true);
		m_ProgressDialog.show();
	}

	//実際に行う処理 , 引数はファイルのダウンロード先のURLリスト(String配列)
	//LongはIntegerのlong ver
	@Override
	protected Long doInBackground(String... params) {
		int count = params.length;
		long total = 0;
		for(int i = 0;i < params.length;i++){
			//SDカードのマウント、空き容量、ネットワークの接続有無によるチェック
			if(m_bIsError == true || SDCardCtrl.checkAll() == false || Tools.CheckNetWork(m_Activity) == false || SDCardCtrl.CheckSDcardAvailableSpace() == false || this.isCancelled() == true || m_ProgressDialog.isShowing() == false){
				m_bIsError = true;
				return total;
			}
			String strReplaceName = new String();
			String SaveImgPath = new String();
			String downLoadFilePath = new String();
			if(params[i].contains(Config.HAIRSTYLE_FILE_NAME)){
				strReplaceName = params[i].replaceAll(Config.HAIRSTYLE_FILE_NAME,"");
				downLoadFilePath =  Config.ServerURL + Config.HAIRSTYLE_FILE_NAME + "imgs/" + strReplaceName;
				SaveImgPath = SDCardCtrl.HairStyleImgsDir + strReplaceName;
			}else if(params[i].contains(Config.STAMP_FILE_NAME)){
				strReplaceName = params[i].replaceAll(Config.STAMP_FILE_NAME,"");
				downLoadFilePath =  Config.ServerURL + Config.STAMP_FILE_NAME + "imgs/" + strReplaceName;
				SaveImgPath = SDCardCtrl.StampImgsDir + strReplaceName;
			}else if(params[i].contains(Config.FRAME_FILE_NAME)){
				strReplaceName = params[i].replaceAll(Config.FRAME_FILE_NAME,"");
				downLoadFilePath =  Config.ServerURL + Config.FRAME_FILE_NAME + "imgs/" + strReplaceName;
				SaveImgPath = SDCardCtrl.FrameImgsDir + strReplaceName;
			}else{
				return total;
			}
			m_DownLoadList.add(SaveImgPath);
			//サーバーからダウンロードしてSDカードに保存する処理
			long size = Tools.DownloadFromUrl(downLoadFilePath ,SaveImgPath);
			if(size > 0){
				//size分加算していく
				total += size;
			} else {
				m_bIsError = true;
				return total;
			}

			publishProgress((int) ((i / (float) count) * m_ProgressDialog.getMax()));
		}
		return total;
	}

	//AsynkTask中にUIを更新させる処理。publishProgressが呼ばれた後に呼ばれる
	@Override
	protected void onProgressUpdate(Integer... progress) {
		m_ProgressDialog.setProgress(progress[0]);
	}

	//UpDate完了
	@Override
	protected void onPostExecute(Long result) {
		if (m_bIsError) {
			//error
			m_ProgressDialog.dismiss();
			deleteDownLoadErrorFiles();
			Toast toast = Toast.makeText(m_Activity, R.string.UpdateError, Toast.LENGTH_LONG);
			toast.show();
			m_Activity.setResult(Activity.RESULT_CANCELED);
		} else if (this.isCancelled() || m_ProgressDialog.isShowing() == false) {
			//キャンセル
			deleteDownLoadErrorFiles();
			Toast toast = Toast.makeText(m_Activity, R.string.UpdateCanceled, Toast.LENGTH_LONG);
			toast.show();
			m_Activity.setResult(Activity.RESULT_CANCELED);
		} else {
			//ダウンロード終了
			m_ProgressDialog.dismiss();
			Toast toast = Toast.makeText(m_Activity, R.string.UpdateFinished, Toast.LENGTH_LONG);
			toast.show();
			m_Activity.setResult(Activity.RESULT_OK);
		}
		m_Activity.finish();
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	@Override
	protected void onCancelled() {
		super.onCancelled();
		m_ProgressDialog.dismiss();
		deleteDownLoadErrorFiles();
		Toast toast = Toast.makeText(m_Activity, R.string.UpdateCanceled, Toast.LENGTH_LONG);
		toast.show();
		m_Activity.setResult(Activity.RESULT_CANCELED);
		m_Activity.finish();
	}

	//---------------------------------------------------------------------------------------------------------------------------------------------------------------

	private void deleteDownLoadErrorFiles(){
		for(int i = 0;i < m_DownLoadList.size();i++){
			File deleteFiles = new File(m_DownLoadList.get(i));
			if(deleteFiles.exists() == true){
				deleteFiles.delete();
			}
		}
	}


}