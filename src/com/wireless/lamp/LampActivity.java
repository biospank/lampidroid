package com.wireless.lamp;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class LampActivity extends Activity implements OnTaskListener {
	
	private UdpClientTask cUdp;
	private TextView lblAutoDisovery;
	private Button btnRefresh;
	private Button btnTest;
	private CheckBox chkSms;
	private ProgressBar prgUdp;
	private CheckBox chkAlarm;
	private TimePicker tpkAlarm;
	// gestione audio
	// private CheckBox chkAudio;
	//private Intent audioCaptureIntent;
	

	IntentFilter intentFilter;
	IntentFilter alarmFilter;
	Intent alarmIntent;
	PendingIntent pendingAlarm;
	
	private BroadcastReceiver intentReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			//---display the SMS received in the TextView---
			lblAutoDisovery.setText(intent.getExtras().getString("sms"));
			launchHttpTask();
		}

	};
	
	private BroadcastReceiver alarmReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Toast.makeText(context, "Rise and Shine!", Toast.LENGTH_SHORT).show();
		}

	};
	
	@Override
	public void onTaskBegin() {
		lblAutoDisovery.setVisibility(View.INVISIBLE);
		prgUdp.setVisibility(View.VISIBLE);
		
	}
	
	@Override
	public void onTaskCompleted() {
		prgUdp.setVisibility(View.GONE);
		lblAutoDisovery.setVisibility(View.VISIBLE);

		String ip = cUdp.getLampiIp();
		if(ip == null) {
			Log.d("task completed", "no device found!!");
			lblAutoDisovery.setText("Lamp device not found!!");
		} else {
			Log.d("task completed", "device found!!");
			lblAutoDisovery.setText("raspi ip: " + ip);
		}
		
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lamp);

		initializeView();
		
		launchUdpTask();
		


	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.lamp, menu);
		return true;
	}
	
	@Override
	protected void onResume() {
		//registerReceiver(intentReceiver, intentFilter);
		// gestione audio
		// startService(audioCaptureIntent);
		super.onResume();
	}
	
	@Override
	protected void onDestroy() {
		unregisterReceiver(intentReceiver);
		unregisterReceiver(alarmReceiver);
		deactivateAlarm(pendingAlarm);
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		//unregisterReceiver(intentReceiver);
		// gestione audio
		// stopService(audioCaptureIntent);
		super.onPause();
		
	}

	protected void initializeView() {
		lblAutoDisovery = (TextView) findViewById(R.id.lblAutoDiscovery);
		btnRefresh = (Button) findViewById(R.id.btnRefresh);
		chkSms = (CheckBox) findViewById(R.id.chkSms);
		tpkAlarm = (TimePicker) findViewById(R.id.tpkAlarm);
		btnTest = (Button) findViewById(R.id.btnTest);
		prgUdp = (ProgressBar) findViewById(R.id.prgUdp);
		chkAlarm = (CheckBox) findViewById(R.id.chkAlarm);
		

		// gestione audio
//		chkAudio = (CheckBox) findViewById(R.id.chkAudio);
//		audioCaptureIntent =  new Intent(this, AudioCaptureService.class);

		intentFilter = new IntentFilter();
		intentFilter.addAction("SMS_RECEIVED_ACTION");
		
		alarmIntent = new Intent("ALARM_ACTION");
		alarmFilter = new IntentFilter("ALARM_ACTION");
		
		registerReceiver(intentReceiver, intentFilter);
		registerReceiver(alarmReceiver, alarmFilter);
		
//		chkAudio.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				if(((CheckBox)v).isChecked()) {
//					Log.d("chkAudio", "Check enabled");
//					// Explicitly start AudioCaptureService 
//					startService(audioCaptureIntent);
//				} else {
//					Log.d("chkAudio", "Check disabled");
//					stopService(audioCaptureIntent);
//				}
//			}
//		});
		
		pendingAlarm = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);

		chkAlarm.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(((CheckBox)v).isChecked()) {
					activateAlarm(pendingAlarm);
				} else {
					deactivateAlarm(pendingAlarm);
				}
				
			}
		});
	
		btnTest.setOnClickListener(new Button.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				launchHttpTask();
			}
			
		});

		btnRefresh.setOnClickListener(new Button.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				launchUdpTask();
				
			}
			
		});

	}
	
	private void launchUdpTask() {
		
		// Kickoff the Client
		//Context ctx = this.getApplicationContext();
		WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		if(wifi.getWifiState() == WifiManager.WIFI_STATE_DISABLED) {
			startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));
		} else {
			cUdp = new UdpClientTask(wifi, this);
			cUdp.execute();

		}
		
	}
	
	private void activateAlarm(PendingIntent alarmIntent) {
		
		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		int alarmType = AlarmManager.RTC_WAKEUP;
		// Trigger the device in 10 seconds
		long timeOrLengthOfWait = 10000;
		
		alarmManager.setInexactRepeating(alarmType, timeOrLengthOfWait, timeOrLengthOfWait, alarmIntent);
		
	}
	
	private void deactivateAlarm(PendingIntent alarmIntent) {
		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(alarmIntent);
	}
	
	private void launchHttpTask() {
		
		if(cUdp.getLampiIp() == null) {
			if(chkSms.isChecked()) {
				new HttpNotifyTask().execute("192.168.1.2");
			}
		} else {
			if(chkSms.isChecked()) {
				new HttpNotifyTask().execute(cUdp.getLampiIp());
			}
		}
	}

}
