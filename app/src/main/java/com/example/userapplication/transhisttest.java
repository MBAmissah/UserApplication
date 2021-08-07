package com.example.userapplication;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class transhisttest extends AppCompatActivity {

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userID;

    RecyclerView recyclerView;
    RecyclerView recyclerView2;
   public List<Transactions> transactionsList = new ArrayList<>();
    List<Months> monthsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transhisttest);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        userID = fAuth.getCurrentUser().getUid();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView2 = findViewById(R.id.recyclerView2);

        DocumentReference documentReference = fStore.collection("users").document(userID);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {

                ArrayList<HashMap<String, HashMap>> user;

                user = (ArrayList<HashMap<String, HashMap>>) documentSnapshot.get("Transaction_History");

                if(user != null){




                    HashMap hash_map = new HashMap<String, HashMap<String, HashMap>>();
                    HashMap hash_mapp = new HashMap<String, HashMap<Integer,HashMap>>();

                    HashMap<String, Integer> years = new HashMap<String, Integer>();
                    HashMap<String, Integer> months = new HashMap<String, Integer>();

                    //get years and months and put in years and months
                    for(int i = user.size()-1; i>-1; i--) {

                        HashMap dataa = user.get(i).get("data");


                        assert dataa != null;
                        String s = String.valueOf(dataa.get("paid_at")).replace("T", " / ");
                        String result = s.substring(0, s.indexOf("."));


                        //SORT DATES INTO YEAR MONTH
                        // Creating empty HashMaps

                        String year;
                        String month;
                        if (result.startsWith(" ")) {
                            year = result.substring(1, 5);
                            month = result.substring(6, 8);
                        } else {
                            year = result.substring(0, 4);
                            month = result.substring(5, 7);
                        }


                        years.put(year, i);
                        months.put(month, i);

                    }
                    //get uniqueyears and months
                    Set<Object> uniqueyears = new HashSet<Object>(years.keySet());
                    Set<Object> uniquemonths = new HashSet<Object>(months.keySet());
                    // Convert HashSet to Array for indexing cause hashset cant be indexed
                    String[] uniqueyearsArray = uniqueyears.toArray(new String[uniqueyears.size()]);
                    String[] uniquemonthsArray = uniquemonths.toArray(new String[uniquemonths.size()]);



                    for(int j = uniqueyearsArray.length -1; j > -1 ; j--){

                        for(int k = uniquemonthsArray.length -1; k > -1 ; k--) {

                            HashMap hash_mappp = new HashMap<Integer,HashMap>();


                            int l = 0;
                            for (int i = user.size() -1; i > -1; i--) {

                                HashMap dataa = user.get(i).get("data");

                                assert dataa != null;
                                String s = String.valueOf(dataa.get("paid_at")).replace("T", " / ");
                                String result = s.substring(0, s.indexOf("."));



                                //SORT DATES INTO YEAR MONTH

                                String year;
                                String month;
                                if (result.startsWith(" ")) {
                                    result = result.substring(1);
                                }


                                year = result.substring(0, 4);
                                month = result.substring(5, 7);


                                String channelReplace = String.valueOf(dataa.get("channel")).replace("_", " ");
                                String channel = capitalizeWord(channelReplace);

                                String status = capitalizeWord((String) dataa.get("status"));

                                if(channel.equals("Mobile Money") || channel.equals("Card")){
                                    channel = "Loaded w/ " + channel;
                                }
                                int monthInt = Integer.parseInt(month);

                                if(year.equals(uniqueyearsArray[j]) && month.equals(uniquemonthsArray[k])) {
                                    hash_mappp.put(l, dataa);
                                    l++;


                                    initData("Date/Time: "+result,"Status: "+  status, channel, "GH¢ " + String.valueOf((Long) dataa.get("amount") /100), "Reference: "+(String) dataa.get("reference") );

                                }

                            }


                            if(!hash_mappp.isEmpty()){
                                hash_mapp.put(uniquemonthsArray[k], hash_mappp);
                                initData2(uniquemonthsArray[k]+" "+uniqueyearsArray[j]);
                            }


                        }
                        if(!hash_mapp.isEmpty()){
                            hash_map.put(uniqueyearsArray[j], hash_mapp);
                        }
                    }

                }
                else {
                    initData("","","No Transactions Made", "", "");
                    initData2("");
                }


                setRecyclerView2();
            }
        });
        


    }

    private void setRecyclerView() {
        TransactionsAdapter transactionsAdapter =  new TransactionsAdapter(transactionsList);
        recyclerView.setAdapter(transactionsAdapter);
        recyclerView.setHasFixedSize(true);
    }

    private void setRecyclerView2() {


        MonthsAdapter monthsAdapter =  new MonthsAdapter(monthsList, transactionsList, transhisttest.this);
        recyclerView2.setAdapter(monthsAdapter);
        recyclerView2.setHasFixedSize(true);
        monthsAdapter.notifyDataSetChanged();
    }

    private void initData(String date_time, String status, String channel, String amount_hist, String reference) {

        transactionsList.add(new Transactions(date_time,status,channel,amount_hist,reference));
    }

    private void initData2(String date) {

        monthsList.add(new Months(date));
    }

    @Override
    public void onBackPressed() {
        Intent setIntent = new Intent(transhisttest.this, MainActivity.class);
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
}