package com.example.capstone;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
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
import com.example.capstone.ui.settings.SettingsFragment;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.Session;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class AddAuthorizedUserActivity extends AppCompatActivity {
    static int pass = 1234567;
    private static int authUserNum = 0;
    private static String name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_auth_user);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Add Authorized User");
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent i = new Intent(this, HomePage.class);
        startActivity(i);
        return super.onOptionsItemSelected(item);
    }
    public void addAuthorizedUserData(View view) {


        SettingsFragment settingsFragment = MainActivity.settingsFragment;
        TextView nameText = findViewById(R.id.Name);
        name = nameText.getText().toString();
        TextView email = findViewById(R.id.authUserEmail);
        Intent i = new Intent(this, HomePage.class);
        startActivity(i);
        if (settingsFragment.authUsers == null) {
            settingsFragment.authUsers = new HashMap<String, String>();
        }
        settingsFragment.authUsers.put(email.getText().toString(), nameText.getText().toString());

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(new Runnable() {
            @Override
            public void run() {
                sendEmail("Authorized User Temporary Password", "Email: " + email.getText().toString() + "\n" + "Temporary Password: " + Integer.toString(pass), email.getText().toString());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        //UI Thread work here
                    }
                });
            }
        });

    }
    public void sendEmail(String subject, String content, String emailAddress) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.createUserWithEmailAndPassword(emailAddress, Integer.toString(pass));
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


        RequestQueue volleyQueue = Volley.newRequestQueue(AddAuthorizedUserActivity.this);

        String createurl = "https://78.138.17.29:3000/createUser";
        JSONObject createjsonObject = new JSONObject();
        try {
            createjsonObject.put("user_email", emailAddress);
            createjsonObject.put("is_primary", 0);
            createjsonObject.put("is_registered", 0);
            createjsonObject.put("is_authorized", 1);
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

                    Toast.makeText(AddAuthorizedUserActivity.this, "Some error occurred! Cannot fetch response", Toast.LENGTH_LONG).show();
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

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com"); // Change this to your email provider's SMTP server
        props.put("mail.smtp.port", "587"); // Port may vary depending on your email provider

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                        return new javax.mail.PasswordAuthentication("noreplywhitnotifications@gmail.com", "rwzx nqmo magx naxr");
                    }
                });
        session.setDebug(true);
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress("noreplywhitnotifications@gmail.com"));
            message.setRecipients(MimeMessage.RecipientType.TO, InternetAddress.parse(emailAddress));
            message.setSubject(subject);
            message.setText(content);

            Transport.send(message);

            System.out.println("Email sent successfully!");

            SharedPreferences sharedPreferences = getSharedPreferences("Capstone", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(emailAddress, name);
            editor.apply();
            authUserNum++;

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

}


