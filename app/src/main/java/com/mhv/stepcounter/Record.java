package com.mhv.stepcounter;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
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
 * Description: This is the object class for a single step count details record.
 */
public class Record{

    private String Duration, Distance, Stepsize, Steps, Date;

    public final static String DELIMETER = ",";


    public Record(){}

    public Record(String Duration, String Distance, String Stepsize, String Steps, String Date){
        this.Date=Date;
        this.Distance=Distance;
        this.Stepsize=Stepsize;
        this.Duration=Duration;
        this.Steps=Steps;
    }

    public String getDate() {
        return Date;
    }

    public String getDistance() { return Distance; }

    public String getSteps() {
        return Steps;
    }

    public String getStepsize() {
        return Stepsize;
    }

    public String getDuration() {
        return Duration;
    }


    // read from the file and generate a list of records
    public static List<Record> readRecords(Context context) {

        List<Record> recordList = new ArrayList<Record>();  // a list of contacts to return
        try {

            InputStream inputStream = context.openFileInput("RecordDetails.txt");// input stream for the file


            if (inputStream != null) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String receivedLine;// line read from file

                while ((receivedLine = bufferedReader.readLine()) != null) {
                    String splitedLine[] = receivedLine.split(Record.DELIMETER);
                    recordList.add(new Record(splitedLine[0], splitedLine[1], splitedLine[2], splitedLine[3], splitedLine[4]));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return recordList;
    }

    //write the record to the file
    public boolean writeRecordToFile(Context context) {


        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("RecordDetails.txt", Context.MODE_APPEND));
            String toWrite = recordString() + "\n";
            outputStreamWriter.write(toWrite);
            outputStreamWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    public String recordString() {
        return  Duration + "," + Distance + ","  + Stepsize + "," + Steps + "," + Date   ;
    }


    @Override
    public String toString() {

        return Date + "\n"  + Stepsize + "\n" + Steps +  "\n" + Distance + "\n" + Duration;
    }


}
