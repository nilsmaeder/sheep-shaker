package ch.zli.m335.sheepshaker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class MainActivity extends AppCompatActivity {
    //To check if the user shakes the device for the first time
    Boolean firstTime = true;
    Boolean animationActive = false;
    Integer round = 0;
    Integer score = 0;
    long startTime;

    private SensorManager mSensorManager;
    private float mAccel;
    private float mAccelCurrent;
    private float mAccelLast;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Objects.requireNonNull(mSensorManager).registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
        mAccel = 10f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;
    }

    //Calculate the score based on the time the reaction time of the user
    protected void calculateScore(){
        long reactionTime = (System.currentTimeMillis() - startTime)/1000;
        switch (Math.toIntExact(reactionTime)){
            case 0:
            case 1:
                score = score + 5;
                break;
            case 2:
                score = score + 4;
                break;
            case 3:
                score = score + 3;
                break;
            case 4:
                score = score + 2;
                break;
            default:
                score = score + 1;
        }
    }

    //Actualize the score in the view
    protected void actualizeScore(){
        TextView scoreTextView = findViewById(R.id.score);
        scoreTextView.setText(score.toString() + " Points");
    }

    //Makes the sheep disappear and appear again after a certain amount of time
    protected void displayAnimation(){
        ImageView sheep = findViewById(R.id.sheep);
        sheep.setVisibility(View.INVISIBLE);

        //Get a random amount of seconds between 1 and 8 to make the sheep disappear for that time
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int rand = random.nextInt(1, 8);

        new Handler().postDelayed(new Runnable() {
            public void run() {
                //When postDelayed ends execute the following actions:
                sheep.setVisibility(View.VISIBLE);
                startTime = System.currentTimeMillis();
                animationActive = false;
                vibrate();
            }
        }, rand * 1000);
    }

    //Make the device vibrate
    protected void vibrate(){
        //Code copied partially from https://stackoverflow.com/questions/13950338/how-to-make-an-android-device-vibrate-with-different-frequency
        Vibrator vibration = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibration.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibration.vibrate(500);
        }
        //Copied code ends here
    }

    //Code copied partially from https://www.tutorialspoint.com/how-to-detect-shake-event-in-android-app
    private final SensorEventListener mSensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            mAccelLast = mAccelCurrent;
            mAccelCurrent = (float) Math.sqrt((double) (x * x + y * y + z * z));
            float delta = mAccelCurrent - mAccelLast;
            mAccel = mAccel * 0.9f + delta;
            //Copied code ends here
            //Execute if the user shakes the device:
            if (mAccel > 3) {
                //If you set if(mAccel > (numberbelow9)) the program in this case detects a shaking event 11 times upon startup
                if(round < 11){
                    round++;
                }else if(firstTime){
                        animationActive = true;
                        firstTime = false;
                        score = 1;
                        actualizeScore();
                        displayAnimation();
                        //Check if an animation is already active so that the user can not get any points just by constantly shaking his phone
                }else if(!animationActive){
                        animationActive = true;
                        calculateScore();
                        actualizeScore();
                        displayAnimation();
                }
            }
        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            //Not implemented
        }
    };
    @Override
    protected void onResume() {
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
        super.onResume();
    }
    @Override
    protected void onPause() {
        mSensorManager.unregisterListener(mSensorListener);
        super.onPause();
    }
}