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
import com.github.mikephil.charting.formatter.ValueFormatter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

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

public class StepsDetailActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener  {

    private BarChart stepsBarChart;
    public static String selectedTime = "Last 7 Days";

    private LineChart stepsLineChart;

    private static String[] monthLabels = {"","","","","","","","","","","",""};
    private static String[] quarterLabels = {"","","","","","","",""};
    private static String[] dayLabelsMonth = {"","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""};
    private List<Entry> lineEntriesMonthly;
    private List<Entry> lineEntriesYearly;
    private List<Entry> lineEntries2Years;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_steps_detail);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Steps Details");
        lineEntriesMonthly = new ArrayList<>();
        lineEntriesYearly = new ArrayList<>();
        lineEntries2Years = new ArrayList<>();
        setupGraphs();
        Spinner spinner = findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.times, android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
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
        RequestQueue volleyQueue = Volley.newRequestQueue(StepsDetailActivity.this);

        String createurl = "https://78.138.17.29:3000/getStepsTakenToday";

        List<BarEntry> barEntries = new ArrayList<>();
        JsonObjectRequest newjsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                createurl,
                null,
                response -> {
                    try {
                        JSONArray jsonArray = response.getJSONArray("results"); // Assuming "users" is the key for your array
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject userObject = jsonArray.getJSONObject(i);
                            // Parse user data here
                            int max_steps = userObject.getInt("max_steps_today");

                            Log.d("Data", "Steps Taken Today: " + max_steps);
                            float km = calculateDistanceFromSteps(max_steps);
                            updateDistanceTravelled(km);
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

                    Toast.makeText(StepsDetailActivity.this, "Some error occurred! Cannot fetch response", Toast.LENGTH_LONG).show();
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
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent i = new Intent(this, HomePage.class);
        startActivity(i);
        return super.onOptionsItemSelected(item);
    }
    private float calculateDistanceFromSteps(int max_steps) {
        return max_steps * 0.0007f;
    }
    private void updateDistanceTravelled(float km) {
        TextView text = findViewById(R.id.distanceData);
        String formattedKm = String.format("%.1f", km);
        text.setText(formattedKm);
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
        changeTime();
    }

    private void changeTime() {
        if (selectedTime.equals("Last 7 Days")) {
            stepsLineChart.setVisibility(View.GONE);
            stepsBarChart.setVisibility(View.VISIBLE);
            updateBarChart(stepsBarChart);
        } else if (selectedTime.equals("Last Month")) {
            stepsBarChart.setVisibility(View.GONE);
            stepsLineChart.setVisibility(View.VISIBLE);
            lineEntriesMonthly.clear();
            updateLineChart(stepsLineChart);

        } else if (selectedTime.equals("Last Year")) {
            stepsBarChart.setVisibility(View.GONE);
            stepsLineChart.setVisibility(View.VISIBLE);
            lineEntriesYearly.clear();
            updateLineChart(stepsLineChart);


        } else if (selectedTime.equals("Last 2 Years")) {
            stepsBarChart.setVisibility(View.GONE);
            stepsLineChart.setVisibility(View.VISIBLE);
            lineEntries2Years.clear();
            updateLineChart(stepsLineChart);

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


        RequestQueue volleyQueue = Volley.newRequestQueue(StepsDetailActivity.this);

        String createurl = "https://78.138.17.29:3000/getDailySteps";

        List<BarEntry> barEntries = new ArrayList<>();
        JsonObjectRequest newjsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                createurl,
                null,
                response -> {
                    try {
                        ArrayList<Integer> steps = new ArrayList<Integer>();
                        JSONArray jsonArray = response.getJSONArray("results"); // Assuming "users" is the key for your array
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject userObject = jsonArray.getJSONObject(i);
                            // Parse user data here
                            int max_steps = userObject.getInt("max_steps");
                            steps.add(max_steps);
                            String day_of_week = userObject.getString("day_of_week");
                            if (day_of_week.equals("Monday")) {
                                barEntries.add(new BarEntry(0, new float[]{max_steps}));
                            }if (day_of_week.equals("Wednesday")) {
                                barEntries.add(new BarEntry(1, new float[]{max_steps}));
                            }if (day_of_week.equals("Thursday")) {
                                barEntries.add(new BarEntry(2, new float[]{max_steps}));
                            }if (day_of_week.equals("Friday")) {
                                barEntries.add(new BarEntry(3, new float[]{max_steps}));
                            }if (day_of_week.equals("Saturday")) {
                                barEntries.add(new BarEntry(4, new float[]{max_steps}));
                            }if (day_of_week.equals("Sunday")) {
                                barEntries.add(new BarEntry(5, new float[]{max_steps}));
                            }
                            BarDataSet barDataSet = new BarDataSet(barEntries, "Heart Rate (BPM)");
                            barDataSet.setColors(new int[]{Color.BLACK});
                            barDataSet.setValueTextSize(12f);

                            BarData barData = new BarData(barDataSet);

                            barChart.setData(barData);

                            String[] labels = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"}; // Example time labels
                            barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
                            barChart.getAxisLeft().setValueFormatter(new ValueFormatter() {
                                @Override
                                public String getFormattedValue(float value) {
                                    return String.format("%.0fk", value / 1000.0);
                                }
                            });
                            barDataSet.setValueFormatter(new ValueFormatter() {
                                @Override
                                public String getFormattedValue(float value) {
                                    // Add space or any other formatting you desire
                                    return String.format("%.0fk", value / 1000.0);
                                }
                            });


                            barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
                            barChart.getXAxis().setGranularity(1f);
                            barChart.getXAxis().setTextSize(18f);
                            barChart.setExtraBottomOffset(20f);

                            YAxis leftAxis = barChart.getAxisLeft();
                            leftAxis.setAxisMinimum(0f);
                            leftAxis.setAxisMaximum(max(steps)*1.1f); // Maximum heart rate
                            leftAxis.setGranularity(40f);
                            leftAxis.setTextSize(18f);
                            barChart.getAxisRight().setEnabled(false); // Disable right y-axis

                            barChart.getDescription().setEnabled(false);
                            barChart.getLegend().setEnabled(false);
                            barChart.setTouchEnabled(false);

                            barChart.invalidate();
                            Log.d("Data", max_steps + " : " + day_of_week);

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

                    Toast.makeText(StepsDetailActivity.this, "Some error occurred! Cannot fetch response", Toast.LENGTH_LONG).show();
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

    private void updateLineChart(LineChart lineChart) {
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
            RequestQueue volleyQueue = Volley.newRequestQueue(StepsDetailActivity.this);
            String createurl = "https://78.138.17.29:3000/getDailyAvgSteps";

            JsonObjectRequest newjsonObjectRequest = new JsonObjectRequest(
                    Request.Method.GET,
                    createurl,
                    null,
                    response -> {
                        try {
                            ArrayList<Float> steps = new ArrayList<Float>();
                            JSONArray jsonArray = response.getJSONArray("results"); // Assuming "users" is the key for your array
                            Description description = new Description();
                            description.setText("Monthly Steps");
                            lineChart.setDescription(description);
                            lineChart.getXAxis().setTextSize(18f);

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject userObject = jsonArray.getJSONObject(i);
                                // Parse user data here
                                float avg_steps = userObject.getLong("avg_steps");
                                steps.add(avg_steps);
                                int day_of_month = userObject.getInt("day_of_month");
                                String day_of_week = userObject.getString("day_of_week");
                                if (day_of_month % 2 == 1 && day_of_month != 11) {
                                    dayLabelsMonth[i] = day_of_month+"st";
                                } else if (day_of_month == 2 && day_of_month != 12 || day_of_month == 22) {
                                    dayLabelsMonth[i] = day_of_month+"nd";
                                } else if (day_of_month == 3 && day_of_month != 13) {
                                    dayLabelsMonth[i] = day_of_month+"rd";
                                } else {
                                    dayLabelsMonth[i] = day_of_month+"th";
                                }


                                lineEntriesMonthly.add(new Entry(i, avg_steps));
                                Log.d("Data", avg_steps + " : " + day_of_month + " : " + day_of_week);

                            }

                            LineDataSet dataSet = new LineDataSet(lineEntriesMonthly, "Heart Rate");
                            dataSet.setColor(Color.BLACK);
                            dataSet.setValueTextColor(Color.BLACK);
                            dataSet.setLineWidth(5f);
                            dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                            dataSet.setDrawValues(false);
                            lineChart.getAxisLeft().setValueFormatter(new ValueFormatter() {
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


                            lineChart.setExtraBottomOffset(20f);


                            LineData lineData = new LineData(dataSet);
                            lineChart.setData(lineData);
                            lineChart.getXAxis().setTextSize(18f);
                            lineChart.setTouchEnabled(true);
                            lineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(dayLabelsMonth));

                            Legend legend = lineChart.getLegend();
                            legend.setEnabled(false);
                            lineChart.setAutoScaleMinMaxEnabled(true);

                            XAxis xAxis = lineChart.getXAxis();
                            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

                            YAxis lineLeftAxis = lineChart.getAxisLeft();
                            lineLeftAxis.setAxisMinimum((float) (min(steps)*0.1)); // Minimum heart rate
                            lineLeftAxis.setAxisMaximum((float) (max(steps)*1.1));
                            lineLeftAxis.setTextSize(18f);
                            lineLeftAxis.setGranularity(40f);

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

                        Toast.makeText(StepsDetailActivity.this, "Some error occurred! Cannot fetch response", Toast.LENGTH_LONG).show();
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
            RequestQueue volleyQueue = Volley.newRequestQueue(StepsDetailActivity.this);
            String createurl = "https://78.138.17.29:3000/getMonthlyAvgSteps";

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
                                float avg_steps = userObject.getLong("avg_steps");
                                pulses.add(avg_steps);
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
                                lineEntriesYearly.add(new Entry(i, avg_steps)); // Heart rate range for time 1 (start at 80, height 3)

                                Log.d("Data", avg_steps + " : " + month_name);

                            }

                            LineDataSet dataSet = new LineDataSet(lineEntriesYearly, "Heart Rate");
                            dataSet.setColor(Color.BLACK);
                            dataSet.setValueTextColor(Color.BLACK);
                            dataSet.setLineWidth(5f);
                            dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                            dataSet.setDrawValues(false);
                            lineChart.setExtraBottomOffset(20f);
                            lineChart.setAutoScaleMinMaxEnabled(true);

                            lineChart.getAxisLeft().setValueFormatter(new ValueFormatter() {
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
                            lineLeftAxis.setGranularity(40f);

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

                        Toast.makeText(StepsDetailActivity.this, "Some error occurred! Cannot fetch response", Toast.LENGTH_LONG).show();
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

            RequestQueue volleyQueue = Volley.newRequestQueue(StepsDetailActivity.this);
            String createurl = "https://78.138.17.29:3000/getQuarterlyAvgSteps";

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
                                float average_steps = userObject.getInt("average_steps");
                                pulses.add(average_steps);
                                if (quarter == 1) {
                                    quarterLabels[i] = "Jan/" + year;
                                } else if (quarter == 2) {
                                    quarterLabels[i] = "Apr/" + year;
                                } else if (quarter == 3) {
                                    quarterLabels[i] = "Aug/" + year;
                                } else if (quarter == 4) {
                                    quarterLabels[i] = "Oct/" + year;
                                }
                                lineEntries2Years.add(new Entry(i, average_steps)); // Heart rate range for time 1 (start at 80, height 3)


                                Log.d("Data", average_steps + " : " + year + " : " + quarter);

                            }

                            LineDataSet dataSet = new LineDataSet(lineEntries2Years, "Heart Rate");
                            dataSet.setColor(Color.BLACK);
                            dataSet.setValueTextColor(Color.BLACK);
                            dataSet.setLineWidth(5f);
                            dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                            dataSet.setDrawValues(false);
                            lineChart.setExtraBottomOffset(20f);

                            lineChart.getAxisLeft().setValueFormatter(new ValueFormatter() {
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
                            lineLeftAxis.setGranularity(40f);

                            YAxis rightAxis = lineChart.getAxisRight();
                            rightAxis.setEnabled(false);
                            lineChart.setAutoScaleMinMaxEnabled(true);
                            Description description = new Description();
                            description.setText("Quarterly Steps");
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

                        Toast.makeText(StepsDetailActivity.this, "Some error occurred! Cannot fetch response", Toast.LENGTH_LONG).show();
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