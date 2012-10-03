package no.hig.level;


import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.app.Activity;
import android.content.Context;
import android.widget.TextView;

public class PreferencesActivity extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
