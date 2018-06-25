package com.mhv.stepcounter;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Project Authors : Gayatri Prabhu , Prachi Chauhan
 * NetIDs : gdp160130 , pxc163630
 * Project Submission Date: April 28, 2018
 * Purpose: Final Project ( Step Counter )
 *
 * Purpose of the Project:
 *  Step Counter detects the number of the steps covered by a person over a period of time. It also allows to set
 *  dynamically the total number of steps as Goal state. Additionally, it allows the user to change step size working in
 *  various units like centimeter and feet. Accordingly, the distance covered is also changed for various units of step
 *  size selected. Step Counter also displays your past activities as logs with the latest activity ordered first.
 *
 *
 **/


/**
 * File Author: Gayatri Prabhu
 * Description: This fragment supports the mainActivity. It covers all the functionalities that are on the main screen.
 */
public class MainActivityFragment extends Fragment implements SensorEventListener {

    //Sensor related variables
    private SensorManager sensorManager;
    private Sensor stepDetectorSensor;

    //Variables used in calculations
    private int stepCount = 0;
    private long startTime = 0;
    long timeInMilliseconds = 0;
    long elapsedTime = 0;
    long updatedTime = 0;
    private double distance = 0;

    //Activity Views
    private TextView dayRecordText;
    private TextView stepText;
    private TextView timeText;
    private TextView distanceText;
    private TextView achievedText;

    private boolean active = false; //Used to checked if the counter is running
    private Handler handler = new Handler(); //Used to update the time in the UI

    private int dayStepRecord;
    private double stepSizeOg;
    private double stepSize;
    private String unitOg;
    private String unit;
    String timeString;

    Date currentTime;
    SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    String currentTimeString ="";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        if (stepDetectorSensor == null)
            showErrorDialog();

    }

    //Shown when necessary censors are not available
    private void showErrorDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setMessage("Necessary step sensors not available!");

        alertDialogBuilder.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getActivity().finish();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_main, container, false);

        //Initialize views
        dayRecordText = (TextView) view.findViewById(R.id.dayRecordText);
        stepText = (TextView) view.findViewById(R.id.stepText);
        timeText = (TextView) view.findViewById(R.id.timeText);
        distanceText = (TextView) view.findViewById(R.id.distanceText);
        achievedText = (TextView) view.findViewById(R.id.achievedText);

        setViewDefaultValues();

        //Step counting and other calculations start when user presses "start" button
        final Button startButton = (Button) view.findViewById(R.id.startButton);

        if (startButton != null) {
            startButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (!active) {
                        startButton.setText(R.string.stop);
                        startButton.setTextColor(ContextCompat.getColor(getActivity(),R.color.red));
                        startButton.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.darkGray));

                        stepCount = 0;
                        distance = 0;
                        elapsedTime = 0;
                        setViewDefaultValues();

                        sensorManager.registerListener(MainActivityFragment.this, stepDetectorSensor, SensorManager.SENSOR_DELAY_NORMAL);

                        startTime = SystemClock.uptimeMillis();
                        handler.postDelayed(timerRunnable, 0);

                        currentTime = Calendar.getInstance().getTime();
                        currentTimeString = df.format(currentTime);

                        active = true;
                    }
                    else {
                        startButton.setText(R.string.start);
                        startButton.setTextColor(ContextCompat.getColor(getActivity(),R.color.green));
                        startButton.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.roundedbutton));

                        sensorManager.unregisterListener(MainActivityFragment.this, stepDetectorSensor);

                        elapsedTime += timeInMilliseconds;
                        handler.removeCallbacks(timerRunnable);

                        active = false;

                        writeContactFile(getActivity());
                    }

                    getActivity().invalidateOptionsMenu();
                }
            });
        }

        return view;
    }


    //Set all views to their initial value
    private void setViewDefaultValues() {

        SharedPreferences prefs = getActivity().getSharedPreferences("stepcounter", Context.MODE_PRIVATE);

        dayStepRecord = prefs.getInt("goal", SettingsActivity.DEFAULT_GOAL);
        stepSizeOg = prefs.getFloat("stepsize_value", SettingsActivity.DEFAULT_STEP_SIZE);

        unit = prefs.getString("stepsize_unit", SettingsActivity.DEFAULT_STEP_UNIT);
        unitOg=unit;
        if (unit.equals("cm")) {
            unit = " meters ";
            stepSize=stepSizeOg/100;
        } else {
            unit = " miles ";
            stepSize=stepSizeOg/5280;
        }

        dayRecordText.setText(String.format(getResources().getString(R.string.record), dayStepRecord));
        stepText.setText(String.format(getResources().getString(R.string.steps), 0));
        timeText.setText(String.format(getResources().getString(R.string.time), "0:00:00"));
        distanceText.setText(String.format(getResources().getString(R.string.distance), "0" + unit));
        achievedText.setVisibility(View.INVISIBLE);
    }


    @Override
    public void onResume() {
        super.onResume();
        if(!active)
            setViewDefaultValues();
    }


    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this, stepDetectorSensor);
    }


    //sensor detects a step
    @Override
    public void onSensorChanged(SensorEvent event) {


            if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR)
            {
                countSteps(event.values[0]);
            }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //Not in use
    }


    //Calculates the number of steps and the other calculations related to them
    private void countSteps(float step) {

        //Step count
        stepCount += (int) step;
        stepText.setText(String.format(getResources().getString(R.string.steps), stepCount));

        //Distance calculation
        distance = stepCount * stepSize;
        String distanceString = String.format("%.2f", distance)+unit;
        distanceText.setText(String.format(getResources().getString(R.string.distance), distanceString));

        //Record achievement
        if (stepCount >= dayStepRecord)
            achievedText.setVisibility(View.VISIBLE);

    }

    // timer calculations
    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {

            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;

            updatedTime = elapsedTime + timeInMilliseconds;

            int seconds = (int) (updatedTime / 1000);
            int minutes = seconds / 60;
            int hours = minutes / 60;
            seconds = seconds % 60;
            minutes = minutes % 60;

            timeString = String.format("%d:%s:%s", hours, String.format("%02d", minutes), String.format("%02d", seconds));

            if (isAdded()) {
                timeText.setText(String.format(getResources().getString(R.string.time), timeString));
            }

            handler.postDelayed(this, 0);

        }
    };

    //create options menu
    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.action_logs:
                Intent intent1 = new Intent(getActivity(), ListDisplay.class);
                startActivity(intent1);
                break;

        }
        return true;
    }

    //if start button is clicked and step counting is on-going, then settings option id disabled
    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        if(active) {

            MenuItem mi = menu.findItem(R.id.action_settings);
            mi.setEnabled(false);

        }
        else {

            MenuItem mi = menu.findItem(R.id.action_settings);
            mi.setEnabled(true);

        }


    }

    // when stop button is pressed we write that entry(step counting details) to the file
    private void writeContactFile (Context context)
    {
        Record record = new Record(" Duration: "+timeString, " Distance: "+distance+" "+unit, " Step-Size: "+stepSizeOg+" "+unitOg, " Step-Count: "+stepCount, " Date/Time: "+currentTimeString);
        try {


                record.writeRecordToFile(context);


        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

}





