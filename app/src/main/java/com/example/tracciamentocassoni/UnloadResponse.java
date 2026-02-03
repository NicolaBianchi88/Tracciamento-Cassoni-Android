// UnloadResponse.java (nuova classe nel package com.example.tracciamentocassoni)
package com.example.tracciamentocassoni;

import com.google.gson.annotations.SerializedName;

public class UnloadResponse {
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
        @SerializedName("cass_id")
        private int cassId;
        @SerializedName("prev_confertrace_id")
        private int prevConfertraceId;
        @SerializedName("garolla")
        private int garolla;

        public int getCassId() { return cassId; }
        public int getPrevConfertraceId() { return prevConfertraceId; }
        public int getGarolla() { return garolla; }
    }
}
