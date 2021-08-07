package com.example.userapplication;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class submitOTP extends AddMoneyActivity{
    public String status200;


    public String submitOtp(String otp,String reference) throws IOException, JSONException {

        String jsonString = "otp="+otp+"&reference="+reference;


        //System.out.println(jsonString);

        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create( jsonString,mediaType);
        Request request = new Request.Builder()
                .url("https://api.paystack.co/charge/submit_otp")
                .method("POST", body)
                .addHeader("Authorization", "Bearer sk_live_dc66ebc5a3963ef991bf78b213864cee29adc247")
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
        Response response = client.newCall(request).execute();

        String resp = response.body().string();
        JSONObject object = new JSONObject(resp);
        String status = object.getString("status");
        status200 = String.valueOf(response.code());

        System.out.println("---------------------"+jsonString);
        System.out.println("---------------------"+status);
        System.out.println("---------------------"+status200);

        return status200;
    }
}
