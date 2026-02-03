package com.example.tracciamentocassoni;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.tracciamentocassoni.SessionManager;

import com.example.tracciamentocassoni.data.network.DatabaseApi;
import com.example.tracciamentocassoni.data.network.LoginResponse;
import com.example.tracciamentocassoni.data.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        EditText etUser = findViewById(R.id.etUsername);
        EditText etPass = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> {
            String user = etUser.getText().toString().trim();
            String pass = etPass.getText().toString().trim();

            if (TextUtils.isEmpty(user) || TextUtils.isEmpty(pass)) {
                Toast.makeText(this, "Inserisci username e password", Toast.LENGTH_SHORT).show();
                return;
            }

            /*
            // Blocco login cablato disattivato
            if (user.equals("admin") && pass.equals("admin")) {
                Toast.makeText(this, "Login bypass riuscito", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Credenziali non valide", Toast.LENGTH_LONG).show();
            }
            */

            // Blocco API
            Toast.makeText(this, "Verifico credenziali...", Toast.LENGTH_SHORT).show();
            DatabaseApi api = RetrofitClient.getClient().create(DatabaseApi.class);
            api.checkLogin(user, pass).enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> resp) {
                    if (resp.isSuccessful() && resp.body() != null) {
                        LoginResponse body = resp.body();
                        if (body.success) {
                            new SessionManager(LoginActivity.this).markLoginNow();
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "Login fallito: " + body.message, Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Errore server: " + resp.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    Toast.makeText(LoginActivity.this, "Errore di rete: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

        });
    }
}
