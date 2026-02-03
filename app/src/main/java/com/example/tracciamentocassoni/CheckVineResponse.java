package com.example.tracciamentocassoni;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CheckVineResponse {
    @SerializedName("success")       private boolean success;
    @SerializedName("hasFilter")     private boolean hasFilter;
    @SerializedName("matches")       private boolean matches;
    @SerializedName("allowed_vines") private List<String> allowedVines;
    @SerializedName("message")       private String message;

    public boolean isSuccess() { return success; }
    public boolean isHasFilter() { return hasFilter; }
    public boolean isMatches() { return matches; }
    public List<String> getAllowedVines() { return allowedVines; }
    public String getMessage() { return message; }
}
