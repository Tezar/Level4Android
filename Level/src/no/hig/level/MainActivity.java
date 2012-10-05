package no.hig.level;


import java.lang.reflect.Array;
import java.util.Arrays;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
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
	private int halfSquareSize;
	private int centerX;
	private int centerY;
	private ImageView bubble;
	private LayoutParams layParams;
	
	private SensorHandler currentHandler;
	
	
	private ProgressDialog  calibrationDialog;
	private int calibrationPosition;
	private final int BUFFER_SIZE = 50;
	private float[] calibrationBufferX;
	private float[] calibrationBufferY;
	private float[] calibrationBufferZ;
	
	private float calX=0;
	private float calY=0;
	private float calZ=0;
	private int offX;
	private int offY;
	
	
	/////////////////////////////

	interface SensorHandler{
		void onEvent(SensorEvent event);
		
	}
	
	class DisplayHandler implements SensorHandler{
		public void onEvent(SensorEvent event){
			
			event.values[0] -= calX;
			event.values[1] -= calY;
			event.values[2] -= calZ;
			
			//scale to 0..1
			event.values[0] /= 10; 
			event.values[1] /= 10;
			event.values[2] /= 10;
			
	    	TextView label = (TextView) findViewById(R.id.textView1);
	    	label.setText( Float.toString(event.values[0]) );
	    	
	    	label = (TextView) findViewById(R.id.textView2);
	    	label.setText( Float.toString(event.values[1]) );
	    	
	    	label = (TextView) findViewById(R.id.textView3);
	    	label.setText( Float.toString(event.values[2]) );
	    	
	    	
	    	layParams.setMargins(centerX+Math.round(halfSquareSize*event.values[0])-offX, centerY-Math.round(halfSquareSize*event.values[1])-offY, 0, 0);
	    	bubble.setLayoutParams(layParams);
		}
	}
	
	class CalibrationHandler implements SensorHandler{
		public void onEvent(SensorEvent event){
			calibrationBufferX[calibrationPosition] =  event.values[0];
			calibrationBufferY[calibrationPosition] =  event.values[1];
			calibrationBufferZ[calibrationPosition] =  event.values[2];
			
			calibrationDialog.setProgress(calibrationPosition);
			
			calibrationPosition++;
			if(calibrationPosition>=BUFFER_SIZE){
				stopCalibration();	
			}
		}
	}	
	
	
	/*************************************/
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
    	mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    	mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
    	
    	if(mSensor == null){
    		Toast.makeText(this, R.string.sensor_not_found , Toast.LENGTH_LONG).show();
    	}
    	
    	currentHandler = new DisplayHandler();
    	
    	//get images dimension so we can offset it for accurate centering 
    	BitmapDrawable bd=(BitmapDrawable) this.getResources().getDrawable(R.drawable.bubble);
    	offX=bd.getBitmap().getHeight() / 2 ;
    	offY=bd.getBitmap().getWidth() / 2 ;

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

    
    
    /* function for calibration */
    
    public void startCalibration(){
    	//create calibration buffers
    	calibrationBufferX = new float[BUFFER_SIZE];
    	calibrationBufferY = new float[BUFFER_SIZE];
    	calibrationBufferZ = new float[BUFFER_SIZE];
    	calibrationPosition = 0;
    	
    	Resources res = getResources();
    	calibrationDialog =  new ProgressDialog( this );
    	calibrationDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    	calibrationDialog.setMessage(res.getString(R.string.calibrating));
    	calibrationDialog.setMax(BUFFER_SIZE);
    	calibrationDialog.setCancelable(false);
    	
    	calibrationDialog.show();
    	
    	currentHandler = new CalibrationHandler();
    	
    	
    	
    }
    
    public void stopCalibration(){
    	//return handler to displaying so this function isn't called mre than once
    	currentHandler = new DisplayHandler();

    	Arrays.sort(calibrationBufferX);
    	calX = calibrationBufferX[ BUFFER_SIZE / 2];
    	
    	Arrays.sort(calibrationBufferY);
    	calY = calibrationBufferY[ BUFFER_SIZE / 2];
    	
    	Arrays.sort(calibrationBufferZ);
    	calZ = calibrationBufferZ[ BUFFER_SIZE / 2];    	
    	
    	//hide our dialog
    	calibrationDialog.dismiss();
    	
    	// delete reference to array, so they get erased 
    	calibrationBufferX = null;
    	calibrationBufferY = null;
    	calibrationBufferZ = null;
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
    		case R.id.menu_calibrate:
    			startCalibration();
    			return true;        	
	        default:
	        	return super.onOptionsItemSelected(item);
    	}
    }
    
    void prepareBubble() {
    	
    	MediaPlayer mpLeveled = MediaPlayer.create(this, R.raw.clank);

    	 DisplayMetrics metrics = new DisplayMetrics();
         getWindowManager().getDefaultDisplay().getMetrics(metrics);
         int width = metrics.widthPixels;
         int height = metrics.heightPixels;

         layParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
    	
    	bubble = (ImageView) findViewById(R.id.bubble);

    	halfSquareSize = Math.min(width, height)/2;
    	centerX = width/2;
    	centerY = height/2;
    	//RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
    	//rl.setLayoutParams(params);

    	
    	//when the bubble is at the leveled point, place the following method
    	mpLeveled.start();
    }
    
}
