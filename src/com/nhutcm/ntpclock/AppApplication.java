package com.nhutcm.ntpclock;

import java.util.ArrayList;

import android.app.Application;
import android.content.Context;

/**
 * Lưu các thông số duy nhất của ứng dụng
 * 
 * @author Chau Minh Nhut
 * 
 */
public class AppApplication extends Application {

	private static AppApplication instance = null;

	public static AppApplication getInstance(Context c) {
		if (instance == null) {
			instance = (AppApplication) c.getApplicationContext();
		}
		return instance;
	}

	public ArrayList<String> getListNtpServer() {
		return lstNtpServer;
	}

	public void setListNtpServer(ArrayList<String> lstNtpServer) {
		this.lstNtpServer = lstNtpServer;
	}

	private ArrayList<String> lstNtpServer = new ArrayList<String>();

	int currentPosition;

	public int getCurrentPosition() {
		return currentPosition;
	}

	public void setCurrentPosition(int currentPosition) {
		this.currentPosition = currentPosition;
	}

	boolean isNetworkConnection = false;

	public boolean isNetworkConnection() {
		return isNetworkConnection;
	}

	public void setNetworkConnection(boolean isNetworkConnection) {
		this.isNetworkConnection = isNetworkConnection;
	}
	
	long utc = 0;

	public long getUTCTime() {
		return utc;
	}

	public void setUTCTime(long utc) {
		this.utc = utc;
	}

}
