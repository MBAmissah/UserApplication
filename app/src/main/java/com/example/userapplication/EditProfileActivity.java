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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class EditProfileActivity extends AppCompatActivity {
   // ImageButton mBack;

    String accountEmail, accountID, accountPhoneNumber, accountFirstName, accountLastName;
    int accountCurrentBalance;
    EditText mId,  mPhone, mFirstName, mLastName;
    //EditText mEmail;

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    Button mConfirmEdit;
    ProgressBar mprogressBar5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);


        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();


        Intent intent = getIntent();
        accountEmail = intent.getStringExtra("Account Email");
        accountID = intent.getStringExtra("Student ID");
        accountPhoneNumber = intent.getStringExtra("Phone Number");
        accountCurrentBalance = intent.getIntExtra("Current Balance", 0);
        accountFirstName = intent.getStringExtra("First Name");
        accountLastName = intent.getStringExtra("Last Name");


        mprogressBar5 = findViewById(R.id.progressBar5);


        mId = findViewById(R.id.changeId);
       // mEmail = findViewById(R.id.changeEmail);
        mPhone = findViewById(R.id.changePhone);
       // mBack = findViewById(R.id.back);
        mConfirmEdit = findViewById(R.id.confirmEditProfile);
        mFirstName = findViewById(R.id.changeFirstName);
        mLastName = findViewById(R.id.changeLastName);

        mId.setText(accountID);
        mPhone.setText(accountPhoneNumber);
       // mEmail.setText(accountEmail);
        mFirstName.setText(accountFirstName);
        mLastName.setText(accountLastName);

       /* //Go back to home
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });*/



        mConfirmEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mId.setError(null);
            //    mEmail.setError(null);
                mPhone.setError(null);
                mFirstName.setError(null);
                mLastName.setError(null);

              //  String email = mEmail.getText().toString().trim();
                String phone = mPhone.getText().toString();
                String id = mId.getText().toString();
                String firstName = mFirstName.getText().toString();
                String lastName = mLastName.getText().toString();

                if (TextUtils.isEmpty(firstName)){
                    mFirstName.setError("Please Enter Your First Name.");
                    return;
                }
                if (TextUtils.isEmpty(lastName)){
                    mLastName.setError("Please Enter Your Last Name.");
                    return;
                }
                if (TextUtils.isEmpty(id)){
                    mId.setError("Please Enter A Student ID.");
                    return;
                }
//                if (TextUtils.isEmpty(email)){
//                    mEmail.setError("Please Enter An Email.");
//                    return;
//                }
                if (TextUtils.isEmpty(phone)){
                    mPhone.setError("Please Enter A Phone Number.");
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


               mprogressBar5.setVisibility(View.VISIBLE);
                //Edit USER IN FIREBASE

                //LOOK FOR ID and Email IN DATABASE AND IF not EXISTS, edit profile
                CollectionReference users = fStore.collection("users");
                Query query =  users.whereEqualTo("Student_ID", id);
                query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            //look for email in database
                            if(Objects.requireNonNull(task.getResult()).isEmpty() || id.equals(accountID)){

                                String userID = fAuth.getCurrentUser().getUid();
                                DocumentReference documentReference = fStore.collection("users").document(userID);
                                Map<String,Object> user = new HashMap<>();
                                user.put("Student_ID",id);
                                //user.put("Email",email);
                                user.put("Phone_Number",phone);
                                user.put("First_Name",firstName);
                                user.put("Last_Name",lastName);
                                documentReference.set(user, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("tag", "OnSuccess: Profile Edited ");
                                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                    }
                                });
                            }
                            else {
                                mId.setError("Account With This Student ID Already Exists.");
                                mprogressBar5.setVisibility(View.INVISIBLE);
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
        Intent setIntent = new Intent(EditProfileActivity.this, MainActivity.class);
        startActivity(setIntent);
        finish();
    }
}