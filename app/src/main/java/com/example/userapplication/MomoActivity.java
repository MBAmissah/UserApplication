package com.example.userapplication;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import co.paystack.android.PaystackSdk;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MomoActivity extends Fragment {

    JSONObject jsonObject;
    String responseString;
    Dialog dialog;
    TextView textView, mMessage, mDisplayText;

    String receiverPhoneNumber;
    private TextInputLayout mReceiverPhoneNumber, mAmount, mEnterOTP;
    Button mConfirmOTP, mConfirmPayment;
    int accountBalance, accountCurrentBalance;
    String amount, provider, status;
    String accountEmail, accountID, accountPhoneNumber;
    //String Bearer = "Bearer sk_test_e158d37d1eeeef34a75e0ed9fa6168fc849c922f";
    String Bearer = "Bearer sk_live_dc66ebc5a3963ef991bf78b213864cee29adc247";
    RadioButton mtn, vodafone,airtel;

    FirebaseAuth fAuth;
    FirebaseFirestore fstore;


    ProgressBar progressBar, mProgressBar4;

    private FirebaseFunctions mFunctions;

    private void initializePaystack() {
        PaystackSdk.initialize(getActivity().getApplicationContext());

        //Never add API keys directly to application code. In production environments, we recommend using a secure mechanism to manage API keys.
        // https://developers.google.com/maps/documentation/places/android-sdk/start#connect-client
        //PaystackSdk.setPublicKey("pk_test_a7656f77752f54a453efa4852c31f78ece6a3808");
        PaystackSdk.setPublicKey("pk_live_e1126dcd84114c58576ec608056d74f953b7d8f5");
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_momo,container,false);


        dialog = new Dialog(getActivity());

        mFunctions = FirebaseFunctions.getInstance();


        fAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();
        initializePaystack();


        mReceiverPhoneNumber = v.findViewById(R.id.enter_phone_number);
        mAmount = v.findViewById(R.id.amount_momo);


        progressBar = v.findViewById(R.id.progressBar3);


        mtn = v.findViewById(R.id.mtn);
        vodafone = v.findViewById(R.id.vod);
        airtel = v.findViewById(R.id.tgo);


        Intent intent = getActivity().getIntent();
        accountEmail = intent.getStringExtra("Account Email");
        accountID = intent.getStringExtra("Student ID");
        accountPhoneNumber = intent.getStringExtra("Phone Number");
        accountCurrentBalance = intent.getIntExtra("Current Balance", 0);

        mReceiverPhoneNumber.getEditText().setText(accountPhoneNumber);



        Button payBtn = v.findViewById(R.id.pay_momo);
        payBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View vv) {


                mAmount.setError(null);
                mReceiverPhoneNumber.setError(null);

               if(mtn.isChecked()){
                    provider = "mtn";
                }else if(vodafone.isChecked()){
                    provider = "vod";
                }
                else if(airtel.isChecked()){
                    provider = "tgo";
                }

                amount = mAmount.getEditText().getText().toString();


                receiverPhoneNumber = mReceiverPhoneNumber.getEditText().getText().toString();

                if (TextUtils.isEmpty(amount)){
                    mAmount.setError("Please Enter An Amount To Load.");
                    return;
                }
                if (TextUtils.isEmpty(receiverPhoneNumber)){
                    mReceiverPhoneNumber.setError("Please Enter A Phone Number.");
                    return;
                }

                if(receiverPhoneNumber.length() < 10){
                    mReceiverPhoneNumber.setError("Please Enter A Valid Phone Number.");
                    return;
                }

                if(Integer.parseInt(amount) > 100){
                    mAmount.setError("You can not load more than GH₵ 100 at once.");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                //CHARGE MOMO
                String oldAmount = amount;
                amount = amount+"00"; //convert to pesewas

                String jsonString = "{\n  \"email\":\"-\",\n   \"amount\":\"-\",\n  \"metadata\":\"0\", \n  \"currency\": \"GHS\",\n  \"mobile_money\": {\n    \"phone\" : \"-\",\n    \"provider\" : \"MTN\"\n  }\n}";


                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(jsonString);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                JSONObject jsonObject2 = null;
                try {
                    jsonObject2 = jsonObject.getJSONObject("mobile_money");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try {
                    jsonObject2.put("phone", receiverPhoneNumber);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    jsonObject2.put("provider", provider);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try {
                    jsonObject.put("email", accountEmail);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    jsonObject.put("amount", amount);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try {
                    jsonObject.put("mobile_money", jsonObject2);
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                System.out.println(jsonObject.toString());

                OkHttpClient client = new OkHttpClient().newBuilder().build();
                MediaType mediaType = MediaType.parse("application/json");
                RequestBody body = RequestBody.create(jsonObject.toString(),mediaType);
                Request request = new Request.Builder()
                        .url("https://api.paystack.co/charge")
                        .method("POST", body)
                        .addHeader("Authorization", Bearer)
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Cookie", "sails.sid=s%3AGDMwgl2hf6da2RNEFMghCXd7mLujDwcO.W4k1zl%2Bh5qWgM4egBCCzbpWh71Ld7R3xzrI2Aq0Sfj4")
                        .build();


                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(@NotNull Call call, @NotNull IOException e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                            if(response.isSuccessful()){
                                String resp = response.body().string();
                                try {
                                    JSONObject object = new JSONObject(resp);


                                    System.out.println("-----------charge----------"+resp);


                                    status = object.getString("status");
                                    String message = object.getString("message");

                                    JSONObject referenceJson = object.getJSONObject("data");
                                    String reference = referenceJson.getString("reference");
                                    String dataStatus = referenceJson.getString("status");
                                    String displayText = referenceJson.getString("display_text");

                                    System.out.println("---------------------"+status);
                                    System.out.println("---------------------"+reference);



                                    getActivity().runOnUiThread(new Runnable() {
                                        @SuppressLint("SetTextI18n")
                                        @Override
                                        public void run() {
                                            dialog.setContentView(R.layout.popout_layout);
                                            textView = dialog.findViewById(R.id.cancel);
                                            dialog.show();

                                            mProgressBar4 = dialog.findViewById(R.id.progressBar4);
                                            textView.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    dialog.dismiss();

                                                    progressBar.setVisibility(View.GONE);
                                                }
                                            });


                                            mMessage = dialog.findViewById(R.id.showMessage);
                                            mDisplayText = dialog.findViewById(R.id.displayText);
                                            mEnterOTP = dialog.findViewById(R.id.enter_otp);
                                            mConfirmOTP = dialog.findViewById(R.id.confirm_otp);


                                            mMessage.setText(message);
                                            mDisplayText.setText(displayText);

                                            if(dataStatus.equals("pay_offline")){
                                                mProgressBar4.setVisibility(View.VISIBLE);
                                                mEnterOTP.setVisibility(View.GONE);
                                                mConfirmOTP.setVisibility(View.GONE);

                                                Handler handler = new Handler();
                                                handler.postDelayed(new Runnable() {
                                                    public void run() {
                                                        mMessage.setText("You May Close The App");
                                                        mDisplayText.setText("If You Entered Your PIN Correctly, Your Balance will be updated shortly");
                                                        mProgressBar4.setVisibility(View.GONE);

                                                    }
                                                }, 10000);   //5 seconds



                                            }
                                            else if (dataStatus.equals("send_otp")){
                                                //SEND OTP
                                                mConfirmOTP.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {

                                                        mEnterOTP.setError(null);
                                                        String otp = mEnterOTP.getEditText().getText().toString();

                                                        if (TextUtils.isEmpty(otp) || otp.length()<6){
                                                            mEnterOTP.setError("Please Enter A Valid OTP.");
                                                            return;
                                                        }
                                                        String jsonString = "otp="+otp+"&reference="+reference;


                                                        System.out.println(jsonString);

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
                                                        client.newCall(request).enqueue(new Callback() {
                                                            @Override
                                                            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                                                e.printStackTrace();
                                                            }

                                                            @Override
                                                            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                                                                if(response.isSuccessful()){
                                                                    String resp = response.body().string();
                                                                    try {
                                                                        JSONObject object = new JSONObject(resp);
                                                                        status = object.getString("status");
                                                                        String message = object.getString("message");

                                                                        JSONObject referenceJson = object.getJSONObject("data");
                                                                        String reference = referenceJson.getString("reference");
                                                                        String dataStatus = referenceJson.getString("status");
                                                                        String displayText = referenceJson.getString("display_text");


                                                                        getActivity().runOnUiThread(new Runnable() {
                                                                                                        @Override
                                                                                                        public void run() {
                                                                                                            mMessage.setText(message);
                                                                                                            mDisplayText.setText(displayText);

                                                                                                            if(dataStatus.equals("pay_offline")){
                                                                                                                mProgressBar4.setVisibility(View.VISIBLE);
                                                                                                                mEnterOTP.setVisibility(View.GONE);
                                                                                                                mConfirmOTP.setVisibility(View.GONE);


                                                                                                                mMessage.setText("You May Close This Window");
                                                                                                                mDisplayText.setText("If You Entered Your PIN Correctly, Your Balance will be updated shortly");

                                                                                                            }
                                                                                                        }
                                                                                                    });




                                                                    } catch (JSONException e) {
                                                                        e.printStackTrace();
                                                                    }
                                                                }
                                                                else{
                                                                    System.out.println("----------4-----------" + response);
                                                                    String resp = response.body().string();

                                                                    try {
                                                                        JSONObject object = new JSONObject(resp);


                                                                        status = object.getString("status");
                                                                        String message = object.getString("message");

                                                                        getActivity().runOnUiThread(new Runnable() {
                                                                            @Override
                                                                            public void run() {
                                                                                dialog.setContentView(R.layout.popout_layout);
                                                                                textView = dialog.findViewById(R.id.cancel);
                                                                                dialog.show();

                                                                                mProgressBar4 = dialog.findViewById(R.id.progressBar4);
                                                                                textView.setOnClickListener(new View.OnClickListener() {
                                                                                    @Override
                                                                                    public void onClick(View v) {
                                                                                        dialog.dismiss();

                                                                                        progressBar.setVisibility(View.GONE);
                                                                                    }
                                                                                });


                                                                                mMessage = dialog.findViewById(R.id.showMessage);
                                                                                mDisplayText = dialog.findViewById(R.id.displayText);
                                                                                mEnterOTP = dialog.findViewById(R.id.enter_otp);
                                                                                mConfirmOTP = dialog.findViewById(R.id.confirm_otp);

                                                                                mMessage.setText(status);
                                                                                mDisplayText.setText(message);

                                                                                mEnterOTP.setVisibility(View.GONE);
                                                                                mConfirmOTP.setVisibility(View.GONE);
                                                                            }
                                                                        });

                                                                        // try one more time
                                                                        if(message.equals("Provided registration token is invalid")){
                                                                            getActivity().runOnUiThread(new Runnable() {
                                                                                @SuppressLint("SetTextI18n")
                                                                                @Override
                                                                                public void run() {
                                                                                    dialog.setContentView(R.layout.popout_layout);
                                                                                    textView = dialog.findViewById(R.id.cancel);
                                                                                    dialog.show();

                                                                                    mProgressBar4 = dialog.findViewById(R.id.progressBar4);
                                                                                    textView.setOnClickListener(new View.OnClickListener() {
                                                                                        @Override
                                                                                        public void onClick(View v) {
                                                                                            dialog.dismiss();

                                                                                            progressBar.setVisibility(View.GONE);
                                                                                        }
                                                                                    });


                                                                                    mMessage = dialog.findViewById(R.id.showMessage);
                                                                                    mDisplayText = dialog.findViewById(R.id.displayText);
                                                                                    mEnterOTP = dialog.findViewById(R.id.enter_otp);
                                                                                    mConfirmOTP = dialog.findViewById(R.id.confirm_otp);


                                                                                    mMessage.setText(message);
                                                                                    mDisplayText.setText(displayText);

                                                                                    if(dataStatus.equals("pay_offline")){
                                                                                        mProgressBar4.setVisibility(View.VISIBLE);
                                                                                        mEnterOTP.setVisibility(View.GONE);
                                                                                        mConfirmOTP.setVisibility(View.GONE);

                                                                                        Handler handler = new Handler();
                                                                                        handler.postDelayed(new Runnable() {
                                                                                            public void run() {
                                                                                                mMessage.setText("You May Close The App");
                                                                                                mDisplayText.setText("If You Entered Your PIN Correctly, Your Balance will be updated shortly");
                                                                                                mProgressBar4.setVisibility(View.GONE);

                                                                                            }
                                                                                        }, 10000);   //5 seconds



                                                                                    }
                                                                                    else if (dataStatus.equals("send_otp")){
                                                                                        //SEND OTP
                                                                                        mConfirmOTP.setOnClickListener(new View.OnClickListener() {
                                                                                            @Override
                                                                                            public void onClick(View v) {

                                                                                                mEnterOTP.setError(null);
                                                                                                String otp = mEnterOTP.getEditText().getText().toString();

                                                                                                if (TextUtils.isEmpty(otp) || otp.length()<6){
                                                                                                    mEnterOTP.setError("Please Enter A Valid OTP.");
                                                                                                    return;
                                                                                                }
                                                                                                String jsonString = "otp="+otp+"&reference="+reference;


                                                                                                System.out.println(jsonString);

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
                                                                                                client.newCall(request).enqueue(new Callback() {
                                                                                                    @Override
                                                                                                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                                                                                        e.printStackTrace();
                                                                                                    }

                                                                                                    @Override
                                                                                                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                                                                                                        if(response.isSuccessful()){
                                                                                                            String resp = response.body().string();
                                                                                                            try {
                                                                                                                JSONObject object = new JSONObject(resp);
                                                                                                                status = object.getString("status");
                                                                                                                String message = object.getString("message");

                                                                                                                JSONObject referenceJson = object.getJSONObject("data");
                                                                                                                String reference = referenceJson.getString("reference");
                                                                                                                String dataStatus = referenceJson.getString("status");
                                                                                                                String displayText = referenceJson.getString("display_text");


                                                                                                                getActivity().runOnUiThread(new Runnable() {
                                                                                                                    @Override
                                                                                                                    public void run() {
                                                                                                                        mMessage.setText(message);
                                                                                                                        mDisplayText.setText(displayText);

                                                                                                                        if(dataStatus.equals("pay_offline")){
                                                                                                                            mProgressBar4.setVisibility(View.VISIBLE);
                                                                                                                            mEnterOTP.setVisibility(View.GONE);
                                                                                                                            mConfirmOTP.setVisibility(View.GONE);


                                                                                                                            mMessage.setText("You May Close This Window");
                                                                                                                            mDisplayText.setText("If You Entered Your PIN Correctly, Your Balance will be updated shortly");

                                                                                                                        }
                                                                                                                    }
                                                                                                                });




                                                                                                            } catch (JSONException e) {
                                                                                                                e.printStackTrace();
                                                                                                            }
                                                                                                        }
                                                                                                        else{
                                                                                                            System.out.println("----------4-----------" + response);
                                                                                                            String resp = response.body().string();

                                                                                                            try {
                                                                                                                JSONObject object = new JSONObject(resp);


                                                                                                                status = object.getString("status");
                                                                                                                String message = object.getString("message");

                                                                                                                getActivity().runOnUiThread(new Runnable() {
                                                                                                                    @Override
                                                                                                                    public void run() {
                                                                                                                        dialog.setContentView(R.layout.popout_layout);
                                                                                                                        textView = dialog.findViewById(R.id.cancel);
                                                                                                                        dialog.show();

                                                                                                                        mProgressBar4 = dialog.findViewById(R.id.progressBar4);
                                                                                                                        textView.setOnClickListener(new View.OnClickListener() {
                                                                                                                            @Override
                                                                                                                            public void onClick(View v) {
                                                                                                                                dialog.dismiss();

                                                                                                                                progressBar.setVisibility(View.GONE);
                                                                                                                            }
                                                                                                                        });


                                                                                                                        mMessage = dialog.findViewById(R.id.showMessage);
                                                                                                                        mDisplayText = dialog.findViewById(R.id.displayText);
                                                                                                                        mEnterOTP = dialog.findViewById(R.id.enter_otp);
                                                                                                                        mConfirmOTP = dialog.findViewById(R.id.confirm_otp);

                                                                                                                        mMessage.setText(status);
                                                                                                                        mDisplayText.setText(message);

                                                                                                                        mEnterOTP.setVisibility(View.GONE);
                                                                                                                        mConfirmOTP.setVisibility(View.GONE);
                                                                                                                    }
                                                                                                                });





                                                                                                            } catch (JSONException e) {
                                                                                                                e.printStackTrace();
                                                                                                            };
                                                                                                        }



                                                                                                    }
                                                                                                });


                                                                                            }
                                                                                        });

                                                                                    }
                                                                                }
                                                                            });

                                                                        }





                                                                    } catch (JSONException e) {
                                                                        e.printStackTrace();
                                                                    };
                                                                }



                                                            }
                                                        });


                                                    }
                                                });

                                            }
                                        }
                                    });

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                };
                            }
                            else{
                                System.out.println("----------1-----------" + response.body());
                                String resp = response.body().string();

                                try {
                                    JSONObject object = new JSONObject(resp);


                                    status = object.getString("message");
                                    JSONObject jsonObject1 = object.getJSONObject("data");
                                    String message = jsonObject1.getString("message");

                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            dialog.setContentView(R.layout.popout_layout);
                                            textView = dialog.findViewById(R.id.cancel);
                                            dialog.show();

                                            mProgressBar4 = dialog.findViewById(R.id.progressBar4);
                                            textView.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    dialog.dismiss();

                                                    progressBar.setVisibility(View.GONE);
                                                }
                                            });


                                            mMessage = dialog.findViewById(R.id.showMessage);
                                            mDisplayText = dialog.findViewById(R.id.displayText);
                                            mEnterOTP = dialog.findViewById(R.id.enter_otp);
                                            mConfirmOTP = dialog.findViewById(R.id.confirm_otp);

                                            mMessage.setText(status);
                                            mDisplayText.setText(message);

                                            mEnterOTP.setVisibility(View.GONE);
                                            mConfirmOTP.setVisibility(View.GONE);
                                        }
                                    });










                                } catch (JSONException e) {
                                    e.printStackTrace();
                                };
                            }


                        }
                    });

            }
        });
        return v;
    }



}
