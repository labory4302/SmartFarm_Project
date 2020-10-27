package com.example.smart_farm;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    ImageButton register_button, check_id_button;
    TextView regit_id, regit_password, regit_password_check;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_page);

        register_button = findViewById(R.id.register_button);
        check_id_button = findViewById(R.id.check_id_button);
        regit_id = findViewById(R.id.regit_id);
        regit_password = findViewById(R.id.regit_password);
        regit_password_check = findViewById(R.id.regit_password_check);

        register_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String pass = regit_password.getText().toString();
                String passCheck = regit_password_check.getText().toString();
                if(pass.equals(passCheck)) {
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
                else{
                    Toast.makeText(RegisterActivity.this, "비밀번호 확인하세요", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
