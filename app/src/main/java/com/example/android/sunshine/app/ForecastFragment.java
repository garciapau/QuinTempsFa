package com.example.android.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.sunshine.app.net.ForecastServiceProxy;
import com.example.android.sunshine.app.util.Constants;
import com.example.android.sunshine.app.util.WeatherDataParser;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by U6023035 on 25/08/2016.
 */

public class ForecastFragment extends Fragment {

    private final String LOG_TAG = ForecastFragment.class.getSimpleName();
    ArrayAdapter<String> forecastAdapter;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecast_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateWeather();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        List<String> weekForecast = new ArrayList<>();
        forecastAdapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_textview, weekForecast);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.list_view_forecast);
        listView.setAdapter(forecastAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                //Toast.makeText(getActivity(), forecastAdapter.getItem(position), Toast.LENGTH_SHORT).show();
                Intent detailIntent = new Intent(getActivity(), DetailActivity.class).putExtra(Intent.EXTRA_TEXT, forecastAdapter.getItem(position));
                startActivity(detailIntent);
            }
        });
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String defaultLocation = sharedPreferences.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default) );
        TextView textView = (TextView) rootView.findViewById(R.id.location);
        textView.setText(defaultLocation);

        return rootView;
    }

    private void updateWeather() {
        FetchWeatherTask fetchWeatherTask = new FetchWeatherTask();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String defaultLocation = sharedPreferences.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default) );
        fetchWeatherTask.execute(defaultLocation);
    }

    public void showMap(Uri geoLocation) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {
        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        @Override
        protected String[] doInBackground(String... params) {
            if (params.length == 0)
                return null;
            ForecastServiceProxy forecastServiceProxy = new ForecastServiceProxy();
            String forecastJsonStr = forecastServiceProxy.callForecastService(params[0]);
            try {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                String units = sharedPreferences.getString(getString(R.string.pref_temp_units_key), getString(R.string.pref_temp_units_default));
                return WeatherDataParser.getWeatherDataFromJson(forecastJsonStr, Constants.DEFAULT_FORECAST_DAYS, units);
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Error ", e);
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onProgressUpdate(Void... progress) {
        }

        @Override
        protected void onPostExecute(String[] result) {
            if (result!=null)
            {
                forecastAdapter.clear();
                for (String forecast : result)
                {
                    forecastAdapter.add(forecast);
                }
            }
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String defaultLocation = sharedPreferences.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default) );
            TextView textView = (TextView) getActivity().findViewById(R.id.location);
            textView.setText(defaultLocation);
        }
    }
}
