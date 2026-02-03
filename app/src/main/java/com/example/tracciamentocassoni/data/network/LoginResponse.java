package com.example.tracciamentocassoni.data.network;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    @SerializedName("success")
    public boolean success;

    @SerializedName("message")
    public String message;

    @SerializedName("user")
    public User user;

    public static class User {
        @SerializedName("id")
        public String id;

        @SerializedName("name")
        public String name;
    }
}
