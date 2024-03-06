package com.example.capstone.ui.home;


import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.capstone.HeartRateDetailActivity;
import com.example.capstone.R;
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

import java.util.Random;

public class HomeFragment extends Fragment implements OnMapReadyCallback {
    private GoogleMap mMap;

    public static int bpm;
    public static int steps;
    public static int oxSat;
    public static Float lat;
    public static Float lon;
    public static int alert;
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


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
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
                    stepsHandler.postDelayed(this, 10);            }
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


}