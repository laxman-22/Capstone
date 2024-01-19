package com.example.capstone.ui.home;


import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.capstone.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.capstone.databinding.FragmentHomeBinding;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.Random;

public class HomeFragment extends Fragment implements OnMapReadyCallback {
    private GoogleMap mMap;

    public static int bpm;
    public static int steps;
    public static int oxSat;
    public static int alert;

    public static int battery;

    private boolean isMapReady;
    public static LatLng location = new LatLng(40, 75);

    private FragmentHomeBinding binding;

    private SupportMapFragment mapFragment;

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

        location = new LatLng(45.3876, -75.6960);

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

        updateBpm(bpm);
        updateO2(oxSat);
        updateSteps(steps);
        updateBatteryLevel(battery);
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
        if (isMapReady && mMap != null && mapFragment != null && location != null) {
            updateLocation(location);
        }
    }

    public void updateBpm(int newBpm) {
        TextView bpm = (TextView) getView().findViewById(R.id.bpm);
        if (bpm != null) {
            bpm.setText(Integer.toString(newBpm));
        }
    }
    public void updateO2(int newO2) {
        TextView spo2 = (TextView) getView().findViewById(R.id.o2num);
        if (spo2 != null) {
            spo2.setText(Integer.toString(newO2)+"%");

        }
    }

    public void updateSteps(int newSteps) {
        TextView steps = (TextView) getView().findViewById(R.id.steps);
        if (steps != null) {
            steps.setText(Integer.toString(newSteps));
        }
    }

    public void updateBatteryLevel(int batteryPercentage) {
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

        mMap.addMarker(new MarkerOptions().position(location).title("Current Location"));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15.0f));
    }


}