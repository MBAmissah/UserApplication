package com.example.userapplication;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import co.paystack.android.Paystack;
import co.paystack.android.PaystackSdk;
import co.paystack.android.Transaction;
import co.paystack.android.model.Card;
import co.paystack.android.model.Charge;

public class CardActivity extends Fragment {

    private TextInputLayout mCardNumber, mCardExpiry, mCardCVV, mEnterAmount;
    FirebaseAuth fAuth;
    FirebaseFirestore fstore;
    Boolean success = false;
    int accountBalance, accountCurrentBalance;
    String accountEmail, accountID, accountPhoneNumber;

    String receiverPhoneNumber;
    private TextInputLayout mReceiverPhoneNumber;


    ProgressBar progressBar;

    private void initializePaystack() {
        PaystackSdk.initialize(getActivity().getApplicationContext());

        //Never add API keys directly to application code. In production environments, we recommend using a secure mechanism to manage API keys.
        // https://developers.google.com/maps/documentation/places/android-sdk/start#connect-client
        // PaystackSdk.setPublicKey("pk_test_a7656f77752f54a453efa4852c31f78ece6a3808");
       PaystackSdk.setPublicKey("pk_live_e1126dcd84114c58576ec608056d74f953b7d8f5");
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_card,container,false);



        fAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();
        initializePaystack();

        mCardNumber = v.findViewById(R.id.til_card_number);
        mCardExpiry = v.findViewById(R.id.til_card_expiry);
        mCardCVV = v.findViewById(R.id.til_card_cvv);
        mEnterAmount = v.findViewById(R.id.enter_amount);

        progressBar = v.findViewById(R.id.progressBar2);



        // add dash to card number
        Objects.requireNonNull(mCardNumber.getEditText()).addTextChangedListener(new TextWatcher() {

            private static final int TOTAL_SYMBOLS = 19; // size of pattern 0000-0000-0000-0000
            private static final int TOTAL_DIGITS = 16; // max numbers of digits in pattern: 0000 x 4
            private static final int DIVIDER_MODULO = 5; // means divider position is every 5th symbol beginning with 1
            private static final int DIVIDER_POSITION = DIVIDER_MODULO - 1; // means divider position is every 4th symbol beginning with 0
            private static final char DIVIDER = '-';

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // noop
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // noop
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!isInputCorrect(s, TOTAL_SYMBOLS, DIVIDER_MODULO, DIVIDER)) {
                    s.replace(0, s.length(), buildCorrectString(getDigitArray(s, TOTAL_DIGITS), DIVIDER_POSITION, DIVIDER));
                }
            }

            private boolean isInputCorrect(Editable s, int totalSymbols, int dividerModulo, char divider) {
                boolean isCorrect = s.length() <= totalSymbols; // check size of entered string
                for (int i = 0; i < s.length(); i++) { // check that every element is right
                    if (i > 0 && (i + 1) % dividerModulo == 0) {
                        isCorrect &= divider == s.charAt(i);
                    } else {
                        isCorrect &= Character.isDigit(s.charAt(i));
                    }
                }
                return isCorrect;
            }

            private String buildCorrectString(char[] digits, int dividerPosition, char divider) {
                final StringBuilder formatted = new StringBuilder();

                for (int i = 0; i < digits.length; i++) {
                    if (digits[i] != 0) {
                        formatted.append(digits[i]);
                        if ((i > 0) && (i < (digits.length - 1)) && (((i + 1) % dividerPosition) == 0)) {
                            formatted.append(divider);
                        }
                    }
                }

                return formatted.toString();
            }

