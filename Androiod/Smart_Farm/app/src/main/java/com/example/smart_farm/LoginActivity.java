package com.example.smart_farm;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    TextView input_password, input_id;

    ImageButton login_button, register_button;

    Button test_login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);

        input_password = findViewById(R.id.input_password);
        input_id = findViewById(R.id.input_id);
        login_button = findViewById(R.id.login_button);
        register_button = findViewById(R.id.go_register_button);

        test_login = findViewById(R.id.test_login);

        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String id = input_id.getText().toString();
                String password = input_password.getText().toString();
                Toast.makeText(LoginActivity.this, ""+id+","+password, Toast.LENGTH_SHORT).show();
            }
        });

        register_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });
        test_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}