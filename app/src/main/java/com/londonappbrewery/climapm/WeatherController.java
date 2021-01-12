package com.londonappbrewery.climapm;

import android.Manifest;
import android.content.AsyncQueryHandler;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;


public class WeatherController extends AppCompatActivity
{

    // Constants:
    final int REQUEST_CODE = 123;
    final String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather";
    // App ID to use OpenWeather data
    final String APP_ID = "18a4229e915c828ed1fc72626c8a2da8";
    // Time between location updates (5000 milliseconds or 5 seconds)
    final long MIN_TIME = 5000;
    // Distance between location updates (1000m or 1km)
    final float MIN_DISTANCE = 1000;

    // TODO: Set LOCATION_PROVIDER here:
    String location = LocationManager.GPS_PROVIDER;


    // Member Variables:
    TextView mCityLabel;
    ImageView mWeatherImage;
    TextView mTemperatureLabel;
    ImageButton changeCityButton;

    // TODO: Declare a LocationManager and a LocationListener here:
    LocationManager mLocationManager;
    LocationListener mLocationListener;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_controller_layout);

        // Linking the elements in the layout to Java code
        mCityLabel = (TextView) findViewById(R.id.locationTV);
        mWeatherImage = (ImageView) findViewById(R.id.weatherSymbolIV);
        mTemperatureLabel = (TextView) findViewById(R.id.tempTV);
        changeCityButton = (ImageButton) findViewById(R.id.changeCityButton);


        // TODO: Add an OnClickListener to the changeCityButton here:
        changeCityButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent myIntent = new Intent(WeatherController.this, ChangeCityControler.class);
                startActivity(myIntent);
            }
        });
    }


    // TODO: Add onResume() here:
    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Clima", "onResume() Called");
        Intent myIntent = getIntent();
        String City = myIntent.getStringExtra("City");
       if (City != null)
        {
            getWeatherFromCity(City);
        }
       else
       {
           Log.d("Clima", "Getting weather for current location");
           getWeatherforCurrentLocation();
       }
    }


    // TODO: Add getWeatherForNewCity(String city) here:
    private void getWeatherFromCity(String City)
    {
        RequestParams params = new RequestParams();
        params.put("q", City);
        params.put("appid", APP_ID);
        lestDoSomeNetWorking(params);
    }


    // TODO: Add getWeatherForCurrentLocation() here:
    private void getWeatherforCurrentLocation()
    {
        mLocationManager = (LocationManager) getSystemService(getApplicationContext().LOCATION_SERVICE);

        mLocationListener = new LocationListener()
        {
            @Override
            public void onLocationChanged(Location location) {
                Log.d("Clima", "onLocationChanged() Callback received");

                String longitud = String.valueOf(location.getLongitude());
                String latitud = String.valueOf(location.getLatitude());

                Log.d("Clima", "Longitud is " + longitud);
                Log.d("Clima", "Latitud is " + latitud);

                RequestParams params =  new RequestParams();
                params.put("lat" , latitud);
                params.put("lon", longitud);
                params.put("appid", APP_ID);
                lestDoSomeNetWorking(params);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                Log.d("Clima", "onProviderDisabled(), callback received");
            }
        };


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return;
        }
        mLocationManager.requestLocationUpdates(location, MIN_TIME, MIN_DISTANCE, mLocationListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Log.d("Clima", "onRequestPermisionResult(): Permision granted!");
                getWeatherforCurrentLocation();
            }
            else
            {
                Log.d("Clima", "Permision denied = (");
            }
        }
    }
// TODO: Add letsDoSomeNetworking(RequestParams params) here:
    private void lestDoSomeNetWorking(RequestParams params)
    {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(WEATHER_URL, params, new JsonHttpResponseHandler()
        {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response)
            {
                Log.d("Clima", "Success ! JSON:" + response.toString());
                WeatherDataModel weatherData = WeatherDataModel.fromJsom(response);
                updateUI(weatherData);
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject response)
            {
                Log.e("Clima", "Fail" + e.toString());
                Log.d("Clima", "Status Code" + statusCode);
                Toast.makeText(WeatherController.this, "Request Failed", Toast.LENGTH_SHORT).show();
            }

        });
    }




    // TODO: Add updateUI() here:
    private void updateUI (WeatherDataModel weather)
    {
        mTemperatureLabel.setText(weather.getTemperature());
        mCityLabel.setText(weather.getCity());

        int resourceID = getResources().getIdentifier(weather.getMlconName(), "drawable", getPackageName());
        mWeatherImage.setImageResource(resourceID);
    }





    // TODO: Add onPause() here:


    @Override
    protected void onPause()
    {
        super.onPause();
        if (mLocationManager !=null) mLocationManager.removeUpdates(mLocationListener);


    }
}
