package com.example.roamsafe.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;


public class WelcomeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Get Phone Number
        TelephonyManager mTelephonyMgr;
        mTelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String yourNumber = mTelephonyMgr.getLine1Number();

        // check if the user is registered
        new CheckUserRegistration().execute(yourNumber);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.welcome, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class CheckUserRegistration extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            String phoneNumber = params[0];


            HttpClient httpClient = new DefaultHttpClient();
            HttpContext localContext = new BasicHttpContext();
            String postUrl = String.format("http://roamsafely.appspot.com/User/GET/%s",
                    phoneNumber);
            HttpGet post = new HttpGet(postUrl);
            String responseText = null;
            try {
                HttpResponse response = httpClient.execute(post, localContext);
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
                Intent intent = new Intent(WelcomeActivity.this, RegisterActivity.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                startActivity(intent);
            }
        }

    }

}
