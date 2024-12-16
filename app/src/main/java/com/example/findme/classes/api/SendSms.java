package com.example.findme.classes.api;

import android.os.AsyncTask;

import java.io.IOException;
import java.util.HashMap;

import models.SendEnhancedRequestBody;
import models.SendEnhancedResponseBody;
import models.SendRequestMessage;
import services.Courier;
import services.SendService;

public class SendSms extends AsyncTask<Void, Void, Void> {
    private String phoneNumber;
    private String body;

    public SendSms(String phoneNumber, String body) {
        this.phoneNumber = phoneNumber;
        this.body = body;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Courier.init("dk_prod_DR99X6DTEH4KC3PMYD5KD9RFFF4Z");

        SendEnhancedRequestBody request = new SendEnhancedRequestBody();
        SendRequestMessage message = new SendRequestMessage();

        HashMap<String, String> to = new HashMap<String, String>();
        to.put("phone_number", "+972" + phoneNumber.substring(1));
        message.setTo(to);

        HashMap<String, Object> content = new HashMap<String, Object>();
        content.put("body", ">\n" + body);
        message.setContent(content);

        request.setMessage(message);
        try {
            SendEnhancedResponseBody response = new SendService().sendEnhancedMessage(request);
            System.out.println(response.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        // Update UI if needed after the network operation is complete
    }
}

