package de.goforittechnologies.go_for_it.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import de.goforittechnologies.go_for_it.R;
import de.goforittechnologies.go_for_it.storage.DataSourceStepData;

public class DashboardActivity extends AppCompatActivity
        implements AdapterView.OnItemSelectedListener,
        SeekBar.OnSeekBarChangeListener {

    private BarChart barChart;
    private List<double[]> inputList;
    private Toolbar tbDashboard;
    private SeekBar seekBar;
    String selectedPeriod;
    private static final String TAG = "DashboardActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);


        barChart=  findViewById(R.id.barChart);

        Spinner dropdown = findViewById(R.id.spinner1);
        dropdown.setOnItemSelectedListener(this);

        seekBar = findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(this);

        tbDashboard= findViewById(R.id.tbDashboard);
        setSupportActionBar(tbDashboard);
        getSupportActionBar().setTitle("Dashboard");
        //get the spinner from the xml.

        //create a list of items for the spinner.
        String[] items = new String[]{"Wochen", "Tage", "Stunden"};
        //create an adapter to describe how the items are displayed, adapters are used in several places in android.
        //There are multiple variations of this, but this is the basic variant.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        //set the spinners adapter to the previously created one.
        dropdown.setAdapter(adapter);


        DataSourceStepData dataSourceStepData = new DataSourceStepData(this,"StepDataTABLE_12",0);
        inputList = new ArrayList<>();
        dataSourceStepData.open();
        inputList = dataSourceStepData.getAllStepData();
        dataSourceStepData.close();
        Log.d(TAG, "onCreate: Size of InputList:" + inputList.size());

        //Creating Random testdata for displaying
        /*inputList = new ArrayList<>();
        double[] day = new double[24];
        Random r = new Random();

        for (int i = 0; i <30; i++){
            for (int j = 0; j <24; j++){
                day[j] =  r.nextInt(9000-100) + 100;
            }
            inputList.add(day);
            day = new double[24];
        }
        //List<BarEntry> entries = buildMonthBarChart(inputList,0,30);
        //List<BarEntry> entries = buildWeekBarChart(inputList,11);
        List<BarEntry> entries =  buildDayBarChart(inputList,12);
        invalitadeBarCHart(entries);*/

    }
    private List<BarEntry> buildMonthBarChart(List<double[]> inputList, int minDay, int maxDay){
        List<BarEntry> entries = new ArrayList<>();
        double value = 0;
        if (minDay < 0){
            minDay = 0;
        }
        if (maxDay >= inputList.size() ){
            maxDay = inputList.size();
        }
        int i = minDay;

        while(i < maxDay){
            for (int j = 0; j<24;j++){
                value += inputList.get(i)[j];
            }
            entries.add(new BarEntry((float)i,(float)value));
            Log.d(TAG, "buildMonthBarChart: Steps of Day:" + value);
            i++;
            value = 0;
        }
        return entries;
    }
    private List<BarEntry> buildDayBarChart(List<double[]> inputList, int day){
        List<BarEntry> entries = new ArrayList<>();
        double value = 0;
        for (int i = 1; i< 24;i++){
            try{
                entries.add(new BarEntry((float)i,(float)inputList.get(day)[i]));
            }
            catch (Exception ex){
                throw ex;
            }

        }
        return entries;
    }


    private List<BarEntry> buildWeekBarChart(List<double[]> inputList, int month){
        List<BarEntry> entries = new ArrayList<>();
        //TODO: Adding Day offset for correct weekdays from monday to sunday
        int weekdayFromMonthFirst = getWeekDayFromDate(1, month);
        int i = 0;
        int m = 1;
        double value = 0;
        while ( i < inputList.size()){

            for (int k = 0; k<7;k++)
            {
                if (i+k < inputList.size()){
                    for (int j = 0; j<24;j++){
                        value =+ inputList.get(i+k)[j];
                    }
                }
            }
            entries.add(new BarEntry((float) m,(float) value));
            m++;
            i= i +7;
        }



        return entries;
    }

    private void invalitadeBarCHart(List<BarEntry> inputList){
        BarDataSet set = new BarDataSet(inputList, "BarDataSet");
        BarData data = new BarData(set);
        data.setBarWidth(0.9f); // set custom bar width
        barChart.setData(data);
        barChart.setFitBars(true); // make the x-axis fit exactly all bars
        barChart.invalidate(); // refresh
    }

    private int getWeekDayFromDate(int day, int month){
        Date currentTime = Calendar.getInstance().getTime();
        int year = currentTime.getYear();
        Calendar.getInstance().set(year,month,day);
        int firstDayofWeek = Calendar.getInstance(Locale.GERMANY).getFirstDayOfWeek();
        int timeDiff = day - firstDayofWeek +1;
        return timeDiff;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        selectedPeriod = adapterView.getItemAtPosition(i).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        List<BarEntry> entries;
        //TODO: Adding switching to different Month by Name of Table
        switch (selectedPeriod) {
            case "Tage":
                seekBar.setMax(2);
                entries =  buildMonthBarChart(inputList,1,28);
                invalitadeBarCHart(entries);
                break;

            case "Wochen":
                seekBar.setMax(2);
                entries = buildWeekBarChart(inputList,12);
                invalitadeBarCHart(entries);
                break;

            case "Stunden":
                seekBar.setMax(28);
                entries = buildDayBarChart(inputList, i);
                invalitadeBarCHart(entries);
                break;

        }
    }
    
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
