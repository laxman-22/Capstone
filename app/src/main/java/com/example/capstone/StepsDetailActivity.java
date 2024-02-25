package com.example.capstone;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.List;

public class StepsDetailActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener  {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_steps_detail);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Steps Details");
        createStepsChart();
        Spinner spinner = findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.times, android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent i = new Intent(this, HomePage.class);
        startActivity(i);
        return super.onOptionsItemSelected(item);
    }

    private void createStepsChart() {
        BarChart stepsBarChart = findViewById(R.id.stepsChart);

        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, new float[]{10000f})); // Heart rate range for time 1 (start at 80, height 3)
        entries.add(new BarEntry(1, new float[]{5000f})); // Heart rate range for time 2 (start at 82, height 3)
        entries.add(new BarEntry(2, new float[]{8000f})); // Heart rate range for time 3 (start at 85, height 3)
        entries.add(new BarEntry(3, new float[]{8800f})); // Heart rate range for time 4 (start at 87, height 3)
        entries.add(new BarEntry(4, new float[]{4000f})); // Heart rate range for time 1 (start at 80, height 3)
        entries.add(new BarEntry(5, new float[]{2500f})); // Heart rate range for time 2 (start at 82, height 3)
        entries.add(new BarEntry(6, new float[]{1250f})); // Heart rate range for time 3 (start at 85, height 3)

        BarDataSet dataSet = new BarDataSet(entries, "Heart Rate (BPM)");
        dataSet.setColors(new int[]{Color.BLACK}); // Color for the bars
        dataSet.setValueTextSize(12f);

        String[] labels = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"}; // Example time labels
        stepsBarChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        stepsBarChart.getAxisLeft().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.0fk", value / 1000.0);
            }
        });


        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                // Add space or any other formatting you desire
                return String.format("%.0fk", value / 1000.0);
            }
        });
        stepsBarChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        stepsBarChart.getXAxis().setGranularity(1f);
        stepsBarChart.getXAxis().setTextSize(18f);
        stepsBarChart.setExtraBottomOffset(20f);

        YAxis leftAxis = stepsBarChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f); // Minimum heart rate
        YAxis yAxis = stepsBarChart.getAxisLeft(); // or getAxisRight() if you want to adjust the right Y-axis
        //dynamically change the range
        yAxis.setAxisMaximum(10000 * 1.1f);

        leftAxis.setGranularity(2500f);
        leftAxis.setTextSize(18f);
        stepsBarChart.getAxisRight().setEnabled(false); // Disable right y-axis

        stepsBarChart.getDescription().setEnabled(false);
        stepsBarChart.getLegend().setEnabled(false);
        stepsBarChart.setTouchEnabled(false);
        BarData barData = new BarData(dataSet);

        stepsBarChart.setData(barData);
        stepsBarChart.invalidate();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String selectedTime = parent.getItemAtPosition(position).toString();

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}