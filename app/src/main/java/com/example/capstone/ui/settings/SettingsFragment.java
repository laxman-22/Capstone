package com.example.capstone.ui.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.example.capstone.HomePage;
import com.example.capstone.MainActivity;
import com.example.capstone.R;
import com.example.capstone.databinding.FragmentSettingsBinding;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SettingsFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private FragmentSettingsBinding binding;
    public static HashMap<String, String> authUsers;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SettingsViewModel settingsViewModel =
                new ViewModelProvider(this).get(SettingsViewModel.class);

        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        TextView text = root.findViewById(R.id.emailField);
        text.setText(MainActivity.email);
        MainActivity.settingsFragment = this;
        showAuthUsers();
        return root;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String selectedTime = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
    public void showAuthUsers() {
        if (this.authUsers == null) {
            return;
        }
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("Capstone", Context.MODE_PRIVATE);
        Map<String, ?> allEntries = sharedPreferences.getAll();

        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            Log.d("SharedPreferences", "Key: " + key + ", Value: " + value);
            Log.d("mesage", entry.toString());

            LinearLayout linearLayout = binding.getRoot().findViewById(R.id.authorizedUsersList);
            RelativeLayout relativeLayout = new RelativeLayout(requireContext());
            RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT
            );
            relativeParams.setMargins(0, dpToPixels(20), 0, 0);
            relativeLayout.setLayoutParams(relativeParams);

            ImageView imageView = new ImageView(requireContext());
            RelativeLayout.LayoutParams imageParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    dpToPixels(125)
            );
            imageView.setPadding(dpToPixels(10), 0, dpToPixels(10), 0);
            imageView.setImageResource(R.drawable.gray_rectangle);
            imageView.setLayoutParams(imageParams);

            TextView name = new TextView(requireContext());
            RelativeLayout.LayoutParams nameParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );
            nameParams.alignWithParent = true;
            nameParams.setMargins(dpToPixels(50), dpToPixels(20),  0, 0);
            name.setText(value.toString());
            name.setTextSize(25f);
            name.setLayoutParams(nameParams);

            TextView emailText = new TextView(requireContext());
            RelativeLayout.LayoutParams emailParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );
            emailText.setLayoutParams(emailParams);
            emailParams.setMargins(dpToPixels(20), dpToPixels(70), 0, 0);
            emailText.setTextSize(18f);
            emailText.setText(key);

            Button button = new Button(requireContext());
            RelativeLayout.LayoutParams buttonParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );
            buttonParams.addRule(RelativeLayout.CENTER_VERTICAL);
            buttonParams.setMargins(dpToPixels(290), 0, 0, dpToPixels(25));
            button.setText("Remove");
            button.setTextSize(15f);
            button.setLayoutParams(buttonParams);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeAuthorizedUser(v, entry.getKey(), linearLayout, relativeLayout);
                }
            });

            relativeLayout.addView(imageView);
            relativeLayout.addView(name);
            relativeLayout.addView(emailText);
            relativeLayout.addView(button);

            linearLayout.addView(relativeLayout);
        }
    }
    public int dpToPixels(int dp) {
        float density = getResources().getDisplayMetrics().density;
        int pixelHeight = (int) (dp * density);
        return pixelHeight;
    }
    public void removeAuthorizedUser(View view, String email, LinearLayout linearLayout, RelativeLayout relativeLayout) {
        this.authUsers.remove(email);
        linearLayout.removeView(relativeLayout);
        removeAuthUser(email);
        //        try {
//            UserRecord userRecord = firebaseAuth.getUserByEmail(userEmail);
//            String uid = userRecord.getUid();
//            System.out.println("UID for email " + userEmail + ": " + uid);
//        } catch (FirebaseAuthException e) {
//            System.err.println("Error getting user by email: " + e.getMessage());
//        }
    }
    public void removeAuthUser(String email) {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("Capstone", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(email);
    }

}
