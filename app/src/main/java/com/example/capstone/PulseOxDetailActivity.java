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
import com.github.mikephil.charting.formatter.ValueFormatter;


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

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class PulseOxDetailActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private BarChart oxBarChart;
    public static String selectedTime = "Last 7 Days";
    private static String[] weekLabels = {"Mon","Tue","Wed","Thu","Fri","Sat","Sun"};
    private static String[] monthLabels = {"","","","","","","","","","","",""};
    private static String[] quarterLabels = {"","","","","","","",""};
    private static String[] dayLabelsMonth = {"","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""};

    private List<Entry> lineEntriesMonthly;
    private List<Entry> lineEntriesYearly;
    private List<Entry> lineEntries2Years;
    private LineChart oxLineChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pulse_ox_detail);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Pulse Oximeter Details");
        lineEntriesMonthly = new ArrayList<>();
        lineEntriesYearly = new ArrayList<>();
        lineEntries2Years = new ArrayList<>();        setupGraphs();
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
        changeTime();

    }

    private void changeTime() {

        if (selectedTime.equals("Last 7 Days")) {
            oxLineChart.setVisibility(View.GONE);
            oxBarChart.setVisibility(View.VISIBLE);
            updateBarChart(oxBarChart);
        } else if (selectedTime.equals("Last Month")) {
            oxBarChart.setVisibility(View.GONE);
            oxLineChart.setVisibility(View.VISIBLE);
            lineEntriesMonthly.clear();
            updateLineChart(oxLineChart);

        } else if (selectedTime.equals("Last Year")) {
            oxBarChart.setVisibility(View.GONE);
            oxLineChart.setVisibility(View.VISIBLE);
            lineEntriesMonthly.clear();
            updateLineChart(oxLineChart);

        } else if (selectedTime.equals("Last 2 Years")) {
            oxBarChart.setVisibility(View.GONE);
            oxLineChart.setVisibility(View.VISIBLE);
            lineEntries2Years.clear();
            updateLineChart(oxLineChart);

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
        RequestQueue volleyQueue = Volley.newRequestQueue(PulseOxDetailActivity.this);

        String createurl = "https://78.138.17.29:3000/getDailyHighLowOxSat";

        List<BarEntry> barEntries = new ArrayList<>();
        JsonObjectRequest newjsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                createurl,
                null,
                response -> {
                    try {
                        ArrayList<Integer> ox_sats = new ArrayList<Integer>();
                        JSONArray jsonArray = response.getJSONArray("results"); // Assuming "users" is the key for your array
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject userObject = jsonArray.getJSONObject(i);
                            // Parse user data here
                            int max_ox_sat = userObject.getInt("max_oxygen_sat");
                            int min_ox_sat = userObject.getInt("min_oxygen_sat");
                            ox_sats.add(max_ox_sat);
                            ox_sats.add(min_ox_sat);
                            String day_of_week = userObject.getString("day_of_week");
                            if (day_of_week.equals("Monday")) {
                                barEntries.add(new BarEntry(0, new float[]{(float)min_ox_sat, (float)max_ox_sat-min_ox_sat})); // Heart rate range for time 1 (start at 80, height 3)
                            }if (day_of_week.equals("Tuesday")) {
                                barEntries.add(new BarEntry(1, new float[]{(float)min_ox_sat, (float)max_ox_sat-min_ox_sat})); // Heart rate range for time 1 (start at 80, height 3)
                            }if (day_of_week.equals("Wednesday")) {
                                barEntries.add(new BarEntry(2, new float[]{(float)min_ox_sat, (float)max_ox_sat-min_ox_sat})); // Heart rate range for time 1 (start at 80, height 3)
                            }if (day_of_week.equals("Thursday")) {
                                barEntries.add(new BarEntry(3, new float[]{(float)min_ox_sat, (float)max_ox_sat-min_ox_sat})); // Heart rate range for time 1 (start at 80, height 3)
                            }if (day_of_week.equals("Friday")) {
                                barEntries.add(new BarEntry(4, new float[]{(float)min_ox_sat, (float)max_ox_sat-min_ox_sat})); // Heart rate range for time 1 (start at 80, height 3)
                            }if (day_of_week.equals("Saturday")) {
                                barEntries.add(new BarEntry(5, new float[]{(float)min_ox_sat, (float)max_ox_sat-min_ox_sat})); // Heart rate range for time 1 (start at 80, height 3)
                            }if (day_of_week.equals("Sunday")) {
                                barEntries.add(new BarEntry(6, new float[]{(float)min_ox_sat, (float)max_ox_sat-min_ox_sat})); // Heart rate range for time 1 (start at 80, height 3)
                            }
                            BarDataSet barDataSet = new BarDataSet(barEntries, "Heart Rate (BPM)");
                            barDataSet.setColors(new int[]{Color.TRANSPARENT, Color.BLUE}); // Color for the bars
                            barDataSet.setValueTextSize(0f);

                            BarData barData = new BarData(barDataSet);

                            barChart.setData(barData);

                            barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(weekLabels));
                            barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
                            barChart.getXAxis().setGranularity(1f);
                            barChart.getXAxis().setTextSize(18f);
                            barChart.setExtraBottomOffset(20f);

                            YAxis leftAxis = barChart.getAxisLeft();
                            leftAxis.setAxisMinimum(75f); // Minimum heart rate
                            leftAxis.setAxisMaximum(105f); // Maximum heart rate
                            leftAxis.setGranularity(10f);
                            leftAxis.setTextSize(18f);
                            barChart.getAxisRight().setEnabled(false); // Disable right y-axis

                            barChart.getDescription().setEnabled(false);
                            barChart.getLegend().setEnabled(false);
                            barChart.setTouchEnabled(false);

                            barChart.invalidate();
                            Log.d("Data", max_ox_sat + " : " + min_ox_sat + " : " + day_of_week);

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

                    Toast.makeText(PulseOxDetailActivity.this, "Some error occurred! Cannot fetch response", Toast.LENGTH_LONG).show();
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
    private void updateLineChart(LineChart lineChart){
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
            RequestQueue volleyQueue = Volley.newRequestQueue(PulseOxDetailActivity.this);
            String createurl = "https://78.138.17.29:3000/getDailyAvgOxSat";

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
                            lineChart.setDescription(description);
                            lineChart.getXAxis().setTextSize(18f);

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject userObject = jsonArray.getJSONObject(i);
                                // Parse user data here
                                float avg_oxygen_sat = userObject.getLong("avg_oxygen_sat");
                                pulses.add(avg_oxygen_sat);
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
                                lineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(dayLabelsMonth));

                                lineEntriesMonthly.add(new Entry(i, avg_oxygen_sat));
                                Log.d("Data", avg_oxygen_sat + " : " + day_of_month + " : " + day_of_week);

                            }

                            LineDataSet dataSet = new LineDataSet(lineEntriesMonthly, "Heart Rate");
                            dataSet.setColor(Color.BLUE);
                            dataSet.setValueTextColor(Color.BLACK);
                            dataSet.setLineWidth(5f);
                            dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                            dataSet.setDrawValues(false);
                            lineChart.setExtraBottomOffset(20f);


                            LineData lineData = new LineData(dataSet);
                            lineChart.setData(lineData);
                            lineChart.getXAxis().setTextSize(18f);
                            lineChart.setTouchEnabled(true);
                            Legend legend = lineChart.getLegend();
                            legend.setEnabled(false);

                            XAxis xAxis = lineChart.getXAxis();
                            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

                            YAxis lineLeftAxis = lineChart.getAxisLeft();
                            lineLeftAxis.setAxisMinimum((float) (min(pulses)*0.9)); // Minimum heart rate
                            lineLeftAxis.setAxisMaximum((float) (max(pulses)*1.1));
                            lineLeftAxis.setTextSize(18f);
                            lineLeftAxis.setGranularity(20f);

                            YAxis rightAxis = lineChart.getAxisRight();
                            rightAxis.setEnabled(false);
                            lineChart.setAutoScaleMinMaxEnabled(true);

                            lineChart.invalidate();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    },
                    error -> {
                        if (error.networkResponse != null) {
                            Log.e("Response", "Error response code: " + error.networkResponse.statusCode);
                            Log.e("Response", "Error response data: " + new String(error.networkResponse.data));
                        }

                        Toast.makeText(PulseOxDetailActivity.this, "Some error occurred! Cannot fetch response", Toast.LENGTH_LONG).show();
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
            RequestQueue volleyQueue = Volley.newRequestQueue(PulseOxDetailActivity.this);
            String createurl = "https://78.138.17.29:3000/getMonthlyAvgOxSat";

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
                                float avg_oxygen_sat = userObject.getLong("avg_oxygen_sat");
                                pulses.add(avg_oxygen_sat);
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
                                lineEntriesYearly.add(new Entry(i, avg_oxygen_sat)); // Heart rate range for time 1 (start at 80, height 3)

                                Log.d("Data", avg_oxygen_sat + " : " + month_name);

                            }

                            LineDataSet dataSet = new LineDataSet(lineEntriesYearly, "Heart Rate");
                            dataSet.setColor(Color.BLUE);
                            dataSet.setValueTextColor(Color.BLACK);
                            dataSet.setLineWidth(5f);
                            dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                            dataSet.setDrawValues(false);
                            lineChart.setExtraBottomOffset(20f);


                            LineData lineData = new LineData(dataSet);
                            lineChart.setData(lineData);
                            lineChart.getXAxis().setTextSize(18f);
                            lineChart.setTouchEnabled(true);
                            Legend legend = lineChart.getLegend();
                            legend.setEnabled(false);

                            XAxis xAxis = lineChart.getXAxis();
                            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

                            YAxis lineLeftAxis = lineChart.getAxisLeft();
                            lineLeftAxis.setAxisMinimum((float) (min(pulses)*.9)); // Minimum heart rate
                            lineLeftAxis.setAxisMaximum((float) (max(pulses)*1.1));
                            lineLeftAxis.setTextSize(18f);
                            lineLeftAxis.setGranularity(20f);

                            YAxis rightAxis = lineChart.getAxisRight();
                            rightAxis.setEnabled(false);
                            lineChart.setAutoScaleMinMaxEnabled(true);
                            Description description = new Description();
                            description.setText("Yearly Heart Rate");
                            lineChart.setDescription(description);
                            lineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(monthLabels));
                            lineChart.getXAxis().setTextSize(18f); // Adjust label text size
                            lineChart.getXAxis().setGranularity(1f);
                            // Customize chart
                            lineChart.setData(lineData);
                            lineChart.invalidate();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    },
                    error -> {
                        if (error.networkResponse != null) {
                            Log.e("Response", "Error response code: " + error.networkResponse.statusCode);
                            Log.e("Response", "Error response data: " + new String(error.networkResponse.data));
                        }

                        Toast.makeText(PulseOxDetailActivity.this, "Some error occurred! Cannot fetch response", Toast.LENGTH_LONG).show();
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

            RequestQueue volleyQueue = Volley.newRequestQueue(PulseOxDetailActivity.this);
            String createurl = "https://78.138.17.29:3000/getQuarterlyAvgOxSat";

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
                                float avg_oxygen_sat = userObject.getInt("avg_oxygen_sat");
                                pulses.add(avg_oxygen_sat);
                                if (quarter == 1) {
                                    quarterLabels[i] = "Jan/" + year;
                                } else if (quarter == 2) {
                                    quarterLabels[i] = "Apr/" + year;
                                } else if (quarter == 3) {
                                    quarterLabels[i] = "Aug/" + year;
                                } else if (quarter == 4) {
                                    quarterLabels[i] = "Oct/" + year;
                                }
                                lineEntries2Years.add(new Entry(i, avg_oxygen_sat)); // Heart rate range for time 1 (start at 80, height 3)


                                Log.d("Data", avg_oxygen_sat + " : " + year + " : " + quarter);

                            }

                            LineDataSet dataSet = new LineDataSet(lineEntries2Years, "Oxygen Saturation");
                            dataSet.setColor(Color.BLUE);
                            dataSet.setValueTextColor(Color.BLACK);
                            dataSet.setLineWidth(5f);
                            dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                            dataSet.setDrawValues(false);
                            lineChart.setExtraBottomOffset(20f);


                            LineData lineData = new LineData(dataSet);
                            lineChart.setData(lineData);
                            lineChart.getXAxis().setTextSize(12f);
                            lineChart.setTouchEnabled(true);
                            Legend legend = lineChart.getLegend();
                            legend.setEnabled(false);

                            XAxis xAxis = lineChart.getXAxis();
                            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

                            YAxis lineLeftAxis = lineChart.getAxisLeft();
                            lineLeftAxis.setAxisMinimum((float) (min(pulses)*.9)); // Minimum heart rate
                            lineLeftAxis.setAxisMaximum((float) (max(pulses)*1.1));
                            lineLeftAxis.setTextSize(18f);
                            lineLeftAxis.setGranularity(20f);

                            YAxis rightAxis = lineChart.getAxisRight();
                            rightAxis.setEnabled(false);
                            lineChart.setAutoScaleMinMaxEnabled(true);
                            Description description = new Description();
                            description.setText("Yearly Oxygen Saturation");
                            lineChart.setDescription(description);


                            lineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(quarterLabels));
                            lineChart.getXAxis().setTextSize(12f); // Adjust label text size

                            // Customize chart
                            lineChart.setData(lineData);
                            lineChart.invalidate();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    },
                    error -> {
                        if (error.networkResponse != null) {
                            Log.e("Response", "Error response code: " + error.networkResponse.statusCode);
                            Log.e("Response", "Error response data: " + new String(error.networkResponse.data));
                        }

                        Toast.makeText(PulseOxDetailActivity.this, "Some error occurred! Cannot fetch response", Toast.LENGTH_LONG).show();
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
