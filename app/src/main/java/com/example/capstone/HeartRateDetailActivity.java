package com.example.capstone;

import static com.example.capstone.ui.home.HomeFragment.bpm;

import android.content.Intent;
import android.graphics.Color;
import android.health.connect.datatypes.units.Length;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintSet;

import com.example.capstone.ui.home.HomeFragment;
import com.example.capstone.ui.notifications.NotificationsFragment;
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
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HeartRateDetailActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{
    public static String selectedTime = "Last 7 Days";
    public static int currentRestingBpm;

    private BarChart heartBarChart;
    private LineChart heartLineChart;
    private List<Entry> lineEntries;
    private List<Entry> lineEntriesMonthly;
    private List<Entry> lineEntriesYearly;
    private List<Entry> lineEntries2Years;

    private final String[] weekLabels = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
    private final String[] monthLabels = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

    private String currentMonth;
    // make the 2 years label go in averages for each quarter


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_rate_detail);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Heart Rate Details");
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
        heartBarChart = findViewById(R.id.heartBarChart);
        heartLineChart = findViewById(R.id.heartLineChart);
        List<BarEntry> barEntries = new ArrayList<>();
        barEntries.add(new BarEntry(0, new float[]{80f, 10f})); // Heart rate range for time 1 (start at 80, height 3)
        barEntries.add(new BarEntry(1, new float[]{82f, 20f})); // Heart rate range for time 2 (start at 82, height 3)
        barEntries.add(new BarEntry(2, new float[]{85f, 12f})); // Heart rate range for time 3 (start at 85, height 3)
        barEntries.add(new BarEntry(3, new float[]{87f, 8f})); // Heart rate range for time 4 (start at 87, height 3)
        barEntries.add(new BarEntry(4, new float[]{80f, 10f})); // Heart rate range for time 1 (start at 80, height 3)
        barEntries.add(new BarEntry(5, new float[]{82f, 20f})); // Heart rate range for time 2 (start at 82, height 3)
        barEntries.add(new BarEntry(6, new float[]{85f, 12f})); // Heart rate range for time 3 (start at 85, height 3)

        BarDataSet barDataSet = new BarDataSet(barEntries, "Heart Rate (BPM)");
        barDataSet.setColors(new int[]{Color.TRANSPARENT, Color.RED}); // Color for the bars
        barDataSet.setValueTextSize(0f);

        heartBarChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(weekLabels));
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
        BarData barData = new BarData(barDataSet);

        heartBarChart.setData(barData);

        heartBarChart.invalidate();

        Random rand = new Random();

        for (int i = 0; i  < 30; i++) {
            lineEntries.add(new Entry(i+1, rand.nextInt(50)+50));
        }

        heartLineChart.setTouchEnabled(true);
        LineDataSet dataSet = new LineDataSet(lineEntries, "Heart Rate");
        dataSet.setColor(Color.BLUE);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setLineWidth(5f);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawValues(false);


        LineData lineData = new LineData(dataSet);

        // Customize chart
        heartLineChart.setData(lineData);

        Description description = new Description();
        description.setText("Monthly Heart Rate");
        heartLineChart.setDescription(description);
        heartLineChart.getXAxis().setTextSize(18f);
        heartLineChart.setExtraBottomOffset(20f);

        Legend legend = heartLineChart.getLegend();
        legend.setEnabled(false);

        XAxis xAxis = heartLineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis lineLeftAxis = heartLineChart.getAxisLeft();
        lineLeftAxis.setAxisMinimum(40f); // Minimum heart rate
        lineLeftAxis.setAxisMaximum(120f);
        lineLeftAxis.setTextSize(18f);
        lineLeftAxis.setGranularity(20f);

        YAxis rightAxis = heartLineChart.getAxisRight();
        rightAxis.setEnabled(false);
        heartLineChart.setAutoScaleMinMaxEnabled(true);

        heartLineChart.invalidate(); // Refresh chart


    }
    private void updateBpm() {
        if (findViewById(R.id.detailPulse) == null) {
            return;
        }
        TextView bpmText = (TextView) findViewById(R.id.detailPulse);
        if (bpmText != null) {
            bpmText.setText(Integer.toString(currentRestingBpm));
        }
    }


    private void changeTime() {

        if (selectedTime.equals("Last 7 Days")) {
            heartLineChart.setVisibility(View.GONE);
            heartBarChart.setVisibility(View.VISIBLE);
        } else if (selectedTime.equals("Last Month")) {
            heartBarChart.setVisibility(View.GONE);
            heartLineChart.setVisibility(View.VISIBLE);
            lineEntries.clear();

            Description description = new Description();
            description.setText("Monthly Heart Rate");
            heartLineChart.setDescription(description);
            heartLineChart.getXAxis().setTextSize(18f);
            Random rand = new Random();
            heartLineChart.getXAxis().setValueFormatter(null);

            for (int i = 0; i  < 30; i++) {
                lineEntries.add(new Entry(i+1, rand.nextInt(50)+50));
            }
            LineDataSet dataSet = new LineDataSet(lineEntries, "Heart Rate");
            dataSet.setColor(Color.RED);
            dataSet.setValueTextColor(Color.BLACK);
            dataSet.setLineWidth(5f);
            dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            dataSet.setDrawValues(false);


            LineData lineData = new LineData(dataSet);
            heartLineChart.setData(lineData);
            heartLineChart.invalidate();

        } else if (selectedTime.equals("Last Year")) {
            heartBarChart.setVisibility(View.GONE);
            heartLineChart.setVisibility(View.VISIBLE);
            lineEntries.clear();
            Random rand = new Random();

            for (int i = 0; i  < 12; i++) {
                lineEntries.add(new Entry(i, rand.nextInt(50)+50));
            }

            LineDataSet dataSet = new LineDataSet(lineEntries, "Heart Rate");
            dataSet.setColor(Color.RED);
            dataSet.setValueTextColor(Color.BLACK);
            dataSet.setLineWidth(5f);
            dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            dataSet.setDrawValues(false);

            LineData lineData = new LineData(dataSet);

            // Customize chart
            heartLineChart.setData(lineData);

            Description description = new Description();
            description.setText("Yearly Heart Rate");
            heartLineChart.setDescription(description);
            heartLineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(monthLabels));
            heartLineChart.getXAxis().setTextSize(18f); // Adjust label text size
            heartLineChart.invalidate();

        } else if (selectedTime.equals("Last 2 Years")) {
            heartBarChart.setVisibility(View.GONE);
            heartLineChart.setVisibility(View.VISIBLE);
            lineEntries.clear();
            Random rand = new Random();

            for (int i = 0; i  < 8; i++) {
                lineEntries.add(new Entry(i, rand.nextInt(50)+50));
            }

            LineDataSet dataSet = new LineDataSet(lineEntries, "Heart Rate");
            dataSet.setColor(Color.RED);
            dataSet.setValueTextColor(Color.BLACK);
            dataSet.setLineWidth(5f);
            dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            dataSet.setDrawValues(false);

            LineData lineData = new LineData(dataSet);

            // Customize chart
            heartLineChart.setData(lineData);

            Description description = new Description();
            description.setText("2 Years Heart Rate");
            heartLineChart.setDescription(description);
            heartLineChart.getXAxis().setValueFormatter(null);
            heartLineChart.getXAxis().setTextSize(18f); // Adjust label text size
            heartLineChart.invalidate();


        }

    }

}
