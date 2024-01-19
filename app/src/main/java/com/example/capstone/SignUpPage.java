package com.example.capstone;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.ActionBar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignUpPage extends AppCompatActivity {
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_page);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Sign Up");
        mAuth = FirebaseAuth.getInstance();

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        return super.onOptionsItemSelected(item);
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
                        }
                        else {
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignUpPage.this, "Sign Up Failed", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}