package com.wireless.lamp;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class LampActivity extends Activity implements OnTaskListener {
	
	public static final String SMS_LAMP_ACTION = "SMS_LAMP_ACTION";
	public static final String ALARM_LAMP_ACTION = "ALARM_LAMP_ACTION";
	private static final int RESULT_SETTINGS = 1;
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
	Intent alarmIntent;
	PendingIntent pendingAlarm;
	
	private BroadcastReceiver intentReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction() == SMS_LAMP_ACTION) {
				//---display the SMS received in the TextView---
				lblAutoDisovery.setText(intent.getExtras().getString("sms"));
				launchHttpTask();
				
			}
			if(intent.getAction() == ALARM_LAMP_ACTION) {
				Toast.makeText(context, "Rise and Shine!", Toast.LENGTH_SHORT).show();
				
			}
				
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
		getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
 
        case R.id.menu_settings:
            Intent settingsIntent = new Intent(this, LampSettingsActivity.class);
            startActivityForResult(settingsIntent, RESULT_SETTINGS);
            break;
 
        }
 
        return true;
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
 
        switch (requestCode) {
        case RESULT_SETTINGS:
            showUserSettings();
            break;
 
        }
 
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
		//unregisterReceiver(alarmReceiver);
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
		intentFilter.addAction(SMS_LAMP_ACTION);
		intentFilter.addAction(ALARM_LAMP_ACTION);
		
		//alarmFilter = new IntentFilter("ALARM_ACTION");
		
		registerReceiver(intentReceiver, intentFilter);
		//registerReceiver(alarmReceiver, alarmFilter);
		
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
		
		alarmIntent = new Intent(ALARM_LAMP_ACTION);
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
		int alarmType = AlarmManager.ELAPSED_REALTIME_WAKEUP;
		// Trigger the device in 20 seconds
		long timeOrLengthOfWait = 20000;
		
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

	private void showUserSettings() {
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);
 
        StringBuilder builder = new StringBuilder();
 
//        builder.append("\n Username: "
//                + sharedPrefs.getString("prefUsername", "NULL"));
 
        builder.append("\n Inbound Sms:"
                + sharedPrefs.getBoolean("pref_key_inbound_sms", false));
 
//        builder.append("\n Sync Frequency: "
//                + sharedPrefs.getString("prefSyncFrequency", "NULL"));
 
        TextView settingsTextView = (TextView) findViewById(R.id.lblAutoDiscovery);
 
        settingsTextView.setText(builder.toString());
    }

}
