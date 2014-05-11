package com.example.smsgateway.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class IncomingSms extends BroadcastReceiver {

    // Get the object of SmsManager
    final SmsManager sms = SmsManager.getDefault();

    public void onReceive(Context context, Intent intent) {

        // Retrieves a map of extended data from the intent.
        final Bundle bundle = intent.getExtras();

        try {

            if (bundle != null) {

                final Object[] pdusObj = (Object[]) bundle.get("pdus");

                for (int i = 0; i < pdusObj.length; i++) {

                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
                    String phoneNumber = currentMessage.getDisplayOriginatingAddress();

                    String senderNum = phoneNumber;
                    String message = currentMessage.getDisplayMessageBody();

                    Log.i("SmsReceiver", "senderNum: " + senderNum + "; message: " + message);
                    new SendPanicSMSRequest().execute(message);

                } // end for loop
            } // bundle is null

        } catch (Exception e) {
            Log.e("SmsReceiver", "Exception smsReceiver" +e);

        }
    }

    private class SendPanicSMSRequest extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String message=params[0];

            HttpClient httpClient = new DefaultHttpClient();
            HttpContext localContext = new BasicHttpContext();
            String postUrl = null;
            try {
                postUrl = String.format("http://roamsafely.appspot.com/User/Panic/FromSMS?text=%s",
                        URLEncoder.encode(message, "utf-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            HttpGet getRequest = new HttpGet(postUrl);
            String responseText = null;
            try {
                HttpResponse response = httpClient.execute(getRequest, localContext);
                responseText = response.getStatusLine().toString();
            } catch (Exception e) {
                responseText = e.getLocalizedMessage();
            }
            Log.d("CheckUserRegistration Post URL", postUrl);
            Log.d("CheckUserRegistration Response", responseText);
            return responseText;

        }

        protected void onPostExecute(String result) {
            if (!result.contains("200")) {
                return;
            } else {
                Log.d("Failed panic from sms", result);
            }


        }

    }
}