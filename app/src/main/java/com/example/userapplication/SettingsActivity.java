package com.example.userapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    Button mForgotText, mDeleteText, mMissingCard, mUpdateEmail;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String accountEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mForgotText = findViewById(R.id.resetPassword);
        mDeleteText = findViewById(R.id.deleteAccount);
        mMissingCard = findViewById(R.id.missingCard);
        mUpdateEmail = findViewById(R.id.updateEmail);
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();


        Intent intent = getIntent();
        accountEmail = intent.getStringExtra("Account Email");



        String userID = fAuth.getCurrentUser().getUid();

        mForgotText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText resetMail = new EditText(v.getContext());
                resetMail.setText(accountEmail);
                AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(v.getContext(), R.style.CustomAlertDialog);
                passwordResetDialog.setTitle("Reset Password?");
                passwordResetDialog.setMessage("Reset Link Will Be Sent To This Email.");
                passwordResetDialog.setView(resetMail);

                passwordResetDialog.setPositiveButton("Reset", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //extract email and send reset link

                        String mail = resetMail.getText().toString();
                        fAuth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(SettingsActivity.this, "Reset Link Sent To Your Email.",Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(SettingsActivity.this, "Error. Reset Link Was Not Sent." + e.getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

                passwordResetDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //close dialog
                    }
                });

                // Create the alert dialog and change Buttons colour
                AlertDialog dialog = passwordResetDialog.create();
                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onShow(DialogInterface arg0) {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(R.color.black);
                        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(R.color.black);
                    }
                });
                dialog.show();
            }
        });

        mUpdateEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LinearLayout layout = new LinearLayout(v.getContext());
                layout.setOrientation(LinearLayout.VERTICAL);

// Add a TextView here for the "Title" label, as noted in the comments
                EditText updateMail = new EditText(v.getContext());
                updateMail.setHint("Updated Email");
                updateMail.setText(accountEmail);
                layout.addView(updateMail); // Notice this is an add method

