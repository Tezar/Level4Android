package no.hig.level;


import android.app.Activity;
import android.app.ProgressDialog;
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
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mSensor;
	private int squareSize;
	private int centerX;
	private int centerY;
	private ImageView bubble;
	private LayoutParams layParams;
	
	private SensorHandler currentHandler;
	
	
	/////////////////////////////

	interface SensorHandler{
		void onEvent(SensorEvent event);
		
	}
	
	class DisplayHandler implements SensorHandler{
		public void onEvent(SensorEvent event){
	    	TextView label = (TextView) findViewById(R.id.textView1);
	    	label.setText( Float.toString(event.values[0]) );
	    	
	    	label = (TextView) findViewById(R.id.textView2);
	    	label.setText( Float.toString(event.values[1]) );
	    	
	    	label = (TextView) findViewById(R.id.textView3);
	    	label.setText( Float.toString(event.values[2]) );
	    	
	    	
	    	layParams.setMargins(centerX+Math.round(squareSize*event.values[0]), centerY+Math.round(squareSize*event.values[1]), 0, 0);
	    	bubble.setLayoutParams(layParams);
		}
	}
	
	class CalibrationHandler implements SensorHandler{
		public void onEvent(SensorEvent event){
			
		}
	}	
	
	
	/*************************************/
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
    	mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    	mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
    	
    	if(mSensor == null){
    		Toast.makeText(this, R.string.sensor_not_found , Toast.LENGTH_LONG).show();
    	}
    	
    	currentHandler = new DisplayHandler();
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
        prepareBubble();
    }

    protected void onPause() {
        super.onPause();
        if(mSensor != null){
        	mSensorManager.unregisterListener(this);
        }
    }

    
    
    /* callbacks for Sensors */
    
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void onSensorChanged(SensorEvent event) {
    	currentHandler.onEvent(event);
    }
    
    
    /* menu handling */
    
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
    
    void prepareBubble() {

    	 DisplayMetrics metrics = new DisplayMetrics();
         getWindowManager().getDefaultDisplay().getMetrics(metrics);
         int width = metrics.widthPixels;
         int height = metrics.heightPixels;

         layParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
    	
    	bubble = (ImageView) findViewById(R.id.bubble);

    	squareSize = Math.min(width, height);
    	centerX = width/2;
    	centerY = height/2;
    	//RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
    	//rl.setLayoutParams(params);

    }
    
}
