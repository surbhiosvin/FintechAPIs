package com.hbcu.Retrofit;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

public interface RetrofitInterface {

    //SignUp Stage1
    @FormUrlEncoded
    @POST("signup")
    Call<ResponseBody>
    signUp1(@Field("email") String email,
            @Field("password") String password,
            @Field("signup_level") String signup_level,
            @Field("signup_type") String signup_type,
            @Field("fb_id") String fb_id);

    //SignUp Stage2
    @Multipart
    @POST("signup")
    Call<ResponseBody>
    signUp2(@Part("first_name") RequestBody first_name,
            @Part("last_name") RequestBody last_name,
            @Part("hbcu") RequestBody hbcu,
            @Part("greek_org") RequestBody greek_org,
            @Part("organization") RequestBody organisation,
            @Part("anonymous") RequestBody anonymous,
            @Part("user_id") RequestBody user_id,
            @Part("referral_link") RequestBody referral_link,
            @Part("signup_level") RequestBody signup_level,
            @Part("profile_url") RequestBody profile_url,
            @Part("percent") RequestBody percent,
            @Part MultipartBody.Part profile);

    //SignUp Stage3
    @FormUrlEncoded
    @POST("signup")
    Call<ResponseBody>
    signUp3(@Field("user_id") String user_id,
            @Field("public_token") String public_token,
            @Field("institution_id") String institution_id,
            @Field("signup_level") String signup_level);

    @FormUrlEncoded
    @POST("createaccesstoken")
    Call<ResponseBody>
    createaccesstoken(@Field("user_id") String user_id,
            @Field("public_token") String public_token,
            @Field("institution_id") String institution_id);

    //Get Accounts
    @Headers("Content-Type: application/json")
    @POST("auth/get")
    Call<ResponseBody>
    getAccounts(@Body Map<String, String> obj);

    //Add Account
    @FormUrlEncoded
    @POST("createAccountId")
    Call<ResponseBody>
    createAccountId(@Field("user_id") String user_id,
                    @Field("access_token") String access_token,
                    @Field("account_id") String account_id);

    //SignUp Stage4
    @FormUrlEncoded
    @POST("signup")
    Call<ResponseBody>
    signUp4(@Field("name_on_card") String name_on_card,
            @Field("card_num") String card_num,
            @Field("card_token") String card_token,
            @Field("card_type") String card_type,
            @Field("is_default") String is_default,
            @Field("user_id") String user_id,
            @Field("signup_level") String signup_level);

    //SignUp Stage5
    @FormUrlEncoded
    @POST("signup")
    Call<ResponseBody>
    signUp5(@Field("pin") String pin,
            @Field("unique_device_id") String unique_device_id,
            @Field("token_id") String token_id,
            @Field("login_via") String login_via,
            @Field("signup_level") String signup_level,
            @Field("user_id") String user_id);

    //Login
    @FormUrlEncoded
    @POST("login")
    Call<ResponseBody>
    login(@Field("email") String email,
          @Field("password") String password,
          @Field("fb_id") String fb_id,
          @Field("login_type") String login_type,
          @Field("unique_device_id") String unique_device_id,
          @Field("token_id") String token_id,
          @Field("login_via") String login_via);

    //ForgotPassword
    @FormUrlEncoded
    @POST("forgotpassword")
    Call<ResponseBody>
    forgotPassword(@Field("email") String email);

    //ForgotPin
    @FormUrlEncoded
    @POST("forgotPin")
    Call<ResponseBody>
    forgotPin(@Field("email") String email,
              @Field("user_id") String user_id);

    //EnterPin
    @FormUrlEncoded
    @POST("enterPin")
    Call<ResponseBody>
    enterPin(@Field("pin") String pin,
             @Field("user_id") String user_id);

    //HBCU List
    @GET("getHbcu")
    Call<ResponseBody>
    getHbcu(@Query("user_id") String user_id);

