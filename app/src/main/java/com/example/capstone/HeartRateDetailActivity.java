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

import java.util.ArrayList;
import java.util.List;

public class HeartRateDetailActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_rate_detail);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Heart Rate Details");
        createHeartRateGraph();
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
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String selectedTime = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void createHeartRateGraph() {
        BarChart heartBarChart = findViewById(R.id.heartChart);

        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, new float[]{80f, 10f})); // Heart rate range for time 1 (start at 80, height 3)
        entries.add(new BarEntry(1, new float[]{82f, 20f})); // Heart rate range for time 2 (start at 82, height 3)
        entries.add(new BarEntry(2, new float[]{85f, 12f})); // Heart rate range for time 3 (start at 85, height 3)
        entries.add(new BarEntry(3, new float[]{87f, 8f})); // Heart rate range for time 4 (start at 87, height 3)
        entries.add(new BarEntry(4, new float[]{80f, 10f})); // Heart rate range for time 1 (start at 80, height 3)
        entries.add(new BarEntry(5, new float[]{82f, 20f})); // Heart rate range for time 2 (start at 82, height 3)
        entries.add(new BarEntry(6, new float[]{85f, 12f})); // Heart rate range for time 3 (start at 85, height 3)

        BarDataSet dataSet = new BarDataSet(entries, "Heart Rate (BPM)");
        dataSet.setColors(new int[]{Color.TRANSPARENT, Color.RED}); // Color for the bars
        dataSet.setValueTextSize(12f);


        String[] labels = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"}; // Example time labels
        heartBarChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        heartBarChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        heartBarChart.getXAxis().setGranularity(1f);
        heartBarChart.getXAxis().setTextSize(18f);
        heartBarChart.setExtraBottomOffset(20f);

        YAxis leftAxis = heartBarChart.getAxisLeft();
        leftAxis.setAxisMinimum(40f); // Minimum heart rate
        leftAxis.setAxisMaximum(180f); // Maximum heart rate
        leftAxis.setGranularity(40f);
        leftAxis.setTextSize(18f);
        heartBarChart.getAxisRight().setEnabled(false); // Disable right y-axis

        heartBarChart.getDescription().setEnabled(false);
        heartBarChart.getLegend().setEnabled(false);
        heartBarChart.setTouchEnabled(false);
        BarData barData = new BarData(dataSet);

        heartBarChart.setData(barData);
        heartBarChart.invalidate();
    }

}
