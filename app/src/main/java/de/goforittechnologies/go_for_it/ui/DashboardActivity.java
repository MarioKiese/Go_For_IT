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
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

import de.goforittechnologies.go_for_it.R;
import de.goforittechnologies.go_for_it.storage.DataSourceStepData;


/**
 * @author  Tom Hammerbacher.
 * @version 0.8.
 * @see AppCompatActivity
 *
 * This class creates the dashboard of the "Go_for_IT" android app.
 * Corresponding layout: res.layout.activity_dashboard.xml
 *
 * The user can choose between displaying the selected data in different time
 * periods (week, day, hour) by klicking on the dropdown spinner in the top.
 * The BarChart shows the choosen period. Zooming in X- and Y-direction is
 * possible by using two fingers.
 * The two seek-bars (sometimes only one is shown) can be used to change the
 * displayed month and day by swiping over it.
 */

public class DashboardActivity extends AppCompatActivity
        implements AdapterView.OnItemSelectedListener,
        SeekBar.OnSeekBarChangeListener {

    private BarChart barChart;
    private List<double[]> inputList;
    private SeekBar seekBarDay;
    private TextView tvSeekbarDayCategory;
    private TextView tvSeekbarDayValue;
    private SeekBar seekBarMonth;
    private TextView tvSeekbarMonthCategory;
    private TextView tvSeekbarMonthValue;
    private int monthForHourUse;
    private Toast noTableToast = null;

    private String selectedPeriod;
    private static final String TAG = "DashboardActivity";
    private DataSourceStepData dataSourceStepData;

    /**
     * method to declare and initialise activity functions and variables.
     * - connecting Views via R.id.
     * -connect to database
     *
     * @see AdapterView
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        dataSourceStepData = null;
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());

        barChart=  findViewById(R.id.barChart);

        Spinner dropdownPeriod = findViewById(R.id.spinnerPeriod);
        dropdownPeriod.setOnItemSelectedListener(this);

        seekBarDay = findViewById(R.id.seekBarDay);
        seekBarDay.setOnSeekBarChangeListener(this);

        seekBarMonth = findViewById(R.id.seekBarMonth);
        seekBarMonth.setOnSeekBarChangeListener(this);

        tvSeekbarDayCategory = findViewById(R.id.tvSeekbarDayCategory);
        tvSeekbarDayValue = findViewById(R.id.tvSeekbarDayValue);

        tvSeekbarMonthCategory = findViewById(R.id.tvSeekbarMonthCategory);
        tvSeekbarMonthValue = findViewById(R.id.tvSeekbarMonthValue);

        Toolbar tbDashboard = findViewById(R.id.tbDashboard);
        setSupportActionBar(tbDashboard);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Dashboard");
        //get the spinner from the xml.

        //create a list of items for the spinner.
        String[] items = new String[]{"Week", "Day", "Hour"};
        //create an adapter to describe how the items are displayed,
        // adapters are used in several places in android.
        //There are multiple variations of this, but this is the basic variant.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, items);
        //set the spinners adapter to the previously created one.
        dropdownPeriod.setAdapter(adapter);

        inputList = new ArrayList<>();

        try {
            dataSourceStepData.open();
            inputList = dataSourceStepData.getAllStepData();
            dataSourceStepData.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "onCreate: Size of InputList:" + inputList.size());

        monthForHourUse = 0;
    }

    /**
     *
     * method to build up bar chart for selected month
     *
     * @param month month that should be shown
     * @return list of entry bars for bar chart
     *
     * @see BarEntry
     */
    private List<BarEntry> buildMonthBarChart(int month){
        List<BarEntry> entries = new ArrayList<>();
        inputList = selectData(month);
        int i = 0;
        double value = 0;

        Log.d(TAG, "onCreate: Size of InputList:" + inputList.size());

        while (i < inputList.size()){
            for (int j = 0; j < 24; j++){
                try {
                    value += inputList.get(i)[j];
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            entries.add(new BarEntry(i +1,(int)value));
            value = 0;
            i++;
        }

        return entries;
    }

    /**
     *
     * method to select data out of selected month (creates toast if no
     * database table of selected month is available).
     *
     * @param month month that should be shown
     * @return list for every day out of 24 value arrays for every hour
     *
     * @see DataSourceStepData
     */
    private List<double[]> selectData(int month){

        List<double[]> list = new ArrayList<>();
        try {
            dataSourceStepData = new DataSourceStepData(this,
                    "StepDataTABLE_"
                    + (month),0);
            dataSourceStepData.open();
            list = dataSourceStepData.getAllStepData();
            dataSourceStepData.close();
        } catch (Exception e) {
            e.printStackTrace();
            if (noTableToast!= null){
                noTableToast.cancel();
            }
            noTableToast = Toast.makeText(this,
                    "No existing table for selected month",
                    Toast.LENGTH_SHORT);
            noTableToast.show();
        }
        return list;
    }

    /**
     * method to build up bar chart for every hour on selected day in
     * selected month.
     *
     * @param day selected day to be shown.
     * @param month selected month to be shown.
     * @return list of entry bars for bar chart
     *
     * @see BarEntry
     */
    private List<BarEntry> buildDayBarChart(int day, int month){

        List<BarEntry> entries = new ArrayList<>();

        inputList = selectData(month);

        for (int i = 0; i< 24;i++){
            try{
                entries.add(new BarEntry((i),
                        (int)inputList.get(day)[i]));
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        return entries;
    }

    /**
     * method to build up bar chart for every week in selected month.
     * info: week  one is count from 1. to 7. of month, second week from 8.
     * to 14. etc..
     * @param month selected month to be shown.
     *
     * @return list of entry bars for bar chart.
     *
     * @see BarEntry
     */
    private List<BarEntry> buildWeekBarChart(int month){
        List<BarEntry> entries = new ArrayList<>();
        double value = 0;
        inputList = selectData(month);
        int i = 0;

        while (i < inputList.size() ){

            for (int j = 0; j < 7; j++){
                if ( i+j < inputList.size()){
                    for (int k = 0; k <24; k++){
                        value += inputList.get(i+j)[k];
                    }
                }
            }
            entries.add(new BarEntry(((i /7)+1),(int)value));
            value = 0;
            i = i+7;
        }

        return entries;
    }

    /**
     * invalidate bar chart for displaying.
     *
     * @param inputList selected entry bar list to be shown.
     *
     * @see BarDataSet
     * @see BarChart
     * @see BarData
     */
    private void invalitadeBarChart(List<BarEntry> inputList){
        BarDataSet set = new BarDataSet(inputList, "Steps");
        BarData data = new BarData(set);
        data.setBarWidth(0.9f); // set custom bar width
        barChart.setData((data));
        barChart.getDescription().setEnabled(false);
        barChart.setFitBars(true); // make the x-axis fit exactly all bars
        barChart.invalidate(); // refresh
    }

    /**
     * method to select procedure based on selected period to show and hide
     * viewed elements.
     *
     * @param adapterView adapter view for selecting view in activity.
     * @param view selected view.
     * @param i value of selected seek bar entry
     * @param l not used
     *
     * @see AdapterView
     * @see View
     */
    @Override
    public void onItemSelected(AdapterView<?> adapterView,
                               View view, int i, long l) {

        selectedPeriod = adapterView.getItemAtPosition(i).toString();
        switch (selectedPeriod){
            case "Day":
                seekBarDay.setMax(11);
                tvSeekbarDayCategory.setText("Month:");
                seekBarMonth.setAlpha(0);
                tvSeekbarMonthCategory.setAlpha(0);
                tvSeekbarMonthValue.setAlpha(0);
                break;
            case "Week":
                seekBarDay.setMax(11);
                tvSeekbarDayCategory.setText("Month:");
                seekBarMonth.setAlpha(0);
                tvSeekbarMonthCategory.setAlpha(0);
                tvSeekbarMonthValue.setAlpha(0);
                break;
            case "Hour":
                seekBarDay.setMax(30);
                seekBarMonth.setMax(11);
                tvSeekbarDayCategory.setText("Day:");
                tvSeekbarMonthCategory.setText("Month:");
                seekBarMonth.setAlpha(1);
                tvSeekbarMonthCategory.setAlpha(1);
                tvSeekbarMonthValue.setAlpha(1);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    /**
     * method to calculate the car chard values based on selected month (day)
     * on seek bar.
     *
     * @param seekBar used seek bar from this activity
     * @param i current value of seek bar
     * @param b not used
     */
    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        List<BarEntry> entries;


        switch (seekBar.getId()){
            case R.id.seekBarDay:
                tvSeekbarDayValue.setText(Integer.toString(i+ 1));
                break;
            case R.id.seekBarMonth:
                tvSeekbarMonthValue.setText(Integer.toString(i + 1));
                monthForHourUse = (i + 1);
                break;

        }
        switch (selectedPeriod) {
            case "Day":
                entries =  buildMonthBarChart(i+1);
                invalitadeBarChart(entries);
                break;
            case "Week":
                entries = buildWeekBarChart(i+1);
                invalitadeBarChart(entries);
                break;
            case "Hour":
                entries = buildDayBarChart(i, monthForHourUse);
                invalitadeBarChart(entries);
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
