package com.example.capstone;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.capstone.databinding.ActivityHomePageBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class HomePage extends AppCompatActivity {
    private boolean hasDevices = false;
    private ActivityHomePageBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityHomePageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_settings, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_home_page);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

    }


    public void logOut(View view) {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        MainActivity.isLoggedIn = false;
    }

    public void addDevice(View view) {

        // handle adding devices from the floating + button
        Log.d(TAG, "addDevice: Success");
        Intent i = new Intent(this, AddDeviceActivity.class);
        startActivity(i);
    }

    public void openPulsePage(View view) {
        Intent i = new Intent(this, PulseOxDetailActivity.class);
        startActivity(i);
    }
    public void openHeartRatePage(View view) {
        Intent i = new Intent(this, HeartRateDetailActivity.class);
        startActivity(i);
    }
    public void openStepsPage(View view) {
        Intent i = new Intent(this, StepsDetailActivity.class);
        startActivity(i);
    }
    public void addAuthorizedUser(View view) {
        Intent i = new Intent(this, AddAuthorizedUserActivity.class);
        startActivity(i);

    }


}