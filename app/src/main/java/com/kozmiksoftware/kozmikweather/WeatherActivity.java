package com.kozmiksoftware.kozmikweather;

import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.kozmiksoftware.kozmikweather.Model.DailyWeatherReport;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class WeatherActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, LocationListener {


    final String URL_BASE = "http://api.openweathermap.org/data/2.5/forecast/daily";
    final String URL_COORD = "?lat=";                                                           //"?lat=-34.633930&lon=-58.444174";
    final String URL_UNITS = "&units=imperial";
    final String URL_API_KEY = "&APPID=10b24a90ee9cc631054449d19b406c26";
    final String URL_COUNT = "&cnt=7";

    private GoogleApiClient mGoogleApiClient;
    private final int PERMISSION_LOCATION = 111;
    private ArrayList<DailyWeatherReport> reports = new ArrayList<>();
    private WeatherAdapter adapter;
    private ImageView weatherIcon;
    private ImageView weatherIconMini;
    private TextView weatherDate;
    private TextView currentTemp;
    private TextView lowTemp;
    private TextView cityCountry;
    private TextView weatherDescription;
    private TextView appName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        weatherIcon = (ImageView)findViewById(R.id.weatherIcon);
        weatherIconMini = (ImageView)findViewById(R.id.weatherIconMini);
        weatherDate = (TextView)findViewById(R.id.weatherDate);
        currentTemp = (TextView)findViewById(R.id.currentTemp);
        lowTemp = (TextView) findViewById(R.id.lowTemp);
        cityCountry = (TextView) findViewById(R.id.cityCountry);
        weatherDescription = (TextView)findViewById(R.id.weatherDescription);
        appName = (TextView)findViewById(R.id.appName);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .enableAutoManage(this, this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.content_weather_reports);

        adapter = new WeatherAdapter(reports);
        recyclerView.setAdapter(adapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getBaseContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        recyclerView.setLayoutManager(layoutManager);
    }

    public void downloadWeatherData(Location location) {
        final String fullCoords = URL_COORD + location.getLatitude() + "&lon=" + location.getLongitude();
        final String url = URL_BASE + fullCoords + URL_UNITS + URL_API_KEY + URL_COUNT;

        //Log.v("FUN2", "URL: " + url);

        final JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try{
                    JSONObject city = response.getJSONObject("city");
                    String cityName = city.getString("name");
                    String country = city.getString("country");

                    JSONArray list = response.getJSONArray("list");

                    for (int x = 0; x < list.length(); x++) {
                        JSONObject obj = list.getJSONObject(x);
                        JSONObject main = obj.getJSONObject("temp");
                        Double dayTemp = main.getDouble("day");
                        Double minTemp = main.getDouble("min");
                        Double maxTemp = main.getDouble("max");

                        JSONArray weatherArr = obj.getJSONArray("weather");
                        JSONObject weather = weatherArr.getJSONObject(0);
                        String weatherType = weather.getString("main");

                        String rawDate = obj.getString("dt");

                        DailyWeatherReport report = new DailyWeatherReport(cityName,dayTemp.intValue(),maxTemp.intValue(),minTemp.intValue(),weatherType,country,rawDate);

                        //Log.v("JSON", "Printing from class: " + report.getWeather());
                        reports.add(report);

                    }

                    //Log.v("JSON", "Name: " + cityName + " - " + "Country: " + country);
                }catch(JSONException e){
                    //Log.v("JSON", "EXC: " + e.getLocalizedMessage());
                }

                updateUI();
                adapter.notifyDataSetChanged();

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Log.v("FUN", "Err: " + error.getLocalizedMessage());
            }
        });

        Volley.newRequestQueue(this).add(jsonRequest);
    }

    public void updateUI() {
        if(reports.size() > 0) {
            DailyWeatherReport report = reports.get(0);

            switch(report.getWeather()) {
                case DailyWeatherReport.WEATHER_TYPE_CLOUDS:
                    weatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.cloudy));
                    weatherIconMini.setImageDrawable(getResources().getDrawable(R.drawable.cloudy));
                    break;
                case DailyWeatherReport.WEATHER_TYPE_RAIN:
                    weatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.rainy));
                    weatherIconMini.setImageDrawable(getResources().getDrawable(R.drawable.rainy));
                    break;
                case DailyWeatherReport.WEATHER_TYPE_SNOW:
                    weatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.snow));
                    weatherIconMini.setImageDrawable(getResources().getDrawable(R.drawable.snow));
                    break;
                default:
                    weatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.sunny));
                    weatherIconMini.setImageDrawable(getResources().getDrawable(R.drawable.sunny));
            }

            appName.setText("Kozmik Weather");
            weatherDate.setText("Today, " + report.getDateString());
            currentTemp.setText(Integer.toString(report.getdayTemp()));
            lowTemp.setText(Integer.toString(report.getMinTemp()));
            cityCountry.setText(report.getCityName() + ", " + report.getCountry());
            weatherDescription.setText(report.getWeather());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_LOCATION);
        }else{
            startLocationServices();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        downloadWeatherData(location);
    }

    public void startLocationServices() {
        try {
            LocationRequest req = LocationRequest.create().setPriority(LocationRequest.PRIORITY_LOW_POWER);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,req,this);
        }catch(SecurityException exception) {

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSION_LOCATION: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocationServices();
                } else {
                    // Show a dialog saying something like, "I can't run your location dummy - you denied permission!"
                    Toast.makeText(this, "I can't run your location dummy - you denied permission!", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public class WeatherViewHolder extends RecyclerView.ViewHolder {

        private ImageView lweatherIcon;
        private TextView  lweatherDate;
        private TextView  lweatherDescription;
        private TextView  ltempHigh;
        private TextView  ltempLow;

        public WeatherViewHolder(View itemView) {
            super(itemView);

            lweatherIcon = (ImageView)itemView.findViewById(R.id.list_weather_icon);
            lweatherDescription = (TextView)itemView.findViewById(R.id.list_weather_description);
            lweatherDate = (TextView)itemView.findViewById(R.id.list_weather_day);
            ltempHigh = (TextView)itemView.findViewById(R.id.list_weather_temp_high);
            ltempLow = (TextView)itemView.findViewById(R.id.list_weather_temp_low);
        }

        public void updateUI(DailyWeatherReport report) {


            //Log.v("KEY", "Weather: " + report.getWeather());

            switch (report.getWeather()) {
                case DailyWeatherReport.WEATHER_TYPE_SNOW:
                    lweatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.snow_mini));
                    break;
                case DailyWeatherReport.WEATHER_TYPE_CLOUDS:
                    lweatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.cloudy_mini));
                    break;
                case DailyWeatherReport.WEATHER_TYPE_RAIN:
                    lweatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.rainy_mini));;
                    break;
                default:
                    lweatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.sunny_mini));
            }

            lweatherDescription.setText(report.getWeather());
            lweatherDate.setText(report.getDateString());
            ltempHigh.setText(Integer.toString(report.getMaxTemp()));
            ltempLow.setText(Integer.toString(report.getMinTemp()));
        }
    }

    public class WeatherAdapter extends RecyclerView.Adapter<WeatherViewHolder> {

        private ArrayList<DailyWeatherReport> list;

        public WeatherAdapter(ArrayList<DailyWeatherReport> list) {
            this.list = list;
        }

        @Override
        public void onBindViewHolder(WeatherViewHolder holder, int position) {
            DailyWeatherReport report = list.get(position);
            holder.updateUI(report);
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        @Override
        public WeatherViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View card = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_weather, parent, false);
            return new WeatherViewHolder(card);
        }
    }
}
