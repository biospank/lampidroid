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
	// gestione audio
	// private CheckBox chkAudio;
	//private Intent audioCaptureIntent;
	
	SharedPreferences sharedPrefs;

	IntentFilter intentFilter;
	Intent alarmIntent;
	PendingIntent pendingAlarm;
	
	private BroadcastReceiver intentReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
//			Log.d("onReceive", intent.getAction());
//			Log.d("onReceive", String.valueOf(intent.getAction().equals(SMS_LAMP_ACTION)));
			if(intent.getAction().equals(SMS_LAMP_ACTION)) {
				//---display the SMS received in the TextView---
				lblAutoDisovery.setText(intent.getExtras().getString("sms"));
				launchHttpTask();
				
			}
			if(intent.getAction().equals(ALARM_LAMP_ACTION)) {
//				Toast.makeText(context, "Rise and Shine!", Toast.LENGTH_SHORT).show();
				launchHttpTask();
				
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
        	manageUserSettings();
            showUserSettings();
            break;
 
        }
 
    }

    private void manageUserSettings() {
    	long timeMillis = sharedPrefs.getLong("pref_key_alarm", 0);
    	if(timeMillis > 0) {
    		activateAlarm(pendingAlarm, timeMillis);
    	} else {
    		deactivateAlarm(pendingAlarm);
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
		btnTest = (Button) findViewById(R.id.btnTest);
		prgUdp = (ProgressBar) findViewById(R.id.prgUdp);
		
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		// gestione audio
//		chkAudio = (CheckBox) findViewById(R.id.chkAudio);
//		audioCaptureIntent =  new Intent(this, AudioCaptureService.class);

		intentFilter = new IntentFilter();
		intentFilter.addAction(SMS_LAMP_ACTION);
		intentFilter.addAction(ALARM_LAMP_ACTION);
		
		//alarmFilter = new IntentFilter("ALARM_ACTION");
		
		registerReceiver(intentReceiver, intentFilter);

		alarmIntent = new Intent(ALARM_LAMP_ACTION);
		pendingAlarm = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);

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
	
	private void activateAlarm(PendingIntent alarmIntent, long time) {
		
		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		int alarmType = AlarmManager.RTC_WAKEUP;
		// Trigger the device in 20 seconds
		//long timeOrLengthOfWait = 20000;
		
		//alarmManager.setInexactRepeating(alarmType, time, AlarmManager.INTERVAL_DAY, alarmIntent);
		alarmManager.set(alarmType, time, alarmIntent);
		
	}
	
	private void deactivateAlarm(PendingIntent alarmIntent) {
		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(alarmIntent);
	}
	
	private void launchHttpTask() {
		SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);
		
		boolean notify = sharedPrefs.getBoolean("pref_key_inbound_sms", false);
		
		if(cUdp.getLampiIp() == null) {
			if(notify) {
				new HttpNotifyTask().execute("192.168.1.2");
			}
		} else {
			if(notify) {
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
        
        Log.d("showUserSettings", "Alarm value:" + sharedPrefs.getLong("pref_key_alarm", 2));
        
//        builder.append("\n Sync Frequency: "
//                + sharedPrefs.getString("prefSyncFrequency", "NULL"));
 
        TextView settingsTextView = (TextView) findViewById(R.id.lblAutoDiscovery);
 
        settingsTextView.setText(builder.toString());
    }

}
