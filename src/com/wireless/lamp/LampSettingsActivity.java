/**
 * 
 */
package com.wireless.lamp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
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
	public static final String ALARM_KEY_PREF = "pref_key_alarm";
	public static final String CONTACT_KEY_PREF = "pref_key_inbound_sms_contact";
	
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

        SharedPreferences sharedPrefs = PreferenceManager
	            .getDefaultSharedPreferences(this);
        Preference alarmPref = findPreference(ALARM_KEY_PREF);
        long timeSet = sharedPrefs.getLong(ALARM_KEY_PREF, 0);
        if(timeSet == 0)
        	showSummary(alarmPref, null, false);
        else
        	showSummary(alarmPref, timeSet, true);
        
        	
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
		if(key.equals(ALARM_KEY_PREF)) {
	        Preference alarmPref = findPreference(key);
	        long timeSet = sharedPreferences.getLong(key, 0);
	        if(timeSet == 0)
	        	showSummary(alarmPref, null, false);
	        else
	        	showSummary(alarmPref, timeSet, true);
		}
		
	}
	
	private void showSummary(Preference pref, Long timeSet, boolean show) {
		if(show) {
	        Date date = new Date(timeSet);
	        DateFormat formatter = new SimpleDateFormat("HH:mm");
	        String dateFormatted = formatter.format(date);
	        pref.setSummary("Notifica alle ore: " + dateFormatted);
		} else {
	        pref.setSummary("Nessuna notifica");
		}
	}

	
}
