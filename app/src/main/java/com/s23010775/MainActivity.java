package com.s23010775;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText username = findViewById(R.id.edit_username);
        EditText password = findViewById(R.id.edit_password);
        Button loginBtn = findViewById(R.id.button_login);
        DBHelper dbHelper = new DBHelper(this);

        loginBtn.setOnClickListener(v -> {
            String user = username.getText().toString();
            String pass = password.getText().toString();
            if (!user.isEmpty() && !pass.isEmpty()) {
                boolean inserted = dbHelper.insertLogin(user, pass);
                if (inserted) {
                    Toast.makeText(this, "Login saved!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, MapActivity.class));
                } else {
                    Toast.makeText(this, "Error saving login!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Enter username and password", Toast.LENGTH_SHORT).show();
            }
        });
    }
}