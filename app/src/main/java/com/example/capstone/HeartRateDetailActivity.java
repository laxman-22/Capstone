package com.example.capstone;

import static java.util.Collections.max;
import static java.util.Collections.min;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class HeartRateDetailActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{
    public static String selectedTime = "Last 7 Days";
    public static int currentRestingBpm;

    private BarChart heartBarChart;
    private LineChart heartLineChart;
    private List<Entry> lineEntriesMonthly;
    private List<Entry> lineEntriesYearly;
    private List<Entry> lineEntries2Years;

    private static String[] weekLabels = {"Mon","Tue","Wed","Thu","Fri","Sat","Sun"};
    private static String[] monthLabels = {"","","","","","","","","","","",""};
    private static String[] quarterLabels = {"","","","","","","",""};
    private static String[] dayLabelsMonth = {"","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""};

    private String currentMonth;
    // make the 2 years label go in averages for each quarter


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_rate_detail);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Heart Rate Details");
        lineEntriesMonthly = new ArrayList<>();
        lineEntriesYearly = new ArrayList<>();
        lineEntries2Years = new ArrayList<>();
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
        changeTime();

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
            updateBarChart(heartBarChart);
        } else if (selectedTime.equals("Last Month")) {
            heartBarChart.setVisibility(View.GONE);
            heartLineChart.setVisibility(View.VISIBLE);
            lineEntriesMonthly.clear();

            updateLineChart(heartLineChart);

        } else if (selectedTime.equals("Last Year")) {
            heartBarChart.setVisibility(View.GONE);
            heartLineChart.setVisibility(View.VISIBLE);
            lineEntriesYearly.clear();

            updateLineChart(heartLineChart);


        } else if (selectedTime.equals("Last 2 Years")) {
            heartBarChart.setVisibility(View.GONE);
            heartLineChart.setVisibility(View.VISIBLE);
            lineEntries2Years.clear();

            updateLineChart(heartLineChart);

        }

    }
    private void updateBarChart(BarChart barChart) {
        try {
            // Create a TrustManager that trusts all certificates
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[]{};
                        }
                    }
            };

            // Initialize SSLContext with the custom TrustManager
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new SecureRandom());

            // Set the SSL socket factory for Volley's HurlStack
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true; // Trust all hostnames
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }


        RequestQueue volleyQueue = Volley.newRequestQueue(HeartRateDetailActivity.this);

        String createurl = "https://78.138.17.29:3000/getDailyHighLowPulse";

        List<BarEntry> barEntries = new ArrayList<>();
        JsonObjectRequest newjsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                createurl,
                null,
                response -> {
                    try {
                            ArrayList<Integer> pulses = new ArrayList<Integer>();
                            JSONArray jsonArray = response.getJSONArray("results"); // Assuming "users" is the key for your array
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject userObject = jsonArray.getJSONObject(i);
                                // Parse user data here
                                int max_pulse = userObject.getInt("max_pulse");
                                int min_pulse = userObject.getInt("min_pulse");
                                pulses.add(max_pulse);
                                pulses.add(min_pulse);
                                String day_of_week = userObject.getString("day_of_week");
                                if (day_of_week.equals("Monday")) {
                                    barEntries.add(new BarEntry(0, new float[]{(float)min_pulse, (float)max_pulse-min_pulse})); // Heart rate range for time 1 (start at 80, height 3)
                                }if (day_of_week.equals("Tuesday")) {
                                    barEntries.add(new BarEntry(1, new float[]{(float)min_pulse, (float)max_pulse-min_pulse})); // Heart rate range for time 1 (start at 80, height 3)
                                }if (day_of_week.equals("Wednesday")) {
                                    barEntries.add(new BarEntry(2, new float[]{(float)min_pulse, (float)max_pulse-min_pulse})); // Heart rate range for time 1 (start at 80, height 3)
                                }if (day_of_week.equals("Thursday")) {
                                    barEntries.add(new BarEntry(3, new float[]{(float)min_pulse, (float)max_pulse-min_pulse})); // Heart rate range for time 1 (start at 80, height 3)
                                }if (day_of_week.equals("Friday")) {
                                    barEntries.add(new BarEntry(4, new float[]{(float)min_pulse, (float)max_pulse-min_pulse})); // Heart rate range for time 1 (start at 80, height 3)
                                }if (day_of_week.equals("Saturday")) {
                                    barEntries.add(new BarEntry(5, new float[]{(float)min_pulse, (float)max_pulse-min_pulse})); // Heart rate range for time 1 (start at 80, height 3)
                                }if (day_of_week.equals("Sunday")) {
                                    barEntries.add(new BarEntry(6, new float[]{(float)min_pulse, (float)max_pulse-min_pulse})); // Heart rate range for time 1 (start at 80, height 3)
                                }
                                BarDataSet barDataSet = new BarDataSet(barEntries, "Heart Rate (BPM)");
                                barDataSet.setColors(new int[]{Color.TRANSPARENT, Color.RED}); // Color for the bars
                                barDataSet.setValueTextSize(0f);

                                BarData barData = new BarData(barDataSet);

                                barChart.setData(barData);

                                barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(weekLabels));
                                barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
                                barChart.getXAxis().setGranularity(1f);
                                barChart.getXAxis().setTextSize(18f);
                                barChart.setExtraBottomOffset(20f);

                                YAxis leftAxis = barChart.getAxisLeft();
                                leftAxis.setAxisMinimum((float) (min(pulses)*.90)); // Minimum heart rate
                                leftAxis.setAxisMaximum((float) (max(pulses)*1.10)); // Maximum heart rate
                                leftAxis.setGranularity(40f);
                                leftAxis.setTextSize(18f);
                                barChart.getAxisRight().setEnabled(false); // Disable right y-axis

                                barChart.getDescription().setEnabled(false);
                                barChart.getLegend().setEnabled(false);
                                barChart.setTouchEnabled(false);

                                barChart.invalidate();
                                Log.d("Data", max_pulse + " : " + min_pulse + " : " + day_of_week);

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                },
                error -> {
                    if (error.networkResponse != null) {
                        Log.e("Response", "Error response code: " + error.networkResponse.statusCode);
                        Log.e("Response", "Error response data: " + new String(error.networkResponse.data));
                    }

                    Toast.makeText(HeartRateDetailActivity.this, "Some error occurred! Cannot fetch response", Toast.LENGTH_LONG).show();
                    Log.e("SignUpPage", "Error: " + error.getMessage(), error);
                }
        ) { @Override
        public Map<String, String> getHeaders() throws AuthFailureError {
            Map<String, String> headers = new HashMap<>();
            headers.put("x-api-key", SignUpPage.apiKey);

            // Add other headers if needed
            return headers;
        }
        };
        volleyQueue.add(newjsonObjectRequest);



    }
    private void updateLineChart(LineChart chart) {
        try {
            // Create a TrustManager that trusts all certificates
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[]{};
                        }
                    }
            };

            // Initialize SSLContext with the custom TrustManager
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new SecureRandom());

            // Set the SSL socket factory for Volley's HurlStack
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true; // Trust all hostnames
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }


        if (selectedTime.equals("Last Month")) {
            RequestQueue volleyQueue = Volley.newRequestQueue(HeartRateDetailActivity.this);
            String createurl = "https://78.138.17.29:3000/getDailyAvgPulse";

            JsonObjectRequest newjsonObjectRequest = new JsonObjectRequest(
                    Request.Method.GET,
                    createurl,
                    null,
                    response -> {
                        try {
                            ArrayList<Float> pulses = new ArrayList<Float>();
                            JSONArray jsonArray = response.getJSONArray("results"); // Assuming "users" is the key for your array
                            Description description = new Description();
                            description.setText("Monthly Heart Rate");
                            chart.setDescription(description);
                            chart.getXAxis().setTextSize(18f);

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject userObject = jsonArray.getJSONObject(i);
                                // Parse user data here
                                float avg_pulse = userObject.getLong("avg_pulse");
                                pulses.add(avg_pulse);
                                int day_of_month = userObject.getInt("day_of_month");
                                String day_of_week = userObject.getString("day_of_week");
                                if (day_of_month % 2 == 1 && day_of_month != 11) {
                                    dayLabelsMonth[i] = day_of_month+"st";
                                } else if (day_of_month == 2 && day_of_month != 12) {
                                    dayLabelsMonth[i] = day_of_month+"nd";
                                } else if (day_of_month == 3 && day_of_month != 13) {
                                    dayLabelsMonth[i] = day_of_month+"rd";
                                } else {
                                    dayLabelsMonth[i] = day_of_month+"th";
                                }
                                chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(dayLabelsMonth));

                                lineEntriesMonthly.add(new Entry(i, avg_pulse));
                                Log.d("Data", avg_pulse + " : " + day_of_month + " : " + day_of_week);

                            }

                            LineDataSet dataSet = new LineDataSet(lineEntriesMonthly, "Heart Rate");
                            dataSet.setColor(Color.RED);
                            dataSet.setValueTextColor(Color.BLACK);
                            dataSet.setLineWidth(5f);
                            dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                            dataSet.setDrawValues(false);
                            chart.setExtraBottomOffset(20f);


                            LineData lineData = new LineData(dataSet);
                            chart.setData(lineData);
                            chart.getXAxis().setTextSize(18f);
                            chart.setTouchEnabled(true);
                            Legend legend = chart.getLegend();
                            legend.setEnabled(false);

                            XAxis xAxis = chart.getXAxis();
                            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

                            YAxis lineLeftAxis = chart.getAxisLeft();
                            lineLeftAxis.setAxisMinimum((float) (min(pulses)*0.9)); // Minimum heart rate
                            lineLeftAxis.setAxisMaximum((float) (max(pulses)*1.1));
                            lineLeftAxis.setTextSize(18f);
                            lineLeftAxis.setGranularity(20f);

                            YAxis rightAxis = chart.getAxisRight();
                            rightAxis.setEnabled(false);
                            chart.setAutoScaleMinMaxEnabled(true);

                            chart.invalidate();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    },
                    error -> {
                        if (error.networkResponse != null) {
                            Log.e("Response", "Error response code: " + error.networkResponse.statusCode);
                            Log.e("Response", "Error response data: " + new String(error.networkResponse.data));
                        }

                        Toast.makeText(HeartRateDetailActivity.this, "Some error occurred! Cannot fetch response", Toast.LENGTH_LONG).show();
                        Log.e("SignUpPage", "Error: " + error.getMessage(), error);
                    }
            ) { @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("x-api-key", SignUpPage.apiKey);

                // Add other headers if needed
                return headers;
            }
            };
            volleyQueue.add(newjsonObjectRequest);

        } else if (selectedTime.equals("Last Year")) {
            RequestQueue volleyQueue = Volley.newRequestQueue(HeartRateDetailActivity.this);
            String createurl = "https://78.138.17.29:3000/getMonthlyAvgPulse";

            JsonObjectRequest newjsonObjectRequest = new JsonObjectRequest(
                    Request.Method.GET,
                    createurl,
                    null,
                    response -> {
                        try {
                            JSONArray jsonArray = response.getJSONArray("results"); // Assuming "users" is the key for your array
                            ArrayList<Float> pulses = new ArrayList<Float>();

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject userObject = jsonArray.getJSONObject(i);
                                // Parse user data here
                                float avg_pulse = userObject.getLong("avg_pulse");
                                pulses.add(avg_pulse);
                                String month_name = userObject.getString("month_name");
                                if (month_name.equals("January")) {
                                    monthLabels[i] = "Jan";
                                }if (month_name.equals("February")) {
                                    monthLabels[i] = "Feb";
                                }if (month_name.equals("March")) {
                                    monthLabels[i] = "Mar";

                                }if (month_name.equals("April")) {
                                    monthLabels[i] = "Apr";

                                }if (month_name.equals("May")) {
                                    monthLabels[i] = "May";

                                }if (month_name.equals("June")) {
                                    monthLabels[i] = "Jun";

                                }if (month_name.equals("July")) {
                                    monthLabels[i] = "Jul";

                                }if (month_name.equals("August")) {
                                    monthLabels[i] = "Aug";

                                }if (month_name.equals("September")) {
                                    monthLabels[i] = "Sep";

                                }if (month_name.equals("October")) {
                                    monthLabels[i] = "Oct";

                                }if (month_name.equals("November")) {
                                    monthLabels[i] = "Nov";

                                }if (month_name.equals("December")) {
                                    monthLabels[i] = "Dec";

                                }
                                lineEntriesYearly.add(new Entry(i, avg_pulse)); // Heart rate range for time 1 (start at 80, height 3)

                                Log.d("Data", avg_pulse + " : " + month_name);

                            }

                            LineDataSet dataSet = new LineDataSet(lineEntriesYearly, "Heart Rate");
                            dataSet.setColor(Color.RED);
                            dataSet.setValueTextColor(Color.BLACK);
                            dataSet.setLineWidth(5f);
                            dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                            dataSet.setDrawValues(false);
                            chart.setExtraBottomOffset(20f);


                            LineData lineData = new LineData(dataSet);
                            chart.setData(lineData);
                            chart.getXAxis().setTextSize(18f);
                            chart.setTouchEnabled(true);
                            Legend legend = chart.getLegend();
                            legend.setEnabled(false);

                            XAxis xAxis = chart.getXAxis();
                            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

                            YAxis lineLeftAxis = chart.getAxisLeft();
                            lineLeftAxis.setAxisMinimum((float) (min(pulses)*.9)); // Minimum heart rate
                            lineLeftAxis.setAxisMaximum((float) (max(pulses)*1.1));
                            lineLeftAxis.setTextSize(18f);
                            lineLeftAxis.setGranularity(20f);

                            YAxis rightAxis = chart.getAxisRight();
                            rightAxis.setEnabled(false);
                            chart.setAutoScaleMinMaxEnabled(true);
                            Description description = new Description();
                            description.setText("Yearly Heart Rate");
                            chart.setDescription(description);
                            chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(monthLabels));
                            chart.getXAxis().setTextSize(18f); // Adjust label text size
                            chart.getXAxis().setGranularity(1f);
                            // Customize chart
                            chart.setData(lineData);
                            chart.invalidate();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    },
                    error -> {
                        if (error.networkResponse != null) {
                            Log.e("Response", "Error response code: " + error.networkResponse.statusCode);
                            Log.e("Response", "Error response data: " + new String(error.networkResponse.data));
                        }

                        Toast.makeText(HeartRateDetailActivity.this, "Some error occurred! Cannot fetch response", Toast.LENGTH_LONG).show();
                        Log.e("SignUpPage", "Error: " + error.getMessage(), error);
                    }
            ) { @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("x-api-key", SignUpPage.apiKey);

                // Add other headers if needed
                return headers;
            }
            };
            volleyQueue.add(newjsonObjectRequest);

        } else if (selectedTime.equals("Last 2 Years")) {

            RequestQueue volleyQueue = Volley.newRequestQueue(HeartRateDetailActivity.this);
            String createurl = "https://78.138.17.29:3000/getQuarterlyAvgPulse";

            JsonObjectRequest newjsonObjectRequest = new JsonObjectRequest(
                    Request.Method.GET,
                    createurl,
                    null,
                    response -> {
                        try {
                            JSONArray jsonArray = response.getJSONArray("results"); // Assuming "users" is the key for your array
                            ArrayList<Float> pulses = new ArrayList<Float>();

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject userObject = jsonArray.getJSONObject(i);
                                // Parse user data here
                                int year = userObject.getInt("year");
                                int quarter = userObject.getInt("quarter");
                                float avg_pulse = userObject.getInt("average_pulse");
                                pulses.add(avg_pulse);
                                if (quarter == 1) {
                                    quarterLabels[i] = "Jan/" + year;
                                } else if (quarter == 2) {
                                    quarterLabels[i] = "Apr/" + year;
                                } else if (quarter == 3) {
                                    quarterLabels[i] = "Aug/" + year;
                                } else if (quarter == 4) {
                                    quarterLabels[i] = "Oct/" + year;
                                }
                                lineEntries2Years.add(new Entry(i, avg_pulse)); // Heart rate range for time 1 (start at 80, height 3)


                                Log.d("Data", avg_pulse + " : " + year + " : " + quarter);

                            }

                            LineDataSet dataSet = new LineDataSet(lineEntries2Years, "Heart Rate");
                            dataSet.setColor(Color.RED);
                            dataSet.setValueTextColor(Color.BLACK);
                            dataSet.setLineWidth(5f);
                            dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                            dataSet.setDrawValues(false);
                            chart.setExtraBottomOffset(20f);


                            LineData lineData = new LineData(dataSet);
                            chart.setData(lineData);
                            chart.getXAxis().setTextSize(12f);
                            chart.setTouchEnabled(true);
                            Legend legend = chart.getLegend();
                            legend.setEnabled(false);

                            XAxis xAxis = chart.getXAxis();
                            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

                            YAxis lineLeftAxis = chart.getAxisLeft();
                            lineLeftAxis.setAxisMinimum((float) (min(pulses)*.9)); // Minimum heart rate
                            lineLeftAxis.setAxisMaximum((float) (max(pulses)*1.1));
                            lineLeftAxis.setTextSize(18f);
                            lineLeftAxis.setGranularity(20f);

                            YAxis rightAxis = chart.getAxisRight();
                            rightAxis.setEnabled(false);
                            chart.setAutoScaleMinMaxEnabled(true);
                            Description description = new Description();
                            description.setText("Yearly Heart Rate");
                            chart.setDescription(description);


                            chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(quarterLabels));
                            chart.getXAxis().setTextSize(12f); // Adjust label text size

                            // Customize chart
                            chart.setData(lineData);
                            chart.invalidate();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    },
                    error -> {
                        if (error.networkResponse != null) {
                            Log.e("Response", "Error response code: " + error.networkResponse.statusCode);
                            Log.e("Response", "Error response data: " + new String(error.networkResponse.data));
                        }

                        Toast.makeText(HeartRateDetailActivity.this, "Some error occurred! Cannot fetch response", Toast.LENGTH_LONG).show();
                        Log.e("SignUpPage", "Error: " + error.getMessage(), error);
                    }
            ) { @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("x-api-key", SignUpPage.apiKey);

                // Add other headers if needed
                return headers;
            }
            };
            volleyQueue.add(newjsonObjectRequest);

        }

    }

}
