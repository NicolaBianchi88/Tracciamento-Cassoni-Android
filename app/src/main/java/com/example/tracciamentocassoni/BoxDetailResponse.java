package com.example.tracciamentocassoni;

import com.google.gson.annotations.SerializedName;

public class BoxDetailResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private Data data;

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Data getData() { return data; }

    public static class Data {
        @SerializedName("rfid")
        private String rfid;

        @SerializedName("cassone_id")
        private String cassoneId;

        @SerializedName("socio")
        private String socio;

        @SerializedName("peso")
        private Double peso;

        @SerializedName("vitigno_codice")
        private String vitignoCodice;

        @SerializedName("vitigno_nome")
        private String vitignoNome;

        public String getRfid() { return rfid; }
        public String getCassone_id() { return cassoneId; }   // getter nome compatibile con uso attuale
        public String getCassoneId() { return cassoneId; }    // getter alternativo (comodo)
        public String getSocio() { return socio; }
        public Double getPeso() { return peso; }
        public String getVitigno_codice() { return vitignoCodice; }
        public String getVitigno_nome() { return vitignoNome; }
    }
}
