package com.nhutcm.ntpclock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 * Bắt các sự kiện mất hay có kết nối Internet, kèm thêm sự kiện phát sinh lúc
 * 0h
 * 
 * @author Chau Minh Nhut
 * 
 */
public class AppReceiver extends BroadcastReceiver {

	public static final String TAG = "AppReceiver";

	@Override
	public void onReceive(Context context, Intent arg1) {
		String action = arg1.getAction();
		if (null == action || action == "") {
			Log.e(TAG, "Error");
			Toast.makeText(null, "Error", Toast.LENGTH_SHORT).show();
		} else if (action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
			Log.d("TAG", arg1.getAction());
			boolean isConnect = Utils.isInternetConnection(context);
			AppApplication.getInstance(context).setNetworkConnection(isConnect);
		} else {
			Toast.makeText(context, action, Toast.LENGTH_SHORT).show();
		}

	}
}
