package no.hig.level;


import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;




public class PreferencesActivity extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        
        //http://stackoverflow.com/questions/2691772/android-preferences-how-to-load-the-default-values-when-the-user-hasnt-used-th
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

    }
}
