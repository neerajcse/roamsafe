package com.example.roamsafe.app;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import android.location.LocationListener;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONObject;
import android.os.Bundle;

import java.io.IOException;
import java.util.ArrayList;

public class UserLocationService extends Service {
    PowerManager.WakeLock wakeLock;

    private LocationManager locationManager;

    public UserLocationService() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        PowerManager pm = (PowerManager) getSystemService(this.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DoNotSleep");
        Log.d("UserLocationService", "Service Created");

    }



    @Override
    public void onStart(Intent intent, int startId) {
        // TODO Auto-generated method stub
        super.onStart(intent, startId);
        Log.d("UserLocationService", "Service Started");
        locationManager = (LocationManager) getApplicationContext()
                .getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                10000, 0, listener);

    }

    private LocationListener listener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            // TODO Auto-generated method stub
            Log.d("UserLocationService", "Location Changed");
            if (location == null)
                return;
            if (isConnectingToInternet(getApplicationContext())) {
                try {
                    Log.d("latitude", location.getLatitude() + "");
                    Log.d("longitude", location.getLongitude() + "");

                    // Get Phone Number
                    TelephonyManager mTelephonyMgr = (TelephonyManager)
                            getSystemService(Context.TELEPHONY_SERVICE);

                    String yourNumber = mTelephonyMgr.getLine1Number();
                    String latitude = (int) location.getLatitude() + "";
                    String longitude = (int) location.getLongitude() + "";
                    new LocationWebService().execute(yourNumber,
                            location.getLatitude() + "",
                            location.getLongitude() + "");
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }

        }


        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // TODO Auto-generated method stub

        }
    };

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();

        wakeLock.release();

    }

    public static boolean isConnectingToInternet(Context _context) {
        ConnectivityManager connectivity = (ConnectivityManager) _context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }

        }
        return false;
    }

    public class LocationWebService extends AsyncTask<String, String, String> {
        public LocationWebService() {
            // TODO Auto-generated constructor stub
        }

        @Override
        protected String doInBackground(String... params) {

            String phoneNumber=params[0];
            String latitude = params[1];
            String longitude = params[2];

            String postUrl = String.format("http://roamsafely.appspot.com/User/POST/%s",
                    phoneNumber);
            String responseText = "";
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("last_known_latitude", (Float.parseFloat(latitude)));

                jsonObject.put("last_known_longitude", (Float.parseFloat(longitude)));


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
                Log.d("LocationWebService POST Status ", responseText);
            } catch (Exception e) {
                responseText = e.getLocalizedMessage();
                Log.e("LocationWebService POST Failure Status ", responseText);
            }
            return responseText;
        }
    }


}
