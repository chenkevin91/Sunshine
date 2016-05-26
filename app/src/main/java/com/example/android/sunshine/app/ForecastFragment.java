package com.example.android.sunshine.app;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by kchen on 5/24/16.
 */
public class ForecastFragment extends Fragment {

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            new FetchWeatherTask().execute("02210");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);


        String[] forecastArray = {
                "Today - Sunny - 88/63",
                "Tomorrow - Foggy - 70/40",
                "Weds - Cloudy - 72/63",
                "Thurs - Asteroids - 75/65",
                "Fri - Heavy Rain - 65/56",
                "Sat - HELP TRAPPED IN WEATHERSTATION - 60/51",
                "Sun - Sunny - 80/68"
        };

        List<String> weekForecast = new ArrayList<String>(Arrays.asList(forecastArray));

        ArrayAdapter<String> mForecastAdapter = new ArrayAdapter<String>(
                getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                weekForecast);

        ListView listViewForecast = (ListView) rootView.findViewById(R.id.listview_forecast);
        listViewForecast.setAdapter(mForecastAdapter);

        return rootView;
    }

    public class FetchWeatherTask extends AsyncTask<String, Void, Void> {

        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        @Override
        protected Void doInBackground(String... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            String units = "metric";
            String mode = "json";
            int numDays = 7;
            final String urlApiKey = "08c551f27fdb86dc25fb808a092e8aa8";

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                final String ZIP_PARAM = "zip";
                final String UNIT_PARAM = "units";
                final String MODE_PARAM = "mode";
                final String DAYS_PARAM = "cnt";
                final String APPID_PARAM = "appid";

                Uri.Builder builder = new Uri.Builder();
                builder.scheme("http")
                        .authority("api.openweathermap.org")
                        .appendPath("data")
                        .appendPath("2.5")
                        .appendPath("forecast")
                        .appendPath("daily")
                        .appendQueryParameter(ZIP_PARAM, params[0])
                        .appendQueryParameter(UNIT_PARAM, units)
                        .appendQueryParameter(MODE_PARAM, mode)
                        .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                        .appendQueryParameter(APPID_PARAM, urlApiKey);
                URL url = new URL(builder.build().toString());
                Log.d(LOG_TAG, "url = " + url);


                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
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
                    return null;
                }
                forecastJsonStr = buffer.toString();

                Log.d(LOG_TAG, "Forecast JSON String:" + forecastJsonStr);
                Log.d("time test", "1464278400: " + getReadableDateString(1464278400));

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
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
            return null;
        }
    }


    /* The date/time conversion code is going to be moved outside the asynctask later,
     * so for convenience we're breaking it out into its own method now.
     */
    // TODO: getReadableDateString
    private String getReadableDateString(long time){
        Date date = new Date(time*1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, - MMM dd");
        String formattedDate = sdf.format(date);

        return formattedDate;
    }


    /**
     * Prepare the weather high/lows for presentation.
     */
    // TODO: formatHighLows
    private String formatHighLows(double high, double low) {
        String highString = Long.toString(Math.round(high));
        String lowString = Long.toString(Math.round(low));

        return highString+"/"+lowString;
    }

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     *
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    // TODO: getWeatherDataFromJson
    //Example: "Mon, Jun 1 - Clear - 18/13"
    private String[] getWeatherDataFromJson(String weatherJsonStr, int numDays)
            throws JSONException {
        String[] weatherData = new String[numDays];

        JSONObject weatherJ = new JSONObject(weatherJsonStr);
        JSONArray week = weatherJ.getJSONArray("list");
        for(int i=0; i<week.length(); i++){
            JSONObject day = week.getJSONObject(i);
            long time = day.getLong("dt");
            String formattedDate = getReadableDateString(time);

            JSONObject temps = day.getJSONObject("temp");
            String formattedTemps = formatHighLows(temps.getDouble("max"), temps.getDouble("min"));

            JSONObject weather = day.getJSONObject("weather");
            String weatherMain = weather.getString("main");

            weatherData[i] = formattedDate + " - " + weatherMain + " - " + formattedTemps;
        }

        return weatherData;
    }


}

