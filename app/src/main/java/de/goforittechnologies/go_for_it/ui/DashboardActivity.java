package de.goforittechnologies.go_for_it.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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

public class DashboardActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private BarChart barChart;
    private List<double[]> inputList;
    private Toolbar tbDashboard;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        barChart=  findViewById(R.id.barChart);
        Spinner dropdown = findViewById(R.id.spinner1);
        dropdown.setOnItemSelectedListener(this);

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

        //Creating Random testdata for displaying
        inputList = new ArrayList<>();
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
        invalitadeBarCHart(entries);

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
                value =+ inputList.get(i)[j];
                entries.add(new BarEntry((float)i,(float)value));
            }
            i++;
        }
        return entries;
    }
    private List<BarEntry> buildDayBarChart(List<double[]> inputList, int day){
        List<BarEntry> entries = new ArrayList<>();
        double value = 0;
        for (int i = 0; i< 24;i++){
            entries.add(new BarEntry((float)i,(float)inputList.get(day)[i]));
        }
        return entries;
    }
    private List<BarEntry> buildWeekBarChart(List<double[]> inputList, int month){
        List<BarEntry> entries = new ArrayList<>();
        //TODO: Adding Dayoffset for correct weekdays from monday to sunday
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
       String slectedPeriod = adapterView.getItemAtPosition(i).toString();
        List<BarEntry> entries;
       switch (slectedPeriod) {
           case "Tage":
               entries =  buildMonthBarChart(inputList,1,30);
               invalitadeBarCHart(entries);
               break;

           case "Wochen":
               entries = buildWeekBarChart(inputList,11);
               invalitadeBarCHart(entries);
               break;
           case "Stunden":
                entries = buildDayBarChart(inputList,12);
               invalitadeBarCHart(entries);
               break;


        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
