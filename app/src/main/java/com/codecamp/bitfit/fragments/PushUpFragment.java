package com.codecamp.bitfit.fragments;


import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.codecamp.bitfit.MainActivity;
import com.codecamp.bitfit.R;
import com.codecamp.bitfit.database.PushUps;
import com.codecamp.bitfit.database.User;
import com.codecamp.bitfit.statistics.PushupStatisticsActivity;
import com.codecamp.bitfit.util.Constants;
import com.codecamp.bitfit.util.CountUpTimer;
import com.codecamp.bitfit.util.DBQueryHelper;
import com.codecamp.bitfit.util.InstructionsDialog;
import com.codecamp.bitfit.util.SharedPrefsHelper;
import com.codecamp.bitfit.util.Util;

import java.util.List;
import java.util.UUID;

/**
 * A simple {@link Fragment} subclass.
 */
public class PushUpFragment extends WorkoutFragment implements SensorEventListener {

    // current User
    User user = DBQueryHelper.findUser();

    // View stuff
    private View container;
    private TextView timeTextView;
    private FloatingActionButton finishButton;
    private TextView pushUpButton;
    private TextView caloriesTextView;
    private TextView avgPushupsTextView;

    // fragment stuff
    private boolean workoutStarted;
    private int count;
    private CountUpTimer countUpTimer;
    private PushUps pushUp;
    private long startTime;

    // sensor stuff
    private SensorManager mSensorManager;
    private Sensor mLight;
    private float maxLightRange;
    private double minLightRange;
    private double averageLightRange;
    private boolean lockPushUpsCount;
    private long elapsedTime;


    public static PushUpFragment getInstance() {
        PushUpFragment fragment = new PushUpFragment();

        return fragment;
    }

    public PushUpFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_push_up, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // light sensor initialization
        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        // find view stuff
        timeTextView = getView().findViewById(R.id.textview_cardview_time);
        finishButton = getView().findViewById(R.id.button_pushup_quit);
        pushUpButton = getView().findViewById(R.id.button_pushup);
        avgPushupsTextView = getView().findViewById(R.id.textview_cardview_avg);
        caloriesTextView = getView().findViewById(R.id.textview_cardview_calories);
        container = getView().findViewById(R.id.container_pushup_counter);

        countUpTimer = new CountUpTimer(1000, timeTextView) {
            @Override
            public void onTick(long elapsedTime) {
                setElapsedTime(elapsedTime);
                avgPushupsTextView.setText(String.valueOf(calcPushupsPerMinute(elapsedTime)));
                timeTextView.setText(Util.getMillisAsTimeString(elapsedTime));
            }
        };

        // set button to start state
        setToInitialState();

        pushUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // show quit button and cardview if we started push ups
                if (!workoutStarted) {
                    container.setVisibility(View.VISIBLE);
                    finishButton.setVisibility(View.VISIBLE);
                    workoutStarted = true;
                    countUpTimer.start();
                    startTime = System.currentTimeMillis();
                } else {
                    // increment count and set text
                    count++;
                    avgPushupsTextView.setText(String.valueOf(calcPushupsPerMinute(elapsedTime)));
                    caloriesTextView.setText(String.valueOf(calcCalories()));
                }
                // Screen keep on Flag set
                getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                pushUpButton.setText(String.valueOf(count));
            }
        });

        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                persistPushupObject();

                // Screen keep on Flag clear
                getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                List<PushUps> allPushUps = DBQueryHelper.findAllPushUps();
                PushUps highscorePushup = DBQueryHelper.findHighscorePushup();
                // set to initial state
                setToInitialState();
                countUpTimer.stop();
            }
        });
    }

    private void persistPushupObject() {
        long duration = System.currentTimeMillis() - startTime;

        // set pushup object
        pushUp.setUser(user);
        pushUp.setId(UUID.randomUUID());
        pushUp.setDurationInMillis(duration);
        pushUp.setPushPerMin(calcPushupsPerMinute(duration));
        pushUp.setRepeats(count);
        pushUp.setCalories(calcCalories());
        pushUp.setCurrentDate(Util.getCurrentDateAsString());

        // save workout to database
        pushUp.save();

        // set as last activity
        new SharedPrefsHelper(getContext())
                .setLastActivity(Constants.WORKOUT_PUSHUPS, pushUp.getId());

    }

    private double calcPushupsPerMinute(long duration) {
        return Util.roundTwoDecimals(((double) count * 60000.0) / (double) duration);
    }

    private double calcCalories() {
        // calculation from http://www.science-at-home.de/wiki/index.php/Kalorienverbrauch_bei_einzelnen_Sport%C3%BCbungen_pro_Wiederholung

        double weight = user.getWeight();

        // wayUp + wayDown = one push up
        double wayUp = ((weight*0.7 * 9.81 * 0.3) / 4.1868) / 1000;
        double wayDown = wayUp / 2.0;

        return Util.roundTwoDecimals((wayDown + wayUp) * (double) count);
    }

    private void setToInitialState() {
        pushUp = new PushUps();
        startTime = 0;
        workoutStarted = false;
        container.setVisibility(View.INVISIBLE);
        finishButton.setVisibility(View.INVISIBLE);
        pushUpButton.setText(R.string.start);
        timeTextView.setText(R.string.default_timer_value);
        caloriesTextView.setText(R.string.default_double_value);
        avgPushupsTextView.setText(R.string.default_double_value);
        count = 0;
        lockPushUpsCount = false;
        maxLightRange = 0;
        minLightRange = 0;
        averageLightRange = 0;
    }

    @Override
    public void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mLight, SensorManager.SENSOR_DELAY_NORMAL);

        // Set title bar
        ((MainActivity) getActivity())
                .setActionBarTitle(getString(R.string.pushups));
    }

    @Override
    public void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Search for light sensor, only start at workoutstart
        // TODO calibration
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            if (event.values != null && workoutStarted) {
                // calculation light range
                if (maxLightRange < event.values[0]) {
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
                    if(maxLightRange < 400) {
                        maxLightRange = (event.values[0]);
                        minLightRange = (double) event.values[0] / 2;
                        averageLightRange = (double) event.values[0] / 4;
                        averageLightRange = averageLightRange * 3;
                        //Toast.makeText(getActivity().getApplicationContext(), "unter", Toast.LENGTH_SHORT).show();

                    } else {
                        maxLightRange = (event.values[0]);
                        minLightRange = (double) event.values[0] / 3;
                        averageLightRange = (double) event.values[0] / 2;
                        //Toast.makeText(getActivity().getApplicationContext(), "drüber", Toast.LENGTH_SHORT).show();
                    }

                }

                // Count++ if is unlock
                if (minLightRange > event.values[0] && lockPushUpsCount == false) {
                    lockPushUpsCount = true;
                    count++;
                    pushUpButton.setText(String.valueOf(count));
                    avgPushupsTextView.setText(String.valueOf(calcPushupsPerMinute(elapsedTime)));
                    caloriesTextView.setText(String.valueOf(calcCalories()));
                }

                // Lock
                if (averageLightRange < event.values[0] && lockPushUpsCount == true) {
                    lockPushUpsCount = false;
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // empty method, not needed but necessary by implementation of interface
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_instructions:
                new InstructionsDialog(getContext(),
                        getString(R.string.pushups),
                        getActivity().getDrawable(R.drawable.push_up_instruction),
                        getString(R.string.pushup_instructions)).show();
                return true;
            case R.id.action_statistics:
                getActivity().startActivity(new Intent(getActivity(), PushupStatisticsActivity.class));
                return true;
            case R.id.action_share:
                // TODO start share intent
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void setElapsedTime(long elapsedTime) {
        this.elapsedTime = elapsedTime;
    }
}