    //Org List
    @GET("getOrganization")
    Call<ResponseBody>
    getOrg(@Query("user_id") String user_id);

    //Check Signup Level
    @FormUrlEncoded
    @POST("checklevel")
    Call<ResponseBody>
    checkLevel(@Field("email") String email);

    //Get Fav Hbcu's
    @GET("getUserHBCU")
    Call<ResponseBody>
    getFavHBCU(@Query("user_id") String user_id);

    //Delete Fav Hbcu's
    @FormUrlEncoded
    @POST("deleteuserhbcu")
    Call<ResponseBody>
    dltFavHBCU(@Field("id") String id);

    //Get User Detail
    @GET("getUserDetails")
    Call<ResponseBody>
    getUserDetail(@Query("user_id") String user_id);

    //Logout
    @FormUrlEncoded
    @POST("logout")
    Call<ResponseBody>
    logout(@Field("user_id") String user_id,
           @Field("unique_deviceId") String unique_device_id);

    //pushNotifications
    @FormUrlEncoded
    @POST("pushNotifications")
    Call<ResponseBody>
    pushNotifications(@Field("user_id") String user_id,
                      @Field("status") String status);

    //pausedonations
    @FormUrlEncoded
    @POST("pausedonations")
    Call<ResponseBody>
    pauseDonations(@Field("user_id") String user_id,
                   @Field("spare_change") String spare_change,
                   @Field("reoccurring") String reoccurring);

    //updateProfile
    @Multipart
    @POST("updateprofile")
    Call<ResponseBody>
    updateProfile(@Part("first_name") RequestBody first_name,
                  @Part("last_name") RequestBody last_name,
                  @Part("hbcu") RequestBody hbcu,
                  @Part("greek_org") RequestBody greek_org,
                  @Part("organization") RequestBody organisation,
                  @Part("user_id") RequestBody user_id,
                  @Part("percent") RequestBody percent,
                  @Part MultipartBody.Part profile);

    //changePassword
    @FormUrlEncoded
    @POST("changePassword")
    Call<ResponseBody>
    changePassword(@Field("user_id") String user_id,
                   @Field("old_password") String unique_device_id,
                   @Field("new_password") String reoccurring);

    //changePin
    @FormUrlEncoded
    @POST("changePin")
    Call<ResponseBody>
    changePin(@Field("pin") String pin,
              @Field("user_id") String user_id);

    //Update Percentage
    @FormUrlEncoded
    @POST("updateHBCUDonationPercent")
    Call<ResponseBody>
    updatePercent(@Field("id") String id,
                  @Field("percent") String percent,
                  @Field("user_id") String user_id,
                  @Field("hbcuType") String hbcuType);

    //Get LinkedCard
    @FormUrlEncoded
    @POST("linkedcards")
    Call<ResponseBody>
    getLinkedCard(@Field("user_id") String user_id);

    //Add Card
    @FormUrlEncoded
    @POST("addCard")
    Call<ResponseBody>
    addCard(@Field("name_on_card") String name_on_card,
            @Field("card_num") String card_num,
            @Field("card_token") String card_token,
            @Field("card_type") String card_type,
            @Field("is_default") String is_default,
            @Field("user_id") String user_id);

    //One time Donation
    @FormUrlEncoded
    @POST("oneTimeDonation")
    Call<ResponseBody>
    oneTimeDonation(@Field("user_id") String user_id,
                    @Field("card_id") String card_id,
                    @Field("hbcu") String hbcu,
                    @Field("amount") String amount);

    //Get Donation Detail
    @GET("getDonationDetails")
    Call<ResponseBody>
    getDonationDetails(@Query("user_id") String user_id);

    //Get Reccurring Donation Detail
    @GET("getReoccurringDonation")
    Call<ResponseBody>
    getReoccurringDonation(@Query("user_id") String user_id);

