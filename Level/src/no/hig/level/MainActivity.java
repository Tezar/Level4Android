package no.hig.level;


import java.lang.reflect.Array;
import java.text.DecimalFormat;
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
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mSensor;
    
    private int halfSquareSizeX;
    private int halfSquareSizeY;
	
	private int centerX;
	private int centerY;
	
	private ImageView bubbley;
	private ImageView bubblex;
	private LayoutParams layParamsY;
	private LayoutParams layParamsX;
	private int verticalBarPosX;
	private int horizontalBarPosY;
	
	private SensorHandler currentHandler;
	
	
	private ProgressDialog  calibrationDialog;
	private int calibrationPosition;
	private final int BUFFER_SIZE = 200;
	private float[] calibrationBufferX;
	private float[] calibrationBufferY;
	private float[] calibrationBufferZ;
	
	private float calX=0;
	private float calY=0;
	private float calZ=0;
	private int offX;
	private int offY;
	
	
	/////////////////////////////
	/*interface for our handlers */
	interface SensorHandler{
		void onEvent(SensorEvent event);
		
	}
	
	/* Handler for moving bubble around */
	class DisplayHandler implements SensorHandler{
		private DecimalFormat df;

		public DisplayHandler() {
	    	//formater so we dont have to construct it everytime
	    	df = new DecimalFormat(" #00.00°;-#00.00°");
		}
		
		public void onEvent(SensorEvent event){
			
			event.values[0] -= calX;
			event.values[1] -= calY;
			event.values[2] -= calZ;
			
			//scale to 0..1
			event.values[0] /= 9.81; 
			event.values[1] /= 9.81;
			event.values[2] /= 9.81;
			
	    	TextView label = (TextView) findViewById(R.id.textView1);
	    	
	    	
	    	
	    	label.setText( df.format(90-Math.atan2(event.values[2] , event.values[0])/Math.PI*180) );
	    	
	    	label = (TextView) findViewById(R.id.textView2);
	    	label.setText( df.format(90-Math.atan2(event.values[2] , event.values[1])/Math.PI*180) );
	    	
	    	
	    	label = (TextView) findViewById(R.id.textView3);
	    	label.setText( Float.toString(event.values[2]) );
	    	
	    	
	    	//layParams.setMargins(centerX+Math.round(halfSquareSize*event.values[0])-offX, centerY-Math.round(halfSquareSize*event.values[1])-offY, 0, 0);
	    	//bubble moves along the y axis
	    	layParamsY.setMargins(verticalBarPosX, centerY-Math.round(halfSquareSizeY*event.values[1])-offY, 0, 0);
	    	//bubble moves along the X axis
	    	layParamsX.setMargins(centerX+Math.round(halfSquareSizeX*event.values[0])-offX, horizontalBarPosY, 0, 0);
	    
	    	bubbley.setLayoutParams(layParamsY);
	    	bubblex.setLayoutParams(layParamsX);
	    	
	    	

		}
	}
	
	/* handler for storing data for sensors and calibrating them */
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
    
	/* lifecycle functions */
	
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
        	//load preference for sensor speed
        	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        	int speed = Integer.parseInt(prefs.getString("pref_speed", "0"));
        	
        	calX = prefs.getFloat("calibration_x", 0);
        	calY = prefs.getFloat("calibration_y", 0);
        	calZ = prefs.getFloat("calibration_z", 0);
        	
        	int speedConstants[] = {SensorManager.SENSOR_DELAY_UI,SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_GAME, SensorManager.SENSOR_DELAY_FASTEST };
        	//play it safe
        	assert(speed < speedConstants.length );	
        	
        	mSensorManager.registerListener(this, mSensor, speedConstants[speed]);
        }
        
        //get everything ready
        prepareBubble();
    }

    protected void onPause() {
        super.onPause();
        if(mSensor != null){
        	mSensorManager.unregisterListener(this);
        }
    }

    
    /*bubble functions */
    
    void prepareBubble() {
    	
    	MediaPlayer mpLeveled = MediaPlayer.create(this, R.raw.clank);

        layParamsY = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layParamsX = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
         
         
    	bubbley = (ImageView) findViewById(R.id.bubbley);
    	bubblex = (ImageView) findViewById(R.id.bubblex);

    	final RelativeLayout rl = (RelativeLayout) findViewById(R.id.my_relative_layout);
    	ViewTreeObserver vto = rl.getViewTreeObserver();
    	vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
    	    public boolean onPreDraw() {
    	        int width =  rl.getWidth();
    	        int height = rl.getHeight();

    	         
    	    	
    	    	centerX = width/2;
    	    	centerY = height/2;
    	    	
    	        return true;
    	    }
    	});


    	
    	
    	//finding the middle of the bubble bars
    	final View verticalBar = findViewById(R.id.bar_vertical);
    	 vto = verticalBar.getViewTreeObserver();
    	vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
    	    public boolean onPreDraw() {
    	    	halfSquareSizeY =  Math.round(verticalBar.getMeasuredHeight()*0.20f);
    	    	verticalBarPosX = verticalBar.getLeft() + (verticalBar.getWidth()/2) - bubblex.getWidth()/2;

    	        return true;
    	    }
    	});

    	final View horizontalBar = findViewById(R.id.bar_horizontal);
      	vto = horizontalBar.getViewTreeObserver();
    	vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
    	    public boolean onPreDraw() {
    	    	halfSquareSizeX =   Math.round(horizontalBar.getMeasuredWidth()*0.3f);
    	    	horizontalBarPosY = horizontalBar.getTop() + (horizontalBar.getHeight()/2) - bubbley.getHeight()/2 ;
    	        return true;
    	    }
    	});
    	
    	

    	
    	    	
    	//when the bubble is at the leveled point, place the following method
    	//mpLeveled.start();
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

    	
    	//use median as calibration value
    	
    	Arrays.sort(calibrationBufferX);
    	calX = calibrationBufferX[ BUFFER_SIZE / 2];
    	
    	Arrays.sort(calibrationBufferY);
    	calY = calibrationBufferY[ BUFFER_SIZE / 2];
    	
    	Arrays.sort(calibrationBufferZ);
    	//calibrate to reflect newton law
    	calZ =  (calibrationBufferZ[ BUFFER_SIZE / 2]-9.81f);    	
    	
    	//save our calibration values
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	SharedPreferences.Editor editor = prefs.edit();
    	editor.putFloat("calibration_x", calX);
    	editor.putFloat("calibration_y", calY);
    	editor.putFloat("calibration_z", calZ);
        editor.commit();

    	
    	
    	//hide our dialog
    	calibrationDialog.dismiss();
    	
    	// delete reference to array, so they get erased 
    	calibrationBufferX = null;
    	calibrationBufferY = null;
    	calibrationBufferZ = null;
    }
    
    
    /* callbacks for Sensors */
    
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    	//empty
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
    
    
}
