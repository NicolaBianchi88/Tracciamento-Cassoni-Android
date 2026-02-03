package com.example.tracciamentocassoni;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class RfidListResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("rfid_list")   // <-- nome campo del tuo JSON
    private List<String> rfidList;

    public boolean isSuccess() { return success; }
    public List<String> getRfidList() { return rfidList; }
}
