package de.goforittechnologies.go_for_it.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;
import java.util.List;

import de.goforittechnologies.go_for_it.R;

public class DashboardActivity extends AppCompatActivity {

    private BarChart barChart;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        barChart= (BarChart) findViewById(R.id.barChart);

        



    }
    private List<BarEntry> buildDayAddedBarChart(List<double[]> inputList, int minDay, int maxDay){
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

}
