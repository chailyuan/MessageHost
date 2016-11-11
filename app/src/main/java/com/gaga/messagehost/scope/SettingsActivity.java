package com.gaga.messagehost.scope;

import android.app.Activity;
import android.os.Bundle;
import android.app.ActionBar;
import android.view.MenuItem;

import com.gaga.messagehost.R;

public class SettingsActivity extends Activity {
    // On create

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();

        // Enable back navigation on action bar
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.settings);
    }

    // On options item selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Switch on item id
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;

            default:
                return false;
        }
        return true;
    }
}
