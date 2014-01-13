package pi.lamp;

import java.lang.reflect.Method;
import java.util.Calendar;
import pi.lamp.R;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.TextView.BufferType;
import android.widget.Toast;

public class LampActivity extends Activity implements OnTaskListener {
	
	public static final String SMS_LAMP_ACTION = "SMS_LAMP_ACTION";
	public static final String CHAT_LAMP_ACTION = "CHAT_LAMP_ACTION";
	public static final String ALARM_LAMP_ACTION = "ALARM_LAMP_ACTION";
	private static final int RESULT_SETTINGS = 1;
    private UdpClientTask cUdp;
//    private ChatNotificationService sCn;
	private ImageView icLocation;
	private ImageView icChat;
	private ImageView icSms;
	private ImageView icAlarm;
	private TextView tvAlarm;
	private TextView tvChat;
	private TextView tvSms;
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

			// bring lamp to front
//			Intent lampActivityIntent = new Intent(context, LampActivity.class);
//			lampActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//			context.startActivity(lampActivityIntent);
			
			if(intent.getAction().equals(SMS_LAMP_ACTION)) {
				boolean notify = sharedPrefs.getBoolean(LampSettingsActivity.SMS_KEY_PREF, false);
				if(notify) {
					launchHttpTask(HttpNotifyTask.LAMPI_NOTIFY_ACTION);
				}
				
			}

			if(intent.getAction().equals(CHAT_LAMP_ACTION)) {
				boolean notify = sharedPrefs.getBoolean(LampSettingsActivity.CHAT_KEY_PREF, false);
				if(notify) {
					launchHttpTask(HttpNotifyTask.LAMPI_NOTIFY_ACTION);
				}
				
			}

