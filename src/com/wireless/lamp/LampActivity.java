package com.wireless.lamp;

import android.app.Activity;
import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.MediaRecorder;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;

public class LampActivity extends Activity implements OnTaskCompleted {
	
	private UdpClientTask cUdp;
	private TextView lblAutoDisovery;
	private Button btnRefresh;
	private Button btnTest;
	private CheckBox chkSms;
	// gestione audio
	// private CheckBox chkAudio;
	//private Intent audioCaptureIntent;
	

	IntentFilter intentFilter;
	
	private BroadcastReceiver intentReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			//---display the SMS received in the TextView---
			lblAutoDisovery.setText(intent.getExtras().getString("sms"));
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

	};
	
	@Override
	public void onTaskCompleted() {
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
		btnTest = (Button) findViewById(R.id.btnTest);
		

		// gestione audio
//		chkAudio = (CheckBox) findViewById(R.id.chkAudio);
//		audioCaptureIntent =  new Intent(this, AudioCaptureService.class);

		intentFilter = new IntentFilter();
		intentFilter.addAction("SMS_RECEIVED_ACTION");
		
		registerReceiver(intentReceiver, intentFilter);
		
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
		
		btnTest.setOnClickListener(new Button.OnClickListener() {
			
			@Override
			public void onClick(View v) {
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
		Context ctx = this.getApplicationContext();
		WifiManager wifi = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
		if(wifi.getWifiState() == WifiManager.WIFI_STATE_DISABLED) {
			startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));
		} else {
			cUdp = new UdpClientTask(wifi, this);
			cUdp.execute();

		}
		
	}

}
