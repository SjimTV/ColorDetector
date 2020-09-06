package com.sjimtv.colordetector;

import android.os.AsyncTask;

import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortOut;

import java.io.IOException;

public class SendHTMLOSCTask extends AsyncTask<String, Void, Void> {

    private OSCPortOut oscSender;


    SendHTMLOSCTask(OSCPortOut oscSender){
        this.oscSender = oscSender;
    }

    @Override
    protected Void doInBackground(String ... html) {


            try {
                oscSender.send(prepareMessage("/html", html[0]));
            } catch (IOException e) {
                e.printStackTrace();
            }



        return null;
    }


    private OSCMessage prepareMessage(String address, String html){
        OSCMessage message = new OSCMessage();
        message.setAddress(address);
        message.addArgument(html);

       return message;
    }
}
