package com.nhutcm.ntpclock;

import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;

/**
 * 1 asyntask thực hiện lấy thời gian từ server, sau đó trả về cho UI
 * 
 * @author Chau Minh Nhut
 * 
 */
public class BackgroundTask extends AsyncTask<String, Void, Long> {

	public static final String TAG = "BackgroundTask";
	private static final int TIME_OUT = 30000;
	private DataResultCallback mCallback;
	
	BackgroundTask(DataResultCallback callback){
		mCallback = callback;
	}

	@Override
	protected Long doInBackground(String... params) {
		String ntpServerAddress = params[0];
		long utc = Long.parseLong(params[1]);
		long now = 0;
		try {
			NtpClient client = new NtpClient();
			if (client.requestTime(ntpServerAddress, TIME_OUT)) {
				now = client.getNtpTime() + SystemClock.elapsedRealtime()
						- client.getNtpTimeReference() + utc;
				Log.d(TAG, String.valueOf(now));
			}
		} catch (Exception ex) {
			Log.e(TAG, ex.getMessage());
		}

		return now;
	}
	
	@Override
	protected void onPostExecute(Long result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		mCallback.onResult(result);
	}

}
