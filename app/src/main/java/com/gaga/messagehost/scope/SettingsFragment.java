package com.gaga.messagehost.scope;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

import com.gaga.messagehost.R;

public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String KEY_PREF_INPUT = "pref_input";
    private static final String KEY_PREF_ABOUT = "pref_about";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource

        addPreferencesFromResource(R.xml.preferences);

        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(getActivity());

        preferences.registerOnSharedPreferenceChangeListener(this);

        // Get about summary
        Preference about = findPreference(KEY_PREF_ABOUT);
        String sum = (String) about.getSummary();

        // Get context and package manager
        Context context = getActivity();
        PackageManager manager = context.getPackageManager();

        // Get info
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo("com.gaga.messagehost", 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        // Set version in text view
        if (info != null) {
            String s = String.format(sum, info.versionName);
            about.setSummary(s);
        }
    }

    // On preference tree click
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
                                         Preference preference) {
        boolean result =
                super.onPreferenceTreeClick(preferenceScreen, preference);

        if (preference instanceof PreferenceScreen) {
            Dialog dialog = ((PreferenceScreen) preference).getDialog();
            ActionBar actionBar = dialog.getActionBar();
            actionBar.setDisplayHomeAsUpEnabled(false);
        }

        return result;
    }

    // On shared preference changed
    @Override
    public void onSharedPreferenceChanged(SharedPreferences preferences,
                                          String key) {
        if (key.equals(KEY_PREF_INPUT)) {
            Preference preference = findPreference(key);
            // Set summary to be the user-description for the selected value
            preference.setSummary(((ListPreference) preference).getEntry());
        }
    }
}
