package com.example.esports_arena;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        Button loginBtn = findViewById(R.id.homeLoginButton);
        Button signupBtn = findViewById(R.id.homeSignupButton);

        loginBtn.setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));
        signupBtn.setOnClickListener(v -> startActivity(new Intent(this, SignupActivity.class)));
    }
}