    //Add Reccurring Donation
    @FormUrlEncoded
    @POST("addReoccurringDonation")
    Call<ResponseBody>
    addReoccurringDonation(@Field("user_id") String user_id,
                           @Field("amount") String amount,
                           @Field("id") String id,
                           @Field("hbcu_id") String hbcu_id,
                           @Field("cycle") String cycle,
                           @Field("card_id") String card_id);

    //editHBCU
    @FormUrlEncoded
    @POST("updateuserhbcu")
    Call<ResponseBody>
    editHbcu(@Field("user_id") String user_id,
             @Field("id") String id,
             @Field("hbcu") String hbcu);

    //addHBCU
    @FormUrlEncoded
    @POST("adduserhbcu")
    Call<ResponseBody>
    adduserhbcu(@Field("user_id") String user_id,
                @Field("percent") String percent,
                @Field("hbcuType") String hbcuType,
                @Field("hbcu") String hbcu);

    //Get MostLoved HBCU
    @GET("getDonations")
    Call<ResponseBody>
    getMostLovedHBCU(@Query("user_id") String user_id,
                     @Query("sort") String sort);

    //Get Top Donors
    @GET("getTopDonors")
    Call<ResponseBody>
    getTopDonors(@Query("user_id") String user_id,
                 @Query("sort") String sort);

    //Get HBCU Detail
    @GET("getHbcuDetails")
    Call<ResponseBody>
    getHBCUDetail(@Query("hbcu") String hbcu);

    //addComment
    @FormUrlEncoded
    @POST("addHBCUComment")
    Call<ResponseBody>
    addHBCUComment(@Field("user_id") String user_id,
                   @Field("hbcu") String hbcu,
                   @Field("comment") String comment);

    //Get HBCU CommentList
    @GET("getHbcuCommentsBy")
    Call<ResponseBody>
    getHbcuCommentsBy(@Query("hbcu") String hbcu);

    //Get HBCU FavoriteList
    @GET("getHbcuFavouriteBy")
    Call<ResponseBody>
    getHbcuFavouriteBy(@Query("hbcu") String hbcu);

    //Get HBCU Donated By list
    @GET("getHbcuDonatedBy")
    Call<ResponseBody>
    getHbcuDonatedBy(@Query("hbcu") String hbcu);

    //Get Donation Statement
    @GET("getDonationStatement")
    Call<ResponseBody>
    getDonationStatement(@Query("user_id") String user_id,
                         @Query("from_date") String from_date,
                         @Query("to_date") String to_date);

    //Download File
    @GET()
    @Streaming
    Call<ResponseBody> downloadFile(@Url String url);


    //Get Transactions
    @FormUrlEncoded
    @POST("plaidtransaction")
    Call<ResponseBody>
    getTransactions(@Field("user_id") String user_id,
                    @Field("page") String page,
                    @Field("type") String type);

    //Approve-Unapprove Spare
    @FormUrlEncoded
    @POST("insertunapproved")
    Call<ResponseBody>
    approveSpare(@Field("user_id") String user_id,
                 @Field("amount") String amount,
                 @Field("txnId") String txnId,
                 @Field("place_name") String place_name,
                 @Field("account_id") String account_id,
                 @Field("spareid") String spareid,
                 @Field("total_amount") String total_amount,
                 @Field("type") String type);

    //Spare Change Donation
    @FormUrlEncoded
    @POST("donateSpare")
    Call<ResponseBody>
    spareChangeDonation(@Field("user_id") String user_id,
                        @Field("amount") String amount,
                        @Field("card_id") String card_id,
                        @Field("approved_id") String approved_id);

    //Get Bank Name & Image
    @FormUrlEncoded
    @POST("getDonationstatusDetail")
    Call<ResponseBody>
    getDonationstatusDetail(@Field("user_id") String user_id);

    //Add Feedback
    @FormUrlEncoded
    @POST("feedback")
    Call<ResponseBody>
    feedback(@Field("user_id") String user_id,
             @Field("comment") String comment,
             @Field("rating") String rating,
             @Field("subject") String subject);

}
