package com.example.capstone;

import static android.content.ContentValues.TAG;

import androidx.core.app.AppOpsManagerCompat;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.capstone.ui.home.HomeFragment;
import com.example.capstone.ui.settings.SettingsFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
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

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    public static boolean isLoggedIn;

    public static SettingsFragment settingsFragment;

    public static String email;

    public static int age = -1;
    public static float weight = -1;
    public static String sex = "";
    public static String wifiPass = "";

    public static boolean isAuthorized;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        // if already logged in, send to home page

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "channel_id",
                    "Channel Name",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
            SharedPreferences sharedPreferences = getSharedPreferences("Capstone", Context.MODE_PRIVATE);

            Map<String, ?> allEntries = sharedPreferences.getAll();

            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                Log.d("SharedPreferences", "Key: " + key + ", Value: " + value);

                // You can now use the key and value as needed
                if (key.contains("@")) {
                    Log.d("SharedPreferences", "Key: " + key + ", Value: " + value);
                }

            }

        }
    }


    public boolean logIn(View view) {
        TextView emailTextField = findViewById(R.id.LoginEmailAddress);
        TextView passwordTextField = findViewById(R.id.LoginPassword);
        String email = emailTextField.getText().toString();
        String password = passwordTextField.getText().toString();
        Intent i = new Intent(this, HomePage.class);
        MainActivity.email = email;
        if (isLoggedIn) {
            Toast.makeText(MainActivity.this, "Already Logged In", Toast.LENGTH_LONG).show();
            startActivity(i);
        }
        else if (!email.isEmpty() && !password.isEmpty()) {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "signInWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                Toast.makeText(MainActivity.this, "Login Successful", Toast.LENGTH_LONG).show();
                                Log.d("success", "Log In Successful");
                                // check SQL server if user is authorized or primary
                                getCurrentUser();
                                startActivity(i);
                                isLoggedIn = true;
                            }
                            else {
                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                                Toast.makeText(MainActivity.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                                isLoggedIn = false;
                                MainActivity.this.recreate();
                            }
                        }
                    });
        }
        else {
            MainActivity.this.recreate();
        }

        return isLoggedIn;
    }
    private void getCurrentUser() {
        SharedPreferences sharedPreferences = getSharedPreferences("Capstone", Context.MODE_PRIVATE);
        String apiKey = sharedPreferences.getString("apiKey", "");
        SignUpPage.apiKey = apiKey;

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


        RequestQueue volleyQueue = Volley.newRequestQueue(MainActivity.this);

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
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }                },
                error -> {
                    if (error.networkResponse != null) {
                        Log.e("Response", "Error response code: " + error.networkResponse.statusCode);
                        Log.e("Response", "Error response data: " + new String(error.networkResponse.data));
                    }

                    Toast.makeText(MainActivity.this, "Some error occurred! Cannot fetch response", Toast.LENGTH_LONG).show();
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
    public boolean launchSignUp(View view) {
        Intent i = new Intent(this, SignUpPage.class);
        startActivity(i);
        return true;
    }
}