package com.example.capstone.ui.home;


import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.capstone.HeartRateDetailActivity;
import com.example.capstone.HomePage;
import com.example.capstone.MainActivity;
import com.example.capstone.R;
import com.example.capstone.SignUpPage;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.capstone.databinding.FragmentHomeBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class HomeFragment extends Fragment implements OnMapReadyCallback {
    private GoogleMap mMap;

    public static int bpm;
    public static int steps;
    public static int oxSat;
    public static Float lat;
    public static Float lon;
    public static int battery;

    private boolean isMapReady;

    private FragmentHomeBinding binding;

    private SupportMapFragment mapFragment;

    private Handler pulseHandler = new Handler();
    private Handler oxHandler = new Handler();
    private Handler stepsHandler = new Handler();
    private Handler batteryHandler = new Handler();
    private Runnable refreshBpm;

    private Runnable refreshOx;

    private Runnable refreshSteps;

    private Runnable refreshBattery;

    public static int isPrimary;
    public static int isRegistered;

    public static int isAuthorized;

    public static int deviceId;
    private SwipeRefreshLayout swipeRefreshLayout;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Log.d("Data here", isRegistered + " : " + deviceId);
        swipeRefreshLayout = binding.swipeRefreshLayout;
        Log.e("here", "here");

        if (swipeRefreshLayout == null) {
            Log.e("Error", "swipeRefreshLayout is null");
        } else {
            Log.d("Success", "swipeRefreshLayout is not null");
        }

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Implement your refresh logic here
                // For example, you can reload data from the server or update the UI
                // For demonstration purposes, let's just show a toast message
                Toast.makeText(getContext(), "Refreshing...", Toast.LENGTH_SHORT).show();

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


                RequestQueue volleyQueue = Volley.newRequestQueue(getContext());

                String url = "https://78.138.17.29:3000/getCurrUserData";
                JsonObjectRequest newjsonObjectRequest = new JsonObjectRequest(
                        Request.Method.GET,
                        url,
                        null,
                        response -> {

                            try {
                                JSONArray jsonArray = response.getJSONArray("results"); // Assuming "users" is the key for your array
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject userObject = jsonArray.getJSONObject(i);
                                    // Parse user data here
                                    int isPrimary = userObject.getInt("is_primary");
                                    int isRegistered = userObject.getInt("is_registered");
                                    int isAuthorized = userObject.getInt("is_authorized");
                                    int device_id = userObject.getInt("device_id");

                                    Log.d("Dataa", Integer.toString(device_id));
                                    Log.d("Dataa", Integer.toString(isRegistered));

                                    HomeFragment.isPrimary = isPrimary;
                                    HomeFragment.isAuthorized = isAuthorized;
                                    HomeFragment.isRegistered = isRegistered;
                                    HomeFragment.deviceId = device_id;
                                    Log.d("registered?", Integer.toString(isRegistered));
                                    if (isRegistered == 0) {
                                        TextView batteryLevel = binding.getRoot().findViewById(R.id.batteryLevel);
                                        batteryLevel.setVisibility(View.GONE);
                                        FrameLayout frameLayout = binding.getRoot().findViewById(R.id.mapContainer);
                                        frameLayout.setVisibility(View.GONE);

                                        RelativeLayout relativeLayout = binding.getRoot().findViewById(R.id.component_1);
                                        relativeLayout.setVisibility(View.GONE);

                                        RelativeLayout relativeLayout1 = binding.getRoot().findViewById(R.id.component_2);
                                        relativeLayout1.setVisibility(View.GONE);

                                        RelativeLayout relativeLayout2 = binding.getRoot().findViewById(R.id.component_3);
                                        relativeLayout2.setVisibility(View.GONE);
                                        ImageView battery = (ImageView) binding.getRoot().findViewById(R.id.imageView4);
                                        battery.setVisibility(View.GONE);

                                    } else {
                                        TextView batteryLevel = binding.getRoot().findViewById(R.id.batteryLevel);
                                        batteryLevel.setVisibility(View.VISIBLE);
                                        FrameLayout frameLayout = binding.getRoot().findViewById(R.id.mapContainer);
                                        frameLayout.setVisibility(View.VISIBLE);
                                        RelativeLayout relativeLayout = binding.getRoot().findViewById(R.id.component_1);
                                        relativeLayout.setVisibility(View.VISIBLE);

                                        RelativeLayout relativeLayout1 = binding.getRoot().findViewById(R.id.component_2);
                                        relativeLayout1.setVisibility(View.VISIBLE);

                                        RelativeLayout relativeLayout2 = binding.getRoot().findViewById(R.id.component_3);
                                        relativeLayout2.setVisibility(View.VISIBLE);
                                        ImageView battery = (ImageView) binding.getRoot().findViewById(R.id.imageView4);
                                        battery.setVisibility(View.VISIBLE);
                                        getData();

                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }                },
                        error -> {
                            if (error.networkResponse != null) {
                                Log.e("Response", "Error response code: " + error.networkResponse.statusCode);
                                Log.e("Response", "Error response data: " + new String(error.networkResponse.data));
                            }

                            Toast.makeText(getContext(), "Some error occurred! Cannot fetch response", Toast.LENGTH_LONG).show();
                            Log.e("MainActivity", "Error: " + error.getMessage(), error);
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

                // After completing your refresh logic, call setRefreshing(false) to stop the refreshing animation
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        View root = binding.getRoot();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_fragment);

        mapFragment.getMapAsync(this);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Log.d("Map State", "Map Ready");
        mMap = googleMap;

        LatLng location = new LatLng(45.3876, -75.6960);

        mMap.addMarker(new MarkerOptions().position(location).title("Marker in Carleton"));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15.0f));
        // Enable zoom controls
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setScrollGesturesEnabled(true);
    }


    @Override
    public void onResume() {
        super.onResume();

        Log.d("Resumed", "Resumed");

        if (refreshBpm == null && refreshOx == null && refreshSteps == null) {
            refreshBpm = new Runnable() {
                @Override
                public void run() {
                    updateBpm(bpm);
                    pulseHandler.postDelayed(this, 10);
                }
            };
            pulseHandler.postDelayed(refreshBpm,10);

            refreshOx = new Runnable() {
                @Override
                public void run() {
                    updateO2(oxSat);
                    oxHandler.postDelayed(this, 10);
                }
            };
            oxHandler.postDelayed(refreshOx, 10);

            refreshSteps = new Runnable() {
                @Override
                public void run() {
                    updateSteps(steps);
                    stepsHandler.postDelayed(this, 10);
                }
            };
            stepsHandler.postDelayed(refreshSteps, 10);

            refreshBattery = new Runnable() {
                @Override
                public void run() {
                    updateBatteryLevel(battery);
                    batteryHandler.postDelayed(this, 10);
                }
            };
            batteryHandler.postDelayed(refreshBattery, 10);
        }


        Thread thread = new Thread(() -> {
            while (mMap == null && mapFragment == null) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            isMapReady = true;
        });
        thread.start();
        if (isMapReady && mMap != null && mapFragment != null && lat != null && lon != null) {
            float latitude = lat;
            float longitude = lon;
            LatLng location = new LatLng(latitude, longitude);
            updateLocation(location);
        }
    }

    public void updateBpm(int newBpm) {
        if (getView() == null) {
            return;
        }
        TextView bpm = (TextView) getView().findViewById(R.id.bpm);
        if (bpm != null) {
            bpm.setText(Integer.toString(newBpm));
        }
    }
    public void updateO2(int newO2) {
        if (getView() == null) {
            return;
        }
        TextView spo2 = (TextView) getView().findViewById(R.id.o2num);
        if (spo2 != null) {
            spo2.setText(Integer.toString(newO2)+"%");

        }
    }

    public void updateSteps(int newSteps) {
        if (getView() == null) {
            return;
        }
        TextView steps = (TextView) getView().findViewById(R.id.steps);
        if (steps != null) {
            steps.setText(Integer.toString(newSteps));
        }
    }

    public void updateBatteryLevel(int batteryPercentage) {
        if (getView() == null) {
            return;
        }
        ImageView battery = (ImageView) getView().findViewById(R.id.imageView4);
        if (75 < batteryPercentage && batteryPercentage <= 100) {
            battery.setImageResource(R.drawable.tabler_battery_4);
        }
        else if(50 < batteryPercentage && batteryPercentage <= 75) {
            battery.setImageResource(R.drawable.tabler_battery_3);
        }
        else if (25 < batteryPercentage && batteryPercentage <= 50) {
            battery.setImageResource(R.drawable.tabler_battery_2);
        } else {
            battery.setImageResource(R.drawable.tabler_battery_1);
        }

    }

    public void updateLocation(LatLng location) {
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(location).title("Marker"));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15.0f));
    }
    private void getData() {
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


        RequestQueue volleyQueue = Volley.newRequestQueue(getContext());

        String url = "https://78.138.17.29:3000/getCurrentDeviceData";
        JsonObjectRequest newjsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {

                    try {
                        JSONArray jsonArray = response.getJSONArray("results"); // Assuming "users" is the key for your array
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject userObject = jsonArray.getJSONObject(i);
                            // Parse user data here
                            int pulse = userObject.getInt("pulse");
                            int oxygen_sat = userObject.getInt("oxygen_sat");
                            int steps1 = userObject.getInt("steps");
                            int lati = userObject.getInt("latitude");
                            int longi = userObject.getInt("longitude");
                            int battery1 = userObject.getInt("battery_level");
                            Log.d("Data Received", pulse + " : " + oxygen_sat + " : " + steps1 + " : " + lati + " : " + longi + " : " + battery1);
                            bpm = pulse;
                            oxSat = oxygen_sat;
                            steps = steps1;
                            battery = battery1;
                            lat = lati / 1000000f;
                            lon = longi / 1000000f;

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }                },
                error -> {
                    if (error.networkResponse != null) {
                        Log.e("Response", "Error response code: " + error.networkResponse.statusCode);
                        Log.e("Response", "Error response data: " + new String(error.networkResponse.data));
                    }

                    Toast.makeText(getContext(), "Some error occurred! Cannot fetch response", Toast.LENGTH_LONG).show();
                    Log.e("MainActivity", "Error: " + error.getMessage(), error);
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