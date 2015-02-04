#IoT Starter for Android
IoT Starter is a demo application for interacting with the IBM Internet of Things (IoT) Foundation.
The application turns your mobile device into a sensor that publishes and receives data to and from the cloud using the MQTT protocol.

# Slidely modified by mvk@ca.ibm.com

##Short Description
IoT Foundation is a cloud-hosted service to simplify managing all of your IoT devices.
It provides functionality for creating recipes which determine how devices communicate with each other.
This application demonstrates using an Android device as one of those IoT devices, and provides a variety of events and
commands that it can publish or receive data to and from.

IoT events and commands are user defined values used to differentiate the data that you publish or receive. For example,
if you have a device that is publishing GPS coordinates, you may choose to publish it as a 'GPS' event. Or, if you
want to send a reboot command to a device, you may choose to publish it as a 'system' or 'reboot' command.

The application can publish data to the following IoT event topics:
- Accelerometer (accel event)
- Touchmove (touchmove event)
- Text (text event)

The application can receive data on the following IoT command topics:
- Color (color command)
- Light (light command)
- Text (text command)
- Alert (alert command)

For more information on IoT Foundation, refer to https://internetofthings.ibmcloud.com/#/

##How it works
A device that is registered with IoT Foundation may publish event data or consume command data using the MQTT protocol.
The Eclipse Paho MQTT Android Service is used to publish and subscribe to IoT Foundation. This can be downloaded from
[Eclipse Paho MQTT Android Service](http://www.eclipse.org/paho/clients/android/).

MQTT is a lightweight messaging protocol that supports publish/subscribe messaging. With MQTT, an application publishes
messages to a topic. These messages may then be received by another application that is subscribed to that topic.
This allows for a detached messaging network where the subscribers and publishers do not need to be aware of each other.
The topics used by this application can be seen in the table below:

##Topics
|Topic|Sample Topic|Sample Message|
|:---------- |:---------- |:------------|
|iot-2/evt/your_event_name/fmt/json|iot-2/evt/touchmove/fmt/json|{"d":{"screenX":0,"screenY":0,"deltaX":0,"deltaY":0}}|
|iot-2/cmd/your_command_name/fmt/json|iot-2/cmd/light/fmt/json|{"d":{"light":"toggle"}}|

For more information on the MQTT protocol, see http://mqtt.org/

##Try it
In order to try the application, you must have an IoT Foundation organization. This can be done by signing up for an IBM
Bluemix trial and creating an instance of the Internet of Things service. This will create an an IoT organization
where you can register devices.

On launching the application for the first time, you will be prompted to enter your credentials to connect your device
to the IoT Foundation. The required information to connect your device includes:

- Your Organization ID, e.g. ab1cd
- Your Device ID, e.g. the MAC Address of your device. This should be the same ID as the device that you registered in
your IoT Foundation organization.
- Your device authorization token. This is returned when registering your device with the IoT Foundation.

Once you have entered the necessary credentials, you may activate your device as a sensor.
Pressing the 'Activate Sensor' button will connect the device to the IoT Foundation and allow it to begin publishing and receiving data.

##Prerequisites
Required:
- An IBM Bluemix account. A 30 day trial account is free. [https://ace.ng.bluemix.net/]
- An Internet of Things service registered in Bluemix.
- An Android SDK installation

##Installation
1. To install the application, download this project and import it into an IDE of your choice (IntelliJ, Android Studio, Eclipse, etc).
2. Set the project to use your Android SDK installation.
3. Set the project libraries to include the Eclipse Paho jar files in the libs directory.
4. Run the application.

##Notes
In order to really see this demo do something, you must have an application to consume its data and publish data back
to the application. For examples, refer to the [IoT Starter demo](http://m2m.demos.ibm.com/iotstarter.html).

##Resources
- [IoT Starter](http://m2m.demos.ibm.com/iotstarter.html)
- [IBM Internet of Things Foundation](https://internetofthings.ibmcloud.com/#/)
- [IBM Bluemix](https://ace.ng.bluemix.net)
- [IoT Recipes](https://developer.ibm.com/iot/)
- [IoT Quickstart](http://quickstart.internetofthings.ibmcloud.com/#/)
- [Node-RED](http://nodered.org/)
- [MQTT](http://mqtt.org/)