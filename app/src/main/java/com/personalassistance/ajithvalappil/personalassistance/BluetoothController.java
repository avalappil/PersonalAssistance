package com.personalassistance.ajithvalappil.personalassistance;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.widget.Toast;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.io.*;
import android.os.Message;
import android.os.Bundle;

/**
 * Created by ajithvalappil2 on 1/5/15.
 */
public class BluetoothController extends Thread {

    public BluetoothAdapter btAdapter = null;
    public BluetoothSocket btSocket = null;
    public OutputStream outStream = null;
    public InputStream inStream = null;

    Handler handler = new Handler();

    // Well known SPP UUID
    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    // Insert your server's MAC address
    //private static String address = "D0:59:E4:E4:96:D0"; //note 4
    public static String address = "88:C9:D0:94:DE:3F";

    String processType = "init";
    /*
    * init - check if bluetooth is enabled or not
    * getlist - get list of bluetooth devices
    * setup - open connections
    *
    *
    *
     */

    boolean hasProcessComplete = false;
    boolean deviceHasBluetooth = false;
    boolean deviceBluetoothIsOn = false;

    public void run(){
        if (processType!=null && processType.equalsIgnoreCase("init")){
            init();
        }else if (processType!=null && processType.equalsIgnoreCase("getlist")){
            getList();
        }else if (processType!=null && processType.equalsIgnoreCase("setup")){
            setUp();
        }
    }

    public void init(){
        hasProcessComplete = false;
        deviceHasBluetooth = false;
        try{
            btAdapter = BluetoothAdapter.getDefaultAdapter();
            if (btAdapter == null) {
                // Device does not support Bluetooth
                deviceHasBluetooth = false;
                System.out.println("Device does not support Bluetooth");
            }else {
                System.out.println("\nDevice does support Bluetooth");
                deviceHasBluetooth = true;
                if (!btAdapter.isEnabled()) {
                    deviceBluetoothIsOn = false;
                }else{
                    deviceBluetoothIsOn = true;
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        hasProcessComplete = true;
    }

    public void getList(){
        hasProcessComplete = false;
        System.out.println("getList.....");
        try{
            if (btAdapter == null) {
                // Device does not support Bluetooth
                System.out.println("Device does not support Bluetooth");
            }else {
                System.out.println("getList Bluetooth");
                if (btAdapter.isEnabled()) {
                    Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
                    System.out.println("getList pairedDevices.size()" + pairedDevices.size());
                    // If there are paired devices
                    if (pairedDevices.size() > 0) {
                        // Loop through paired devices
                        for (BluetoothDevice device : pairedDevices) {
                            // Add the name and address to an array adapter to show in a ListView
                            PersonalAssistance.items.add(device.getName() + "\n" + device.getAddress());
                        }
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        hasProcessComplete = true;
    }

    public void setUp(){
        hasProcessComplete = false;
        PersonalAssistance.processingComplete = false;
        try{
            Message msg = handler.obtainMessage();
            Bundle bundle = new Bundle();
            // Set up a pointer to the remote node using it's address.
            BluetoothDevice device = btAdapter.getRemoteDevice(address);
            // Two things are needed to make a connection:
            //   A MAC address, which we got above.
            //   A Service ID or UUID.  In this case we are using the
            //     UUID for SPP.
            try {
                btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                System.out.println("Fatal Error In onResume() and socket create failed: " + e.getMessage() + ".");
            }

            // Discovery is resource intensive.  Make sure it isn't going on
            // when you attempt to connect and pass your message.
            btAdapter.cancelDiscovery();
            // Establish the connection.  This will block until it connects.
            System.out.println("...Connecting to Remote...");
            try {
                btSocket.connect();
                System.out.println("...Connection established and data link opened...");
                PersonalAssistance.isDevicesConnected = true;
            } catch (IOException e) {
                try {
                    btSocket.close();
                } catch (IOException e2) {
                    System.out.println("Fatal Error In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
                    PersonalAssistance.isDevicesConnected = false;
                }
            }

            // Create a data stream so we can talk to server.
            //Log.d(TAG, "...Creating Socket...");

            try {
                outStream = btSocket.getOutputStream();
                inStream = btSocket.getInputStream();
            } catch (IOException e) {
                System.out.println("Fatal Error In onResume() and output stream creation failed:" + e.getMessage() + ".");
                PersonalAssistance.isDevicesConnected = false;
            }

            if (PersonalAssistance.isDevicesConnected){
                bundle.putString("connected", "Connected");
            }else{
                bundle.putString("connected", "Disconnected");
            }
            PersonalAssistance.processingComplete = true;
            msg.setData(bundle);
            handler.sendMessage(msg);

        }catch(Exception e){
            e.printStackTrace();
        }
        hasProcessComplete = true;
    }

    public BluetoothAdapter getBtAdapter() {
        return btAdapter;
    }

    public void setBtAdapter(BluetoothAdapter btAdapter) {
        this.btAdapter = btAdapter;
    }

    public BluetoothSocket getBtSocket() {
        return btSocket;
    }

    public void setBtSocket(BluetoothSocket btSocket) {
        this.btSocket = btSocket;
    }

    public OutputStream getOutStream() {
        return outStream;
    }

    public void setOutStream(OutputStream outStream) {
        this.outStream = outStream;
    }

    public InputStream getInStream() {
        return inStream;
    }

    public void setInStream(InputStream inStream) {
        this.inStream = inStream;
    }

    public static UUID getMyUuid() {
        return MY_UUID;
    }

    public static String getAddress() {
        return address;
    }

    public static void setAddress(String address) {
        BluetoothController.address = address;
    }

    public String getProcessType() {
        return processType;
    }

    public void setProcessType(String processType) {
        this.processType = processType;
    }

    public boolean isHasProcessComplete() {
        return hasProcessComplete;
    }

    public void setHasProcessComplete(boolean hasProcessComplete) {
        this.hasProcessComplete = hasProcessComplete;
    }

    public boolean isDeviceHasBluetooth() {
        return deviceHasBluetooth;
    }

    public void setDeviceHasBluetooth(boolean deviceHasBluetooth) {
        this.deviceHasBluetooth = deviceHasBluetooth;
    }

    public boolean isDeviceBluetoothIsOn() {
        return deviceBluetoothIsOn;
    }

    public void setDeviceBluetoothIsOn(boolean deviceBluetoothIsOn) {
        this.deviceBluetoothIsOn = deviceBluetoothIsOn;
    }

    public Handler getHandler() {
        return handler;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }
}
