package com.example.fithub360.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fithub360.R;
import com.example.fithub360.database.DatabaseHelper;
import com.example.fithub360.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText usernameEditText;
    private TextInputEditText passwordEditText;
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializar componentes
        databaseHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        // Forzar que cada arranque requiera autenticación: limpiar cualquier sesión previa
        sessionManager.logout();

        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        MaterialButton loginButton = findViewById(R.id.loginButton);
        MaterialButton registerButton = findViewById(R.id.registerButton);

        loginButton.setOnClickListener(v -> attemptLogin());
        registerButton.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void attemptLogin() {
        String username = String.valueOf(usernameEditText.getText());
        String password = String.valueOf(passwordEditText.getText());

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (databaseHelper.checkUser(username, password)) {
            // Marcar sesión activa para la ejecución actual
            sessionManager.setLogin(true, username);
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
        }
    }
}
