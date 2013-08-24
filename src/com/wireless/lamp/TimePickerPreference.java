package com.wireless.lamp;

import java.util.Calendar;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;

public class TimePickerPreference extends DialogPreference {
//	Context mContext;
	private TimePicker tpkAlarm;
	SharedPreferences sharedPrefs;

	
	public TimePickerPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	
//		mContext = context;
		
		sharedPrefs = PreferenceManager
	            .getDefaultSharedPreferences(context);
		
		setPersistent(false);
        
        setDialogLayoutResource(R.layout.timepicker_dialog);
        
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		SharedPreferences.Editor editor = sharedPrefs.edit();
		
	    // When the user selects "OK", persist the new value
	    if (positiveResult) {
	    	setAlarm(editor, true);
	
	    } else {
	    	setAlarm(editor, false);
	    }

		editor.apply();

	}
	
	private void setAlarm(SharedPreferences.Editor editor, boolean display) {
		if(display) {
			Integer hour = tpkAlarm.getCurrentHour();
			Integer minute = tpkAlarm.getCurrentMinute();
	
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.HOUR_OF_DAY, hour);
			cal.set(Calendar.MINUTE, minute);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			
			editor.putLong(LampSettingsActivity.ALARM_KEY_PREF, cal.getTimeInMillis());
			editor.putBoolean(LampSettingsActivity.ALARM_KEY_ACTIVE, true);
		} else {
			// l'informazione dell'orario non viene rimossa se l'allarme non viene settato
			//editor.remove(LampSettingsActivity.ALARM_KEY_PREF);
			editor.putBoolean(LampSettingsActivity.ALARM_KEY_ACTIVE, false);
		}
	}

	@Override
    public void onBindDialogView(View view){
		tpkAlarm = (TimePicker)view.findViewById(R.id.tpkAlarm);
		tpkAlarm.setIs24HourView(true);

		boolean activeAlarm = sharedPrefs.getBoolean(LampSettingsActivity.ALARM_KEY_ACTIVE, false);

		if(activeAlarm) {
			long timeSet = sharedPrefs.getLong(LampSettingsActivity.ALARM_KEY_PREF, 0);
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(timeSet);
			tpkAlarm.setCurrentHour(cal.get(Calendar.HOUR_OF_DAY));
			tpkAlarm.setCurrentMinute(cal.get(Calendar.MINUTE));
		}
		
		
        super.onBindDialogView(view);
    }	
	
//	@Override
//    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
//            builder.setTitle(R.string.pin_changepin_title);
//            builder.setPositiveButton(null, null);
//            builder.setNegativeButton(null, null);
//            super.onPrepareDialogBuilder(builder);  
//    }
	
//	@Override
//	public void onClick(DialogInterface dialog, int which) { 
//		super.onClick(dialog, which);
//		if (DialogInterface.BUTTON_POSITIVE == which) { 
//			SharedPreferences sharedPrefs = PreferenceManager
//	                .getDefaultSharedPreferences(mContext);
//			
//			SharedPreferences.Editor editor = sharedPrefs.edit();
//			//editor.pu
//		}
//	}
}
