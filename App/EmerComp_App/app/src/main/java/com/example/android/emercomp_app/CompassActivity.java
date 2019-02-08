package com.example.android.emercomp_app;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;


public class CompassActivity extends AppCompatActivity implements SensorEventListener{

    private ImageView mPointer;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;
    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private float[] mR = new float[9];
    private float[] I = new float[9];
    private float[] mOrientation = new float[3];
    private float mDegree;
    private float mRadians;
    private float mCurrentDegree = 0f;
    private float GRAVITY = 0.97f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);
        mPointer = (ImageView) findViewById(R.id.compass_image_id);
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        main_page_method();
    }

    public void main_page_method(){

        Button button_goto_main = (Button) findViewById(R.id.button_goto_main);
        button_goto_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Intent returnContactsIntent = new Intent();

                //to finish the activity and go back to main page
                finish();
            }
        });


    }
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == mAccelerometer)
        {
            mLastAccelerometer[0] = GRAVITY * mLastAccelerometer[0] + (1-GRAVITY) * event.values[0];
            mLastAccelerometer[1] = GRAVITY * mLastAccelerometer[1] + (1-GRAVITY) * event.values[1];
            mLastAccelerometer[2] = GRAVITY * mLastAccelerometer[2] + (1-GRAVITY) * event.values[2];
            //System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
            //mLastAccelerometerSet = true;
        }
        else if (event.sensor == mMagnetometer)
        {
            mLastMagnetometer[0] = GRAVITY * mLastMagnetometer[0] + (1-GRAVITY) * event.values[0];
            mLastMagnetometer[1] = GRAVITY * mLastMagnetometer[1] + (1-GRAVITY) * event.values[1];
            mLastMagnetometer[2] = GRAVITY * mLastMagnetometer[2] + (1-GRAVITY) * event.values[2];
            //System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
            //mLastMagnetometerSet = true;
        }
        boolean everything_ok = SensorManager.getRotationMatrix(mR, I, mLastAccelerometer, mLastMagnetometer);

        if (everything_ok) {
           // SensorManager.getRotationMatrix(mR, null, mLastAccelerometer, mLastMagnetometer);
            SensorManager.getOrientation(mR, mOrientation);

             mRadians = mOrientation[0];
             mDegree = (((float)(Math.toDegrees(mRadians))) + 360) % 360;

            Animation ra = new RotateAnimation(
                    -mCurrentDegree,
                    -mDegree,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);

            mCurrentDegree = mDegree;
            ra.setDuration(500);
            ra.setRepeatCount(0);
            ra.setFillAfter(true);

            mPointer.startAnimation(ra);

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub

    }
}
