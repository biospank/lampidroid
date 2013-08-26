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
import android.content.SharedPreferences.Editor;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
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
	private ImageView icLocation;
	private ImageView icSms;
	private ImageView icAlarm;
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
				boolean notify = sharedPrefs.getBoolean(LampSettingsActivity.SMS_KEY_PREF, false);
				if(notify) {
					launchHttpTask();
				}
				
			}

			if(intent.getAction().equals(ALARM_LAMP_ACTION)) {
				boolean notify = sharedPrefs.getBoolean(LampSettingsActivity.ALARM_KEY_ACTIVE, false);
				if(notify) {
					launchHttpTask();
				}
				
			}
				
		}

	};
	
	@Override
	public void onTaskBegin() {
		icLocation.setImageResource(R.drawable.ic_location_search);

//		Thread anim = new Thread(new Runnable() {
//			
//			@Override
//			public void run() {
//				try {
//					for(int i = 0; i < 4; i++) {
//						icLocation.setImageResource(R.drawable.ic_location_off);
//							Thread.sleep(500);
//						icLocation.setImageResource(R.drawable.ic_location_search);
//						Thread.sleep(500);
//					}
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				
//			}
//		});
//		
//		anim.run();
			
	}
	
	@Override
	public void onTaskCompleted() {
		String ip = cUdp.getLampiIp();
		if(ip == null) {
//			Log.d("task completed", "no device found!!");
			icLocation.setImageResource(R.drawable.ic_location_off);
			Toast.makeText(icLocation.getContext(), "Device not found: check lan cable connection on lamp device.", Toast.LENGTH_LONG).show();
		} else {
//			Log.d("task completed", "device found!!");
			icLocation.setImageResource(R.drawable.ic_location_found);
			Toast.makeText(icLocation.getContext(), "Device found: ip address " + ip, Toast.LENGTH_LONG).show();
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
 
        case R.id.menu_test:
    		launchHttpTask();

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
//    	long currentTime = System.currentTimeMillis();
		boolean activeAlarm = sharedPrefs.getBoolean(LampSettingsActivity.ALARM_KEY_ACTIVE, false);
//        long timeSet = sharedPrefs.getLong(LampSettingsActivity.ALARM_KEY_PREF, 0);

        if(activeAlarm) {
//    		long timeAlarm;
//    		if(currentTime >= timeSet){
//    			Calendar c = Calendar.getInstance();
//    			if(timeSet > 0) {
//        			c.setTimeInMillis(timeSet);
//    			} else {
//        			c.setTimeInMillis(currentTime);
//    			}
//    			c.add(Calendar.DATE, 1);
//    			timeAlarm = c.getTimeInMillis();
//    		} else {
//    			timeAlarm = timeSet; 
//    		}
    		activateAlarm(pendingAlarm, getTimeAlarm());
    	} else {
    		deactivateAlarm(pendingAlarm);
    	}
        
        showIconStateFor(R.id.icAlarm);
        showIconStateFor(R.id.icSms);
		
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
		icLocation = (ImageView) findViewById(R.id.icLocation);
		icSms = (ImageView) findViewById(R.id.icSms);
		icAlarm = (ImageView) findViewById(R.id.icAlarm);
//		tvTopLeft = (TextView) findViewById(R.id.tvTopLeft);
//		tvTopRight = (TextView) findViewById(R.id.tvTopRight);
		//tvBottomLeft = (TextView) findViewById(R.id.tvBottomLeft);
		//tvBottomRight = (TextView) findViewById(R.id.tvBottomRight);
		
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		
        showIconStateFor(R.id.icAlarm);
        showIconStateFor(R.id.icSms);
		
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

		icSms.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				changeStateFor(R.id.icSms);
			}
			
		});
		
		icAlarm.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				changeStateFor(R.id.icAlarm);
			}
			
		});
		
		icLocation.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(cUdp.getLampiIp() == null) {
					launchUdpTask();
				}
			}
			
		});
		
	}
	
	private void changeStateFor(int resId) {
		Editor editor = sharedPrefs.edit();
		
		switch (resId) {
		case R.id.icSms:
			if(sharedPrefs.getBoolean(LampSettingsActivity.SMS_KEY_PREF, false)) {
				editor.putBoolean(LampSettingsActivity.SMS_KEY_PREF, false);
				Toast.makeText(icSms.getContext(), "Sms off!", Toast.LENGTH_SHORT).show();
			} else {
				editor.putBoolean(LampSettingsActivity.SMS_KEY_PREF, true);
				Toast.makeText(icSms.getContext(), "Sms on!", Toast.LENGTH_SHORT).show();
			}
			break;

		case R.id.icAlarm:
			if(sharedPrefs.getBoolean(LampSettingsActivity.ALARM_KEY_ACTIVE, false)) {
	    		deactivateAlarm(pendingAlarm);
				editor.putBoolean(LampSettingsActivity.ALARM_KEY_ACTIVE, false);
				Toast.makeText(icAlarm.getContext(), "Alarm off!", Toast.LENGTH_SHORT).show();
			} else {
//		    	long currentTime = System.currentTimeMillis();
//		        long timeSet = sharedPrefs.getLong(LampSettingsActivity.ALARM_KEY_PREF, 0);
//	    		long timeAlarm;
//	    		if(currentTime >= timeSet){
//	    			Calendar c = Calendar.getInstance();
//	    			if(timeSet > 0) {
//	        			c.setTimeInMillis(timeSet);
//	    			} else {
//	        			c.setTimeInMillis(currentTime);
//	    			}
//	    			c.add(Calendar.DATE, 1);
//	    			timeAlarm = c.getTimeInMillis();
//	    		} else {
//	    			timeAlarm = timeSet; 
//	    		}
	    		long timeAlarm = getTimeAlarm();
	    		activateAlarm(pendingAlarm, timeAlarm);
    			editor.putLong(LampSettingsActivity.ALARM_KEY_PREF, timeAlarm);
				editor.putBoolean(LampSettingsActivity.ALARM_KEY_ACTIVE, true);
				Toast.makeText(icAlarm.getContext(), "Alarm on!", Toast.LENGTH_SHORT).show();
			}
			break;

		}

		editor.apply();
		
		showIconStateFor(resId);
	}
	
	private void showIconStateFor(int resId) {
		switch (resId) {
		case R.id.icSms:
			if(sharedPrefs.getBoolean(LampSettingsActivity.SMS_KEY_PREF, false)) {
				icSms.setImageResource(R.drawable.ic_sms_active);
			} else {
				icSms.setImageResource(R.drawable.ic_sms);
			}
			break;

		case R.id.icAlarm:
			boolean activeAlarm = sharedPrefs.getBoolean(LampSettingsActivity.ALARM_KEY_ACTIVE, false);
			long timeSet = sharedPrefs.getLong(LampSettingsActivity.ALARM_KEY_PREF, 0);
			if(activeAlarm) {
				icAlarm.setImageResource(R.drawable.ic_alarm_active);
			} else {
				icAlarm.setImageResource(R.drawable.ic_alarm);
			}
			break;

		}

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
		
		if(cUdp.getLampiIp() == null) {
			new HttpNotifyTask().execute("192.168.1.2");
		} else {
			new HttpNotifyTask().execute(cUdp.getLampiIp());
		}
	}

	private void showUserSettings() {
 
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
	
	private long getTimeAlarm() {
    	long currentTime = System.currentTimeMillis();
        long timeSet = sharedPrefs.getLong(LampSettingsActivity.ALARM_KEY_PREF, 0);
		long timeAlarm;
		if(currentTime >= timeSet){
			Calendar c = Calendar.getInstance();
			if(timeSet > 0) {
    			c.setTimeInMillis(timeSet);
			} else {
    			c.setTimeInMillis(currentTime);
			}
			c.add(Calendar.DATE, 1);
			timeAlarm = c.getTimeInMillis();
		} else {
			timeAlarm = timeSet; 
		}

		return timeAlarm;
	}

}
