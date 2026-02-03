package com.example.tracciamentocassoni;

import java.util.List;

public class ConfirmCassoniRequest {
    private String member_id;
    private List<String> vuoti;
    private List<String> pieni;

    public ConfirmCassoniRequest(String member_id, List<String> vuoti, List<String> pieni) {
        this.member_id = member_id;
        this.vuoti = vuoti;
        this.pieni = pieni;
    }

    public String getMember_id() {
        return member_id;
    }

    public List<String> getVuoti() {
        return vuoti;
    }

    public List<String> getPieni() {
        return pieni;
    }
}
