package com.example.roamsafe.app;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.*;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MainActivity extends ActionBarActivity implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener, LocationListener {

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 1001;

    GoogleMap map;
    LocationClient mLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
         * Create a new location client, using the enclosing class to
         * handle callbacks.
         */
        mLocationClient = new LocationClient(this, this, this);
        mLocationClient.connect();

        setContentView(R.layout.activity_main);

        map = ((MapFragment) getFragmentManager()
                .findFragmentById(R.id.map)).getMap();
        map.setMyLocationEnabled(true);


        // Panic Button
        Button button = (Button) findViewById(R.id.panicbutton);
        button.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Location mCurrentLocation;
                if (mLocationClient.isConnected()) {
                    mCurrentLocation = mLocationClient.getLastLocation();
                } else {
                    // set a dummy lat/long if locationClient is not connected
                    mCurrentLocation = new Location("");
                    mCurrentLocation.setLatitude(0.0);
                    mCurrentLocation.setLongitude(0.0);
                }
                // Get Phone Number
                TelephonyManager mTelephonyMgr;
                mTelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

                String yourNumber = mTelephonyMgr.getLine1Number();
                String latitude = mCurrentLocation.getLatitude() + "";
                String longitude = mCurrentLocation.getLongitude() + "";
                // Call Rest API for PANIC Mode
                Log.d("PANIC BUTTON PRESSED LOCATION", "Phone number: " + yourNumber +
                        "; Location:" + mCurrentLocation.getLatitude() +
                        ", " + mCurrentLocation.getLongitude());
                new PostPanicMessage().execute(yourNumber, latitude, longitude);

                // Call Emergency Number
                String hotLinePhoneNumber ="6504229421";

                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + hotLinePhoneNumber));
                callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(callIntent);
            }
        });

        startService(new Intent(this, UserLocationService.class));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    /*
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle dataBundle) {
        // Display the connection status
        Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
        Location mCurrentLocation = mLocationClient.getLastLocation();
        if (mCurrentLocation != null) {
            LatLng markerLoc = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            final CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(markerLoc)      // Sets the center of the map to Mountain View
                    .zoom(17)                   // Sets the zoom
                    .bearing(90)                // Sets the orientation of the camera to east
                    .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                    .build();
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                @Override
                public void onCameraChange(CameraPosition position) {
                    LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
                    new FetchUnSafeLocations().execute(bounds.southwest.latitude + "",
                            bounds.southwest.longitude + "",
                            bounds.northeast.latitude + "",
                            bounds.northeast.longitude + "");
                }
            });

        }
    }

    /*
     * Called by Location Services if the connection to the
     * location client drops because of an error.
     */
    @Override
    public void onDisconnected() {
        // Display the connection status
        Toast.makeText(this, "Disconnected. Please re-connect.",
                Toast.LENGTH_SHORT).show();
    }

    /*
     * Called by Location Services if the attempt to
     * Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            showErrorDialog(connectionResult.getErrorCode());
        }
    }

/*
 * Called when the Activity becomes visible.
 */
    @Override
    protected void onStart() {
        super.onStart();
        // Connect the client.
        mLocationClient.connect();
    }

    /*
     * Called when the Activity is no longer visible.
     */
    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        mLocationClient.disconnect();
        super.onStop();
    }

    void showErrorDialog(int code) {
        GooglePlayServicesUtil.getErrorDialog(code, this,
                REQUEST_CODE_RECOVER_PLAY_SERVICES).show();
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("WHATEVER", provider + " provider enabled");
    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onLocationChanged(Location location) {
        // Report to the UI that the location was updated
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
    }

    private class PostPanicMessage extends AsyncTask<String, Void, String> {

        private String panicedPhoneNumber;
        private String latitude;
        private String longitude;

        @Override
        protected String doInBackground(String... params) {

            panicedPhoneNumber=params[0];
            latitude = params[1];
            longitude = params[2];

            HttpClient httpClient = new DefaultHttpClient();
            HttpContext localContext = new BasicHttpContext();
//            String postUrl = String.format("http://roamsafely.appspot.com/User/Panic/%s/%s/%s",
//                    panicedPhoneNumber, latitude, longitude);
            // Dummy Wrong url for post to fail when sms would be sent to hotline number.
            String postUrl = String.format("http://roamsafely.appspot.com/User/Panic/WHATEVER/%s/%s/%s",
                    panicedPhoneNumber, latitude, longitude);
            HttpGet post = new HttpGet(postUrl);
            String responseText = null;
            try {
                HttpResponse response = httpClient.execute(post, localContext);
                responseText = response.getStatusLine().toString();
            } catch (Exception e) {
                responseText = e.getLocalizedMessage();
            }
            Log.d("PANIC BUTTON PRESSED Post URL", postUrl);
            Log.d("PANIC BUTTON PRESSED Response", responseText);
            return responseText;

        }

        protected void onPostExecute(String result) {
            // send sms if sending to server failed
            if (!result.contains("200")) {
                String hotLinePhoneNumber = "6504229421";
                String message = String.format(
                        "%s,%s,%s",
                        panicedPhoneNumber,
                        latitude,
                        longitude);

                SmsManager smsManager = SmsManager.getDefault();
                ArrayList<String> parts = smsManager.divideMessage(message);
                smsManager.sendMultipartTextMessage(hotLinePhoneNumber, null, parts, null, null);
            }

        }

    }

    private class FetchUnSafeLocations extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            String swLat=params[0];
            String swLong = params[1];
            String neLat = params[2];
            String neLong = params[3];

            HttpClient httpClient = new DefaultHttpClient();
            HttpContext localContext = new BasicHttpContext();
            String postUrl = String.format(
                    "http://roamsafely.appspot.com/UnsafeLocations/%s/%s/%s/%s",
                    swLat, swLong,
                    neLat, neLong);
            HttpGet post = new HttpGet(postUrl);
            String responseText = null;
            try {
                HttpResponse response = httpClient.execute(post, localContext);
                responseText = getASCIIContentFromEntity(response.getEntity());
            } catch (Exception e) {
                responseText = e.getLocalizedMessage();
            }
            Log.d("FetchUnSafeLocations: Map Bounding Box : ", postUrl);
            Log.d("FetchUnSafeLocations: Map Bounding Box : ", responseText);
            return responseText;

        }

        protected String getASCIIContentFromEntity(HttpEntity entity) throws IllegalStateException,
                IOException {
            InputStream inputStream = entity.getContent();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line = "";
            String result = "";
            while((line = bufferedReader.readLine()) != null)
                result += line;

            inputStream.close();
            return result;
        }

        protected void onPostExecute(String result) {
            if (!result.contains(";")) {
                return;
            }
            Log.d("FetchUnSafeLocations: onPostExecute result : ", result);
            String[] latLongs = result.substring(0, result.lastIndexOf(";")).split(";");
            for (int i = 0; i < latLongs.length; i++) {
                String[] strs = latLongs[i].split(",");
                map.addMarker(new MarkerOptions()
                        .position(new LatLng(Double.parseDouble(strs[0]),
                                Double.parseDouble(strs[1])))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                );

            }

        }

    }

}
