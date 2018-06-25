package com.mhv.stepcounter;

import android.os.Bundle;
import android.app.Activity;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.Collections;
import java.util.List;

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
 * File Author: Prachi Chauhan
 * Description: This activity is for displaying the previous records
 */

public class ListDisplay extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_main);
        // read from the file using the class 'Record'
        List<Record> recordList = Record.readRecords(ListDisplay.this);
        Collections.reverse(recordList);

        //display the list of previous records into the list view
        ArrayAdapter adapter = new ArrayAdapter<Record>(this,
                R.layout.activity_listview, recordList);

        ListView listView = (ListView) findViewById(R.id.mobile_list);
        listView.setAdapter(adapter);
    }
}