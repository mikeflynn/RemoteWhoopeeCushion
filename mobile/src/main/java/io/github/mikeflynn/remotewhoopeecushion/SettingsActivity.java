package io.github.mikeflynn.remotewhoopeecushion;

import android.app.Activity;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.List;


public class SettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new PrefsFragment())
                .commit();
    }

    public static class PrefsFragment extends PreferenceFragment {
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);

            // Set the fart options dynamically
            List<String> farts = FartsActivity.getAllFarts(getActivity());

            // Set display names
            ArrayList<String> fartNames = new ArrayList<String>();
            for(int i = 0; i < farts.size(); i++) {
                fartNames.add(farts.get(i).toUpperCase());
            }

            ListPreference fartOptions = new ListPreference(getActivity());
            fartOptions.setKey("io.github.mikeflynn.remotewhoopeecushion.fart_type");
            fartOptions.setTitle(R.string.settings_header_fart_type);
            fartOptions.setSummary(R.string.settings_desc_fart_type);
            fartOptions.setDialogTitle(R.string.settings_header_fart_type);
            fartOptions.setDefaultValue("chipotle");
            fartOptions.setEntries(fartNames.toArray(new CharSequence[farts.size()]));
            fartOptions.setEntryValues(farts.toArray(new CharSequence[farts.size()]));
            fartOptions.setOrder(0);

            PreferenceCategory fartPrefs = (PreferenceCategory)findPreference("farts");
            fartPrefs.addPreference(fartOptions);
        }
    }
}
