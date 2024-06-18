package com.example.capstone;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.ActionBar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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

public class SignUpPage extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private FirebaseAuth mAuth;

    private Spinner spinnerSex;
    private ArrayAdapter<CharSequence> sexAdapter;

    public static String sex;
    public static float weight;
    public static int age;

    public static String wifiPassword;
    public static String apiKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_page);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Sign Up");
        mAuth = FirebaseAuth.getInstance();
        spinnerSex = findViewById(R.id.signUpSexSpinner);
        sexAdapter = ArrayAdapter.createFromResource(this, R.array.sex_options, android.R.layout.simple_spinner_item);
        sexAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSex.setAdapter(sexAdapter);
        spinnerSex.setOnItemSelectedListener(this);

    }




    public void signUp(View view) {
        // send to firebase to sign up
        TextView emailTextField = findViewById(R.id.signUpEmail);
        TextView passwordTextField = findViewById(R.id.signUpPassword);
        String email = emailTextField.getText().toString();
        String password = passwordTextField.getText().toString();
        Intent i = new Intent(this, MainActivity.class);
        // add checks for email and passwords for formats

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            // send back to login page
                            startActivity(i);
                            Toast.makeText(SignUpPage.this, "Signed Up Successfully", Toast.LENGTH_LONG).show();
                            EditText weightText = findViewById(R.id.signUpWeight);
                            weight = Float.parseFloat(weightText.getText().toString());
                            EditText ageText = findViewById(R.id.signUpAge);
                            age = Integer.parseInt(ageText.getText().toString());
                            EditText wifiPassText = findViewById(R.id.wifiPassword);
                            String wifiPass = wifiPassText.getText().toString();
                            wifiPassword = wifiPass;
                            Log.d("Details", weight + " : " + age + " : " + sex + " : " + wifiPassword);
                            MainActivity.age = age;
                            MainActivity.weight = weight;
                            MainActivity.sex = sex;
                            MainActivity.wifiPass = wifiPassword;
                            // register device and get api key
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

                            RequestQueue volleyQueue = Volley.newRequestQueue(SignUpPage.this);

                            String url = "https://78.138.17.29:3000/register";
                            JSONObject jsonObject = new JSONObject();
                            try {
                                jsonObject.put("user_email", email);
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                                    Request.Method.POST,
                                    url,
                                    jsonObject,

                                    response -> {
                                        try {
                                            if (response.has("apiKey")) {
                                                Log.d("Response", response.getString("apiKey"));
                                                SignUpPage.apiKey = response.getString("apiKey");
                                                SharedPreferences sharedPreferences = getSharedPreferences("Capstone", Context.MODE_PRIVATE);
                                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                                editor.putString("apiKey", SignUpPage.apiKey);

                                                editor.apply();
                                                MainActivity.email = email;
                                                createUser();
                                            } else {
                                                Log.d("Response", "No message key found in JSON response");
                                            }
                                        } catch (JSONException e) {
                                            throw new RuntimeException(e);
                                        }

                                    },
                                    // when the HTTP request fails
                                    (Response.ErrorListener) error -> {

                                        Toast.makeText(SignUpPage.this, "Some error occurred! Cannot fetch these balls", Toast.LENGTH_LONG).show();
                                        // log the error message in the error stream
                                        Log.e("SignUpPage", "Error: " + error.getMessage(), error);
                                    }

                            );
                            // add the json request object created above
                            // to the Volley request queue
                            volleyQueue.add(jsonObjectRequest);

                        }
                        else {
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignUpPage.this, "Sign Up Failed", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
    private void createUser() {
        Log.d("apiKey", SignUpPage.apiKey);

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


        RequestQueue volleyQueue = Volley.newRequestQueue(SignUpPage.this);

        String createurl = "https://78.138.17.29:3000/createUser";
        JSONObject createjsonObject = new JSONObject();
        try {
            createjsonObject.put("is_primary", 1);
            createjsonObject.put("is_registered", 0);
            createjsonObject.put("is_authorized", 0);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        JsonObjectRequest newjsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                createurl,
                createjsonObject,
                response -> {
                    Log.d("Response", "Raw response: " + response.toString());

                },
                error -> {
                    if (error.networkResponse != null) {
                        Log.e("Response", "Error response code: " + error.networkResponse.statusCode);
                        Log.e("Response", "Error response data: " + new String(error.networkResponse.data));
                    }

                    Toast.makeText(SignUpPage.this, "Some error occurred! Cannot fetch response", Toast.LENGTH_LONG).show();
                    Log.e("SignUpPage", "Error: " + error.getMessage(), error);
                }
        ) { @Override
        public Map<String, String> getHeaders() throws AuthFailureError {
            Map<String, String> headers = new HashMap<>();
            headers.put("x-api-key", SignUpPage.apiKey.toString());

            // Add other headers if needed
            return headers;
        }
        };
        volleyQueue.add(newjsonObjectRequest);
    }
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String selectedSex = spinnerSex.getSelectedItem().toString();
        sex = selectedSex;
        Log.d("Selected Item", selectedSex);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        String selectedSex = spinnerSex.getSelectedItem().toString();
        sex = selectedSex;
        Log.d("Selected Item", selectedSex);
    }
}