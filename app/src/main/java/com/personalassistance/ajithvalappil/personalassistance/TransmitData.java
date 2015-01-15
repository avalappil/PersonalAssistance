package com.personalassistance.ajithvalappil.personalassistance;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by ajithvalappil2 on 1/12/15.
 */
public class TransmitData extends Thread{

    public OutputStream outStream = null;
    public boolean continueProcess = true;

    public void run(){

        while(continueProcess){

/*            try{
                Thread.sleep(10);
            }catch (InterruptedException  interruptedException){
                System.out.println(  "First Thread is interrupted when it is  sleeping" +interruptedException);
            }*/
            //send the data to controller
            //sendMessage(PersonalAssistance.faceData);
            sendMessage("f");
            sendMessage(String.valueOf(PersonalAssistance.numberOfFaces));
            sendMessage("x");
            sendMessage(String.valueOf(PersonalAssistance.servoXPosition));
            sendMessage("y");
            sendMessage(String.valueOf(PersonalAssistance.servoYPosition));

        }
    }

    public void sendMessage(String messg){
        byte[] msgBuffer = messg.getBytes();
        try {
            if (outStream!=null) {
                //System.out.println(messg);
                outStream.write(msgBuffer);
            }else{
                System.out.println("Please connect to a device...");
            }
        } catch (IOException e) {
            System.out.println("In onResume() and an exception occurred during write: " + e.getMessage());
        }
    }

    public OutputStream getOutStream() {
        return outStream;
    }

    public void setOutStream(OutputStream outStream) {
        this.outStream = outStream;
    }

    public boolean isContinueProcess() {
        return continueProcess;
    }

    public void setContinueProcess(boolean continueProcess) {
        this.continueProcess = continueProcess;
    }
}
