package com.example.dopravo.vollyproject;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import com.example.dopravo.vollyproject.data.Channel;
import com.example.dopravo.vollyproject.data.Items;
import com.example.dopravo.vollyproject.service.WeatherServiceCallback;
import com.example.dopravo.vollyproject.service.YahooWeatherService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.*;

import android.location.*;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements WeatherServiceCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    LocationManager locationManager;
    private static final int LOCATION_REQUEST_CODE = 5000;
    private static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 900;
    private ImageView weatherIcon;
    private TextView tempTxt, conditionTxt, locationTxt;
    private YahooWeatherService service;
    private ProgressDialog dialog;
    RelativeLayout layoutHolder;
    String searchCity;
    Button findButton;
    AutoCompleteTextView searchEditText;
    private LocationRequest mLocationRequest;
    AlertDialog locationSettingsAD;
    //
    private GoogleApiClient mGoogleApiClient;
    public static final String TAG = MainActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        layoutHolder = (RelativeLayout) findViewById(R.id.relativeLayout1);

        weatherIcon = (ImageView) findViewById(R.id.weatherImgView);
        tempTxt = (TextView) findViewById(R.id.tempTxtView);
        conditionTxt = (TextView) findViewById(R.id.conditionTxtView);
        locationTxt = (TextView) findViewById(R.id.locationTxtView);
        searchEditText = (AutoCompleteTextView) findViewById(R.id.searchLocation);
        findButton = (Button) findViewById(R.id.button);

        service = new YahooWeatherService(this);
        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading..");
        dialog.show();

        /*create googleApiClient obj
        tell the new client that MainActivity will handle connections
        add location services
        build the client*/
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000);

    }

    public void findTempForLocationSpecified(View v) {
        dialog.show();
        //find the weather based on the typed location
        if (v.getId() == R.id.button) {
            searchCity = searchEditText.getText().toString();
            if (searchCity.equals("")) {
                service.refreshWeather("");
                Toast.makeText(this, "Please specify a location", Toast.LENGTH_SHORT).show();
            } else {
                service.refreshWeather(searchCity);
            }

            InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            //Find the currently focused view, so we can grab the correct window token from it.
            View view = getCurrentFocus();
            //If no view currently has focus, create a new one, just so we can grab a window token from it
            if (view == null) {
                view = new View(this);
            }
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            dialog.hide();
        }
    }

    @Override
    public void serviceSuccess(Channel channel) {
        //extract the item from the channel
        Items item = channel.getItem();

        //based on the item, specify the corresponding drawable using item.getCondition().getCode()
        int resourceId = getResources()
                .getIdentifier("drawable/icon_" + item.getCondition().getCode(), null, getPackageName());
        Drawable weatherIconDrawable = getResources().getDrawable(resourceId);

        //populate the UI with the extracted data
        weatherIcon.setImageDrawable(weatherIconDrawable);
        locationTxt.setText(service.getTempTitle());
        tempTxt.setText(item.getCondition().getTemp() + " " + channel.getUnit().getTemp());
        conditionTxt.setText(item.getCondition().getDescription());

        //get the temperature
        int tempITem = channel.getItem().getCondition().getTemp();
        Log.e("TEMP    ", tempITem + "");

        //based on temp, change the background color
        if (tempITem <= 80) {
            //setBackground = @color/lowTemp
            layoutHolder.setBackgroundColor(getResources().getColor(R.color.lowTemp));
        } else if (tempITem > 80 && tempITem <= 95) {
            //setBackground = @color/medTemp
            layoutHolder.setBackgroundColor(getResources().getColor(R.color.medTemp));
        } else if (tempITem > 95) {
            //setBackground = @color/highTemp
            layoutHolder.setBackgroundColor(getResources().getColor(R.color.highTemp));
        } else {
            layoutHolder.setBackgroundColor(getResources().getColor(R.color.backgroundColor));
        }

        dialog.hide();
    }


    @Override
    public void serviceFailure(Exception exception) {
        locationTxt.setText("No data found for the given location");
        tempTxt.setText("No data");
        conditionTxt.setText("No data");
        layoutHolder.setBackgroundColor(getResources().getColor(R.color.backgroundColor));
        Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
        dialog.hide();
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResult) {
        switch (requestCode) {
            case LOCATION_REQUEST_CODE:
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                boolean gpsEnabled = false, networkEnabled = false;
                try {
                    gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                    networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                } catch (Exception e) {
                }

                if (!gpsEnabled && !networkEnabled) {
                    Toast.makeText(this, "Location was not enabled", Toast.LENGTH_LONG).show();
                }
                locationSettingsAD.dismiss();
                return;

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        dialog.hide();
        Log.i(TAG, "Location service connected");
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location == null) {
            //Toast.makeText(this, "No location detected. Enable location!", Toast.LENGTH_LONG).show();
            locationSettingsAD = new AlertDialog.Builder(this)
                    .setTitle("No location detected")
                    .setMessage("Enable location from settings")
                    .setPositiveButton("Location settings", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), LOCATION_REQUEST_CODE);
                            locationSettingsAD.dismiss();
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            locationSettingsAD.dismiss();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            //locationSettingsAD.show();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

            // dialog.show();

        } else {
//            locationSettingsAD.show();
            if (locationSettingsAD != null && locationSettingsAD.isShowing()) {
                locationSettingsAD.dismiss();
            }
            handleNewLocation(location);
        }
    }

    private void handleNewLocation(Location location) {
        if (locationSettingsAD != null && locationSettingsAD.isShowing()) {
            locationSettingsAD.dismiss();
        }
        dialog.show();
        Log.d(TAG, location.toString());
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;
        Log.d("GEOCODER", geocoder.toString());

        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String cityName = addresses.get(0).getAddressLine(0);

        String stateName = addresses.get(0).getAddressLine(1);
        String countryName = addresses.get(0).getAddressLine(2);
        service.refreshWeather(stateName);
        Log.i(TAG, cityName + "\n" + stateName + "\n" + countryName);
        dialog.hide();

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location service suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }
}
