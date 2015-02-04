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
package com.ibm.demo.IoTStarter;

import android.app.Application;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Camera;
import android.location.Location;
import android.util.Log;

import com.ibm.demo.IoTStarter.utils.Constants;
import com.ibm.demo.IoTStarter.utils.DeviceSensor;
import com.ibm.demo.IoTStarter.utils.IoTProfile;

import java.util.*;

/**
 * Main class for the IoT Starter application. Stores values for
 * important device and application information.
 */
public class IoTStarterApplication extends Application {
    private final static String TAG = IoTStarterApplication.class.getName();

    // Current activity of the application, updated whenever activity is changed
    private String currentRunningActivity;

    // Values needed for connecting to IoT
    private String organization;
    private String deviceId;
    private String authToken;
    private Constants.ConnectionType connectionType;

    private SharedPreferences settings;

    // Application state variables
    private boolean connected = false;
    private int publishCount = 0;
    private int receiveCount = 0;
    private int unreadCount = 0;

    private int color = Color.WHITE;
    private boolean isCameraOn = false;
    private float[] accelData;
    private boolean accelEnabled = true;

    private DeviceSensor deviceSensor;
    private Location currentLocation;
    private Camera camera;

    // Message log for log activity
    private ArrayList<String> messageLog = new ArrayList<String>();

    private List<IoTProfile> profiles = new ArrayList<IoTProfile>();
    private ArrayList<String> profileNames = new ArrayList<String>();

    /**
     * Called when the application is created. Initializes the application.
     */
    @Override
    public void onCreate() {
        Log.d(TAG, ".onCreate() entered");
        super.onCreate();

        settings = getSharedPreferences(Constants.SETTINGS, 0);

        loadProfiles();
    }

    /**
     * Called when old application stored settings values are found.
     * Converts old stored settings into new profile setting.
     */
    private void createNewDefaultProfile() {
        Log.d(TAG, "organization not null. compat profile setup");
        // If old stored property settings exist, use them to create a new default profile.
        String organization = settings.getString(Constants.ORGANIZATION, null);
        String deviceId = settings.getString(Constants.DEVICE_ID, null);
        String authToken = settings.getString(Constants.AUTH_TOKEN, null);
        IoTProfile newProfile = new IoTProfile("default", organization, deviceId, authToken);
        this.profiles.add(newProfile);
        this.profileNames.add("default");

        // Put the new profile into the store settings and remove the old stored properties.
        Set<String> defaultProfile = newProfile.convertToSet();

        SharedPreferences.Editor editor = settings.edit();
        editor.putStringSet(newProfile.getProfileName(), defaultProfile);
        editor.remove(Constants.ORGANIZATION);
        editor.remove(Constants.DEVICE_ID);
        editor.remove(Constants.AUTH_TOKEN);
        editor.commit();

        this.setOrganization(newProfile.getOrganization());
        this.setDeviceId(newProfile.getDeviceID());
        this.setAuthToken(newProfile.getAuthorizationToken());

        return;
    }

    /**
     * Load existing profiles from application stored settings.
     */
    private void loadProfiles() {
        // Compatability
        if (settings.getString(Constants.ORGANIZATION, null) != null) {
            createNewDefaultProfile();
            return;
        }

        Map<String,?> profileList = settings.getAll();
        if (profileList != null) {
            for (String key : profileList.keySet()) {
                Set<String> profile;// = new HashSet<String>();
                try {
                    // If the stored property is a Set<String> type, parse the profile and add it to the list of
                    // profiles.
                    if ((profile = settings.getStringSet(key, null)) != null) {
                        Log.d(TAG, "profile name: " + key);
                        IoTProfile newProfile = new IoTProfile(profile);
                        this.profiles.add(newProfile);
                        this.profileNames.add(newProfile.getProfileName());

                        if (newProfile.getProfileName().equals("default")) {
                            this.setOrganization(newProfile.getOrganization());
                            this.setDeviceId(newProfile.getDeviceID());
                            this.setAuthToken(newProfile.getAuthorizationToken());
                        }
                    }
                } catch (Exception e) {
                    continue;
                }
            }
        }
    }

