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
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PulseOxDetailActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private BarChart oxBarChart;
    public static String selectedTime = "Last 7 Days";

    private LineChart oxLineChart;
    private List<Entry> lineEntries;

    private final String[] weekLabels = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
    private final String[] monthLabels = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pulse_ox_detail);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Pulse Oximeter Details");
        lineEntries = new ArrayList<>();
        setupGraphs();
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
        selectedTime = parent.getItemAtPosition(position).toString();
        changeTime();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
    private void setupGraphs() {
        oxBarChart = findViewById(R.id.oxBarChart);
        oxLineChart = findViewById(R.id.oxLineChart);

        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, new float[]{80f, 10f})); // Heart rate range for time 1 (start at 80, height 3)
        entries.add(new BarEntry(1, new float[]{82f, 18f})); // Heart rate range for time 2 (start at 82, height 3)
        entries.add(new BarEntry(2, new float[]{85f, 12f})); // Heart rate range for time 3 (start at 85, height 3)
        entries.add(new BarEntry(3, new float[]{87f, 8f})); // Heart rate range for time 4 (start at 87, height 3)
        entries.add(new BarEntry(4, new float[]{80f, 10f})); // Heart rate range for time 1 (start at 80, height 3)
        entries.add(new BarEntry(5, new float[]{82f, 18f})); // Heart rate range for time 2 (start at 82, height 3)
        entries.add(new BarEntry(6, new float[]{85f, 12f})); // Heart rate range for time 3 (start at 85, height 3)

        BarDataSet dataSet = new BarDataSet(entries, "Heart Rate (BPM)");
        dataSet.setColors(new int[]{Color.TRANSPARENT, Color.BLUE}); // Color for the bars
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                // Add space or any other formatting you desire
                return String.format("%.0f%%", value);
            }
        });
        dataSet.setValueTextSize(0f);

        oxBarChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(weekLabels));
        oxBarChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        oxBarChart.getXAxis().setGranularity(1f);
        oxBarChart.getXAxis().setTextSize(18f);
        oxBarChart.setExtraBottomOffset(20f);

        YAxis leftAxis = oxBarChart.getAxisLeft();
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.0f%%", value);
            }
        });
        leftAxis.setAxisMinimum(75f); // Minimum heart rate
        leftAxis.setAxisMaximum(105f); // Maximum heart rate
        leftAxis.setGranularity(10f);
        leftAxis.setTextSize(18f);
        oxBarChart.getAxisRight().setEnabled(false); // Disable right y-axis

        oxBarChart.getDescription().setEnabled(false);
        oxBarChart.getLegend().setEnabled(false);
        oxBarChart.setTouchEnabled(false);
        BarData barData = new BarData(dataSet);

        oxBarChart.setData(barData);
        oxBarChart.invalidate();

        Random rand = new Random();

        for (int i = 0; i  < 30; i++) {
            lineEntries.add(new Entry(i+1, rand.nextInt(50)+50));
        }

        oxLineChart.setTouchEnabled(true);
        LineDataSet lineDataSet = new LineDataSet(lineEntries, "Heart Rate");
        lineDataSet.setColor(Color.BLUE);
        lineDataSet.setValueTextColor(Color.BLACK);
        lineDataSet.setLineWidth(5f);
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        lineDataSet.setDrawValues(false);


        LineData lineData = new LineData(lineDataSet);

        // Customize chart
        oxLineChart.setData(lineData);

        Description description = new Description();
        description.setText("Monthly Heart Rate");
        oxLineChart.setDescription(description);
        oxLineChart.getXAxis().setTextSize(18f);
        oxLineChart.setExtraBottomOffset(20f);

        Legend legend = oxLineChart.getLegend();
        legend.setEnabled(false);

        XAxis xAxis = oxLineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis lineLeftAxis = oxLineChart.getAxisLeft();
        lineLeftAxis.setAxisMinimum(40f); // Minimum heart rate
        lineLeftAxis.setAxisMaximum(120f);
        lineLeftAxis.setTextSize(18f);
        lineLeftAxis.setGranularity(20f);

        YAxis rightAxis = oxLineChart.getAxisRight();
        rightAxis.setEnabled(false);
        oxLineChart.setAutoScaleMinMaxEnabled(true);

        oxLineChart.invalidate(); // Refresh chart


    }

    private void changeTime() {

        if (selectedTime.equals("Last 7 Days")) {
            oxLineChart.setVisibility(View.GONE);
            oxBarChart.setVisibility(View.VISIBLE);
        } else if (selectedTime.equals("Last Month")) {
            oxBarChart.setVisibility(View.GONE);
            oxLineChart.setVisibility(View.VISIBLE);
            lineEntries.clear();

            Description description = new Description();
            description.setText("Monthly Heart Rate");
            oxLineChart.setDescription(description);
            oxLineChart.getXAxis().setTextSize(18f);
            Random rand = new Random();
            oxLineChart.getXAxis().setValueFormatter(null);

            for (int i = 0; i  < 30; i++) {
                lineEntries.add(new Entry(i+1, rand.nextInt(50)+50));
            }
            LineDataSet dataSet = new LineDataSet(lineEntries, "Heart Rate");
            dataSet.setColor(Color.BLUE);
            dataSet.setValueTextColor(Color.BLACK);
            dataSet.setLineWidth(5f);
            dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            dataSet.setDrawValues(false);


            LineData lineData = new LineData(dataSet);
            oxLineChart.setData(lineData);
            oxLineChart.invalidate();

        } else if (selectedTime.equals("Last Year")) {
            oxBarChart.setVisibility(View.GONE);
            oxLineChart.setVisibility(View.VISIBLE);
            lineEntries.clear();
            Random rand = new Random();

            for (int i = 0; i  < 12; i++) {
                lineEntries.add(new Entry(i, rand.nextInt(50)+50));
            }

            LineDataSet dataSet = new LineDataSet(lineEntries, "Heart Rate");
            dataSet.setColor(Color.BLUE);
            dataSet.setValueTextColor(Color.BLACK);
            dataSet.setLineWidth(5f);
            dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            dataSet.setDrawValues(false);

            LineData lineData = new LineData(dataSet);

            // Customize chart
            oxLineChart.setData(lineData);

            Description description = new Description();
            description.setText("Yearly Heart Rate");
            oxLineChart.setDescription(description);
            oxLineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(monthLabels));
            oxLineChart.getXAxis().setTextSize(18f); // Adjust label text size
            oxLineChart.invalidate();

        } else if (selectedTime.equals("Last 2 Years")) {
            oxBarChart.setVisibility(View.GONE);
            oxLineChart.setVisibility(View.VISIBLE);
            lineEntries.clear();
            Random rand = new Random();

            for (int i = 0; i  < 8; i++) {
                lineEntries.add(new Entry(i, rand.nextInt(50)+50));
            }

            LineDataSet dataSet = new LineDataSet(lineEntries, "Heart Rate");
            dataSet.setColor(Color.BLUE);
            dataSet.setValueTextColor(Color.BLACK);
            dataSet.setLineWidth(5f);
            dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            dataSet.setDrawValues(false);

            LineData lineData = new LineData(dataSet);

            // Customize chart
            oxLineChart.setData(lineData);

            Description description = new Description();
            description.setText("2 Years Heart Rate");
            oxLineChart.setDescription(description);
            oxLineChart.getXAxis().setValueFormatter(null);
            oxLineChart.getXAxis().setTextSize(18f); // Adjust label text size
            oxLineChart.invalidate();


        }

    }

}
