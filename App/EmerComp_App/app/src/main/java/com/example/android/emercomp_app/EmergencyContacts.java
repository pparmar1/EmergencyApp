package com.example.android.emercomp_app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Map;

public class EmergencyContacts extends AppCompatActivity {
    private static final int RESULT_PICK_CONTACT = 1001;
    String contactNumber = null;
    String contactName = null;
    public static ArrayList NAMES = new ArrayList();
    public static ArrayList NUMBERS = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_contacts);
        /*SharedPreferences sharedPref = getSharedPreferences("contactInfo",Context.MODE_PRIVATE);
        Map<String, ?> allEntries = sharedPref.getAll();
        if (!allEntries.isEmpty()) {
            System.out.println("true");
            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                NAMES.add(entry.getKey());
                NUMBERS.add(entry.getValue().toString());
            }
            ListView listView = (ListView) findViewById(R.id.listView);
            CustomAdapter customAdapter = new CustomAdapter();
            listView.setAdapter(customAdapter);
        }*/
    }

    public void pickContact(View v) {

        Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(contactPickerIntent, RESULT_PICK_CONTACT);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        SharedPreferences sharedPref = getSharedPreferences("contactInfo",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case RESULT_PICK_CONTACT:
                    Cursor cursor = null;
                    try {

                        // getData() method will have the Content Uri of the selected contact
                        Uri uri = data.getData();
                        //Query the content uri
                        cursor = getContentResolver().query(uri, null, null, null, null);
                        cursor.moveToFirst();
                        // column index of the phone number
                        int phoneIndex = cursor.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER);
                        // column index of the contact name
                        int nameIndex = cursor.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                        contactNumber = cursor.getString(phoneIndex);

                        contactName = cursor.getString(nameIndex);
                        editor.putString(contactName,contactNumber);
                        editor.apply();
                        Toast.makeText(this,"Saved "+contactName+"!",Toast.LENGTH_LONG).show();
                        NUMBERS.add(contactNumber);
                        NAMES.add(contactName);
                       // System.out.println(NAMES+" "+NUMBERS);
                       /* Map<String, ?> allEntries = sharedPref.getAll();
                        if (!allEntries.isEmpty()) {
                            System.out.println("true");
                            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                                NAMES.add(entry.getKey());
                                NUMBERS.add(entry.getValue().toString());
                            }
                            ListView listView = (ListView) findViewById(R.id.listView);
                            CustomAdapter customAdapter = new CustomAdapter();
                            listView.setAdapter(customAdapter);
                        }*/

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }

    public void displayContact(View view){
      /*  SharedPreferences sharedPref = getSharedPreferences("contactInfo", Context.MODE_PRIVATE);
        Map<String, ?> allEntries = sharedPref.getAll();

        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            NAMES.add(entry.getKey());
            NUMBERS.add(entry.getValue().toString());
        }*/
        ListView listView = (ListView)findViewById(R.id.listView);
        
        CustomAdapter customAdapter = new CustomAdapter();
        listView.setAdapter(customAdapter);
    }

    class CustomAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return NAMES.size();

        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = getLayoutInflater().inflate(R.layout.customlayout,null);
            TextView textView_name = view.findViewById(R.id.textView_name);
            TextView textView_number = view.findViewById(R.id.textView_number);

            textView_name.setText(NAMES.get(i).toString());
            textView_number.setText(NUMBERS.get(i).toString());
            return view;
        }
    }

}
