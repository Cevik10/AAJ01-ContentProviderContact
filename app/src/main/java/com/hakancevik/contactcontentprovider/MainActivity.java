package com.hakancevik.contactcontentprovider;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.ContactsContract;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.hakancevik.contactcontentprovider.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    ListView listView;

    ActivityResultLauncher<String> permissionLauncher;

    int requestCount;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        registerLauncher();

        requestCount = 0;
        sharedPreferences = this.getSharedPreferences(MainActivity.this.getPackageName(), MODE_PRIVATE);


        listView = findViewById(R.id.listView);


        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                //location permission
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_CONTACTS)) {

                        Snackbar.make(binding.getRoot(), "Permission needed for access your contact.", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                //request permission
                                permissionLauncher.launch(Manifest.permission.READ_CONTACTS);

                            }
                        }).show();


                    } else {
                        // request permission
                        permissionLauncher.launch(Manifest.permission.READ_CONTACTS);

//                        int getRequestCount = sharedPreferences.getInt("requestCount",0);
//                        Log.d("system.out","i: "+getRequestCount);
//
//                        if (getRequestCount >= 2){
//                            Intent intent = new Intent();
//                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                            Uri uri = Uri.fromParts("package",MainActivity.this.getPackageName(),null);
//                            intent.setData(uri);
//                            MainActivity.this.startActivity(intent);
//                        }


                    }

                } else {


                    // Content Provider
                    // access contact
                    ContentResolver contentResolver = getContentResolver();
                    String[] projection = {ContactsContract.Contacts.DISPLAY_NAME};
                    Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI,
                            projection,
                            null,
                            null,
                            ContactsContract.Contacts.DISPLAY_NAME);

                    if (cursor != null) {

                        ArrayList<String> contactList = new ArrayList<String>();
                        String columnIx = ContactsContract.Contacts.DISPLAY_NAME;

                        while (cursor.moveToNext()) {
                            contactList.add(cursor.getString(cursor.getColumnIndexOrThrow(columnIx)));
                        }

                        cursor.close();

                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, contactList);
                        listView.setAdapter(adapter);

                    }


                }


                int getRequestCount = sharedPreferences.getInt("requestCount", 0);
                Log.d("system.out", "i: " + getRequestCount);

                if (getRequestCount >= 2 && (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED)) {

                    Dialog dialog = new Dialog(MainActivity.this);
                    dialog.setContentView(R.layout.popup_open_settings);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                    TextView popupAllowAccessButton = dialog.findViewById(R.id.popupAllowAccessButton);

                    popupAllowAccessButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", MainActivity.this.getPackageName(), null);
                            intent.setData(uri);
                            MainActivity.this.startActivity(intent);
                        }
                    });


                    dialog.show();

                }


            }
        });
    }

    private void registerLauncher() {
        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if (result) {
                    //permission granted
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {

                        // access contact
                        ContentResolver contentResolver = getContentResolver();
                        String[] projection = {ContactsContract.Contacts.DISPLAY_NAME};
                        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI,
                                projection,
                                null,
                                null,
                                ContactsContract.Contacts.DISPLAY_NAME);

                        if (cursor != null) {

                            ArrayList<String> contactList = new ArrayList<String>();
                            String columnIx = ContactsContract.Contacts.DISPLAY_NAME;

                            while (cursor.moveToNext()) {
                                contactList.add(cursor.getString(cursor.getColumnIndexOrThrow(columnIx)));
                            }

                            cursor.close();

                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, contactList);
                            listView.setAdapter(adapter);

                        }


                    }


                } else {
                    //permission denied

                    //Toast.makeText(MainActivity.this, "Permission needed!", Toast.LENGTH_SHORT).show();
                    requestCount++;
                    sharedPreferences.edit().putInt("requestCount", requestCount).apply();


                }
            }

        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}