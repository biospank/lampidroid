/**
 * 
 */
package com.wireless.lamp;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;

/**
 * @author biospank
 *
 */
public class LampSettingsActivity extends PreferenceActivity implements
		OnPreferenceChangeListener {

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
 
        addPreferencesFromResource(R.xml.settings);
 
    }
	
	@Override
	public boolean onPreferenceChange(Preference arg0, Object arg1) {
		// TODO Auto-generated method stub
		return false;
	}

}
