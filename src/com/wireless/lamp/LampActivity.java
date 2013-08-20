package com.wireless.lamp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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
import android.widget.TextView;
import android.widget.Toast;

public class LampActivity extends Activity implements OnTaskListener {
	
	public static final String SMS_LAMP_ACTION = "SMS_LAMP_ACTION";
	public static final String ALARM_LAMP_ACTION = "ALARM_LAMP_ACTION";
	private static final int RESULT_SETTINGS = 1;
    private UdpClientTask cUdp;
//	private TextView tvTopLeft;
//	private TextView tvTopRight;
//	private TextView tvBottomLeft;
//	private TextView tvBottomRight;
//	private Button btnRefresh;
	private Button btnTest;
	private TextView tvMsg;
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

			Intent lampActivityIntent = new Intent(context, LampActivity.class);
			lampActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(lampActivityIntent);
			
			if(intent.getAction().equals(SMS_LAMP_ACTION)) {
				//---display the SMS received in the TextView---
				tvMsg.setText(intent.getExtras().getString("sms"));
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
		tvMsg.setText("Ricerca del dispositivo in corso...");
		
	}
	
	@Override
	public void onTaskCompleted() {
		String ip = cUdp.getLampiIp();
		if(ip == null) {
			Log.d("task completed", "no device found!!");
			tvMsg.setText("Dispositivo non trovato: verifica la connessione e il cavo di rete");
		} else {
			Log.d("task completed", "device found!!");
			tvMsg.setText("Dispositivo trovato, indirizzo: " + ip);
		}
		
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lamp);

		initializeView();
		
		showUserSettings();
		
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
    	long currentTime = System.currentTimeMillis();
    	long timeSet = sharedPrefs.getLong("pref_key_alarm", 0);
    	if(timeSet > 0) {
    		long timeAlarm;
    		if(currentTime >= timeSet){
    			Calendar c = Calendar.getInstance();
    			c.setTimeInMillis(timeSet);
    			c.add(Calendar.DATE, 1);
    			timeAlarm = c.getTimeInMillis();
    		} else {
    			timeAlarm = timeSet; 
    		}
    		activateAlarm(pendingAlarm, timeAlarm);
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
//		btnRefresh = (Button) findViewById(R.id.btnRefresh);
		tvMsg = (TextView) findViewById(R.id.tvMsg);
//		tvTopLeft = (TextView) findViewById(R.id.tvTopLeft);
//		tvTopRight = (TextView) findViewById(R.id.tvTopRight);
		//tvBottomLeft = (TextView) findViewById(R.id.tvBottomLeft);
		//tvBottomRight = (TextView) findViewById(R.id.tvBottomRight);
		btnTest = (Button) findViewById(R.id.btnTest);
		
		tvMsg.setText("Welcome to lampidroid!!");
		
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

//		btnRefresh.setOnClickListener(new Button.OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				launchUdpTask();
//				
//			}
//			
//		});

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
 
        boolean smsActive = sharedPrefs.getBoolean("pref_key_inbound_sms", false);
        
        if(smsActive) {
//        	tvTopLeft.setTextColor(Color.GREEN);
//        	tvTopLeft.setText("Notifica sms abilitata");
        } else {
//        	tvTopLeft.setTextColor(Color.RED);
//        	tvTopLeft.setText("Notifica sms disabilitata");
        }

        long alarmActive = sharedPrefs.getLong("pref_key_alarm", 0);
        
        if(alarmActive > 0) {
//        	tvTopRight.setTextColor(Color.GREEN);
	        Date date = new Date(alarmActive);
	        DateFormat formatter = new SimpleDateFormat("dd/MM/yyy HH:mm");
	        String dateFormatted = formatter.format(date);
        	
	        StringBuilder builder = new StringBuilder();
	        builder.append("Alert attivo:")
	        	.append("\n\t: "  + dateFormatted);
//        	tvTopRight.setText(builder);
        } else {
//        	tvTopLeft.setTextColor(Color.RED);
//        	tvTopLeft.setText("Alert disabilitato");
        }
        
    }

}
