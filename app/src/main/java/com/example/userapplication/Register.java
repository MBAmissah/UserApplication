package com.example.userapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Register extends AppCompatActivity {
    public static final String TAG = "TAG";
    EditText mId, mEmail, mPassword, mPasswordConfirm, mPhone, mFirstName, mLastName;
    Button mRegisterBtn;
    TextView mLoginPage;
    FirebaseAuth fAuth;
    ProgressBar progressBar;
    FirebaseFirestore fstore;
    String userID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mId = findViewById(R.id.id);
        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password1);
        mPasswordConfirm = findViewById(R.id.password2);
        mPhone = findViewById(R.id.phone);
        mRegisterBtn = findViewById(R.id.register);
        mLoginPage = findViewById(R.id.toLoginPage);
        mFirstName = findViewById(R.id.firstName);
        mLastName = findViewById(R.id.lastName);

        fAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();
        progressBar = findViewById(R.id.progressBar);




        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mId.setError(null);
                mPassword.setError(null);
                mPasswordConfirm.setError(null);
                mPhone.setError(null);
                mEmail.setError(null);

                String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();
                String passwordConfirm = mPasswordConfirm.getText().toString().trim();
                String phone = mPhone.getText().toString();
                String id = mId.getText().toString();
                String firstName = mFirstName.getText().toString();
                String lastName = mLastName.getText().toString();
                int balance = 0;

                if (TextUtils.isEmpty(firstName)){
                    mFirstName.setError("Please Enter Your First Name.");
                    return;
                }
                if (TextUtils.isEmpty(lastName)){
                    mLastName.setError("Please Enter Your Last Name.");
                    return;
                }
                if (TextUtils.isEmpty(email)){
                    mEmail.setError("Please Enter Your Email.");
                    return;
                }

                if (TextUtils.isEmpty(id)){
                    mId.setError("Please Enter Your Student ID.");
                    return;
                }
                if(phone.length() < 10){
                    mPhone.setError("Please Enter A Valid Phone Number.");
                    return;
                }
                if(id.length() < 8){
                    mId.setError("Please Enter A Valid ID.");
                    return;
                }
                if (TextUtils.isEmpty(password)){
                    mPassword.setError("Please Enter A Password.");
                    return;
                }
                if (TextUtils.isEmpty(passwordConfirm)){
                    mPasswordConfirm.setError("Please Confirm Password.");
                    return;
                }
               /* if(password.length() < 6){
                    mPassword.setError("Password must be more than 6 characters.");
                    return;
                }*/
                if(!password.equals(passwordConfirm)){
                    mPasswordConfirm.setError("Two passwords do not match.");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);
                //REGISTER USER IN FIREBASE

                //LOOK FOR ID and Email IN DATABASE AND IF not EXISTS, edit profile
                CollectionReference users = fstore.collection("users");
                Query query =  users.whereEqualTo("Student_ID", id);
                query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            //if it doesnt exist
                            if(Objects.requireNonNull(task.getResult()).isEmpty()){


                                String jsonString = "{\r\n  \"email\": \""+email+"\",\r\n  \"password\": \""+password+"\",\r\n  \"id\": \""+id+"\",\r\n  \"phone\": \""+phone+"\",\r\n  \"balance\": 0,\r\n  \"firstname\": \""+firstName+"\",\r\n  \"lastname\": \""+lastName+"\"}";

                                OkHttpClient client = new OkHttpClient().newBuilder()
                                        .build();
                                MediaType mediaType = MediaType.parse("application/json");
                                RequestBody body = RequestBody.create(jsonString,mediaType);
                                Request request = new Request.Builder()
                                        .url("https://us-central1-userapplication-4369d.cloudfunctions.net/createUser")
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

                                        String resp = response.body().string();
                                        if(response.isSuccessful()){
                                            Register.this.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(Register.this, resp, Toast.LENGTH_SHORT).show();
                                                    progressBar.setVisibility(View.GONE);
                                                    startActivity(new Intent(getApplicationContext(),Login.class));
                                                    finish();
                                                }
                                            });
                                        }else {
                                            Register.this.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(Register.this, resp, Toast.LENGTH_SHORT).show();
                                                    progressBar.setVisibility(View.GONE);
                                                }
                                            });
                                        }











                                    }
                                });


//                                fAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        if(task.isSuccessful()){
//
//                            //verify email
//
//                            FirebaseUser fuser = fAuth.getCurrentUser();
//                            fuser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
//                                @Override
//                                public void onSuccess(Void aVoid) {
//                                    Toast.makeText(Register.this, "Verification Email Has Been Sent", Toast.LENGTH_SHORT).show();
//                                }
//                            }).addOnFailureListener(new OnFailureListener() {
//                                @Override
//                                public void onFailure(@NonNull Exception e) {
//                                    Log.d(TAG, "On Failure: Verification email not sent" + e.getMessage());
//                                }
//                            });
//
//
//
//
//
//
//                        }else {
//                            Toast.makeText(Register.this, "Error! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
//                            progressBar.setVisibility(View.GONE);
//
//                        }
//                    }
//                });

                            }
                            else {
                                mId.setError("Account With This Student ID Already Exists.");

                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        } else {
                            Log.d("TAG", "Error getting documents: ", task.getException());

                        }
                    }
                });
            }
        });


        mLoginPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),Login.class));
                finish();
            }
        });
    }
}