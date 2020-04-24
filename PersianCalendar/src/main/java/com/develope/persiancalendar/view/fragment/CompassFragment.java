package com.develope.persiancalendar.view.fragment;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.develope.persiancalendar.CalendarTool;
import com.develope.persiancalendar.R;
import com.develope.persiancalendar.util.Utils;
import com.develope.persiancalendar.view.QiblaCompassView;
import com.github.praytimes.Coordinate;

/**
 * Compass/Qibla activity
 *
 * @author ebraminio
 */
public class CompassFragment extends Fragment {
    public QiblaCompassView compassView;
    private SensorManager sensorManager;
    private Sensor sensor;
    private SensorEventListener compassListener;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_compass, container, false);

        Context context = getContext();
        Utils utils = Utils.getInstance(context);
        Coordinate coordinate = utils.getCoordinate();
        if (coordinate == null) {
            utils.setActivityTitleAndSubtitle(getActivity(), getString(R.string.compass), "");
        } else {
            utils.setActivityTitleAndSubtitle(getActivity(), getString(R.string.qibla_compass),
                    utils.getCityName(true));
        }



        compassListener = new SensorEventListener() {
            /*
             * time smoothing constant for low-pass filter 0 ≤ alpha ≤ 1 ; a smaller
             * value basically means more smoothing See:
             * http://en.wikipedia.org/wiki/Low-pass_filter#Discrete-time_realization
             */
            static final float ALPHA = 0.15f;
            float azimuth;

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }

            @Override
            public void onSensorChanged(SensorEvent event) {
                // angle between the magnetic north direction
                // 0=North, 90=East, 180=South, 270=West
                azimuth = lowPass(event.values[0], azimuth);
                compassView.setBearing(azimuth);
                compassView.invalidate();
            }

            /**
             * https://en.wikipedia.org/wiki/Low-pass_filter#Algorithmic_implementation
             * http://developer.android.com/reference/android/hardware/SensorEvent.html#values
             */
            private float lowPass(float input, float output) {
                if (Math.abs(180 - input) > 170) {
                    return input;
                }
                return output + ALPHA * (input - output);
            }
        };
        compassView = (QiblaCompassView) view.findViewById(R.id.compass_view);

        CalendarTool calendarTool = new CalendarTool();

        switch (calendarTool.getIranianMonth()){


            case 1:
            case 2:
            case 3:
                compassView.setBackgroundResource(R.drawable.spring1);
                break;
            case 4:
            case 5:
            case 6:
                compassView.setBackgroundResource(R.drawable.summer1);
                break;
            case 7:
            case 8:
            case 9:
                compassView.setBackgroundResource(R.drawable.autumn1);
                break;
            case 10:
            case 11:
            case 12:
                compassView.setBackgroundResource(R.drawable.winter1);
                break;


        }


        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;
        compassView.setScreenResolution(width, height - 2 * height / 8);

        if (coordinate != null) {
            compassView.setLongitude(coordinate.getLongitude());
            compassView.setLatitude(coordinate.getLatitude());
            compassView.initCompassView();
            compassView.invalidate();
        }

        sensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        if (sensor != null) {
            sensorManager.registerListener(compassListener, sensor,
                    SensorManager.SENSOR_DELAY_FASTEST);
        } else {
            utils.quickToast(getString(R.string.compass_not_found));
        }
        return view;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (sensor != null) {
            sensorManager.unregisterListener(compassListener);
        }
    }
}