// Add another TextView here for the "Description" label
                EditText mPassword  = new EditText(v.getContext());
                mPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                mPassword.setHint("Password");
                layout.addView(mPassword); // Another add method



                AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(v.getContext(), R.style.CustomAlertDialog);
                passwordResetDialog.setTitle("Update Email");
                passwordResetDialog.setMessage("Enter New Email And Type Your Password.");
                passwordResetDialog.setView(layout);

                passwordResetDialog.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //extract email and send reset link

                        mPassword.setError(null);
                        mUpdateEmail.setError(null);

                        String mail = updateMail.getText().toString();
                        String password = mPassword.getText().toString().trim();

                        if (TextUtils.isEmpty(password)){

                            Toast.makeText(SettingsActivity.this, "No Password Entered", Toast.LENGTH_SHORT).show();
                        }
                        if (TextUtils.isEmpty(mail)){
                            Toast.makeText(SettingsActivity.this, "No Email Entered", Toast.LENGTH_SHORT).show();
                        }

                        if(!TextUtils.isEmpty(password) && !TextUtils.isEmpty(mail)){

                            fAuth.signInWithEmailAndPassword(accountEmail,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){


                                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                        user.updateEmail(mail)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Toast.makeText(SettingsActivity.this, "Email Updated", Toast.LENGTH_SHORT).show();

                                                            String userID = fAuth.getCurrentUser().getUid();
                                                            DocumentReference documentReference = fStore.collection("users").document(userID);
                                                            Map<String,Object> user = new HashMap<>();
                                                            user.put("Email",mail);
                                                            documentReference.set(user, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    Log.d("tag", "OnSuccess: Profile Edited ");
                                                                }
                                                            });
                                                        }
                                                        else {
                                                            Toast.makeText(SettingsActivity.this, "Error! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });

                                    }else {
                                        Toast.makeText(SettingsActivity.this, "Error! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        //       progressBar.setVisibility(View.GONE);
                                    }
                                }
                            });
                        }
                    }
                });

                passwordResetDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //close dialog
                    }
                });

                // Create the alert dialog and change Buttons colour
                AlertDialog dialog = passwordResetDialog.create();
                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onShow(DialogInterface arg0) {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(R.color.black);
                        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(R.color.black);
                    }
                });
                dialog.show();

            }
        });

        mDeleteText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText mPassword  = new EditText(v.getContext());
                mPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());

                AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(v.getContext(), R.style.CustomAlertDialog);
                passwordResetDialog.setTitle("Delete Account Password?");
                passwordResetDialog.setMessage("Enter Your Password And Click Delete");

                passwordResetDialog.setView(mPassword);

                passwordResetDialog.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        mPassword.setError(null);
                        String password = mPassword.getText().toString().trim();

                        if (TextUtils.isEmpty(password)){
                            mPassword.setError("Please Enter Your Password.");
                            return;
                        }
                        //extract email and send reset link

                        fAuth.signInWithEmailAndPassword(accountEmail,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){

                                    fStore.collection("users").document(userID).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            fAuth.getCurrentUser().delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {

                                                    Toast.makeText(SettingsActivity.this, "Account Deleted", Toast.LENGTH_SHORT).show();
                                                    fAuth.signOut();
                                                    startActivity(new Intent(getApplicationContext(), Login.class));
                                                    finish();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull @NotNull Exception e) {

                                                    Toast.makeText(SettingsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {

                                                    Toast.makeText(SettingsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });

                                }else {
                                    Toast.makeText(SettingsActivity.this, "Error! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                             //       progressBar.setVisibility(View.GONE);
                                }
                            }
                        });


                    }
                });

                passwordResetDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //close dialog
                    }
                });

                // Create the alert dialog and change Buttons colour
                AlertDialog dialog = passwordResetDialog.create();
                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onShow(DialogInterface arg0) {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(R.color.black);
                        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(R.color.black);
                    }
                });
                dialog.show();
            }
        });

        mMissingCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(v.getContext(), R.style.CustomAlertDialog);
                passwordResetDialog.setTitle("Deactivate Missing Or Stolen Card?");
                passwordResetDialog.setMessage("Your Card Will Be Deactivated And You Will Have To Get A New One.");


                passwordResetDialog.setPositiveButton("Deactivate", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {


                        DocumentReference docRef = fStore.collection("users").document(userID);
                        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {
                                        String card_Id = (String) document.get("Card_Id");

                                        if(card_Id.equals("")){
                                            Toast.makeText(SettingsActivity.this, "You Have No Cards Or Your Card Has Already Been Deactivated.",Toast.LENGTH_LONG).show();
                                        }
                                        else {
                                            DocumentReference documentReferencee = fStore.collection("deactivatedCards").document(userID);
                                            Map<String,Object> user = new HashMap<>();
                                            user.put("Card_Id",card_Id);
                                            documentReferencee.set(user, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.d("TAG", "OnSuccess: Profile created for " + userID);
                                                }
                                            });
                                            Map<String,Object> userr = new HashMap<>();
                                            userr.put("Card_Id","");
                                            docRef.set(userr, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.d("TAG", "OnSuccess: Profile created for " + userID);
                                                    Toast.makeText(SettingsActivity.this, "Card Deactivated.",Toast.LENGTH_LONG).show();
                                                }
                                            });
                                        }
                                    } else {
                                        Log.d("TAG", "No such document");
                                    }
                                } else {
                                    Log.d("TAG", "get failed with ", task.getException());
                                }
                            }
                        });

                    }
                });

                passwordResetDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //close dialog
                    }
                });

                // Create the alert dialog and change Buttons colour
                AlertDialog dialog = passwordResetDialog.create();
                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onShow(DialogInterface arg0) {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(R.color.black);
                        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(R.color.black);
                    }
                });
                dialog.show();
            }
        });


    }







    @Override
    public void onBackPressed() {
        Intent setIntent = new Intent(SettingsActivity.this, MainActivity.class);
        startActivity(setIntent);
        finish();
    }
}