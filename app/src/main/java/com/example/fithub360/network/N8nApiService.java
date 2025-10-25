package com.example.fithub360.network;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface N8nApiService {

    class EmailPayload {
        public String email;
        public String subject;
        public String content;
        public EmailPayload(String email, String subject, String content) {
            this.email = email;
            this.subject = subject;
            this.content = content;
        }
    }

    // Test URL: https://.../webhook-test/fitHub360-send-email?secret=...
    @Headers("Content-Type: application/json")
    @POST("webhook-test/fitHub360-send-email")
    Call<ResponseBody> sendEmailTest(@Query("secret") String secret, @Body EmailPayload body);

    // Prod URL: https://.../webhook/fitHub360-send-email?secret=...
    @Headers("Content-Type: application/json")
    @POST("webhook/fitHub360-send-email")
    Call<ResponseBody> sendEmailProd(@Query("secret") String secret, @Body EmailPayload body);
}
