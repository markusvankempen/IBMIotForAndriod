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

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.ibm.demo.IoTStarter.R;
import com.ibm.demo.IoTStarter.fragments.DrawFragment;
import com.ibm.demo.IoTStarter.fragments.IoTFragment;
import com.ibm.demo.IoTStarter.fragments.LogFragment;
import com.ibm.demo.IoTStarter.fragments.LoginFragment;
import com.ibm.demo.IoTStarter.utils.Constants;

import java.util.ArrayList;

/**
 * MainActivity acts as the primary activity in the application that displays
 * the fragment of the currently selected action bar tab.
 */
public class MainActivity extends Activity implements ActionBar.TabListener {
    private final static String TAG = MainActivity.class.getName();
    private DrawFragment drawFragment;
    private LogFragment logFragment;
    private LoginFragment loginFragment;
    private IoTFragment iotFragment;

    private ArrayList<String> backStack;

    /**
     * Callback for handling when a new tab is selected. Replace fragment_container content
     * with the new fragment. In the case of IoT tab, also replace fragment_containerDraw.
     * @param tab The selected tab
     * @param fragmentTransaction The transaction containing this tab selection
     */
    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        Log.d(TAG, ".onTabSelected() entered");

        if (tab.getText().equals(Constants.LOGIN_LABEL)) {
            fragmentTransaction.replace(R.id.fragment_container, loginFragment);
            try {
                fragmentTransaction.remove(drawFragment);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (tab.getText().equals(Constants.IOT_LABEL)) {
            fragmentTransaction.replace(R.id.fragment_container, iotFragment);
            fragmentTransaction.replace(R.id.fragment_containerDraw, drawFragment);
            try {
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (tab.getText().equals(Constants.LOG_LABEL)) {
            fragmentTransaction.replace(R.id.fragment_container, logFragment);
            try {
                fragmentTransaction.remove(drawFragment);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // If switching to log tab, reset its badge value to 0
            updateBadge(tab, 0);
        }
    }

    /**
     * Keep track of tab backStack when leaving tabs.
     * @param tab The tab being left
     * @param fragmentTransaction The transaction containing this tab selection
     */
    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        Log.d(TAG, ".onTabUnselected() entered");
        int index = backStack.size()-1;
        if (tab.getText().equals(Constants.LOGIN_LABEL)) {
            if (!backStack.isEmpty() && Constants.LOGIN_LABEL.equals(backStack.get(index))) {
                backStack.remove(index);
            } else {
                backStack.add(Constants.LOGIN_LABEL);
            }
        } else if (tab.getText().equals(Constants.IOT_LABEL)) {
            if (!backStack.isEmpty() && Constants.IOT_LABEL.equals(backStack.get(index))) {
                backStack.remove(index);
            } else {
                backStack.add(Constants.IOT_LABEL);
            }
            try {
                fragmentTransaction.remove(drawFragment);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (tab.getText().equals(Constants.LOG_LABEL)) {
            if (!backStack.isEmpty() && Constants.LOG_LABEL.equals(backStack.get(index))) {
                backStack.remove(index);
            } else {
                backStack.add(Constants.LOG_LABEL);
            }
        }
    }

    /**
     * Do nothing for now
     * @param tab The tab being selected
     * @param fragmentTransaction The transaction containing this tab selection
     */
    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        Log.d(TAG, ".onTabReselected() entered");
    }

    /**
     * Create the MainActivity. Initialize the action bar tabs and restore activity saved state.
     * @param savedInstanceState The saved activity state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize all the fragments 1 time when creating the activity
        loginFragment = new LoginFragment();
        iotFragment = new IoTFragment();
        logFragment = new LogFragment();
        drawFragment = new DrawFragment();

        // backStack used for overriding back button
        backStack = new ArrayList<String>();

        // Setup the action bar
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        ActionBar.Tab loginTab = actionBar.newTab();
        loginTab.setText(Constants.LOGIN_LABEL);
        loginTab.setTabListener(this);
        actionBar.addTab(loginTab);

        ActionBar.Tab iotTab = actionBar.newTab();
        iotTab.setText(Constants.IOT_LABEL);
        iotTab.setTabListener(this);
        actionBar.addTab(iotTab);

        // The Log tab uses a custom view so that a badge value can be displayed.
        ActionBar.Tab logTab = actionBar.newTab();
        logTab.setText(Constants.LOG_LABEL);
        logTab.setTabListener(this);
        logTab.setCustomView(createLogTabView());
        actionBar.addTab(logTab);

        // Set current tab based on saved state. Mainly for screen rotations when activity is recreated.
        if(savedInstanceState != null) {
            int tabIndex = savedInstanceState.getInt("tabIndex");
            getActionBar().setSelectedNavigationItem(tabIndex);
        }

        setContentView(R.layout.main);
    }

    /**
     * Save the current state of the activity. This is used to store the index of the currently
     * selected tab.
     * @param outState The state of the activity
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        int tabIndex = getActionBar().getSelectedNavigationIndex();
        outState.putInt("tabIndex", tabIndex);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * Maintain a custom back stack for switching to previous tabs with the back button.
     * If back stack is empty, follow default activity behavior.
     */
    @Override
    public void onBackPressed() {
        Log.d(TAG, ".onBackPressed() entered");
        //super.onBackPressed();
        if (!backStack.isEmpty()) {
            int selectedIndex = getActionBar().getSelectedNavigationIndex();
            int index = backStack.size()-1;
            String tabLabel = backStack.get(index);
            backStack.remove(index);
            if (selectedIndex == 0) {
                backStack.add(Constants.LOGIN_LABEL);
            } else if (selectedIndex == 1) {
                backStack.add(Constants.IOT_LABEL);
            } else if (selectedIndex == 2) {
                backStack.add(Constants.LOG_LABEL);
            }
            if (Constants.LOGIN_LABEL.equals(tabLabel)) {
                getActionBar().setSelectedNavigationItem(0);
            } else if (Constants.IOT_LABEL.equals(tabLabel)) {
                getActionBar().setSelectedNavigationItem(1);
            } else if (Constants.LOG_LABEL.equals(tabLabel)) {
                getActionBar().setSelectedNavigationItem(2);
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d(TAG, ".onConfigurationChanged entered()");
        super.onConfigurationChanged(newConfig);
    }

    /**
     * Creates the custom view for the Log Tab to support a badge value
     * @return view The custom view for the tab.
     */
    public View createLogTabView() {
        FrameLayout view = (FrameLayout) this.getLayoutInflater().inflate(R.layout.log_badge, null);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        ((TextView) view.findViewById(R.id.tab_title)).setText(Constants.LOG_LABEL);
        updateBadge((TextView) view.findViewById(R.id.tab_badge), 0);
        return view;
    }

    /**
     * Update the badge value on the specified action bar tab.
     * @param tab The tab to update.
     * @param badgeNumber The new badge value.
     */
    public void updateBadge(ActionBar.Tab tab, int badgeNumber) {
        updateBadge((TextView) tab.getCustomView().findViewById(R.id.tab_badge), badgeNumber);
    }

    /**
     * Update the badge value on the specified action bar tab.
     * @param view The view to update.
     * @param badgeNumber The new badge value.
     */
    private void updateBadge(TextView view, int badgeNumber) {
        if (badgeNumber > 0) {
            view.setVisibility(View.VISIBLE);
            view.setText(Integer.toString(badgeNumber));
        } else {
            view.setVisibility(View.GONE);
        }
    }

}