			if(intent.getAction().equals(ALARM_LAMP_ACTION)) {
				boolean notify = sharedPrefs.getBoolean(LampSettingsActivity.ALARM_KEY_ACTIVE, false);
				if(notify) {
					launchHttpTask(HttpNotifyTask.LAMPI_NOTIFY_ACTION);
				}
				
	            long timeSet = sharedPrefs.getLong(LampSettingsActivity.ALARM_KEY_PREF, 0);
	    		long timeAlarm = LampUtil.getTimeAlarmFor(timeSet);
	    		activateAlarm(pendingAlarm, timeAlarm);
				String formattedDate = LampUtil.getFullFormattedDateFor(timeAlarm, getApplicationContext());
				tvAlarm.setText(LampUtil.getAlarmSummarySpanText(formattedDate), BufferType.SPANNABLE);
		        
			}
				
		}

	};
	
	@Override
	public void onTaskBegin() {
		icLocation.setImageResource(R.drawable.ic_location_search);
	}
	
	@Override
	public void onTaskCompleted() {
		String ip = cUdp.getLampiIp();
		if(ip == null) {
//			Log.d("task completed", "no device found!!");
			icLocation.setImageResource(R.drawable.ic_location_off);
			Toast.makeText(icLocation.getContext(), "Device not found: check lamp device.", Toast.LENGTH_LONG).show();
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
		
//		Intent i = new Intent(this, ChatNotificationService.class);
//        startService(i);

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
 
//        case R.id.menu_settings:
//            Intent settingsIntent = new Intent(this, LampSettingsActivity.class);
//            startActivityForResult(settingsIntent, RESULT_SETTINGS);
//            break;
 
        case R.id.menu_test:
    		launchHttpTask(HttpNotifyTask.LAMPI_NOTIFY_ACTION);

            break;
 
        case R.id.menu_reset:
    		launchHttpTask(HttpNotifyTask.LAMPI_RESET_ACTION);

            break;
 
        case R.id.menu_about:
        	AlertDialog.Builder builder=new AlertDialog.Builder(this);
        	builder.setIcon(R.drawable.ic_launcher);
        	builder.setTitle("About");
        	builder.setMessage("Lamp v. 1.2");
        	builder.setCancelable(true);
        	builder.create();
        	builder.show();
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

        if(activeAlarm) {
            long timeSet = sharedPrefs.getLong(LampSettingsActivity.ALARM_KEY_PREF, 0);
    		activateAlarm(pendingAlarm, LampUtil.getTimeAlarmFor(timeSet));
    	} else {
    		deactivateAlarm(pendingAlarm);
    	}
        
        showIconStateFor(R.id.icAlarm);
        showIconStateFor(R.id.icSms);
        showIconStateFor(R.id.icChat);
		
	}

	@Override
	protected void onResume() {
		//registerReceiver(intentReceiver, intentFilter);
		// gestione audio
		// startService(audioCaptureIntent);
		showNotificationIcon(false);
		super.onResume();
		
	}
	
	@Override
	protected void onDestroy() {
		unregisterReceiver(intentReceiver);
		//unregisterReceiver(alarmReceiver);
		//deactivateAlarm(pendingAlarm);
		showNotificationIcon(false);
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		//unregisterReceiver(intentReceiver);
		// gestione audio
		// stopService(audioCaptureIntent);
		if(!isFinishing())
			showNotificationIcon(true);
		super.onPause();
		
	}

	protected void showNotificationIcon(boolean show) {
		NotificationManager mNotificationManager =
			    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		int mId = 999;
		if(show) {
			NotificationCompat.Builder mBuilder =
			        new NotificationCompat.Builder(this)
			        .setSmallIcon(R.drawable.ic_notification)
			        .setContentTitle("Lamp")
			        .setContentText("Tap to show.");
			// Creates an explicit intent for an Activity in your app
			Intent resultIntent = new Intent(this, LampActivity.class);

			// The stack builder object will contain an artificial back stack for the
			// started Activity.
			// This ensures that navigating backward from the Activity leads out of
			// your application to the Home screen.
			TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
			// Adds the back stack for the Intent (but not the Intent itself)
			stackBuilder.addParentStack(LampActivity.class);
			// Adds the Intent that starts the Activity to the top of the stack
			stackBuilder.addNextIntent(resultIntent);
			PendingIntent resultPendingIntent =
			        stackBuilder.getPendingIntent(
			            0,
			            PendingIntent.FLAG_UPDATE_CURRENT
			        );
			mBuilder.setContentIntent(resultPendingIntent);
			// mId allows you to update the notification later on.
			mNotificationManager.notify(mId , mBuilder.build());
		} else {
			mNotificationManager.cancel(mId);
		}
	}
	
	protected void initializeView() {
//		btnRefresh = (Button) findViewById(R.id.btnRefresh);
		icLocation = (ImageView) findViewById(R.id.icLocation);
		icSms = (ImageView) findViewById(R.id.icSms);
		icChat = (ImageView) findViewById(R.id.icChat);
		icAlarm = (ImageView) findViewById(R.id.icAlarm);
		tvAlarm = (TextView) findViewById(R.id.tvAlarm);
		tvChat = (TextView) findViewById(R.id.tvChat);
		tvSms = (TextView) findViewById(R.id.tvSms);
		
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		
        showIconStateFor(R.id.icAlarm);
        showIconStateFor(R.id.icSms);
        showIconStateFor(R.id.icChat);
		
		// gestione audio
//		chkAudio = (CheckBox) findViewById(R.id.chkAudio);
//		audioCaptureIntent =  new Intent(this, AudioCaptureService.class);
//        sCn = new ChatNotificationService(this);

		intentFilter = new IntentFilter();
		intentFilter.addAction(SMS_LAMP_ACTION);
		intentFilter.addAction(ALARM_LAMP_ACTION);
		intentFilter.addAction(CHAT_LAMP_ACTION);
		
		registerReceiver(intentReceiver, intentFilter);

		alarmIntent = new Intent(ALARM_LAMP_ACTION);
		pendingAlarm = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);

		icChat.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				changeStateFor(R.id.icChat, null);
			}
			
		});
		
		icSms.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				changeStateFor(R.id.icSms, null);
			}
			
		});
		
		icAlarm.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				changeStateFor(R.id.icAlarm, null);
			}
			
		});
		
		tvAlarm.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {

				Calendar timeCalendar = LampUtil.getCalendarFor(sharedPrefs.getLong(LampSettingsActivity.ALARM_KEY_PREF, 0));

				TimePickerDialog timePickerDialog = new TimePickerDialog(LampActivity.this, new OnTimeSetListener() {
					
					@Override
					public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
						Calendar cal = Calendar.getInstance();
						cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
						cal.set(Calendar.MINUTE, minute);
						cal.set(Calendar.SECOND, 0);
						cal.set(Calendar.MILLISECOND, 0);

						changeStateFor(R.id.tvAlarm, LampUtil.getCalendarFor(cal.getTimeInMillis()));
					}
					
				}, timeCalendar.get(Calendar.HOUR_OF_DAY), timeCalendar.get(Calendar.MINUTE), true);
				
				timePickerDialog.show();
			}
			
		});
		
		icLocation.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				launchUdpTask();
			}
			
		});
		
	}
	
	private void changeStateFor(int resId, Object extra) {
		Editor editor = sharedPrefs.edit();
		String formattedText;
		
		switch (resId) {
		case R.id.icChat:
			if(sharedPrefs.getBoolean(LampSettingsActivity.CHAT_KEY_PREF, false)) {
				editor.putBoolean(LampSettingsActivity.CHAT_KEY_PREF, false);
				formattedText = "Chat alert off!";
			} else {
				editor.putBoolean(LampSettingsActivity.CHAT_KEY_PREF, true);
				formattedText = "Chat alert on!";
			}

			tvChat.setText(LampUtil.getChatSummarySpanText(formattedText));
			Toast.makeText(icChat.getContext(), formattedText, Toast.LENGTH_SHORT).show();
			
			break;

		case R.id.icSms:
			if(sharedPrefs.getBoolean(LampSettingsActivity.SMS_KEY_PREF, false)) {
				editor.putBoolean(LampSettingsActivity.SMS_KEY_PREF, false);
				formattedText = "Sms alert off!";
			} else {
				editor.putBoolean(LampSettingsActivity.SMS_KEY_PREF, true);
				formattedText = "Sms alert on!";
			}

			tvSms.setText(LampUtil.getSmsSummarySpanText(formattedText));
			Toast.makeText(icSms.getContext(), formattedText, Toast.LENGTH_SHORT).show();
			
			break;

		case R.id.icAlarm:
			if(sharedPrefs.getBoolean(LampSettingsActivity.ALARM_KEY_ACTIVE, false)) {
	    		deactivateAlarm(pendingAlarm);
				editor.putBoolean(LampSettingsActivity.ALARM_KEY_ACTIVE, false);
				Toast.makeText(icAlarm.getContext(), "Alarm off!", Toast.LENGTH_SHORT).show();
			} else {
	            long timeSet = sharedPrefs.getLong(LampSettingsActivity.ALARM_KEY_PREF, 0);
	    		long timeAlarm = LampUtil.getTimeAlarmFor(timeSet);
	    		activateAlarm(pendingAlarm, timeAlarm);
    			editor.putLong(LampSettingsActivity.ALARM_KEY_PREF, timeAlarm);
				editor.putBoolean(LampSettingsActivity.ALARM_KEY_ACTIVE, true);
				String formattedDate = LampUtil.getFullFormattedDateFor(timeAlarm, getApplicationContext());
				tvAlarm.setText(LampUtil.getAlarmSummarySpanText(formattedDate), BufferType.SPANNABLE);
				Toast.makeText(icAlarm.getContext(), "Alarm on: " + formattedDate, Toast.LENGTH_SHORT).show();
			}
			break;

		case R.id.tvAlarm:
			long timeAlarm = ((Calendar) extra).getTimeInMillis();
    		activateAlarm(pendingAlarm, timeAlarm);
			editor.putLong(LampSettingsActivity.ALARM_KEY_PREF, timeAlarm);
			String formattedDate = LampUtil.getFullFormattedDateFor(timeAlarm, getApplicationContext());
			tvAlarm.setText(LampUtil.getAlarmSummarySpanText(formattedDate), BufferType.SPANNABLE);
			Toast.makeText(icAlarm.getContext(), "Alarm on: " + formattedDate, Toast.LENGTH_SHORT).show();

			break;
		}

		editor.apply();
		
		showIconStateFor(resId);
	}
	
	private void showIconStateFor(int resId) {
		switch (resId) {
		case R.id.icChat:
			if(sharedPrefs.getBoolean(LampSettingsActivity.CHAT_KEY_PREF, false)) {
				icChat.setImageResource(R.drawable.ic_chat_active);
			} else {
				icChat.setImageResource(R.drawable.ic_chat);
			}
			break;

		case R.id.icSms:
			if(sharedPrefs.getBoolean(LampSettingsActivity.SMS_KEY_PREF, false)) {
//				icSms.setBackgroundResource(R.drawable.summary_border);
				icSms.setImageResource(R.drawable.ic_sms_active);
			} else {
//				icSms.setBackgroundResource(R.drawable.function_border);
				icSms.setImageResource(R.drawable.ic_sms);
			}
			break;

		case R.id.icAlarm:
			boolean activeAlarm = sharedPrefs.getBoolean(LampSettingsActivity.ALARM_KEY_ACTIVE, false);
			if(activeAlarm) {
//				icAlarm.setBackgroundResource(R.drawable.summary_border);
				icAlarm.setImageResource(R.drawable.ic_alarm_active);
			} else {
//				icAlarm.setBackgroundResource(R.drawable.function_border);
				icAlarm.setImageResource(R.drawable.ic_alarm);
			}
			break;

		}

	}
	
	private void launchUdpTask() {
		
		// Kickoff the Client
		//Context ctx = this.getApplicationContext();
		WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		if(isSharingWiFiEnabled(wifi)) {
			cUdp = new UdpClientTask(wifi, this, false);
			cUdp.execute();
		} else {
			if(wifi.getWifiState() == WifiManager.WIFI_STATE_DISABLED) {
				startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));
			} else {
				//if(cUdp == null)
					cUdp = new UdpClientTask(wifi, this, true);
				
				//if(cUdp.getLampiIp() == null)
					//if(cUdp.getStatus() == AsyncTask.Status.PENDING)
						cUdp.execute();
	
			}

		}
	}
	
	private boolean isSharingWiFiEnabled(WifiManager manager) {
	    try {
	        final Method method = manager.getClass().getDeclaredMethod("isWifiApEnabled");
	        method.setAccessible(true); //in the case of visibility change in future APIs
	        return (Boolean) method.invoke(manager);
	    } catch (Throwable ignored) {
	    
	    }

	    return false;
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
	
	private void launchHttpTask(int type) {
		if(cUdp == null) {
			launchUdpTask();
		} else {
			String ip = cUdp.getLampiIp();
			
			if(ip == null) {
				//new HttpNotifyTask().execute("192.168.1.2");
				icLocation.setImageResource(R.drawable.ic_location_off);
				Toast.makeText(icLocation.getContext(), "Device not found: check lamp device.", Toast.LENGTH_LONG).show();
			} else {
				new HttpNotifyTask(type).execute(ip);
				icLocation.setImageResource(R.drawable.ic_location_found);
				//Toast.makeText(icLocation.getContext(), "Device found: ip address " + ip, Toast.LENGTH_LONG).show();
			}
		}
	}

	private void showUserSettings() {
 
        boolean chatActive = sharedPrefs.getBoolean(LampSettingsActivity.CHAT_KEY_PREF, false);
        
        if(chatActive) {
			String formattedText = "Chat alert on!";
			tvChat.setText(LampUtil.getChatSummarySpanText(formattedText));
        } else {
			String formattedText = "Chat alert off!";
			tvChat.setText(LampUtil.getSmsSummarySpanText(formattedText));
        }

        boolean smsActive = sharedPrefs.getBoolean(LampSettingsActivity.SMS_KEY_PREF, false);
        
        if(smsActive) {
			String formattedText = "Sms alert on!";
			tvSms.setText(LampUtil.getSmsSummarySpanText(formattedText));
        } else {
			String formattedText = "Sms alert off!";
			tvSms.setText(LampUtil.getSmsSummarySpanText(formattedText));
        }

        long timeSet = sharedPrefs.getLong(LampSettingsActivity.ALARM_KEY_PREF, 0);
		if(timeSet > 0) {
    		long timeAlarm = LampUtil.getTimeAlarmFor(timeSet);
			String formattedDate = LampUtil.getFullFormattedDateFor(timeAlarm, getApplicationContext());
			tvAlarm.setText(LampUtil.getAlarmSummarySpanText(formattedDate), BufferType.SPANNABLE);
        } else {
			tvAlarm.setText("Tap to set alarm");
        }
        
    }

}
