package com.example.tracciamentocassoni;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tracciamentocassoni.data.network.DatabaseApi;
import com.example.tracciamentocassoni.data.network.RetrofitClient;
import com.rscja.deviceapi.RFIDWithUHFUART;
import com.rscja.deviceapi.entity.UHFTAGInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UnloadBoxesActivity extends AppCompatActivity {

    enum State { IDLE, BOX_LOCKED, DEST_DETECTING, DEST_LOCKED, CONFIRMING, UPDATING, DONE, ERROR }

    private TextView textStatus, textBox, textGarolla;
    private Button btnCancel, btnUnloaded;

    private int currentCassoneId = -1;
    private State state = State.IDLE;
    private String currentBoxTag = null, currentBoxVineCode = null, currentDestTag = null;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private boolean testMode;
    private RFIDWithUHFUART rfidReader;
    private Thread rfidThread;
    private DatabaseApi databaseApi;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unload_boxes);

        textStatus  = findViewById(R.id.text_status);
        textBox     = findViewById(R.id.text_box1);
        textGarolla = findViewById(R.id.text_box2);
        btnCancel   = findViewById(R.id.button_cancel);
        btnUnloaded = findViewById(R.id.button_unloaded);

        btnCancel.setOnClickListener(v -> resetFlow());
        btnUnloaded.setOnClickListener(v -> onClickUnloaded());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() {
                if (state == State.UPDATING || state == State.CONFIRMING) {
                    Toast.makeText(UnloadBoxesActivity.this, "Operazione in corso...", Toast.LENGTH_SHORT).show();
                } else {
                    finish();
                }
            }
        });

        databaseApi = RetrofitClient.getClient().create(DatabaseApi.class);
        testMode = getResources().getBoolean(R.bool.test_mode);

        try {
            rfidReader = RFIDWithUHFUART.getInstance();
            if (rfidReader != null && rfidReader.init(this)) {
                rfidReader.setPower(30);
                startRfidReadingLoop();
            } else {
                Toast.makeText(this, "Lettore RFID non inizializzato", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Modulo RFID non disponibile", Toast.LENGTH_SHORT).show();
        }

        setState(State.IDLE);
    }

    private void startRfidReadingLoop() {
        rfidThread = new Thread(() -> {
            String strongestTag = null;
            int bestRssi = Integer.MIN_VALUE;
            while (!Thread.currentThread().isInterrupted()) {
                UHFTAGInfo tag = rfidReader.readTagFromBuffer();
                if (tag != null) {
                    int rssi = Integer.MIN_VALUE;
                    try {
                        rssi = Integer.parseInt(tag.getRssi());
                    } catch (NumberFormatException e) {
                        rssi = Integer.MIN_VALUE; // fallback in caso di errore
                    }
                    if (rssi > bestRssi) {
                        bestRssi = rssi;
                        strongestTag = tag.getEPC();
                        if (!TextUtils.isEmpty(strongestTag)) {
                            String finalTag = strongestTag;
                            runOnUiThread(() -> onTagRead(finalTag));
                        }
                    }
                }
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        rfidThread.start();
    }

    private void setState(State newState) {
        state = newState;
        updateUiForState();
        Log.d("Unload", "State -> " + state);
    }

    private void updateUiForState() {
        textStatus.setText("Stato: " + state.name());
        switch (state) {
            case IDLE:
                textBox.setText("Cassone: —");
                textGarolla.setText("Garolla: —");
                currentBoxTag = null;
                currentBoxVineCode = null;
                currentDestTag = null;
                currentCassoneId = -1;
                btnUnloaded.setEnabled(false);
                btnCancel.setEnabled(true);
                break;
            case BOX_LOCKED:
                textGarolla.setText("Garolla: rilevando...");
                btnUnloaded.setEnabled(false);
                break;
            case DEST_DETECTING:
                btnUnloaded.setEnabled(false);
                break;
            case DEST_LOCKED:
                btnUnloaded.setEnabled(currentBoxTag != null && currentDestTag != null);
                break;
            case CONFIRMING:
            case UPDATING:
                btnUnloaded.setEnabled(false);
                btnCancel.setEnabled(false);
                break;
            case DONE:
            case ERROR:
                btnUnloaded.setEnabled(false);
                btnCancel.setEnabled(true);
                break;
        }
    }
    private void resetFlow() {
        setState(State.IDLE);
    }

    private void onTagRead(String rawTag) {
        String tag = normalize(rawTag);
        if (TextUtils.isEmpty(tag)) return;
        if (state == State.IDLE) {
            lockBox(tag);
        }
    }

    private void lockBox(String tag) {
        currentBoxTag = tag;
        textBox.setText("Cassone: " + tag + " (carico dati...)");
        setState(State.BOX_LOCKED);
        fetchBoxDetail(tag);
        setState(State.DEST_DETECTING);
    }

    private void simulateGarollaApi(String destinazione) {
        currentDestTag = destinazione;
        textGarolla.setText("Garolla: " + destinazione + " (stabile)");
        setState(State.DEST_LOCKED);
    }

    private void fetchBoxDetail(String rfidRaw) {
        final String rfid = normalizeRfidForApi(rfidRaw);
        databaseApi.getBoxDetail(rfid).enqueue(new Callback<BoxDetailResponse>() {
            @Override
            public void onResponse(Call<BoxDetailResponse> call, Response<BoxDetailResponse> resp) {
                if (resp.isSuccessful() && resp.body() != null && resp.body().isSuccess()) {
                    BoxDetailResponse.Data d = resp.body().getData();
                    currentBoxVineCode = d.getVitigno_codice();
                    try {
                        currentCassoneId = Integer.parseInt(d.getCassone_id());
                    } catch (NumberFormatException e) {
                        currentCassoneId = -1;
                    }
                    String vitigno = d.getVitigno_nome() != null ? d.getVitigno_nome() : d.getVitigno_codice();
                    String peso = d.getPeso() != null ? d.getPeso() + " kg" : "—";
                    String socio = d.getSocio() != null ? d.getSocio() : "—";
                    String cassoneId = d.getCassone_id() != null ? d.getCassone_id() : rfid;
                    textBox.setText("Cassone: " + cassoneId + "\nVitigno: " + vitigno + "\nSocio: " + socio + "\nPeso: " + peso);

                    handler.postDelayed(() -> simulateGarollaApi("Garolla 4"), 800);

                } else {
                    textBox.setText("Cassone: " + rfid + "\n(dati non disponibili)");
                }
            }

            @Override
            public void onFailure(Call<BoxDetailResponse> call, Throwable t) {
                textBox.setText("Cassone: " + rfid + "\n(errore rete)");
            }
        });
    }

    private void onClickUnloaded() {
        if (currentBoxTag == null || currentDestTag == null) {
            Toast.makeText(this, "Dati incompleti", Toast.LENGTH_SHORT).show();
            return;
        }
        setState(State.CONFIRMING);
        new AlertDialog.Builder(this)
                .setTitle("Confermare lo scarico?")
                .setMessage("Cassone: " + currentBoxTag + "\nGarolla: " + currentDestTag)
                .setPositiveButton("Conferma", (dialog, which) -> proceedConfirmUnload())
                .setNegativeButton("Annulla", (dialog, which) -> setState(State.DEST_LOCKED))
                .setOnCancelListener(d -> setState(State.DEST_LOCKED))
                .show();
    }

    private void proceedConfirmUnload() {
        if (currentCassoneId <= 0 || currentDestTag == null) {
            Toast.makeText(this, "Dati cassone o garolla non validi", Toast.LENGTH_LONG).show();
            setState(State.ERROR);
            return;
        }
        Integer cavity = extractCavityNumber(currentDestTag);
        if (cavity == null || cavity <= 0) {
            Toast.makeText(this, "Numero garolla non valido", Toast.LENGTH_LONG).show();
            setState(State.ERROR);
            return;
        }
        setState(State.UPDATING);
        databaseApi.unloadBox(currentCassoneId, cavity).enqueue(new Callback<UnloadResponse>() {
            @Override
            public void onResponse(Call<UnloadResponse> call, Response<UnloadResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(UnloadBoxesActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    setState(State.DONE);
                    handler.postDelayed(UnloadBoxesActivity.this::resetFlow, 2000);
                } else {
                    Toast.makeText(UnloadBoxesActivity.this, "Errore scarico: " + (response.body() != null ? response.body().getMessage() : "Errore server"), Toast.LENGTH_LONG).show();
                    setState(State.ERROR);
                }
            }

            @Override
            public void onFailure(Call<UnloadResponse> call, Throwable t) {
                Toast.makeText(UnloadBoxesActivity.this, "Errore rete: " + t.getMessage(), Toast.LENGTH_LONG).show();
                setState(State.ERROR);
            }
        });
    }

    private String normalize(String raw) {
        if (raw == null) return null;
        return raw.trim();
    }

    private String normalizeRfidForApi(String raw) {
        if (raw == null) return null;
        String t = raw.trim();
        if (t.startsWith("BOX-")) t = t.substring(4);
        if (t.startsWith("GAR-")) t = t.substring(4);
        return t.toUpperCase();
    }

    private Integer extractCavityNumber(String destTag) {
        if (destTag == null) return null;
        Matcher m = Pattern.compile("(\\d+)").matcher(destTag);
        Integer last = null;
        while (m.find()) last = Integer.parseInt(m.group(1));
        return last;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (rfidThread != null && rfidThread.isAlive()) {
            rfidThread.interrupt();
        }
        if (rfidReader != null) {
            rfidReader.stopInventory();
            rfidReader.free();
        }
    }
}
