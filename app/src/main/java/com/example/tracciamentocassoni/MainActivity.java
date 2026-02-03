package com.example.tracciamentocassoni;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Abilita il layout edge-to-edge
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_main);

        // Applica padding per evitare che i contenuti finiscano sotto status & navigation bar
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom
            );
            return insets;
        });

        // Trova i pulsanti dal layout
        Button btnAccept = findViewById(R.id.btnAcceptMember);
        Button btnUnload = findViewById(R.id.btnUnloadBoxes);

        // Al click, apri AcceptMemberActivity
        btnAccept.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AcceptMemberActivity.class);
            startActivity(intent);
        });

        // Al click, apri UnloadBoxesActivity
        btnUnload.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, UnloadBoxesActivity.class);
            startActivity(intent);
        });
    }
}
