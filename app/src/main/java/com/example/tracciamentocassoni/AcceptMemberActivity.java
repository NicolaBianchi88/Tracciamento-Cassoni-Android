package com.example.tracciamentocassoni;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tracciamentocassoni.data.network.DatabaseApi;
import com.example.tracciamentocassoni.data.network.RetrofitClient;
import com.rscja.deviceapi.RFIDWithUHFUART;
import com.rscja.deviceapi.entity.UHFTAGInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AcceptMemberActivity extends AppCompatActivity {

    private ProgressBar scanningProgressBar;
    private TextView textMemberCode, textMemberName, textMemberSurname, textError;
    private Button buttonConfirmRead, buttonManualRescan;
    private RecyclerView recyclerCassoni;
    private CassoniAdapter cassoniAdapter;
    private DatabaseApi databaseApi;

    private boolean memberLoaded = false;
    private boolean cassoniLoaded = false;
    private String memberId;

    private NfcAdapter nfcAdapter;
    private RFIDWithUHFUART rfidReader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accept_member);

        scanningProgressBar = findViewById(R.id.scanningProgressBar);
        textMemberCode = findViewById(R.id.textMemberCode);
        textMemberName = findViewById(R.id.textMemberName);
        textMemberSurname = findViewById(R.id.textMemberSurname);
        textError = findViewById(R.id.textError);
        recyclerCassoni = findViewById(R.id.recyclerCassoni);
        buttonConfirmRead = findViewById(R.id.buttonConfirmRead);
        buttonManualRescan = findViewById(R.id.buttonManualRescan);

        recyclerCassoni.setLayoutManager(new GridLayoutManager(this, 3));
        cassoniAdapter = new CassoniAdapter();
        recyclerCassoni.setAdapter(cassoniAdapter);
        recyclerCassoni.setVisibility(View.GONE);

        buttonConfirmRead.setEnabled(false);
        buttonConfirmRead.setOnClickListener(v -> onConfirmRead());
        buttonManualRescan.setOnClickListener(v -> onManualRescan());

        databaseApi = RetrofitClient.getClient().create(DatabaseApi.class);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC non disponibile su questo dispositivo", Toast.LENGTH_LONG).show();
        } else if (!nfcAdapter.isEnabled()) {
            Toast.makeText(this, "NFC disattivato, attivalo nelle impostazioni", Toast.LENGTH_LONG).show();
            startActivity(new Intent(android.provider.Settings.ACTION_NFC_SETTINGS));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nfcAdapter != null) {
            Intent intent = new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            IntentFilter[] filters = new IntentFilter[]{ new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED) };
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, filters, null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null && NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (tag != null && tag.getId() != null) {
                String tagId = bytesToHex(tag.getId());
                handleRfidRead(tagId);
            }
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    private void handleRfidRead(String tag) {
        textMemberCode.setText("RFID: " + tag);
        textMemberCode.setVisibility(View.VISIBLE);

        databaseApi.getMemberIdByRfid(tag).enqueue(new Callback<RfidMemberResponse>() {
            @Override
            public void onResponse(Call<RfidMemberResponse> call, Response<RfidMemberResponse> resp) {
                if (resp.isSuccessful() && resp.body() != null && resp.body().isSuccess()) {
                    loadMemberDetails(resp.body().getMembersId());
                } else {
                    showMemberError();
                }
            }
            @Override
            public void onFailure(Call<RfidMemberResponse> call, Throwable t) {
                showMemberError();
            }
        });
    }

    private void loadMemberDetails(String memberId) {
        this.memberId = memberId;
        databaseApi.getMemberDetail(memberId).enqueue(new Callback<MemberResponse>() {
            @Override
            public void onResponse(Call<MemberResponse> call, Response<MemberResponse> resp) {
                if (resp.isSuccessful() && resp.body() != null && resp.body().isSuccess()) {
                    MemberResponse.Member m = resp.body().getMember();
                    showMemberInfo(m.getNome(), m.getCognome());
                    loadCassoni();
                } else {
                    showMemberError();
                }
            }
            @Override
            public void onFailure(Call<MemberResponse> call, Throwable t) {
                showMemberError();
            }
        });
    }

    private void loadCassoni() {
        databaseApi.getRfidList().enqueue(new Callback<RfidListResponse>() {
            @Override
            public void onResponse(Call<RfidListResponse> call, Response<RfidListResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<String> list = response.body().getRfidList();
                    if (list != null && !list.isEmpty()) {
                        cassoniAdapter.setCassoni(list);
                        recyclerCassoni.setVisibility(View.VISIBLE);
                        cassoniLoaded = true;
                    } else {
                        recyclerCassoni.setVisibility(View.GONE);
                        cassoniLoaded = false;
                    }
                } else {
                    recyclerCassoni.setVisibility(View.GONE);
                    cassoniLoaded = false;
                }
                updateActionsState();
            }
            @Override
            public void onFailure(Call<RfidListResponse> call, Throwable t) {
                recyclerCassoni.setVisibility(View.GONE);
                cassoniLoaded = false;
                updateActionsState();
            }
        });
    }

    private void updateActionsState() {
        buttonConfirmRead.setEnabled(memberLoaded && cassoniLoaded);
    }

    private void showMemberInfo(String name, String surname) {
        textError.setVisibility(View.GONE);
        textMemberName.setText("Nome: " + name);
        textMemberSurname.setText("Cognome: " + surname);
        textMemberName.setVisibility(View.VISIBLE);
        textMemberSurname.setVisibility(View.VISIBLE);
        memberLoaded = true;
        updateActionsState();
    }

    private void showMemberError() {
        textMemberName.setVisibility(View.GONE);
        textMemberSurname.setVisibility(View.GONE);
        recyclerCassoni.setVisibility(View.GONE);
        textError.setText("Errore: socio non trovato");
        textError.setVisibility(View.VISIBLE);
        memberLoaded = false;
        cassoniLoaded = false;
        updateActionsState();
    }

    private void onConfirmRead() {
        Set<String> selezionati = cassoniAdapter.getSelectedCassoni();
        List<String> vuoti = new ArrayList<>(selezionati);
        List<String> pieni = new ArrayList<>();
        for (String c : cassoniAdapter.getAllCassoni()) {
            if (!selezionati.contains(c)) pieni.add(c);
        }

        String vuotiCsv = android.text.TextUtils.join(",", vuoti);
        String pieniCsv = android.text.TextUtils.join(",", pieni);

        databaseApi.confirmCassoniForm(this.memberId, vuotiCsv, pieniCsv)
                .enqueue(new Callback<ConfirmBoxResponse>() {
                    @Override
                    public void onResponse(Call<ConfirmBoxResponse> call, Response<ConfirmBoxResponse> resp) {
                        if (resp.isSuccessful() && resp.body() != null && resp.body().isSuccess()) {
                            Toast.makeText(AcceptMemberActivity.this, "Cassoni aggiornati", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            String msg = (resp.body()!=null ? resp.body().getMessage() : ("HTTP " + resp.code()));
                            Toast.makeText(AcceptMemberActivity.this, "Errore: " + msg, Toast.LENGTH_LONG).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<ConfirmBoxResponse> call, Throwable t) {
                        Toast.makeText(AcceptMemberActivity.this, "Errore rete: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void onManualRescan() {
        if (rfidReader == null) {
            try {
                rfidReader = RFIDWithUHFUART.getInstance();
            } catch (Exception e) {
                Toast.makeText(this, "Errore inizializzazione RFID: " + e.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }
        }
        try {
            if (rfidReader.init(this)) {
                if (nfcAdapter != null) nfcAdapter.disableForegroundDispatch(this);

                UHFTAGInfo tagInfo = rfidReader.inventorySingleTag();
                if (tagInfo != null && tagInfo.getEPC() != null) {
                    String tagId = tagInfo.getEPC();
                    handleRfidRead(tagId);
                } else {
                    Toast.makeText(this, "Nessun tag UHF rilevato, riprova.", Toast.LENGTH_SHORT).show();
                }

                rfidReader.free();

                if (nfcAdapter != null) {
                    Intent intent = new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                    IntentFilter[] filters = new IntentFilter[]{ new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED) };
                    nfcAdapter.enableForegroundDispatch(this, pendingIntent, filters, null);
                }
            } else {
                Toast.makeText(this, "Errore inizializzazione lettore UHF.", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lettura UHF fallita: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
