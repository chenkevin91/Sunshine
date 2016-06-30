package com.example.android.sunshine.app;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.sunshine.app.data.WeatherContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = DetailFragment.class.getSimpleName();

    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";

    private ShareActionProvider mShareActionProvider;
    private String mForecast;

    private static final int DETAIL_LOADER = 0;

    private static final String[] DETAIL_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID
    };

    // these constants correspond to the projection defined above, and must change if the
    // projection changes
    private static final int COL_WEATHER_ID = 0;
    private static final int COL_WEATHER_DATE = 1;
    private static final int COL_WEATHER_DESC = 2;
    private static final int COL_WEATHER_MAX_TEMP = 3;
    private static final int COL_WEATHER_MIN_TEMP = 4;
    private static final int COL_WEATHER_HUMIDITY = 5;
    private static final int COL_WEATHER_WIND_SPEED = 6;
    private static final int COL_WEATHER_DEGREES = 7;
    private static final int COL_WEATHER_PRESSURE = 8;
    private static final int COL_WEATHER_CONDITION_ID = 9;

    private ImageView mIconView;
    private TextView mDayView;
    private TextView mDateView;
    private TextView mDescriptionView;
    private TextView mHighTempView;
    private TextView mLowTempView;
    private TextView mHumidityView;
    private TextView mWindSpeedView;
    private TextView mPressureView;


    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        mIconView = (ImageView) rootView.findViewById(R.id.detail_icon);
        mDayView = (TextView) rootView.findViewById(R.id.detail_day_textview);
        mDateView = (TextView) rootView.findViewById(R.id.detail_date_textview);
        mDescriptionView = (TextView) rootView.findViewById(R.id.detail_forecast_textview);
        mHighTempView = (TextView) rootView.findViewById(R.id.detail_high_textview);
        mLowTempView = (TextView) rootView.findViewById(R.id.detail_low_textview);
        mHumidityView = (TextView) rootView.findViewById(R.id.detail_humidity_textview);
        mWindSpeedView = (TextView) rootView.findViewById(R.id.detail_wind_textview);
        mPressureView = (TextView) rootView.findViewById(R.id.detail_pressure_textview);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.detailfragment, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // If onLoadFinished happens before this, we can go ahead and set the share intent now.
        if (mForecast != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mForecast + FORECAST_SHARE_HASHTAG);
        return shareIntent;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.v(LOG_TAG, "In onCreateLoader");
        Intent intent = getActivity().getIntent();
        if (intent == null) {
            return null;
        }

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(
                getActivity(),
                intent.getData(),
                DETAIL_COLUMNS,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.v(LOG_TAG, "In onLoadFinished");
        if (data != null && data.moveToFirst()) {

            long date = data.getLong(COL_WEATHER_DATE);
            String dateString = Utility.formatDate(date);
            String day = Utility.getDayName(getActivity(), date);
            String formattedMonthDay = Utility.getFormattedMonthDay(getActivity(), date);
            mDayView.setText(day);
            mDateView.setText(formattedMonthDay);

            boolean isMetric = Utility.isMetric(getActivity());
            String high = Utility.formatTemperature(getActivity(), data.getDouble(COL_WEATHER_MAX_TEMP), isMetric);
            mHighTempView.setText(high);

            String low = Utility.formatTemperature(getActivity(), data.getDouble(COL_WEATHER_MIN_TEMP), isMetric);
            mLowTempView.setText(low);

            int weatherId = data.getInt(COL_WEATHER_CONDITION_ID);
            mIconView.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));

            String weatherDescription = data.getString(COL_WEATHER_DESC);
            mDescriptionView.setText(weatherDescription);

            String formattedHumidity = Utility.getFormattedHumidity(getActivity(), data.getFloat(COL_WEATHER_HUMIDITY));
            mHumidityView.setText(formattedHumidity);

            String formattedWind = Utility.getFormattedWind(getActivity(), data.getFloat(COL_WEATHER_WIND_SPEED), data.getFloat(COL_WEATHER_DEGREES));
            mWindSpeedView.setText(formattedWind);

            String formattedPressure = Utility.getFormattedPressure(getActivity(), data.getFloat(COL_WEATHER_PRESSURE));
            mPressureView.setText(formattedPressure);

            mForecast = String.format("%s - %s - %s/%s", dateString, weatherDescription, high, low);

            // If onCreateOptionsMenu has already happened, we need to update the share intent now.
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareForecastIntent());
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}