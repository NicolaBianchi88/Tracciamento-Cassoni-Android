package com.example.tracciamentocassoni.data.network;

import com.example.tracciamentocassoni.data.network.LoginResponse;
import com.example.tracciamentocassoni.RfidMemberResponse;
import com.example.tracciamentocassoni.MemberResponse;
import com.example.tracciamentocassoni.RfidListResponse;
import com.example.tracciamentocassoni.BoxDetailResponse;
import com.example.tracciamentocassoni.CheckVineResponse;
import com.example.tracciamentocassoni.UnloadResponse;
import com.example.tracciamentocassoni.ConfirmBoxResponse;
import com.example.tracciamentocassoni.ConfirmCassoniRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface DatabaseApi {

    @FormUrlEncoded
    @POST("check_login.php")
    Call<LoginResponse> checkLogin(
            @Field("username") String username,
            @Field("password") String password
    );

    @FormUrlEncoded
    @POST("get_member_by_rfid.php")
    Call<RfidMemberResponse> getMemberIdByRfid(@Field("rfid") String rfid);

    @FormUrlEncoded
    @POST("get_member_detail.php")
    Call<MemberResponse> getMemberDetail(@Field("members_id") String membersId);
    //  cassoni: niente FormUrlEncoded,  field

    @FormUrlEncoded
    @POST("unload_box.php")
    Call<UnloadResponse> unloadBox(
            @Field("cass_id") int cassId,
            @Field("garolla") int garolla
    );

    // --- conferma (POST JSON corretto) ---
    @POST("confirm_member_boxes.php")
    Call<ConfirmBoxResponse> confirmCassoni(@Body ConfirmCassoniRequest body);

    // --- SOLO DEBUG: versione GET identica a quella che ti funziona da browser ---
    @GET("confirm_member_boxes.php")
    Call<ConfirmBoxResponse> confirmCassoniForm(
            @Query("member_id") String memberId,
            @Query("vuoti") String vuotiCsv,
            @Query("pieni") String pieniCsv
    );

    @GET("get_rfid_list.php")
    Call<RfidListResponse> getRfidList();

    @GET("get_box_detail.php")
    Call<BoxDetailResponse> getBoxDetail(@Query("rfid") String rfid);

    @GET("check_cavity_vine.php")
    Call<CheckVineResponse> checkCavityVine(@Query("cavity") int cavity, @Query("vine") String vine);
}