    /**
     * Enables or disables the publishing of accelerometer data
     */
    public void toggleAccel() {
        this.setAccelEnabled(!this.isAccelEnabled());
        if (connected && accelEnabled) {
            // Device Sensor was previously disabled, and the device is connected, so enable the sensor
            if (deviceSensor == null) {
                deviceSensor = DeviceSensor.getInstance(this);
            }
            deviceSensor.enableSensor();
        } else if (connected && !accelEnabled) {
            // Device Sensor was previously enabled, and the device is connected, so disable the sensor
            if (deviceSensor != null) {
                deviceSensor.disableSensor();
            }
        }
    }

    /**
     * Turn flashlight on or off when a light command message is received.
     */
    public void handleLightMessage() {
        Log.d(TAG, ".handleLightMessage() entered");
        if (this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            if (!isCameraOn) {
                Log.d(TAG, "FEATURE_CAMERA_FLASH true");
                camera = Camera.open();
                Camera.Parameters p = camera.getParameters();
                p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                camera.setParameters(p);
                camera.startPreview();
                isCameraOn = true;
            } else {
                camera.stopPreview();
                camera.release();
                isCameraOn = false;
            }
        } else {
            Log.d(TAG, "FEATURE_CAMERA_FLASH false");
        }
    }

    /**
     * Overwrite an existing profile in the stored application settings.
     * @param newProfile The profile to save.
     */
    public void overwriteProfile(IoTProfile newProfile) {
        // Put the new profile into the store settings and remove the old stored properties.
        Set<String> profileSet = newProfile.convertToSet();

        SharedPreferences.Editor editor = settings.edit();
        editor.remove(newProfile.getProfileName());
        editor.putStringSet(newProfile.getProfileName(), profileSet);
        editor.commit();

        for (IoTProfile existingProfile : profiles) {
            if (existingProfile.getProfileName().equals(newProfile.getProfileName())) {
                profiles.remove(existingProfile);
                break;
            }
        }
        profiles.add(newProfile);
    }
    /**
     * Save the profile to the application stored settings.
     * @param profile The profile to save.
     */
    public void saveProfile(IoTProfile profile) {
        // Put the new profile into the store settings and remove the old stored properties.
        Set<String> profileSet = profile.convertToSet();

        SharedPreferences.Editor editor = settings.edit();
        editor.putStringSet(profile.getProfileName(), profileSet);
        editor.commit();
        this.profiles.add(profile);
        this.profileNames.add(profile.getProfileName());
    }

    /**
     * Remove all saved profile information.
     */
    public void clearProfiles() {
        this.profiles.clear();
        this.profileNames.clear();

        SharedPreferences.Editor editor = settings.edit();
        editor.clear();
        editor.commit();
    }

    // Getters and Setters
    public String getCurrentRunningActivity() { return currentRunningActivity; }

    public void setCurrentRunningActivity(String currentRunningActivity) { this.currentRunningActivity = currentRunningActivity; }

    public String getOrganization() {
      //  return organization;
    	return "hhy6xb";
//        return "eywvcm";
        
        
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getDeviceId() {
//       return deviceId;
//       return "mvk-andriod";
       
      return "8c705ae36b0c";
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getAuthToken() {
//        return authToken;
        return "(LxTDNY&w79Qr*D*k*";
   
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public void setConnectionType(Constants.ConnectionType type) {
        this.connectionType = type;
    }

    public Constants.ConnectionType getConnectionType() {
        return this.connectionType;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public int getPublishCount() {
        return publishCount;
    }

    public void setPublishCount(int publishCount) {
        this.publishCount = publishCount;
    }

    public int getReceiveCount() {
        return receiveCount;
    }

    public void setReceiveCount(int receiveCount) {
        this.receiveCount = receiveCount;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public float[] getAccelData() { return accelData; };

    public void setAccelData(float[] accelData) {
        this.accelData = accelData.clone();
    }

    public ArrayList<String> getMessageLog() {
        return messageLog;
    }

    public boolean isAccelEnabled() {
        return accelEnabled;
    }

    public void setAccelEnabled(boolean accelEnabled) {
        this.accelEnabled = accelEnabled;
    }

    public DeviceSensor getDeviceSensor() {
        return deviceSensor;
    }

    public void setDeviceSensor(DeviceSensor deviceSensor) {
        this.deviceSensor = deviceSensor;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
    }

    public List<IoTProfile> getProfiles() {
        return profiles;
    }

    public ArrayList<String> getProfileNames() {
        return profileNames;
    }
}
