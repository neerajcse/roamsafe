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
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONObject;


public class RegisterActivity extends Activity {

    // used to track if the user is registered or not.
    boolean userRegistered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Get Phone Number
        TelephonyManager mTelephonyMgr;
        mTelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String yourNumber = mTelephonyMgr.getLine1Number();

        TextView phoneNumberTextView = (TextView)findViewById(R.id.phone_value);
        phoneNumberTextView.setText(yourNumber);

        // Panic Button
        Button button = (Button) findViewById(R.id.register);
        button.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                String phoneNumber = ((TextView) findViewById(R.id.phone_value)).getText().toString();
                String name = ((EditText) findViewById(R.id.name_value)).getText().toString();
                String address = ((EditText) findViewById(R.id.address_value)).getText().toString();
                String eContact1 = ((EditText) findViewById(R.id.emergency_phone1_value)).getText().toString();
                String eContact2 = ((EditText) findViewById(R.id.emergency_phone2_value)).getText().toString();
                String eContact3 = ((EditText) findViewById(R.id.emergency_phone3_value)).getText().toString();
                boolean helpUnknown = ((CheckBox) findViewById(R.id.help_unknown_value)).isChecked();
                boolean sendMsgToUnknown = ((CheckBox) findViewById(R.id.send_distress_unknown_value)).isChecked();

                new RegisterUser().execute(phoneNumber,
                        name,
                        address,
                        eContact1,
                        eContact2,
                        eContact3,
                        Boolean.toString(helpUnknown),
                        Boolean.toString(sendMsgToUnknown));
            }
        });

        // check if the user is registered
        new CheckUserRegistration().execute(yourNumber);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.register, menu);
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
                userRegistered = false;
            } else {
                userRegistered = true;
            }
        }

    }

    private class RegisterUser extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            if (userRegistered) {
                return processUserRegistered(params);
            } else {
                return processUserNotRegistered(params);
            }
        }

        protected String processUserNotRegistered(String... params) {
            String phoneNumber=params[0];
            String name = params[1];
            String address = params[2];
            String eContact1 = params[3];
            String eContact2 = params[4];
            String eContact3 = params[5];
            String helpUnknown = params[6];
            String sendMsgToUnknown = params[7];

            String responseText = null;
            String postUrl = String.format("http://roamsafely.appspot.com/User/PUT");
            try {
                JSONObject jsonObject = new JSONObject();
                if (!name.isEmpty()) {
                    jsonObject.put("name", name);
                }
                if (!address.isEmpty()) {
                    jsonObject.put("address", address);
                }
                if (!phoneNumber.isEmpty()) {
                    jsonObject.put("phone_number", phoneNumber);
                }
                if (!eContact1.isEmpty()) {
                    jsonObject.put("emergency_phone_1", eContact1);
                }
                if (!eContact2.isEmpty()) {
                    jsonObject.put("emergency_phone_2", eContact2);
                }
                if (!eContact3.isEmpty()) {
                    jsonObject.put("emergency_phone_3", eContact3);
                }
                if (!helpUnknown.isEmpty()) {
                    jsonObject.put("help_unknown_people", Boolean.parseBoolean(helpUnknown));
                }
                if (!sendMsgToUnknown.isEmpty()) {
                    jsonObject.put("send_distress_to_unknown_people", Boolean.parseBoolean(sendMsgToUnknown));
                }

                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(postUrl);

                HttpParams httpParameters = new BasicHttpParams();

                httpclient = new DefaultHttpClient(httpParameters);

                StringEntity se = new StringEntity(jsonObject.toString());
                Log.d("LocationWebService JSON ", jsonObject.toString());
                httppost.setEntity(se);

                Log.d("POST URL ", httppost.toString());

                HttpResponse response;
                response = httpclient.execute(httppost);
                responseText = response.getStatusLine().toString();
                Log.d("RegisterUser POST Status ", responseText);
            } catch (Exception e) {
                responseText = e.getLocalizedMessage();
            }
            Log.d("RegisterUser User Not Registered Before Post URL", postUrl);
            Log.d("RegisterUser User Not Registered Before Response", responseText);
            return responseText;
        }

        protected String processUserRegistered(String... params) {
            String phoneNumber=params[0];
            String name = params[1];
            String address = params[2];
            String eContact1 = params[3];
            String eContact2 = params[4];
            String eContact3 = params[5];
            String helpUnknown = params[6];
            String sendMsgToUnknown = params[7];

            String responseText = null;
            String postUrl = String.format("http://roamsafely.appspot.com/User/POST/%s",phoneNumber);
            try {
                JSONObject jsonObject = new JSONObject();
                if (!name.isEmpty()) {
                    jsonObject.put("name", name);
                }
                if (!address.isEmpty()) {
                    jsonObject.put("address", address);
                }
                if (!eContact1.isEmpty()) {
                    jsonObject.put("emergency_phone_1", eContact1);
                }
                if (!eContact2.isEmpty()) {
                    jsonObject.put("emergency_phone_2", eContact2);
                }
                if (!eContact3.isEmpty()) {
                    jsonObject.put("emergency_phone_3", eContact3);
                }
                if (!helpUnknown.isEmpty()) {
                    jsonObject.put("help_unknown_people", Boolean.parseBoolean(helpUnknown));
                }
                if (!sendMsgToUnknown.isEmpty()) {
                    jsonObject.put("send_distress_to_unknown_people", Boolean.parseBoolean(sendMsgToUnknown));
                }

                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(postUrl);

                HttpParams httpParameters = new BasicHttpParams();

                httpclient = new DefaultHttpClient(httpParameters);

                StringEntity se = new StringEntity(jsonObject.toString());
                Log.d("LocationWebService JSON ", jsonObject.toString());
                httppost.setEntity(se);

                Log.d("POST URL ", httppost.toString());

                HttpResponse response;
                response = httpclient.execute(httppost);
                responseText = response.getStatusLine().toString();
                Log.d("RegisterUser POST Status ", responseText);
            } catch (Exception e) {
                responseText = e.getLocalizedMessage();
            }
            Log.d("RegisterUser User Registered Before Post URL", postUrl);
            Log.d("RegisterUser User Registered Before Response", responseText);
            return responseText;
        }

        protected void onPostExecute(String result) {
            if (!result.contains("200")) {
                return;
            } else {
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(intent);
            }
        }

    }

}
