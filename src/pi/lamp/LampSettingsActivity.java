/**
 * 
 */
package pi.lamp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import pi.lamp.R;


import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.PreferenceActivity;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;

/**
 * @author biospank
 *
 */
public class LampSettingsActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {

	private static final int PICK_CONTACT = 1001;
	public static final String SMS_KEY_PREF = "pref_key_inbound_sms";
	public static final String ALARM_KEY_PREF = "pref_key_alarm";
	public static final String ALARM_KEY_ACTIVE = "key_alarm_active";
	public static final String CONTACT_KEY_PREF = "pref_key_inbound_sms_contact";

	SharedPreferences sharedPrefs;

//	@Override
//	public boolean onPreferenceClick(Preference pref) {
//		if(pref.getKey().equals("pref_key_inbound_sms_contact")) {
//			Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
//			startActivityForResult(contactPickerIntent, PICK_CONTACT);
//			return true;
//		}
//		
//		return false;
//	}
	
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
 
        addPreferencesFromResource(R.xml.settings);
        
        Preference contactPref = findPreference("pref_screen_key_inbound_sms_contact");
        Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
		contactPickerIntent.setType(Phone.CONTENT_TYPE);
        contactPref.setIntent(contactPickerIntent);

        sharedPrefs = PreferenceManager
	            .getDefaultSharedPreferences(this);

        Preference alarmPref = findPreference(ALARM_KEY_PREF);

		boolean activeAlarm = sharedPrefs.getBoolean(LampSettingsActivity.ALARM_KEY_ACTIVE, false);

        long timeSet = sharedPrefs.getLong(ALARM_KEY_PREF, 0);

        if(activeAlarm && timeSet > 0)
        	showSummary(alarmPref, timeSet, true);
        else
        	showSummary(alarmPref, timeSet, false);
        
        	
	}
	
	@Override
	protected void onResume() {
	    super.onResume();
	    getPreferenceScreen().getSharedPreferences()
	    	.registerOnSharedPreferenceChangeListener(this);
	}

	@Override 
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences()
			.unregisterOnSharedPreferenceChangeListener(this);
	}
	
	// da implementare al cambio valore
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if(key.equals(CONTACT_KEY_PREF)) {
//			Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
//			startActivityForResult(contactPickerIntent, PICK_CONTACT);
		}

		boolean activeAlarm = sharedPrefs.getBoolean(LampSettingsActivity.ALARM_KEY_ACTIVE, false);

		if(key.equals(ALARM_KEY_PREF)) {
	        Preference alarmPref = findPreference(key);
	        long timeSet = sharedPreferences.getLong(key, 0);

	        if(activeAlarm)
	        	showSummary(alarmPref, timeSet, true);
	        else
	        	showSummary(alarmPref, timeSet, false);
		}
		
	}
	
	private void showSummary(Preference pref, long timeSet, boolean show) {
		if(show) {
			long timeAlarm = LampUtil.getTimeAlarmFor(timeSet);
	        pref.setSummary("Alarm set on: " + LampUtil.getFormattedDateFor(timeAlarm, getApplicationContext()));
		} else {
	        pref.setSummary("No alarm set");
		}
	}

	
}
