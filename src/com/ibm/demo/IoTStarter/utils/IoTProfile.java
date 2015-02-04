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
package com.ibm.demo.IoTStarter.utils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class IoTProfile {
    private String profileName;
    private String organization;
    private String deviceID;
    private String authorizationToken;

    private static final String NAME_PREFIX = "name:";
    private static final String ORG_PREFIX = "org:";
    private static final String DEVICE_ID_PREFIX = "deviceId:";
    private static final String AUTH_TOKEN_PREFIX = "authToken:";

    public IoTProfile(String profileName, String organization, String deviceID, String authorizationToken) {
        this.profileName = profileName;
        this.organization = organization;
        this.deviceID = deviceID;
        this.authorizationToken = authorizationToken;
    }

    public IoTProfile(Set<String> profileSet) {
        Iterator<String> iter = profileSet.iterator();
        while (iter.hasNext()) {
            String value = iter.next();
            if (value.contains(NAME_PREFIX)) {
                this.profileName = value.substring(NAME_PREFIX.length());
            } else if (value.contains(ORG_PREFIX)) {
                this.organization = value.substring(ORG_PREFIX.length());
            } else if (value.contains(DEVICE_ID_PREFIX)) {
                this.deviceID = value.substring(DEVICE_ID_PREFIX.length());
            } else if (value.contains(AUTH_TOKEN_PREFIX)) {
                this.authorizationToken = value.substring(AUTH_TOKEN_PREFIX.length());
            }
        }
    }

    public Set<String> convertToSet() {
        // Put the new profile into the store settings and remove the old stored properties.
        Set<String> profileSet = new HashSet<String>();
        profileSet.add(NAME_PREFIX + this.profileName);
        profileSet.add(ORG_PREFIX + this.organization);
        profileSet.add(DEVICE_ID_PREFIX + this.deviceID);
        profileSet.add(AUTH_TOKEN_PREFIX + this.authorizationToken);

        return profileSet;
    }

    public String getProfileName() {
        return profileName;
    }

    public String getOrganization() {
        return organization;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public String getAuthorizationToken() {
        return authorizationToken;
    }
}
