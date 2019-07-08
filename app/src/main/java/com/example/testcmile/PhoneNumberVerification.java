package com.example.testcmile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.goodiebag.pinview.Pinview;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;

import java.util.concurrent.TimeUnit;

public class PhoneNumberVerification extends AppCompatActivity {
    private String mVerificationId;
    private Pinview mPinView;
    private FirebaseAuth mAuth;
    private TextView mTextViewNumber;
    private TextView mTextViewTimer;
    private TextView mTextViewResendOtp;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private SharedPreferences.Editor mEditor;

    public static final String MY_PREF="myPrefManager";
    public static final String IS_LOGIN="isLogin";
    public static final String MOB_NUMBER="mobileNumber";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_number_verification);

        getSupportActionBar().setTitle("Get Started");
        mAuth = FirebaseAuth.getInstance();
        mPinView = findViewById(R.id.PinView);

        mTextViewNumber = findViewById(R.id.textViewNumber);
        mTextViewTimer = findViewById(R.id.textViewTimer);
        mTextViewResendOtp=findViewById(R.id.textViewResendCode);





        new CountDownTimer(60000, 1000) {

            public void onTick(long millisUntilFinished) {
                mTextViewTimer.setText("seconds remaining: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                mTextViewTimer.setText(" ");
            }
        }.start();




        Intent intent = getIntent();
        final String mobile = intent.getStringExtra("mobile");
        mTextViewNumber.setText(mobile);
        sendVerificationCode(mobile);

        mTextViewResendOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resendVerificationCode("+91"+mobile,mResendToken);
            }
        });

        findViewById(R.id.buttonSignIn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = mPinView.getValue().toString().trim();
                if (code.isEmpty() || code.length() < 6) {
                    mPinView.requestFocus();
                    return;
                }

                verifyVerificationCode(code);
            }
        });

    }


    private void sendVerificationCode(String mobile) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91" + mobile,
                60,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                mCallbacks);
    }

    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS,
                this,
                mCallbacks,
                token);
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

            String code = phoneAuthCredential.getSmsCode();

            if (code != null) {
                mPinView.setValue(code);
                verifyVerificationCode(code);

            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(PhoneNumberVerification.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);

            mVerificationId = s;
            mResendToken = forceResendingToken;

            Log.d("TAG","Mesaage"+s);
        }
    };



    private void verifyVerificationCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(PhoneNumberVerification.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            String mobile=mTextViewNumber.getText().toString();

                            mEditor = getSharedPreferences(MY_PREF,MODE_PRIVATE).edit();
                            mEditor.putBoolean(IS_LOGIN,true);
                            mEditor.putString(MOB_NUMBER,mobile);
                            mEditor.apply();

                            Intent intent = new Intent(PhoneNumberVerification.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);



                        } else {


                            String message = "Somthing is wrong, we will fix it soon...";

                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                message = "Invalid code entered...";
                            }else {

                            }
                        }
                    }
                });
    }

}