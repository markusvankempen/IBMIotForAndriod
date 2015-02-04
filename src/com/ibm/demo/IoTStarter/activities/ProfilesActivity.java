/*******************************************************************************
 * Copyright (c) 2014 IBM Corp.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *   http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *    Mike Robertson - initial contribution
 *******************************************************************************/
package com.ibm.demo.IoTStarter.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.ibm.demo.IoTStarter.IoTStarterApplication;
import com.ibm.demo.IoTStarter.R;
import com.ibm.demo.IoTStarter.utils.Constants;
import com.ibm.demo.IoTStarter.utils.IoTProfile;

import java.util.ArrayList;

/**
 * The Profiles activity lists saved connection profiles to use to connect to IoT.
 */
public class ProfilesActivity extends Activity {
    private final static String TAG = ProfilesActivity.class.getName();
    protected Context context;
    protected IoTStarterApplication app;
    protected BroadcastReceiver broadcastReceiver;

    protected ListView listView;
    protected ArrayAdapter<String> listAdapter;

    /**************************************************************************
     * Activity functions for establishing the activity
     **************************************************************************/

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, ".onCreate() entered");

        super.onCreate(savedInstanceState);

        setContentView(R.layout.profiles);
    }

    /**
     * Called when the activity is resumed.
     */
    @Override
    public void onResume() {
        Log.d(TAG, ".onResume() entered");

        super.onResume();
        context = getApplicationContext();

        listView = (ListView)findViewById(android.R.id.list);

        app = (IoTStarterApplication) getApplication();
        app.setCurrentRunningActivity(TAG);

        listAdapter = new ArrayAdapter<String>(this.context, R.layout.list_item, app.getProfileNames());
        listView.setAdapter(listAdapter);

        if (broadcastReceiver == null) {
            Log.d(TAG, ".onResume() - Registering LogBroadcastReceiver");
            broadcastReceiver = new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.d(TAG, ".onReceive() - Received intent for logBroadcastReceiver");
                    processIntent(intent);
                }
            };
        }

        getApplicationContext().registerReceiver(broadcastReceiver,
                new IntentFilter(Constants.APP_ID + Constants.INTENT_PROFILES));

        // initialise
        initializeProfilesActivity();
    }

    /**
     * Called when the activity is destroyed.
     */
    @Override
    public void onDestroy() {
        Log.d(TAG, ".onDestroy() entered");

        getApplicationContext().unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    /**
     * Initializing onscreen elements and shared properties
     */
    private void initializeProfilesActivity() {
        Log.d(TAG, ".initializeProfilesActivity() entered");

        Button button = (Button) findViewById(R.id.saveButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSave();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                String profileName = (String) listView.getAdapter().getItem(position);
                handleSelection(profileName);
            }
        });
    }

    /**
     * Callback for when a profile is selected from the list. Set the application properties to
     * the content of the profile and close the profiles activity.
     * @param profileName The name of the selected profile.
     */
    private void handleSelection(String profileName) {
        Log.d(TAG, ".handleSelection() entered");

        ArrayList<IoTProfile> profiles = (ArrayList<IoTProfile>) app.getProfiles();
        for (IoTProfile profile : profiles) {
            if (profile.getProfileName().equals(profileName)) {
                app.setOrganization(profile.getOrganization());
                app.setDeviceId(profile.getDeviceID());
                app.setAuthToken(profile.getAuthorizationToken());
                break;
            }
        }
        finish();
    }

    /**
     * Callback for when the save button is pressed. Prompt the user for a profile name.
     * If the chosen name is already in use, prompt the user to overwrite the existing profile.
     */
    private void handleSave() {
        Log.d(TAG, ".handleSave() entered");

        final EditText input = new EditText(context);
        new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.save_dialog_title))
                .setMessage(getResources().getString(R.string.save_dialog_text))
                .setView(input)
                .setPositiveButton(getResources().getString(R.string.save_dialog_ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Editable value = input.getText();
                        IoTProfile profile = new IoTProfile(value.toString(), app.getOrganization(), app.getDeviceId(), app.getAuthToken());

                        // Check if profile name already exists.
                        if (app.getProfileNames().contains(profile.getProfileName())) {
                            final IoTProfile newProfile = profile;
                            new AlertDialog.Builder(ProfilesActivity.this)
                                    .setTitle(getResources().getString(R.string.profile_exists_title))
                                    .setMessage(getResources().getString(R.string.profile_exists_text))
                                            .setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int whichButton) {
                                                    app.overwriteProfile(newProfile);
                                                    listAdapter.notifyDataSetInvalidated();
                                                }
                                            }).setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    // Do nothing
                                }
                            }).show();
                        } else {
                            app.saveProfile(profile);
                            listAdapter.notifyDataSetInvalidated();
                        }
                    }
                }).setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Do nothing
                    }
                }).show();
    }

    /**
     * Process intents received by the Broadcast Receiver.
     * @param intent The intent that was received.
     */
    private void processIntent(Intent intent) {
        Log.d(TAG, ".processIntent() entered");

        String data = intent.getStringExtra(Constants.INTENT_DATA);
        assert data != null;
        listAdapter.notifyDataSetInvalidated();

        if (data.equals(Constants.ALERT_EVENT)) {
            String message = intent.getStringExtra(Constants.INTENT_DATA_MESSAGE);
            new AlertDialog.Builder(this)
                    .setTitle(getResources().getString(R.string.alert_dialog_title))
                    .setMessage(message)
                    .setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                    }).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, ".onCreateOptions() entered");
        getMenuInflater().inflate(R.menu.profiles_menu, menu);

        return true;
    }

    /**
     * Process the selected iot_menu item.
     *
     * @param item The selected iot_menu item.
     * @return true in all cases.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, ".onOptionsItemSelected() entered");

        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_accel:
                app.toggleAccel();
                return true;
            case R.id.action_clear_profiles:
                app.clearProfiles();
                listAdapter.notifyDataSetInvalidated();
                return true;
            case R.id.clear:
                app.setUnreadCount(0);
                app.getMessageLog().clear();
                return true;
            default:
                if (item.getTitle().equals(getResources().getString(R.string.app_name))) {
                    openOptionsMenu();
                    return true;
                } else {
                    return super.onOptionsItemSelected(item);
                }
        }
    }
}
