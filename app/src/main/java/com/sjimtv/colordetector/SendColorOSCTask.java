package com.sjimtv.colordetector;

import android.os.AsyncTask;

import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortOut;

import java.io.IOException;

public class SendColorOSCTask extends AsyncTask<int[], Void, Void> {

    private OSCPortOut oscSender;
    private boolean sendMQ;

    private final String[] colorArguments = {"/color/red", "/color/green", "/color/blue", "/color/white"};
    private final String[] mqArguments = {"/rpc/6,16,%sH", "/rpc/6,17,%sH", "/rpc/6,18,%sH", "/rpc/6,19,%sH"};

    SendColorOSCTask(OSCPortOut oscSender, boolean sendMQ){
        this.oscSender = oscSender;
        this.sendMQ = sendMQ;
    }

    @Override
    protected Void doInBackground(int[]... colorArray) {

        for (int i = 0; colorArray[0].length > i; i++){
            try {
                if (sendMQ){
                    int colorValue = colorArray[0][i];
                    if (i != 3) colorValue = turnByteUpsideDown(colorValue);
                    oscSender.send(prepareUncheckedMessage(String.format(mqArguments[i], colorValue)));
                } else {
                    oscSender.send(prepareMessage(colorArguments[i], colorArray[0][i]));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    private OSCMessage prepareMessage(String address, int value){
        OSCMessage message = new OSCMessage();
        message.setAddress(address);
        message.addArgument(value);
       return message;
    }

    private UncheckedOSCMessage prepareUncheckedMessage(String address){
        return new UncheckedOSCMessage(address, null);
    }

    private int turnByteUpsideDown(int oldValue){
        return 255 - oldValue;
    }
}
