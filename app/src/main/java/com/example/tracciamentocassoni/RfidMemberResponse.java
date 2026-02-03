package com.example.tracciamentocassoni;

import com.google.gson.annotations.SerializedName;

public class RfidMemberResponse {
    private boolean success;
    private String members_id;
    private String error;

    public boolean isSuccess() { return success; }
    public String getMembersId() { return members_id; }
    public String getError() { return error; }
}
