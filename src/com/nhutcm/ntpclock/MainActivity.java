package com.nhutcm.ntpclock;

import java.io.DataOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnItemSelectedListener,
		DataResultCallback {

	// vn.pool.ntp.org
	// 0.ubuntu.pool.ntp.org
	// pool.ntp.org

	private static final String TAG = "MainActivity";
	private static final int ONE_SECOND = 1000;
	private static final int ONE_MINUTE = 60 * ONE_SECOND;

	private CountDownTimer timer = null;
	private TextView txtCountdownTimer;
	private TextView txtDate;
	private DateFormat outFormatCountdown;
	private Date d;
	private Spinner spinner;
	private Context mContext;
	private AppApplication app;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mContext = this;

		if (!checkSU()) {
			Toast.makeText(getApplicationContext(), "Sorry. NOT-Root device",
					Toast.LENGTH_LONG).show();
			showAlert(this);
			return;
		}

		txtDate = (TextView) findViewById(R.id.txtDate);
		txtCountdownTimer = (TextView) findViewById(R.id.txtCountdownTimer);
		outFormatCountdown = new SimpleDateFormat("HH:mm:ss");
		outFormatCountdown.setTimeZone(TimeZone.getTimeZone("UTC"));
		spinner = (Spinner) findViewById(R.id.spinner);
		spinner.setOnItemSelectedListener(this);

		app = AppApplication.getInstance(this);
		app.setNetworkConnection(Utils.isInternetConnection(this));
		app.setUTCTime(getCurrentUTCTime());

		loadListNtpServer(this);
		d = new Date();

		// Đồng bộ thời gian khi vừa khởi động lên
		getInternetTime();

		// hiển thị ngày hiện hành
		txtDate.setText(Utils.getCurrentDate());

		createCountdownTimer();
	}

	/**
	 * Timer đếm ngược đến lần đồng bộ tiếp theo
	 */
	private void createCountdownTimer() {
		timer = new CountDownTimer(10 * ONE_MINUTE, ONE_SECOND) {

			@Override
			public void onTick(long millisUntilFinished) {
				d.setTime(millisUntilFinished);
				String result = outFormatCountdown.format(d);
				txtCountdownTimer.setText(result);
				txtDate.setText(Utils.getCurrentDate());
			}

			@Override
			public void onFinish() {
				getInternetTime();
				resetTimerEvery10minutes();
			}
		};
		timer.start();
	}

	/**
	 * Lấy UTC time in current location
	 * 
	 * @return
	 */
	private long getCurrentUTCTime() {
		TimeZone tz = TimeZone.getDefault();
		Date now = new Date();
		return tz.getOffset(now.getTime());
	}

	/**
	 * Manual sync
	 * 
	 * @param v
	 */
	public void syncTimeNow(View v) {
		getInternetTime();
		resetTimerEvery10minutes();
	}

	/**
	 * reset timer mỗi 10 phút
	 */
	private void resetTimerEvery10minutes() {
		timer.cancel();
		timer.start();
	}

	/**
	 * Lấy internet time
	 */
	private void getInternetTime() {
		if (!app.isNetworkConnection()) {
			Toast.makeText(mContext, "Network disconnection",
					Toast.LENGTH_SHORT).show();
			return;
		}

		String ntpServerAddress = app.getListNtpServer().get(
				app.getCurrentPosition());
		if (ntpServerAddress == null || ntpServerAddress == "") {
			Toast.makeText(mContext, "Sync fails. Not found NTP server",
					Toast.LENGTH_SHORT).show();
			return;
		}
		BackgroundTask task = new BackgroundTask(this);
		task.execute(ntpServerAddress, String.valueOf(app.getUTCTime()));
	}

	/**
	 * Kiểm tra thiết bị đã root hay chưa
	 * 
	 * @return
	 */
	private boolean checkSU() {
		return ShellInterface.isSuAvailable();
	}

	/**
	 * Cập nhật lại thời gian hệ thống
	 * 
	 * @param time
	 */
	private void setDate(long time) {
		String configTime = Utils.convertTimeToString(time);
		if (configTime == "") {
			Log.e(TAG, "Format wrong");
			return;
		}
		try {
			Process process = Runtime.getRuntime().exec("su");
			DataOutputStream os = new DataOutputStream(
					process.getOutputStream());
			String cmd = "date -s " + configTime + "; \n";
			os.writeBytes(cmd);
			Toast.makeText(mContext, "Time was sync", Toast.LENGTH_SHORT)
					.show();
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
	}

	/**
	 * Load danh sách các server hiện có, nếu ko add địa chỉ mặc định:
	 * 0.ubuntu.pool.ntp.org
	 * 
	 * @param c
	 */
	private void loadListNtpServer(Context c) {
		ArrayList<String> lst = app.getListNtpServer();
		if (0 == lst.size()) {
			lst.add("0.ubuntu.pool.ntp.org");// default
			app.setCurrentPosition(0);
		}

		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, lst);
		dataAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(dataAdapter);

		spinner.setSelection(app.getCurrentPosition());

	}

	public void addAddress(View v) {
		showDialog(mContext);
	}

	private void showDialog(final Context context) {

		LayoutInflater li = LayoutInflater.from(context);
		View promptsView = li.inflate(R.layout.custom_dialog, null);

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				context);
		alertDialogBuilder.setTitle(R.string.hint);
		alertDialogBuilder.setView(promptsView);

		final EditText userInput = (EditText) promptsView
				.findViewById(R.id.editTextDialogUserInput);

		// set dialog message
		alertDialogBuilder
				.setCancelable(false)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {

						String result = userInput.getText().toString();
						if (result != "") {
							app.getListNtpServer().add(result);
						}
					}
				})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();
	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		// Lưu vị trí hiện hành của spinner
		app.setCurrentPosition(position);

	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub

	}

	private void showAlert(Context context) {
		AlertDialog.Builder alertDialog = new Builder(context);

		alertDialog.setTitle("Alert Dialog");

		alertDialog.setMessage("Your phone is not root. Close app.");

		alertDialog.setIcon(R.drawable.delete);

		alertDialog.setCancelable(false);
		// Setting OK Button
		alertDialog.setPositiveButton("OK",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
						finish();
					}
				});

		alertDialog.show();
	}

	@Override
	public void onResult(long time) {
		try {
			if (0 != time) {
				setDate(time);
				txtDate.setText(Utils.getCurrentDate());
			} else {
				Toast.makeText(mContext, "Sync fail. Please try again.",
						Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
	}

}
