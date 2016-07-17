package com.example.dopravo.vollyproject.service;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.example.dopravo.vollyproject.data.Channel;
import com.example.dopravo.vollyproject.data.Items;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Dopravo on 7/4/2016.
 */
public class YahooWeatherService {
    private WeatherServiceCallback callback;
    private String location;
    private Exception error;
    private String tempTitle;

    public String getTempTitle() {
        return tempTitle;
    }

    public YahooWeatherService(WeatherServiceCallback callback) {
        this.callback = callback;
    }

    public void refreshWeather(String l) {
        this.location = l;

        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                String YQL = String.format("select * from weather.forecast where woeid in (select woeid from geo.places(1) where text=\"%s\")", params[0]);
                String endPoint = String.format("https://query.yahooapis.com/v1/public/yql?q=%s&format=json", Uri.encode(YQL));
                try {
                    URL url = new URL(endPoint);
                    URLConnection connection = url.openConnection();

                    InputStream inputStream = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    return result.toString();
                } catch (Exception e) {
                    error = e;
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String s) {
                if (s == null && error != null) {
                    callback.serviceFailure(error);
                    return;
                }

                try {
                    //get the JSON obj from the string
                    JSONObject data = new JSONObject(s);

                    //find the 'query' from in the json obj
                    JSONObject query = data.optJSONObject("query");

                    //find the 'count' from the query
                    int count = query.optInt("count");


                    if (count == 0 && query.optJSONObject("results") == null) {
                        //if no results where returned on the specified city name, then display the below toast
                        callback.serviceFailure(new LocationWeatherException("No weather info found for " + location));
                        return;
                    }

                    //otherwise, find the channel form the json result
                    Channel channel = new Channel();
                    channel.populate(query.optJSONObject("results").optJSONObject("channel"));

                    tempTitle = query.optJSONObject("results").optJSONObject("channel").optString("title");
                    Log.e("CITY", tempTitle);
                    tempTitle = tempTitle.substring(17);

                    //extract the needed results in MainActivity
                    callback.serviceSuccess(channel);

                } catch (JSONException e) {
                    callback.serviceFailure(e);
                }

            }
        }.execute(location);
    }

    public String getLocation() {
        return location;
    }

    public class LocationWeatherException extends Exception {
        public LocationWeatherException(String message) {
            super(message);


        }
    }
}
