package com.nhutcm.ntpclock;

import java.io.DataOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Sử lý các tiện ích
 * 
 * @author Chau Minh Nhut
 * 
 */
public class Utils {

	private static final String TAG = "Utils";

	/**
	 * Kiểm tra kết nối mạng
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isInternetConnection(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		boolean isConnected = activeNetwork != null
				&& activeNetwork.isConnectedOrConnecting();
		return isConnected;
	}

	/**
	 * Convert từ 1 long time to String
	 * 
	 * @param time
	 * @return
	 */
	public static String convertTimeToString(long time) {
		String result = "";
		DateFormat outFormat = new SimpleDateFormat("yyyyMMdd.HHmmss");
		outFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

		Date d = new Date(time);
		result = outFormat.format(d);
		return result;
	}

	public static Calendar convertLongtimeToCalendar(long time) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(time);
		return calendar;
	}

	/**
	 * Cấu hình lại thời gian hệ thống (test)
	 * 
	 */
	public static void changeSystemTime() {
		try {
			Process process = Runtime.getRuntime().exec("su");
			DataOutputStream os = new DataOutputStream(
					process.getOutputStream());
			String cmd = "date -s 20140716.235900; \n";
			os.writeBytes(cmd);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
	}

	public static String getCurrentDate() {
		Calendar c = Calendar.getInstance();
		return c.get(Calendar.DAY_OF_MONTH) + "/" + (c.get(Calendar.MONTH) + 1)
				+ "/" + c.get(Calendar.YEAR);
	}

}
