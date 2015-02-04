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
package com.ibm.demo.IoTStarter.fragments;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import com.ibm.demo.IoTStarter.IoTStarterApplication;
import com.ibm.demo.IoTStarter.R;
import com.ibm.demo.IoTStarter.activities.MainActivity;
import com.ibm.demo.IoTStarter.activities.ProfilesActivity;

/**
 * This class provides common properties and functions for fragment subclasses used in the application.
 */
public class IoTStarterFragment extends Fragment {
    protected final static String TAG = IoTStarterFragment.class.getName();
    protected Context context;
    protected IoTStarterApplication app;
    protected Menu menu;
    protected BroadcastReceiver broadcastReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    /**
     * Update strings in the fragment based on IoTStarterApplication values.
     */
    protected void updateViewStrings() {
        Log.d(TAG, ".updateViewStrings() entered");
        int unreadCount = app.getUnreadCount();
        ((MainActivity) getActivity()).updateBadge(getActivity().getActionBar().getTabAt(2), unreadCount);
    }

    /**************************************************************************
     * Functions to handle the menu bar
     **************************************************************************/

    /**
     * Switch to the IoT fragment.
     */
    protected void openIoT() {
        Log.d(TAG, ".openIoT() entered");
        getActivity().getActionBar().setSelectedNavigationItem(1);
    }

    protected void openProfiles() {
        Log.d(TAG, ".handleProfiles() entered");
        Intent profilesIntent = new Intent(getActivity().getApplicationContext(), ProfilesActivity.class);
        startActivity(profilesIntent);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.d(TAG, ".onCreateOptions() entered");
        this.menu = menu;
        getActivity().getMenuInflater().inflate(R.menu.menu, this.menu);

        super.onCreateOptionsMenu(menu, inflater);
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

        if (LoginFragment.class.getName().equals(app.getCurrentRunningActivity())) {
            app.setDeviceId(((EditText) getActivity().findViewById(R.id.deviceIDValue)).getText().toString());
            app.setOrganization(((EditText) getActivity().findViewById(R.id.organizationValue)).getText().toString());
            app.setAuthToken(((EditText) getActivity().findViewById(R.id.authTokenValue)).getText().toString());
        }

        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_accel:
                app.toggleAccel();
                return true;
            case R.id.action_profiles:
                openProfiles();
                return true;
            case R.id.action_clear_profiles:
                app.clearProfiles();
                return true;
            case R.id.clear:
                app.setUnreadCount(0);
                app.getMessageLog().clear();
                updateViewStrings();
                return true;
            default:
                if (item.getTitle().equals(getResources().getString(R.string.app_name))) {
                    getActivity().openOptionsMenu();
                    return true;
                } else {
                    return super.onOptionsItemSelected(item);
                }
        }
    }
}
