/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.emercomp_app;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;

import com.google.android.gms.location.*;
import com.google.android.gms.tasks.*;

import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements
        FetchAddressTask.OnTaskCompleted {


    public TextView voiceToText;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;

    public static final int REQUEST_LOCATION_PERMISSION = 1;
    private static final String TAG = "MainActivity";
    //public final int PICK_CONTACT = 2015;
    Location mLastLocation;
    FusedLocationProviderClient mFusedLocationClient;
    TextView mLocationTextView;
    private boolean mTrackingLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Button mLocationButton = (Button) findViewById(R.id.button_location);

        mLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLocation();
            }
        });

        mLocationTextView = (TextView) findViewById(R.id.textview_location);
        //FusedLocationProviderClient mFusedLocationClient;
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        //initialize textview for voice input
        voiceToText = (TextView) findViewById(R.id.voiceToText);


        // ShakeDetector initialization
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {

            @Override
            public void onShake(int count) {
                /*
                 * The following method, "handleShakeEvent(count):" is a stub //
                 * method you would use to setup whatever you want done once the
                 * device has been shook.
                 */
                handleShakeEvent(count);
            }
        });

        compass_method();

    }


    public void compass_method()
    {
        Button compass_button = (Button) findViewById(R.id.compass_button);
        compass_button.setOnClickListener(new View.OnClickListener() {
            //@Override
            Intent i2 = new Intent(MainActivity.this, CompassActivity.class);
            public void onClick(View v) {
                startActivity(i2);
            }
        });
    }


    private void handleShakeEvent(int count) {
        System.out.println("shooooooook");
        Toast.makeText(this,"Shaken "+count+"!",Toast.LENGTH_LONG).show();
        //send sms when phone is shaken twice
        if (count >= 2) {
            getLocation();
            //smsSendMessage();
        }
    }
    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]
                            {Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        } else {
            //Log.d(TAG, "getLocation: permissions granted");
            mFusedLocationClient.getLastLocation().addOnSuccessListener(
                    new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                // Start the reverse geocode AsyncTask
                                new FetchAddressTask(MainActivity.this,
                                        MainActivity.this).execute(location);
                            } else {
                                mLocationTextView.setText(R.string.no_location);
                            }
                        }
                    });
        }
        mLocationTextView.setText(getString(R.string.address_text,
                getString(R.string.loading),
                System.currentTimeMillis()));
        //smsSendMessage();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION:
                // If the permission is granted, get the location,
                // otherwise, show a Toast
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocation();
                } else {
                    Toast.makeText(this,
                            "location permission denied",
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }


    @Override
    public void onTaskCompleted(String result) {

        // Update the UI
        mLocationTextView.setText(getString(R.string.address_text,
                result, System.currentTimeMillis()));
        smsSendMessage();
    }

    //method for voice input
    public void getVoiceInput(View view) {
//implicit intent to take user input(voice)
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault()); //even if commented, takes devices default lang

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, 10);
        } else {
            Toast.makeText(this, "Smartphone doesnt take speech input!!", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //switch(requestCode)
        //{
        //case 10:
        //for voice input

        if (requestCode == 10) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                voiceToText.setText(result.get(0));
                //String result_String = result.toString();

                if (result.contains("Danger") | result.contains("danger") | result.contains("DANGER") |
                        result.contains("help") | result.contains("Help") | result.contains("HELP")) {
                    getLocation();

                }

                //voiceToText.setText("Ankit Patel");
            } else {
                voiceToText.setText("Ankit Patel");
            }
            /*default:
                voiceToText.setText("requestCode is " + requestCode + "resultCode is " + resultCode + "and RESULT_OK is " + RESULT_OK);
                break;*/
        }
        // }

       /* if (requestCode == PICK_CONTACT && resultCode == RESULT_OK) {
            Uri contactUri = data.getData();
            Cursor cursor = getContentResolver().query(contactUri, null, null, null, null);
            cursor.moveToFirst();
            int column = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            //(new normalizePhoneNumberTask()).execute(cursor.getString(column));
            Log.d("phone number", cursor.getString(column));
        }*/
    }


    public void smsSendMessage() {
        /*EmergencyContacts emergencyContacts = new EmergencyContacts();*/
        SharedPreferences sharedPref = getSharedPreferences("contactInfo",Context.MODE_PRIVATE);
        Map<String, ?> allEntries = sharedPref.getAll();
        if (!allEntries.isEmpty()) {
            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
               /* emergencyContacts.NAMES.add(entry.getKey());
                emergencyContacts.NUMBERS.add(entry.getValue().toString());
*/
                // Set the phone number to the string
                String destinationAddress = entry.getValue().toString();
                // Get the text of the sms message.

                String smsMessage = "Help Needed!!!!\n" + mLocationTextView.getText().toString();
                //System.out.println(smsMessage);

                // Sets the SMSC(short msg service center) address, default = null.
                String scAddress = null;
                //to make sure msg is sent or delivered
                PendingIntent sentIntent = null, deliveryIntent = null;

                // Using SmsManager class.
                SmsManager smsManager = SmsManager.getDefault();
                Log.d("default sms manager", smsMessage);
                smsManager.sendTextMessage(destinationAddress, scAddress, smsMessage, sentIntent, deliveryIntent);
            }

        }
        /*// Set the phone number to the string
        String destinationAddress = "16073740508";
        // Get the text of the sms message.

        String smsMessage = "Help Needed!!!!\n" + mLocationTextView.getText().toString();
        //System.out.println(smsMessage);

        // Sets the SMSC(short msg service center) address, default = null.
        String scAddress = null;
        //to make sure msg is sent or delivered
        PendingIntent sentIntent = null, deliveryIntent = null;

        // Using SmsManager class.
        SmsManager smsManager = SmsManager.getDefault();
        Log.d("default sms manager", smsMessage);
        smsManager.sendTextMessage(destinationAddress, scAddress, smsMessage, sentIntent, deliveryIntent);*/

    }


    @Override
    public void onResume() {
        super.onResume();
        // Add the following line to register the Session Manager Listener onResume
        mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onPause() {
        // Add the following line to unregister the Sensor Manager onPause
        mSensorManager.unregisterListener(mShakeDetector);
        super.onPause();
    }

    public void startEmergencyContactActivity(View view) {
        Intent startNewActivity = new Intent(this, EmergencyContacts.class);
        startActivity(startNewActivity);
    }

    public void startCompassActivity(View view){
        Intent startNewActivity = new Intent(this, CompassActivity.class);
        startActivity(startNewActivity);
    }
}
