// MemberResponse.java
package com.example.tracciamentocassoni;

import com.google.gson.annotations.SerializedName;

public class MemberResponse {
    private boolean success;
    private Member member;
    private String error;

    public boolean isSuccess() { return success; }
    public Member getMember() { return member; }
    public String getError() { return error; }

    public static class Member {
        private String members_id;
        private String nome;
        private String cognome;

        public String getMembersId() { return members_id; }
        public String getNome() { return nome; }
        public String getCognome() { return cognome; }
    }
}

