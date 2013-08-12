/**
 * 
 */
package com.wireless.lamp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;

/**
 * @author biospank
 *
 */
public class LampSettingsActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {

	private static final int PICK_CONTACT = 1001;
	
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
		if(key.equals("pref_key_inbound_sms_contact")) {
//			Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
//			startActivityForResult(contactPickerIntent, PICK_CONTACT);
		}
		if(key.equals("pref_key_alarm")) {
	        Preference alarmPref = findPreference(key);
	        long timeMillis = sharedPreferences.getLong(key, 0);
	        if(timeMillis == 0) {
	        	alarmPref.setSummary("Nessuna notifica ");
	        } else {
		        Date date = new Date(timeMillis);
		        DateFormat formatter = new SimpleDateFormat("HH:mm");
		        String dateFormatted = formatter.format(date);
				alarmPref.setSummary("Notifica alle ore: " + dateFormatted);
	        }
		}
		
	}

	
}
