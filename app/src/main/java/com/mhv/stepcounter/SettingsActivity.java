package com.mhv.stepcounter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.app.Dialog;
import android.content.DialogInterface.OnClickListener;
import android.widget.NumberPicker;


import java.util.Locale;


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
 * Description: This activity is for preferences settings
 */
public class SettingsActivity extends PreferenceActivity {
    final static int DEFAULT_GOAL = 500;
    final static float DEFAULT_STEP_SIZE = Locale.getDefault() == Locale.US ? 2.5f : 75f;
    final static String DEFAULT_STEP_UNIT = Locale.getDefault() == Locale.US ? "ft" : "cm";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new SettingsFragment()).commit();

    }

    public static class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences);

            final SharedPreferences prefs = getActivity().getSharedPreferences("stepcounter", Context.MODE_PRIVATE);

            //to set the goal
            Preference goal = findPreference("goal");
            goal.setOnPreferenceClickListener(this);
            goal.setSummary(getString(R.string.goal_summary, prefs.getInt("goal", DEFAULT_GOAL)));

            //to set step-size and step-size input
            Preference stepsize = findPreference("stepsize");
            stepsize.setOnPreferenceClickListener(this);
            stepsize.setSummary(getString(R.string.step_size_summary,
                    prefs.getFloat("stepsize_value", DEFAULT_STEP_SIZE),
                    prefs.getString("stepsize_unit", DEFAULT_STEP_UNIT)));

            setHasOptionsMenu(true);
        }

        @Override
        public void onResume() {
            super.onResume();
        }

        @Override
        public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
            inflater.inflate(R.menu.menu, menu);
        }

        @Override
        public void onPrepareOptionsMenu(final Menu menu) {
            super.onPrepareOptionsMenu(menu);
            menu.findItem(R.id.action_settings).setVisible(false);
        }

        @Override
        public boolean onPreferenceClick(final Preference preference) {
            AlertDialog.Builder builder;
            View v;
            final SharedPreferences prefs = getActivity().getSharedPreferences("stepcounter", Context.MODE_PRIVATE);
            switch (preference.getTitleRes()) {
                case R.string.goal:
                    builder = new AlertDialog.Builder(getActivity());
                    //use number picker for setting the goal
                    final NumberPicker np = new NumberPicker(getActivity());

                    np.setMinValue(1);
                    np.setMaxValue(100);

                    //increment in 50s
                    String[] displayedValues = new String[100];
                    for (int i = 1; i <= 100; i++)
                        displayedValues [i-1] = String.valueOf((i) * 50);
                    np.setDisplayedValues(displayedValues);

                    np.setValue(prefs.getInt("goal", DEFAULT_GOAL)/50);

                    np.setWrapSelectorWheel(false);
                    np.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

                    builder.setView(np);
                    builder.setTitle(R.string.set_goal);
                    builder.setPositiveButton(android.R.string.ok, new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            np.clearFocus();
                            prefs.edit().putInt("goal", np.getValue()*50).commit();
                            preference.setSummary(getString(R.string.goal_summary, np.getValue()*50));
                            dialog.dismiss();

                        }
                    });
                    builder.setNegativeButton(android.R.string.cancel, new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    Dialog dialog = builder.create();
                    dialog.show();
                    break;

                case R.string.step_size:
                    builder = new AlertDialog.Builder(getActivity());
                    v = getActivity().getLayoutInflater().inflate(R.layout.stepsize, null);
                    final RadioGroup unit = (RadioGroup) v.findViewById(R.id.unit);
                    final EditText value = (EditText) v.findViewById(R.id.value);
                    unit.check(
                            prefs.getString("stepsize_unit", DEFAULT_STEP_UNIT).equals("cm") ? R.id.cm :
                                    R.id.ft);
                    value.setText(String.valueOf(prefs.getFloat("stepsize_value", DEFAULT_STEP_SIZE)));
                    builder.setView(v);
                    builder.setTitle(R.string.set_step_size);
                    builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                prefs.edit().putFloat("stepsize_value",
                                        Float.valueOf(value.getText().toString()))
                                        .putString("stepsize_unit",
                                                unit.getCheckedRadioButtonId() == R.id.cm ? "cm" : "ft")
                                        .apply();
                                preference.setSummary(getString(R.string.step_size_summary,
                                        Float.valueOf(value.getText().toString()),
                                        unit.getCheckedRadioButtonId() == R.id.cm ? "cm" : "ft"));
                            } catch (NumberFormatException nfe) {
                                nfe.printStackTrace();
                            }
                            dialog.dismiss();
                        }
                    });
                    builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.create().show();
                    break;

            }
            return false;
        }
    }
}
