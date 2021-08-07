package com.example.userapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    TextView mFirstName, mBalance, mId;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    FirebaseMessaging firebaseMessaging;
    String userID, accountEmail, accountID, accountPhoneNumber, accountFirstName, accountLastName;
    ImageButton mEditProfile,  mSendMoney, mSettings, mAddMoney;
    Button mCheckHistoryButton;
    int accountCurrentBalance;

    RecyclerView recyclerView;
   public List<Transactions> transactionsList = new ArrayList<>();;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 21) {
//            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true);
//        }
//        if (Build.VERSION.SDK_INT >= 19) {
//            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
//        }
//
//        if (Build.VERSION.SDK_INT >= 21) {
//            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
//            getWindow().setStatusBarColor(Color.TRANSPARENT);
//        }

        System.out.println(FirebaseAuth.getInstance().getCurrentUser()+"-----------3");

        mFirstName = findViewById(R.id.id);
        mBalance = findViewById(R.id.balance);
        mId = findViewById(R.id.mId);
        mEditProfile = findViewById(R.id.editProfile);
        mSettings = findViewById(R.id.settings);
        mAddMoney = findViewById(R.id.addMoneyBtn);
        mSendMoney = findViewById(R.id.sendMoneyButton);
        mCheckHistoryButton = findViewById(R.id.CheckHistoryButton);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        firebaseMessaging = FirebaseMessaging.getInstance();


        userID = fAuth.getCurrentUser().getUid();

        recyclerView = findViewById(R.id.recyclerView);




        DocumentReference documentReferencee = fStore.collection("users").document(userID);
        documentReferencee.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {

                ArrayList<HashMap<String, HashMap>> user;

                user = (ArrayList<HashMap<String, HashMap>>) documentSnapshot.get("Transaction_History");

                if(user != null){

                    // Creating an empty HashMap
                    HashMap hash_map = new HashMap<Integer, String>();

                    // Mapping string values to int keys
                    hash_map.put(0, "Date/Time");
                    hash_map.put(1, "Amount (GH¢)");
                    hash_map.put(2, "Channel");
                    hash_map.put(3, "Reference");
                    hash_map.put(4, "Status");

                    int previewSize;
                    if(user.size() < 4){
                        previewSize = -1;
                    }
                    else {
                        previewSize = user.size() - 4;
                    }
                    for(int i = user.size()-1; i > previewSize; i--){

                        HashMap dataa = user.get(i).get("data");

                        // Creating an empty HashMap
                        HashMap hash_mapp = new HashMap<Integer, String>();
                        assert dataa != null;
                        String s = String.valueOf(dataa.get("paid_at")).replace("T", " / ");
                        String result = s.substring(0, s.indexOf("."));


                        String channelReplace = String.valueOf(dataa.get("channel")).replace("_", " ");
                        String channelReplace2 = capitalizeWord(channelReplace);
                        String channel2 = channelReplace2.replace("Received ", "");
                        String channel = channel2.replace("Sent ", "");

                        String status = capitalizeWord((String) dataa.get("status"));

                        if(channel.equals("Mobile Money") || channel.equals("Card")){
                            channel = "Add - " + channel;
                        }




                        initData("Date/Time: "+result,"Status: "+  status, channel, "GH¢ " + String.valueOf((Long) dataa.get("amount") /100), "Reference: "+(String) dataa.get("reference") );

                    }
                }
                else {
                    initData("","","No Transactions Made", "", "");
                }

                setRecyclerView();}
        });


        DocumentReference documentReference = fStore.collection("users").document(userID);
        documentReference.addSnapshotListener(this, (documentSnapshot, error) -> {

            if (error != null) {
                Log.w("TAG", "Listen failed.", error);
                return;
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                accountEmail = documentSnapshot.getString("Email");
                accountID = documentSnapshot.getString("Student_ID");
                accountPhoneNumber = documentSnapshot.getString("Phone_Number");
                accountFirstName = documentSnapshot.getString("First_Name");
                accountLastName = documentSnapshot.getString("Last_Name");
                accountCurrentBalance = ((Long) Objects.requireNonNull(documentSnapshot.get("Account_Balance"))).intValue();
                mFirstName.setText(accountFirstName);
                mId.setText(accountID);
                mBalance.setText("GH₵ " + documentSnapshot.get("Account_Balance"));




                firebaseMessaging.getToken()
                        .addOnCompleteListener(task -> {
                            if (!task.isSuccessful()) {
                                Log.w("TAG", "Fetching FCM registration token failed", task.getException());
                                return;
                            }

                            // Get new FCM registration token
                            String token = task.getResult();
                            Map<String,Object> user = new HashMap<>();
                            user.put("FCM_Token",token);
                            documentReference.set(user, SetOptions.merge()).addOnSuccessListener(aVoid -> Log.d("tag", "OnSuccess: Main Activity token added"));
                        });
            } else {
                Log.d("TAG", "Current data: null");
            }





        });

        //go to edit profile
        mEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, EditProfileActivity.class);
            intent.putExtra("Account Email", accountEmail);
            intent.putExtra("Student ID", accountID);
            intent.putExtra("Phone Number", accountPhoneNumber);
            intent.putExtra("First Name", accountFirstName);
            intent.putExtra("Last Name", accountLastName);
            startActivity(intent);
            finish();
        });

        //go to settings
        mSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            intent.putExtra("Account Email", accountEmail);
            startActivity(intent);
            finish();
        });

        //Add money to balance
        mAddMoney.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddMoneyActivity.class);
            intent.putExtra("Account Email", accountEmail);
            intent.putExtra("Student ID", accountID);
            intent.putExtra("Phone Number", accountPhoneNumber);
            intent.putExtra("Current Balance", accountCurrentBalance);
            startActivity(intent);
            finish();
        });

        //SEND MONEY TO OTHER ACCOUNT
        mSendMoney.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SendMoneyActivity.class);
            intent.putExtra("Account Email", accountEmail);
            intent.putExtra("Student ID", accountID);
            intent.putExtra("Phone Number", accountPhoneNumber);
            intent.putExtra("Current Balance", accountCurrentBalance);
            startActivity(intent);
            finish();
        });

        //GO TO TRANSACTION HISTORY
        mCheckHistoryButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, transhisttest.class);
            startActivity(intent);
            finish();
        });


    }




    public void logout(View view){
        String userID = Objects.requireNonNull(fAuth.getCurrentUser()).getUid();
        DocumentReference documentReference = fStore.collection("users").document(userID);
        Map<String,Object> user = new HashMap<>();
        user.put("FCM_Token","token");
         documentReference.set(user, SetOptions.merge()).addOnSuccessListener(aVoid -> Log.d("tag", "OnSuccess: Logout token removed"));

        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(),Login.class));
        finish();
    }


    private void setRecyclerView() {
        TransactionsAdapter transactionsAdapter =  new TransactionsAdapter(transactionsList);
        recyclerView.setAdapter(transactionsAdapter);
        recyclerView.setHasFixedSize(true);
    }

    private void initData(String date_time, String status, String channel, String amount_hist, String reference) {

        transactionsList.add(new Transactions(date_time,status,channel,amount_hist,reference));
    }

    @Override
    public void onBackPressed() {
        Intent setIntent = new Intent(MainActivity.this, MainActivity.class);
        startActivity(setIntent);
        finish();
    }


    public static String capitalizeWord(String str){
        String words[]=str.split("\\s");
        String capitalizeWord="";
        for(String w:words){
            String first=w.substring(0,1);
            String afterfirst=w.substring(1);
            capitalizeWord+=first.toUpperCase()+afterfirst+" ";
        }
        return capitalizeWord.trim();
    }

//    public static void setWindowFlag(Activity activity, final int bits, boolean on) {
//        Window win = activity.getWindow();
//        WindowManager.LayoutParams winParams = win.getAttributes();
//        if (on) {
//            winParams.flags |= bits;
//        } else {
//            winParams.flags &= ~bits;
//        }
//        win.setAttributes(winParams);
//    }
}