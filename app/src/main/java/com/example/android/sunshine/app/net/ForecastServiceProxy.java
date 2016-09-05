package com.example.android.sunshine.app.net;

import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.android.sunshine.app.util.Constants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by U6023035 on 26/08/2016.
 */
public class ForecastServiceProxy {
    private final String LOG_TAG = ForecastServiceProxy.class.getSimpleName();
    // These two need to be declared outside the try/catch
    // so that they can be closed in the finally block.
    HttpURLConnection urlConnection;
    BufferedReader reader;
    // Will contain the raw JSON response as a string.
    String forecastJsonStr;

    public ForecastServiceProxy()
    {
        urlConnection = null;
        reader = null;
        forecastJsonStr = null;
    }

    @Nullable
    public String callForecastService(String param) {

        try {
            // Construct the URL for the OpenWeatherMap query
            // Possible parameters are available at OWM's forecast API page, at
            // http://openweathermap.org/API#forecast

            Uri completeURI = Uri.parse(Constants.FORECAST_BASE_URL).buildUpon()
                    .appendQueryParameter(Constants.QUERY_PARAM, param)
                    .appendQueryParameter(Constants.FORMAT_PARAM, Constants.FORMAT_JSON)
                    .appendQueryParameter(Constants.UNITS_PARAM, Constants.UNITS_METRIC)
                    .appendQueryParameter(Constants.DAYS_PARAM, (String.valueOf(Constants.DEFAULT_FORECAST_DAYS)))
                    .appendQueryParameter(Constants.API_PARAM, Constants.API_KEY).build();
            URL url = new URL(completeURI.toString());
            Log.d(LOG_TAG, "Calling OpenWeatherMap with: " + completeURI.toString());

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                forecastJsonStr = null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                forecastJsonStr = null;
            }
            forecastJsonStr = buffer.toString();
            Log.v(LOG_TAG, "OpenWeatherMap succesfully called: " + forecastJsonStr);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attempting
            // to parse it.
            forecastJsonStr = null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
        return forecastJsonStr;
    }

}
