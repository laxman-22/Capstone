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

public class StepsDetailActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener  {

    private BarChart stepsBarChart;
    public static String selectedTime = "Last 7 Days";

    private LineChart stepsLineChart;
    private List<Entry> lineEntries;

    private final String[] weekLabels = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
    private final String[] monthLabels = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_steps_detail);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Steps Details");
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
        stepsBarChart = findViewById(R.id.stepsBarChart);
        stepsLineChart = findViewById(R.id.stepsLineChart);

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

        Random rand = new Random();

        for (int i = 0; i  < 30; i++) {
            lineEntries.add(new Entry(i+1, rand.nextInt(50)+50));
        }

        stepsLineChart.setTouchEnabled(true);
        LineDataSet lineDataSet = new LineDataSet(lineEntries, "Heart Rate");
        lineDataSet.setColor(Color.BLUE);
        lineDataSet.setValueTextColor(Color.BLACK);
        lineDataSet.setLineWidth(5f);
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        lineDataSet.setDrawValues(false);


        LineData lineData = new LineData(lineDataSet);

        // Customize chart
        stepsLineChart.setData(lineData);

        Description description = new Description();
        description.setText("Monthly Heart Rate");
        stepsLineChart.setDescription(description);
        stepsLineChart.getXAxis().setTextSize(18f);
        stepsLineChart.setExtraBottomOffset(20f);

        Legend legend = stepsLineChart.getLegend();
        legend.setEnabled(false);

        XAxis xAxis = stepsLineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis lineLeftAxis = stepsLineChart.getAxisLeft();
        lineLeftAxis.setAxisMinimum(40f); // Minimum heart rate
        lineLeftAxis.setAxisMaximum(120f);
        lineLeftAxis.setTextSize(18f);
        lineLeftAxis.setGranularity(20f);

        YAxis rightAxis = stepsLineChart.getAxisRight();
        rightAxis.setEnabled(false);
        stepsLineChart.setAutoScaleMinMaxEnabled(true);

        stepsLineChart.invalidate(); // Refresh chart



    }

    private void changeTime() {
        if (selectedTime.equals("Last 7 Days")) {
            stepsLineChart.setVisibility(View.GONE);
            stepsBarChart.setVisibility(View.VISIBLE);
        } else if (selectedTime.equals("Last Month")) {
            stepsBarChart.setVisibility(View.GONE);
            stepsLineChart.setVisibility(View.VISIBLE);
            lineEntries.clear();

            Description description = new Description();
            description.setText("Monthly Heart Rate");
            stepsLineChart.setDescription(description);
            stepsLineChart.getXAxis().setTextSize(18f);
            Random rand = new Random();
            stepsLineChart.getXAxis().setValueFormatter(null);

            for (int i = 0; i  < 30; i++) {
                lineEntries.add(new Entry(i+1, rand.nextInt(50)+50));
            }
            LineDataSet dataSet = new LineDataSet(lineEntries, "Heart Rate");
            dataSet.setColor(Color.BLACK);
            dataSet.setValueTextColor(Color.BLACK);
            dataSet.setLineWidth(5f);
            dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            dataSet.setDrawValues(false);


            LineData lineData = new LineData(dataSet);
            stepsLineChart.setData(lineData);
            stepsLineChart.invalidate();

        } else if (selectedTime.equals("Last Year")) {
            stepsBarChart.setVisibility(View.GONE);
            stepsLineChart.setVisibility(View.VISIBLE);
            lineEntries.clear();
            Random rand = new Random();

            for (int i = 0; i  < 12; i++) {
                lineEntries.add(new Entry(i, rand.nextInt(50)+50));
            }

            LineDataSet dataSet = new LineDataSet(lineEntries, "Heart Rate");
            dataSet.setColor(Color.BLACK);
            dataSet.setValueTextColor(Color.BLACK);
            dataSet.setLineWidth(5f);
            dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            dataSet.setDrawValues(false);

            LineData lineData = new LineData(dataSet);

            // Customize chart
            stepsLineChart.setData(lineData);

            Description description = new Description();
            description.setText("Yearly Heart Rate");
            stepsLineChart.setDescription(description);
            stepsLineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(monthLabels));
            stepsLineChart.getXAxis().setTextSize(18f); // Adjust label text size
            stepsLineChart.invalidate();

        } else if (selectedTime.equals("Last 2 Years")) {
            stepsBarChart.setVisibility(View.GONE);
            stepsLineChart.setVisibility(View.VISIBLE);
            lineEntries.clear();
            Random rand = new Random();

            for (int i = 0; i  < 8; i++) {
                lineEntries.add(new Entry(i, rand.nextInt(50)+50));
            }

            LineDataSet dataSet = new LineDataSet(lineEntries, "Heart Rate");
            dataSet.setColor(Color.BLACK);
            dataSet.setValueTextColor(Color.BLACK);
            dataSet.setLineWidth(5f);
            dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            dataSet.setDrawValues(false);

            LineData lineData = new LineData(dataSet);

            // Customize chart
            stepsLineChart.setData(lineData);

            Description description = new Description();
            description.setText("2 Years Heart Rate");
            stepsLineChart.setDescription(description);
            stepsLineChart.getXAxis().setValueFormatter(null);
            stepsLineChart.getXAxis().setTextSize(18f); // Adjust label text size
            stepsLineChart.invalidate();


        }
    }
}