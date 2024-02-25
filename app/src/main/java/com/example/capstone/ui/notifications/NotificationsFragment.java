package com.example.capstone.ui.notifications;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.capstone.R;
import com.example.capstone.databinding.FragmentNotificationsBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        NotificationsViewModel notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        createNotification("Fall Detected!", "A fall has been detected, notifying emergency contacts.");
//        createNotification("Abnormal Pulse Detected!", "Contact your health provider.");
//        createNotification("Low Oxygen Sat. Detected!", "Contact your health provider.");
//
//        createNotification("Fall Detected", "A fall has been detected, notifying emergency contacts.");
//        createNotification("Abnormal Pulse Detected!", "Contact your health provider.");
//        createNotification("Low Oxygen Sat. Detected!", "Contact your health provider.");
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
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

    private int dpToPixels(int dp) {
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