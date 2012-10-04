package no.hig.level;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mSensor;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
    	mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    	mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
    	
    	if(mSensor == null){
    		Toast.makeText(this, R.string.sensor_not_found , Toast.LENGTH_LONG).show();
    	}
    }

    
    

    
    protected void onResume() {
        super.onResume();
        if(mSensor != null){
        	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        	int speed = Integer.parseInt(prefs.getString("pref_speed", "0"));
        	
        	int speedConstants[] = {SensorManager.SENSOR_DELAY_UI,SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_GAME, SensorManager.SENSOR_DELAY_FASTEST };

        	assert(speed < speedConstants.length );
        	
        	mSensorManager.registerListener(this, mSensor, speedConstants[speed]);
        	Toast.makeText(this, "speed"+speed, Toast.LENGTH_LONG).show();
        }
    }

    protected void onPause() {
        super.onPause();
        if(mSensor != null){
        	mSensorManager.unregisterListener(this);
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void onSensorChanged(SensorEvent event) {
    	
    	TextView label = (TextView) findViewById(R.id.textView1);
    	label.setText( Float.toString(event.values[0]) );
    	
    	label = (TextView) findViewById(R.id.textView2);
    	label.setText( Float.toString(event.values[1]) );
    	
    	label = (TextView) findViewById(R.id.textView3);
    	label.setText( Float.toString(event.values[2]) );
    }
    
    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
	        case R.id.menu_settings:
	        	startActivity(new Intent(MainActivity.this, PreferencesActivity.class));
	        return true;
	        default:
	        	return super.onOptionsItemSelected(item);
    	}
    }
    
    void setBubble(float percent) {
    	RelativeLayout rl = (RelativeLayout) findViewById(R.id.my_relative_layout); 

    	ImageView iv = (ImageView) findViewById(R.id.imageView1);

    	RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(30, 40); 
    	params.leftMargin = 50; 
    	params.topMargin = 60; 
    	rl.addView(iv, params);
    	
    	Display display = getWindowManager().getDefaultDisplay(); 
    	Point size = new Point(); 
    	((Object) display).getSize(size); 
    	float width = size.x; 
    	float height = size.y; 
    	
    	params.leftMargin = size.x * (100 + 0.5)  + iv.getWidth() * 0.5; 
    	

    }
    
}
