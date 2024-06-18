package com.example.capstone.ui.notifications;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.capstone.MainActivity;
import com.example.capstone.R;
import com.example.capstone.SignUpPage;
import com.example.capstone.databinding.FragmentNotificationsBinding;
import com.example.capstone.ui.home.HomeFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;
    private int notif_id;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        NotificationsViewModel notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    @Override
    public void onResume() {
        super.onResume();
        Handler handler = new Handler();
        Runnable refreshNotifications = new Runnable() {
            @Override
            public void run() {
                checkNotifications();
                handler.postDelayed(this, 5000);
            }
        };
        handler.postDelayed(refreshNotifications,5000);
    }

    public void checkNotifications() {
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

        String url = "https://78.138.17.29:3000/getNotif";
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
                            int fallDetected = userObject.getInt("fall_detected");
                            int lowPulse = userObject.getInt("low_pulse");
                            int lowOxSat= userObject.getInt("low_ox_sat");
                            int sent= userObject.getInt("sent");
                            notif_id = userObject.getInt("notif_id");

                            if (fallDetected == 1 && sent == 0) {
                                createNotification("Fall Detected!", "A fall has been detected, notifying emergency contacts.");
                            } if (lowPulse == 1 && sent == 0) {
                                createNotification("Abnormal Pulse Detected!", "Contact your health provider.");
                            } if (lowOxSat == 1 && sent == 0) {
                                createNotification("Low Oxygen Sat. Detected!", "Contact your health provider.");
                            }
                            updateStatus();

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
    private void updateStatus() {
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

        String url = "https://78.138.17.29:3000/setStatus";
        JSONObject jsonObject = new JSONObject();
        Log.d("balls", notif_id +"");

        try {
            jsonObject.put("notif_id", notif_id);
            jsonObject.put("sent", 1);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        JsonObjectRequest newjsonObjectRequest = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                jsonObject,
                response -> {
                    },
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

    public void createNotification(String messageType, String messageDescription) {

        LinearLayout linearLayout = (LinearLayout) binding.getRoot().findViewById(R.id.linearLayout);
        RelativeLayout relativeLayout = new RelativeLayout(requireContext());
        RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                dpToPixels(100)
        );

        // Set layout parameters for the RelativeLayout
        relativeParams.setMargins(0, dpToPixels(22), 0, 0);  // Adjust margins as needed
        relativeLayout.setLayoutParams(relativeParams);

        // Create and configure ImageView
        ImageView imageView = new ImageView(requireContext());
        imageView.setImageResource(R.drawable.rectangle_28);  // Set your image resource
        RelativeLayout.LayoutParams imageParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                dpToPixels(100)

        );
        imageView.setLayoutParams(imageParams);

        ImageView notif = new ImageView(requireContext());
        notif.setImageResource(R.drawable.material_symbols_notifications);
        RelativeLayout.LayoutParams notifParams = new RelativeLayout.LayoutParams(
                dpToPixels(28),
                dpToPixels(27)
        );
        notifParams.setMargins(dpToPixels(335), dpToPixels(35), 0, 0);
        notif.setLayoutParams(notifParams);

        // Create and configure other views (TextViews)
        TextView titleTextView = new TextView(requireContext());
        RelativeLayout.LayoutParams textParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                dpToPixels(30)
        );
        textParams.setMargins(dpToPixels(20), dpToPixels(15), 0, 0);
        titleTextView.setLayoutParams(textParams);
        titleTextView.setText(messageType);
        titleTextView.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.varela_round));
        titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);

        TextView descriptionTextView = new TextView(requireContext());
        RelativeLayout.LayoutParams descriptionLayoutParams = new RelativeLayout.LayoutParams(
                dpToPixels(275),
                dpToPixels(40)
        );
        descriptionLayoutParams.setMargins(dpToPixels(20), dpToPixels(50), dpToPixels(114), dpToPixels(10));
        descriptionTextView.setLayoutParams(descriptionLayoutParams);
        descriptionTextView.setText(messageDescription);
        descriptionTextView.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.varela_round));
        descriptionTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);


        // Add views to the RelativeLayout
        relativeLayout.addView(imageView);
        relativeLayout.addView(titleTextView);
        relativeLayout.addView(descriptionTextView);
        relativeLayout.addView(notif);

        // Add the RelativeLayout to the LinearLayout
        linearLayout.addView(relativeLayout);
        Log.d("Notification attempt", "Notification attempt");

        sendNotification(messageType, messageDescription);
    }

    public int dpToPixels(int dp) {
        float density = getResources().getDisplayMetrics().density;
        int pixelHeight = (int) (dp * density);
        return pixelHeight;
    }

    private void sendNotification(String title, String notifDescription) {
        Log.d("Notification send attempt", "Notification send attempt");

        Context context = getContext().getApplicationContext();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "channel_id")
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                .setContentTitle(title)
                .setContentText(notifDescription)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(1, builder.build());
        Log.d("Notification Sent", "Notification Sent");

    }

}