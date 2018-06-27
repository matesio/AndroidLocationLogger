package gps.apps.hammad.com.finallocation;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    String mlatitude, mlongitude, maltitude;
    JSONObject json;
    GoogleMap mGoogleMap;
    SupportMapFragment mapFrag;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    LocationManager locationManager;
    Handler handler = new Handler();
    double longitude;
    double latitude;
    double altitiude;
    double accuracy;

   String temlongitude;
    double temlatitude;
    double temaltitiude;
    //   double time ;
    String spmsg = null, s = null;

    List<NameValuePair> params;
    ServerRequest sr;

    //JSONParser jsonParser = new JSONParser();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        } else {
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                showGPSDisabledAlertToUser();
            }
        }

      /*  MongoClient mongoClient = new MongoClient("192.168.0.114:27017");
        db = mongoClient.getDB("gps");
        collectionNames = db.getCollectionNames();
       */
        mLastLocation=locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER);
       /// mLastLocation=locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);
        params = new ArrayList<NameValuePair>();
        sr = new ServerRequest();
        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFrag.getMapAsync(this);
    //    mlatitude=""+mLastLocation.getLatitude();
//        mlongitude=""+mLastLocation.getLongitude();
  //      maltitude=""+mLastLocation.getAltitude();
//        new  Operation().execute();
        //Toast.makeText(MapsActivity.this,"daskjdasffahfu"+mlongitude,Toast.LENGTH_LONG).show();
        handler.postDelayed(runLocation, 1000);
    }

    public Runnable runLocation = new Runnable() {

        @Override
        public void run() {
            mlongitude = String.valueOf(mLastLocation.getLongitude());
            mlatitude = String.valueOf(mLastLocation.getLatitude());
            maltitude = String.valueOf(mLastLocation.getAltitude());
            //     lat.setText(String.valueOf(latitude));
            //    lon.setText(String.valueOf(longitude));
            //  db.createCollection("hello",null);
            // params.add(new BasicNameValuePair("email",coordinates));
            //    Toast.makeText(MapsActivity.this, ""+mLastLocation.getLongitude()+"location check"+mLastLocation.getLatitude(), Toast.LENGTH_SHORT).show();
            if (!mlongitude.equals(null)&&!mlatitude.equals(null)&&!maltitude.equals(null)) {
                params.add(new BasicNameValuePair("latitude", "0" + mlongitude));
                params.add(new BasicNameValuePair("altitude","0"+mLastLocation.getAltitude()));
                 params.add(new BasicNameValuePair("longitude","0"+mLastLocation.getLongitude()));
                json = sr.getJSON("http://192.168.0.114:8080/register", params);

                if (json != null) {
                    try {
                        Toast.makeText(MapsActivity.this, "Value of json is thsi  here itt is" + json, Toast.LENGTH_LONG).show();
                        String jsonstr = json.getString("response");
                        Toast.makeText(getApplication(), jsonstr, Toast.LENGTH_LONG).show();
                        Log.d("JSPJon string sample", jsonstr);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else
                    Toast.makeText(MapsActivity.this, "Json errro" + mlongitude + mlatitude + maltitude, Toast.LENGTH_LONG).show();

//            new Operation().execute();
                MapsActivity.this.handler.postDelayed(MapsActivity.this.runLocation, 3000);

            }
        }
    };


    private void showGPSDisabledAlertToUser() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it?")
                .setCancelable(false)
                .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(callGPSSettingIntent);

                        mapFrag.getMapAsync(MapsActivity.this);
                    }
                });
        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    @Override
    public void onPause() {
        super.onPause();

        //stop location updates when Activity is no longer active
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mGoogleMap.setMyLocationEnabled(true);
            }
        } else {
            buildGoogleApiClient();
            mGoogleMap.setMyLocationEnabled(true);
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(10000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            try {
                Float thespeed = location.getSpeed();
                Float sp = (thespeed / 1000);
                spmsg = "Speed : " + sp;
                Toast.makeText(this, spmsg, Toast.LENGTH_LONG).show();

            } catch (Exception e) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
            }
            longitude = location.getLongitude();
            latitude = location.getLatitude();
            altitiude = location.getAltitude();
            accuracy = location.getAccuracy();
            CameraPosition cameraPosition = new CameraPosition.Builder().target(
                    new LatLng(latitude, longitude)).zoom(14).build();

            mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            MarkerOptions marker = new MarkerOptions().position(new LatLng(latitude, longitude)).title("Hello Maps ");

            // adding marker
            mGoogleMap.addMarker(marker);
            // mCurrLocationMarker.remove();
        }


        //Place current location marker

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
     //   Toast.makeText(this, "Longitude" + location.getLongitude() + "/t Latitude" + location.getLatitude(), Toast.LENGTH_LONG).show();
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);

        //move map camera
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(11));

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                showGPSDisabledAlertToUser();
            }

            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            showGPSDisabledAlertToUser();
                        }

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mGoogleMap.setMyLocationEnabled(true);
                    }
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public class Operation extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... args) {
     //       handler.postDelayed(runLocation, 1000);

        //    Toast.makeText(MapsActivity.this,"Executed successfully",Toast.LENGTH_LONG).show();



            return "true";

        }

        protected void onPostExecute(String result) {
             // txt.setText(result);
            // might want to change "executed" for the returned string passed
            // into onPostExecute() but that is upto you
        }
      /*  */
    }
}