            private char[] getDigitArray(final Editable s, final int size) {
                char[] digits = new char[size];
                int index = 0;
                for (int i = 0; i < s.length() && index < size; i++) {
                    char current = s.charAt(i);
                    if (Character.isDigit(current)) {
                        digits[index] = current;
                        index++;
                    }
                }
                return digits;
            }
        });


        //add a forward slash (/) between the cards expiry date
        Objects.requireNonNull(mCardExpiry.getEditText()).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() == 2 && !s.toString().contains("/")) {
                    s.append("/");
                }
            }
        });


        //when pay button is pressed
        Button payBtn = v.findViewById(R.id.btn_send_money_card);
        payBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mEnterAmount.setError(null);
                mCardNumber.setError(null);
                mCardExpiry.setError(null);
                mCardCVV.setError(null);

                String inputAmount = mEnterAmount.getEditText().getText().toString();
                String cardNumber = mCardNumber.getEditText().getText().toString();
                String cardExpiry = mCardExpiry.getEditText().getText().toString();
                String cardCVV = mCardCVV.getEditText().getText().toString();


                //Checks for input errors
                if (TextUtils.isEmpty(inputAmount)) {
                    mEnterAmount.setError("You can top up at least GH₵ 1.00.");
                    return;
                }
                if (Integer.parseInt(inputAmount) > 100) {
                    mEnterAmount.setError("Cant top up more than GH₵ 100.00 at once");
                    return;
                }
                if (TextUtils.isEmpty(cardNumber) || cardNumber.length() < 19) {
                    mCardNumber.setError("Please enter a valid card number");
                    return;
                }
                if (TextUtils.isEmpty(cardExpiry) || cardExpiry.length() < 5) {
                    mCardExpiry.setError("Enter valid expiry date");
                    return;
                }
                if (TextUtils.isEmpty(cardCVV) || cardCVV.length() < 3) {
                    mCardCVV.setError("Enter valid card CVV");
                    return;
                }


                progressBar.setVisibility(View.VISIBLE);



                //perform charge
               performCharge(Integer.parseInt(inputAmount.trim()));


                //receiverPhoneNumber = mReceiverPhoneNumber.getEditText().getText().toString();


            }
        });
        return v;
    }



    private void performCharge(int amount) {
        Intent intent = getActivity().getIntent();
        String inputAmount = mEnterAmount.getEditText().getText().toString();
        String cardNumber = mCardNumber.getEditText().getText().toString().replaceAll("\\D", "");
        String cardExpiry = mCardExpiry.getEditText().getText().toString();
        String cvv = mCardCVV.getEditText().getText().toString();


        String[] cardExpiryArray = cardExpiry.split("/");

        if (!cardExpiryArray[0].matches("[0-9]+") || !cardExpiryArray[1].matches("[0-9]+")) {
            mCardExpiry.setError("Enter valid expiry date");
            return;
        }
        int expiryMonth = Integer.parseInt(cardExpiryArray[0]);
        int expiryYear = Integer.parseInt(cardExpiryArray[1]);
        amount *= 100; //convert to pesewas


        accountEmail = intent.getStringExtra("Account Email");
        accountID = intent.getStringExtra("Student ID");
        accountPhoneNumber = intent.getStringExtra("Phone Number");
        accountCurrentBalance = intent.getIntExtra("Current Balance", 0);

        Card card = new Card(cardNumber, expiryMonth, expiryYear, cvv);
        Charge charge = new Charge();
        charge.setAmount(amount);
        //just cause I dont want to be spammed
        charge.setEmail(accountEmail);
        charge.setCard(card);
        charge.setCurrency("GHS");


        PaystackSdk.chargeCard(getActivity(), charge, new Paystack.TransactionCallback() {
            @Override
            public void onSuccess(Transaction transaction) {
                parseResponse(transaction.getReference());

/*
                //If payment successful, update Account Balance on both database and app
                accountBalance = Integer.parseInt(inputAmount.trim());


                String userID = fAuth.getCurrentUser().getUid();
                DocumentReference documentReference = fstore.collection("users").document(userID);
                Map<String,Object> user = new HashMap<>();
                user.put("Student_ID",accountID);
                user.put("Email",accountEmail);
                user.put("Phone_Number",accountPhoneNumber);
                user.put("Account_Balance", accountBalance + accountCurrentBalance);
                documentReference.set(user, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Toast.makeText(getActivity(), "Amount Successfully added", Toast.LENGTH_SHORT).show();
                        Log.d("tag", "OnSuccess: Amount Successfully added ");
                        startActivity(new Intent(getActivity().getApplicationContext(), MainActivity.class));
                    }
                });*/
            }

            @Override
            public void beforeValidate(Transaction transaction) {
                Log.d("Main Activity", "beforeValidate: " + transaction.getReference());
            }

            @Override
            public void onError(Throwable error, Transaction transaction) {
                Log.d("Main Activity", "onError: " + error.getLocalizedMessage());
                Log.d("Main Activity", "onError: " + error);

                String message = "Payment Failed - " + error.getLocalizedMessage();
                Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void parseResponse(String transactionReference) {
        String message = "Payment Successful - ";// + transactionReference;
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
        success = true;
    }


    }

