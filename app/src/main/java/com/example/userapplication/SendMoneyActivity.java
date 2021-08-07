package com.example.userapplication;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SendMoneyActivity extends AppCompatActivity {
    EditText mAmountSent, mReceiverId;
    Button mPay;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    int amountSent, recipientAccountCurrentBalance, accountCurrentBalance;
    String recipientAccountEmail, recipientAccountID, recipientAccountIDMain, recipientAccountPhoneNumber;
    String accountEmail, accountID, accountPhoneNumber;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_money);

        mAmountSent = findViewById(R.id.amountSent);
        mReceiverId = findViewById(R.id.receiverId);
        mPay = findViewById(R.id.btn_send_money_card);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        mPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //INITIALISE CURRENT USER VARIABLES
                Intent intent = getIntent();
                accountEmail = intent.getStringExtra("Account Email");
                accountID = intent.getStringExtra("Student ID");
                accountPhoneNumber = intent.getStringExtra("Phone Number");
                accountCurrentBalance = intent.getIntExtra("Current Balance", 0);


                mReceiverId.setError(null);
                mAmountSent.setError(null);
                progressBar = findViewById(R.id.progressBar7);

                String receiverId = mReceiverId.getText().toString().trim();
                String amountSent = mAmountSent.getText().toString().trim();

                //ERROR CHECKS
                if (TextUtils.isEmpty(receiverId)){
                    mReceiverId.setError("Please Enter Receiver's ID.");
                    return;
                }
                if (TextUtils.isEmpty(amountSent)){
                    mAmountSent.setError("Please Enter An Amount To Send.");
                    return;
                }
                if(receiverId.length() < 8){
                    mReceiverId.setError("Enter Correct Student ID.");
                    return;
                }
                if(Integer.parseInt(amountSent) > accountCurrentBalance){
                    mAmountSent.setError("Not Enough Funds To Send.");
                    return;
                }
                if(Integer.parseInt(amountSent) < 1){
                    mAmountSent.setError("Please Send At Least GH₵ 1.00.");
                    return;
                }
                if(Integer.parseInt(amountSent) > 50){
                    mAmountSent.setError("You Can't Send More Than GH₵ 50.0 at once.");
                    return;
                }

                if(receiverId.equals(accountID)){
                    mReceiverId.setError("You Can't Send To Yourself.");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                SendMoneyActivity.this.amountSent = Integer.parseInt(mAmountSent.getText().toString());


                //LOOK FOR RECEIVER IN DATABASE AND IF EXISTS, SEND MONEY
                CollectionReference users = fStore.collection("users");
                Query query =  users.whereEqualTo("Student_ID", mReceiverId.getText().toString());
                query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            //UPDATE RECIPIENT DATABASE ENTRY
                            if(!task.getResult().isEmpty()){

                                for (QueryDocumentSnapshot document : task.getResult()) {

                                    recipientAccountEmail = document.getString("Email");
                                    recipientAccountID = document.getString("Student_ID");
                                    recipientAccountPhoneNumber = document.getString("Phone_Number");
                                    recipientAccountCurrentBalance = ((Long) document.get("Account_Balance")).intValue();
                                    recipientAccountIDMain = document.getId();

                                    String jsonString = "{\n  \"event\": \"charge.success\",\n  \"data\": {\n    \"status\": \"success\",\n    \"reference\": \" \",\n    \"amount\": "+amountSent+"00,\n    \"paid_at\": \" "+ LocalDate.now()+"T"+ LocalTime.now()+"Z"+ "\",\n    \"channel\": \"Received from "+accountID+"\",\n    \"customer\": {\n      \"email\": \""+recipientAccountEmail+"\"\n    }\n}}";
                                    //Update Receiver Info

                                    OkHttpClient client = new OkHttpClient().newBuilder()
                                            .build();
                                    MediaType mediaType = MediaType.parse("application/json");
                                    RequestBody body = RequestBody.create(jsonString, mediaType);
                                    Request request = new Request.Builder()
                                            .url("https://us-central1-userapplication-4369d.cloudfunctions.net/momoUpdate")
                                            .method("POST", body)
                                            .addHeader("Content-Type", "application/json")
                                            .build();
                                    client.newCall(request).enqueue(new Callback() {
                                        @Override
                                        public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                            e.printStackTrace();
                                        }

                                        @Override
                                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                                        }
                                    });

                            }

                            //SUBTRACT FROM CURRENT USER

                                String jsonString = "{\n  \"event\": \"charge.success\",\n  \"data\": {\n    \"status\": \"success\",\n    \"reference\": \" \",\n    \"amount\": -"+amountSent+"00,\n    \"paid_at\": \" "+ LocalDate.now()+"T"+ LocalTime.now()+"Z"+ "\",\n    \"channel\": \"Sent to "+recipientAccountID+"\",\n    \"customer\": {\n      \"email\": \""+accountEmail+"\"\n    }\n}}";

                                //Update Receiver Info

                                OkHttpClient client = new OkHttpClient().newBuilder()
                                        .build();
                                MediaType mediaType = MediaType.parse("application/json");
                                RequestBody body = RequestBody.create(jsonString, mediaType);
                                Request request = new Request.Builder()
                                        .url("https://us-central1-userapplication-4369d.cloudfunctions.net/momoUpdate")
                                        .method("POST", body)
                                        .addHeader("Content-Type", "application/json")
                                        .build();
                                    client.newCall(request).enqueue(new Callback() {
                                    @Override
                                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                        e.printStackTrace();
                                    }

                                    @Override
                                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                                        SendMoneyActivity.this.runOnUiThread(new Runnable() {
                                            public void run() {
                                                String message = "GH₵ "+amountSent+" Sent To "+recipientAccountID;
                                                Toast.makeText(SendMoneyActivity.this, message, Toast.LENGTH_LONG).show();
                                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                            }
                                        });


                                    }
                                });

                            }
                            else {
                                mReceiverId.setError("No Account With This Student ID Exists.");
                            }
                        } else {
                            Log.d("TAG", "Error getting documents: ", task.getException());

                        }
                    }
                });






            }
        });


    }
    @Override
    public void onBackPressed() {
        Intent setIntent = new Intent(SendMoneyActivity.this, MainActivity.class);
        startActivity(setIntent);
        finish();
    }
}