package com.example.capstone;

import static android.content.ContentValues.TAG;

import androidx.core.app.AppOpsManagerCompat;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    public static boolean isLoggedIn;

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
        }
    }

    public boolean logIn(View view) {
        TextView emailTextField = findViewById(R.id.LoginEmailAddress);
        TextView passwordTextField = findViewById(R.id.LoginPassword);
        String email = emailTextField.getText().toString();
        String password = passwordTextField.getText().toString();
        Intent i = new Intent(this, HomePage.class);

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
    public boolean launchSignUp(View view) {
        Intent i = new Intent(this, SignUpPage.class);
        startActivity(i);
        return true;
    }
}