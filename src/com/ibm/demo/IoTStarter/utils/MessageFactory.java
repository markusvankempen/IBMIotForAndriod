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

/**
 * Build messages to be published by the application.
 * This class is currently unused.
 */
public class MessageFactory {
    private final static String TAG = MessageFactory.class.getName();

    /**
     * Construct a JSON formatted string accel event message
     * @param G Float array with accelerometer x, y, z data
     * @param O Float array with gyroscope roll, pitch data
     * @param yaw Float representing gyroscope yaw value
     * @param lon Double containing device longitude
     * @param lat Double containing device latitude
     * @return String containing JSON formatted message
     */
    public static String getAccelMessage(float G[], float O[], float yaw, double lon, double lat) {
        String messageData = "{ \"d\": {" +
        		"\"myName\":\"MVK Nexus Accelerometer\", " +
                "\"acceleration_x\":" + G[0] + ", " +
                "\"acceleration_y\":" + G[1] + ", " +
                "\"acceleration_z\":" + G[2] + ", " +
                "\"roll\":" + O[2] + ", " +
                "\"pitch\":" + O[1] + ", " +
                "\"yaw\":" + yaw + ", " +
                "\"lon\":" + lon + ", " +
                "\"lat\":" + lat + " " +
                "} }";
        return messageData;
    }

    /**
     * Construct a JSON formatted string text event message
     * @param text String of text message to send
     * @return String containing JSON formatted message
     */
    public static String getTextMessage(String text) {
        String messageData = "{\"d\":{" +
                "\"text\":\"" + text.toString() + "\"" +
                " } }";
        return messageData;
    }

    /**
     * Construct a JSON formatted string touchmove event message
     * @param x Double of relative x position on screen
     * @param y Double of relative y position on screen
     * @param dX Double of relative x delta from previous position
     * @param dY Double of relative y delta from previous position
     * @param ended True if final message of the touch, false otherwise
     * @return String containing JSON formatted message
     */
    public static String getTouchMessage(double x, double y, double dX, double dY, boolean ended) {
        String endString;
        if (ended) {
            endString = ", \"ended\":1 } }";
        } else {
            endString = " } }";
        }

        String messageData = "{ \"d\": { " +
                "\"screenX\":" + x + ", " +
                "\"screenY\":" + y + ", " +
                "\"deltaX\":" + dX + ", " +
                "\"deltaY\":" + dY +
                endString;
        return messageData;
    }

}
